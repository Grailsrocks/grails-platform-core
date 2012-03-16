/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (stephane.maldini@gmail.com)
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
import org.grails.plugin.platform.events.EventObject;
import org.grails.plugin.platform.events.ListenerId;
import org.grails.plugin.platform.events.dispatcher.builder.MappedEventMethod;
import org.springframework.aop.framework.Advised;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
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

    public String addListener(String topic, Closure callback) {
        return registerHandler(callback, topic);
    }

    public String addListener(String topic, Object bean, String callbackName) {
        return registerHandler(bean, ReflectionUtils.findMethod(bean.getClass(), callbackName), topic);
    }

    public String addListener(String topic, Object bean, Method callback) {
        return registerHandler(bean, callback, topic);
    }

    public int removeListeners(String callbackId) {
        ListenerId listener = ListenerId.parse(callbackId);
        if (listener == null)
            return 0;
        Set<ListenerHandler> listeners = findAll(listener);
        for (ListenerHandler _listener : listeners) {
            this.listeners.remove(_listener);
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

    private String registerHandler(Closure callback, String topic) {

        ListenerId listener = ListenerId.build(topic, callback);
        ListenerHandler handler = new ListenerHandler(callback, ReflectionUtils.findMethod(
                callback.getClass(),
                "call",
                Object[].class
        ), listener);

        listeners.add(handler);

        return listener.toString();
    }

    private String registerHandler(Object bean, Method callback, String topic) {
        Object target = bean;

        if (bean instanceof Advised) {
            try {
                target = ((Advised) bean).getTargetSource().getTarget();
            } catch (Exception e) {
                log.error("failed to retrieve bean origin from proxy", e);
            }
        }
        ListenerId listener = ListenerId.build(topic, target, callback);

        ListenerHandler handler = new ListenerHandler(target, callback, listener);

        listeners.add(handler);

        return listener.toString();
    }

    private Set<ListenerHandler> findAll(ListenerId listener) {
        Set<ListenerHandler> listeners =
                new HashSet<ListenerHandler>();

        for (ListenerHandler _listener : this.listeners) {
            if (listener.matches(_listener.getListenerId())) {
                listeners.add(_listener);
            }
        }

        return listeners;
    }

    public InvokeResult invokeListeners(EventObject evt) {
        ListenerId listener = new ListenerId(evt.getEvent());
        Set<ListenerHandler> listeners = findAll(listener);

        List<Object> results = new ArrayList<Object>();
        Object result;
        for (ListenerHandler _listener : listeners) {
            result = _listener.invoke(evt.getData());
            if (result != null) results.add(result);
        }

        return new InvokeResult(results.size(), results.size() != 1 ? results : results.get(0));
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

    private static class ListenerHandler implements EventHandler{
        private Object bean;
        private Method method;
        private ListenerId listenerId;
        //private MappedEventMethod mapping;

        public ListenerHandler(Object bean, Method m, ListenerId listenerId/*, MappedEventMethod mapping*/) {
            this.listenerId = listenerId;
            this.method = m;
            this.bean = bean;
            //this.mapping = mapping;
        }

        public Object invoke(Object... args) {
            Object res = null;
            try {
                res = method.invoke(bean, args);
            }catch (IllegalArgumentException e){
                log.trace("ignoring call for bean "+bean+ " with args "+args.toString());
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            return res;
        }

        public ListenerId getListenerId() {
            return listenerId;
        }
    }


}
