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
package org.grails.plugin.platform.events.publisher;

import groovy.lang.Closure;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor;
import org.grails.plugin.platform.events.EventMessage;
import org.grails.plugin.platform.events.EventReply;
import org.grails.plugin.platform.events.registry.DefaultEventsRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Stephane Maldini <smaldini@vmware.com>
 * @version 1.0
 * @file
 * @date 16/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class DefaultEventsPublisher implements EventsPublisher, ApplicationContextAware, InitializingBean {


    private static final String EXECUTOR = "executor";
    private static final String DEFAULT_EXECUTOR = "grailsTopicExecutor";
    private static final String QUEUE_EXECUTOR = "grailsP2PExecutor";
    private final static Logger log = Logger.getLogger(DefaultEventsPublisher.class);

    private DefaultEventsRegistry grailsEventsRegistry;

    @Autowired
    protected Map<String, AsyncTaskExecutor> taskExecutors;

    private PersistenceContextInterceptor persistenceInterceptor;
    private boolean catchFlushExceptions = false;
    private ApplicationContext context;

    public void setCatchFlushExceptions(boolean catchFlushExceptions) {
        this.catchFlushExceptions = catchFlushExceptions;
    }

    public void setPersistenceInterceptor(PersistenceContextInterceptor persistenceInterceptor) {
        this.persistenceInterceptor = persistenceInterceptor;
    }

    public void setGrailsEventsRegistry(DefaultEventsRegistry grailsEventsRegistry) {
        this.grailsEventsRegistry = grailsEventsRegistry;
    }

    //API

    public EventReply event(EventMessage event) {
        DefaultEventsRegistry.InvokeResult invokeResult = grailsEventsRegistry.invokeListeners(event);
        return new EventReply(invokeResult.getResult(), invokeResult.getInvoked());
    }

    public EventReply eventAsync(final EventMessage event, final Map<String, Object> params) {
        AsyncTaskExecutor taskExecutor = params != null && params.containsKey(EXECUTOR) ?
                taskExecutors.get( params.get(EXECUTOR) ) :
                taskExecutors.get(DEFAULT_EXECUTOR);

        Future<DefaultEventsRegistry.InvokeResult> invokeResult =
                taskExecutor.submit(new Callback(event));

        final WrappedFuture reply = new WrappedFuture(invokeResult, -1);

        if (params != null) {
            reply.setOnError((Closure)params.get(ON_ERROR));
            if (params.get(ON_REPLY) != null) {
                taskExecutor.execute(new Runnable() {

                    public void run() {
                        try {
                            if (params.get(TIMEOUT) != null)
                                reply.get((Long) params.get(TIMEOUT), TimeUnit.MILLISECONDS);
                            else
                                reply.get();

                            reply.throwError();
                            ((Closure) params.get(ON_REPLY)).call(reply);
                        } catch (Throwable e) {
                            reply.setCallingError(e);
                        }
                    }
                });

            }
        }
        return reply;
    }

    public void afterPropertiesSet() throws BeansException {
        //try to lazy load contextInterceptor
        if(persistenceInterceptor == null){
            try{
                persistenceInterceptor = context.getBean("persistenceInterceptor", PersistenceContextInterceptor.class);
            }catch (BeansException ex){
                log.debug("No persistence context interceptor found", ex);
            }
        }
    }

    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    //INTERNAL

    private class Callback implements Callable<DefaultEventsRegistry.InvokeResult> {

        private EventMessage event;

        public Callback(EventMessage event) {
            this.event = event;
        }

        public DefaultEventsRegistry.InvokeResult call() {
            boolean gormSession = persistenceInterceptor != null && event.isGormSession();
            if (gormSession) {
                persistenceInterceptor.init();
            }

            DefaultEventsRegistry.InvokeResult invokeResult = grailsEventsRegistry.invokeListeners(event);

            if (gormSession) {
                try {
                    persistenceInterceptor.flush();
                } catch (RuntimeException re) {
                    if (!catchFlushExceptions)
                        throw re;
                } finally {
                    persistenceInterceptor.destroy();
                }
            }
            return invokeResult;
        }
    }

    private static class WrappedFuture extends EventReply {

        public WrappedFuture(Future<?> wrapped, int receivers) {
            super(wrapped, receivers);
        }

        @Override
        protected void initValues(Object val) {
            DefaultEventsRegistry.InvokeResult message = (DefaultEventsRegistry.InvokeResult) val;
            setReceivers(message.getInvoked());
            super.initValues(message.getResult());
        }

        public void setCallingError(Throwable e) {
            super.initValues(e);
            if(getOnError() != null){
                getOnError().call(this);
            }
        }

    }
}
