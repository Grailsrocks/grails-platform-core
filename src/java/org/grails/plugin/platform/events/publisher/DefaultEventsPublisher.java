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
package org.grails.plugin.platform.events.publisher;

import groovy.lang.Closure;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor;
import org.grails.plugin.platform.events.EventMessage;
import org.grails.plugin.platform.events.EventReply;
import org.grails.plugin.platform.events.registry.DefaultEventsRegistry;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.*;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 16/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class DefaultEventsPublisher implements EventsPublisher {

    private final static Logger log = Logger.getLogger(DefaultEventsPublisher.class);

    private DefaultEventsRegistry grailsEventsRegistry;
    protected AsyncTaskExecutor taskExecutor;
    private PersistenceContextInterceptor persistenceInterceptor;
    private boolean catchFlushExceptions = false;

    public void setCatchFlushExceptions(boolean catchFlushExceptions) {
        this.catchFlushExceptions = catchFlushExceptions;
    }

    public void setTaskExecutor(AsyncTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
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

    public EventReply eventAsync(final EventMessage event, final Closure onComplete, final long timeout) {
        Future<DefaultEventsRegistry.InvokeResult> invokeResult =
                taskExecutor.submit(new Callback(event));

        final WrappedFuture reply = new WrappedFuture(invokeResult, -1);
        if(onComplete != null){
            taskExecutor.execute(new Runnable(){

                public void run() {
                    try {
                        if(timeout != -1l)
                            reply.get(timeout, TimeUnit.MILLISECONDS);
                        else
                            reply.get();
                    } catch (Exception e) {
                        reply.setCallingError(e);
                    }
                    onComplete.call(reply);
                }
            });

        }
        return reply;
    }

    public EventReply eventAsync(final EventMessage event, final Closure onComplete) {
        return eventAsync(event, onComplete, -1);
    }

    //INTERNAL

    private class Callback implements Callable<DefaultEventsRegistry.InvokeResult> {

        private EventMessage event;

        public Callback(EventMessage event) {
            this.event = event;
        }

        public DefaultEventsRegistry.InvokeResult call() {
            boolean gormSession = event.isGormSession();
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

        public void setCallingError(Throwable e){
            super.initValues(e);
        }

    }
}
