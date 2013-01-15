/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (smaldini@vmware.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugin.platform.events.registry;

import groovy.lang.Closure;
import org.apache.log4j.Logger;
import org.grails.plugin.platform.events.EventMessage;
import org.grails.plugin.platform.events.ListenerId;
import org.grails.plugin.platform.events.utils.EventsUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Stephane Maldini <smaldini@vmware.com>
 * @version 1.0
 * @file
 * @date 02/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class DefaultEventsRegistry implements EventsRegistry {

    static final private Logger log = Logger.getLogger(DefaultEventsRegistry.class);

    private Set<ListenerHandler> listeners = new HashSet<ListenerHandler>();

    /*
        API
     */

    public String on(String namespace, String topic, Closure callback) {
        return registerHandler(callback, namespace, topic);
    }

    public String on(String namespace, String topic, Object bean, String callbackName) {
        return registerHandler(bean, ReflectionUtils.findMethod(bean.getClass(), callbackName), namespace, topic);
    }

    public String on(String namespace, String topic, Object bean, Method callback) {
        return registerHandler(bean, callback, namespace, topic);
    }

    public int removeListeners(String callbackId) {
        ListenerId listener = ListenerId.parse(callbackId);
        if (listener == null)
            return 0;
        synchronized (listeners) {
            Set<ListenerHandler> listeners = findAll(listener);
            for (ListenerHandler _listener : listeners) {
                this.listeners.remove(_listener);
            }
        }

        return listeners.size();
    }

    public int countListeners(String callbackId) {
        ListenerId listener = ListenerId.parse(callbackId);
        if (listener == null)
            return 0;

        return findAll(listener).size();
    }

    /*
       INTERNAL
    */

    private String registerHandler(Closure callback, String namespace, String topic) {
        if (log.isDebugEnabled()) {
            log.debug("Registering event handler [" + callback.getClass() + "] for topic [" + topic + "]");
        }

        ListenerId listener = ListenerId.build(namespace, topic, callback);
        ListenerHandler handler = new ListenerHandler(callback, ReflectionUtils.findMethod(
                callback.getClass(),
                "call",
                Object.class
        ), listener);

        synchronized (listeners) {
            listeners.add(handler);
        }

        return listener.toString();
    }

    private String registerHandler(Object bean, Method callback, String namespace, String topic) {
        if (log.isDebugEnabled()) {
            log.debug("Registering event handler on bean [" + bean + "] method [" + callback + "] for topic [" + topic + "]");
        }

        ListenerId listener = ListenerId.build(namespace, topic, EventsUtils.unproxy(bean), callback);

        ListenerHandler handler = new ListenerHandler(bean, callback, listener);

        synchronized (listeners) {
            listeners.add(handler);
        }

        return listener.toString();
    }

    private Set<ListenerHandler> findAll(ListenerId listener) {
        if (log.isDebugEnabled()) {
            log.debug("Finding listeners matching listener id [" + listener.toString() + "]");
        }
        Set<ListenerHandler> listeners =
                new HashSet<ListenerHandler>();

        for (ListenerHandler _listener : this.listeners) {
            if (listener.matches(_listener.getListenerId())) {
                listeners.add(_listener);
            }
        }

        return listeners;
    }

    public InvokeResult invokeListeners(EventMessage evt) {
        if (log.isDebugEnabled()) {
            log.debug("Invoking listeners for event [" + evt.getEvent() + "] namespaced on [" + evt.getNamespace() + "] with data [" + evt.getData() + "]");
        }
        ListenerId listener = new ListenerId(evt.getNamespace(), evt.getEvent());
        Set<ListenerHandler> listeners = findAll(listener);

        if (log.isDebugEnabled()) {
            log.debug("Found " + listeners.size() + " listeners for event [" + evt.getEvent() + "] with data [" + evt.getData() + "]");
        }
        List<Object> results = new ArrayList<Object>();
        Object result;
        for (ListenerHandler _listener : listeners) {
            if (log.isDebugEnabled()) {
                log.debug("Invoking listener [" + _listener.bean.getClass() + '.' + _listener.method.getName() + "(arg)] for event [" + evt.getEvent() + "] with data [" + evt.getData() + "]");
            }
            try {
                result = _listener.invoke(evt);
            } catch (Throwable throwable) {
                result = throwable;
            }
            if (result != null) results.add(result);
        }

        Object resultValues = null;
        // Make sure no-result does not cause an error
        if (results.size() >= 1) {
            if (results.size() != 1) {
                resultValues = results;
            } else {
                resultValues = results.get(0);
            }
        }
        return new InvokeResult(results.size(), resultValues);
    }

    public class InvokeResult {
        private int invoked;
        private Object result;

        public int getInvoked() {
            return invoked;
        }

        public Object getResult() {
            return result;
        }

        public InvokeResult(int invoked, Object result) {
            this.invoked = invoked;
            this.result = result;
        }
    }

    private static class ListenerHandler implements EventHandler {
        private Object bean;
        private Method method;
        private ListenerId listenerId;
        private boolean useEventMessage = false;
        private boolean noArgs = false;

        public ListenerHandler(Object bean, Method m, ListenerId listenerId) {
            this.listenerId = listenerId;
            this.method = m;

            if (m.getParameterTypes().length > 0) {
                Class type = m.getParameterTypes()[0];
                useEventMessage = EventMessage.class.isAssignableFrom(type);
                if (useEventMessage && log.isDebugEnabled()) {
                    log.debug("Listener " + bean.getClass() + "." + method.getName() + " will receive EventMessage enveloppe");
                }
            } else {
                noArgs = true;
            }
            this.bean = bean;
            //this.mapping = mapping;
        }

        public Object invoke(EventMessage _arg) throws Throwable {
            Object res = null;

            Object arg = this.isUseEventMessage() ? _arg : _arg.getData();

            if (log.isDebugEnabled()) {
                StringBuilder argTypes = new StringBuilder();
                for (Object e : method.getParameterTypes()) {
                    argTypes.append(e.toString());
                    argTypes.append(',');
                }
                log.debug("About to invoke listener method " + bean.getClass() + "." + method.getName() + " with arg type " + argTypes +
                        " with arg " + arg.toString());
            }
            try {
                if (noArgs) {
                    res = method.invoke(bean);
                } else {
                    res = method.invoke(bean, arg);
                }
            } catch (IllegalArgumentException e) {
                //ignoring
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring call to " + bean.getClass() + "." + method.getName() + " with args " + arg.toString() + " - illegal arg exception: " + e.toString());
                }
            } catch (InvocationTargetException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failing call to " + bean.getClass() + "." + method.getName() + " with args " + arg.toString() + " - illegal arg invokation " + e.toString());
                }
                throw e.getCause();
            } catch (Throwable e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failing call to " + bean.getClass() + "." + method.getName() + " with args " + arg.toString() + " : " + e.toString());
                }
                throw e;
            }

            return res;
        }

        public ListenerId getListenerId() {
            return listenerId;
        }

        public boolean isUseEventMessage() {
            return useEventMessage;
        }
    }


}
