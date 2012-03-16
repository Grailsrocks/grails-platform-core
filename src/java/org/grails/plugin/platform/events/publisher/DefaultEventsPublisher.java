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
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor;
import org.grails.plugin.platform.events.EventObject;
import org.grails.plugin.platform.events.EventReply;
import org.grails.plugin.platform.events.dispatcher.GormTopicSupport;
import org.grails.plugin.platform.events.registry.DefaultEventsRegistry;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 16/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class DefaultEventsPublisher implements EventsPublisher, ApplicationListener {

    static final private String GORM_EVENT_PACKAGE = "org.grails.datastore.mapping.engine.event";

    private DefaultEventsRegistry grailsEventsRegistry;
    private AsyncTaskExecutor taskExecutor;
    private PersistenceContextInterceptor persistenceInterceptor;
    private GormTopicSupport gormTopicSupport;
    private boolean catchFlushExceptions = false;

    public void setCatchFlushExceptions(boolean catchFlushExceptions) {
        this.catchFlushExceptions = catchFlushExceptions;
    }

    public void setGormTopicSupport(GormTopicSupport gormTopicSupport) {
        this.gormTopicSupport = gormTopicSupport;
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

    public EventReply event(EventObject event) {
        DefaultEventsRegistry.InvokeResult invokeResult = grailsEventsRegistry.invokeListeners(event);
        return new EventReply(invokeResult.getResult(), invokeResult.getInvoked());
    }

    public EventReply eventAsync(final EventObject event) {
        Future<DefaultEventsRegistry.InvokeResult> invokeResult =
                taskExecutor.submit(new Callback(event));

        return new WrappedFuture(invokeResult, -1);
    }

    public void eventAsync(final EventObject event, final Closure onComplete) {
        taskExecutor.execute(new Runnable() {
            public void run() {
                DefaultEventsRegistry.InvokeResult invokeResult = new Callback(event).call();
                onComplete.call(new EventReply(invokeResult.getResult(), invokeResult.getInvoked()));
            }
        });
    }

    public EventReply[] waitFor(EventReply... replies) throws ExecutionException, InterruptedException {
        for (EventReply reply : replies) {
            if (reply != null) reply.get();
        }
        return replies;
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        //fixme horrible hack to support grails 1.3.x
        if (applicationEvent.getClass().getName().startsWith(GORM_EVENT_PACKAGE)) {
            String topic = gormTopicSupport.convertTopic(applicationEvent);
            EventReply reply = event(new EventObject(topic,
                    ReflectionUtils.invokeMethod(
                            ReflectionUtils.findMethod(applicationEvent.getClass(),"getEntityObject"),
                            applicationEvent
                    ), GormTopicSupport.GORM_SOURCE));
            try {
                gormTopicSupport.processCancel(applicationEvent, reply.getValues());
            } catch (Exception e) {
                throw new RuntimeException(e);//shouldn't happen as its sync event
            }
        }
    }

    //INTERNAL

    private class Callback implements Callable<DefaultEventsRegistry.InvokeResult> {

        private EventObject event;

        public Callback(EventObject event) {
            this.event = event;
        }

        public DefaultEventsRegistry.InvokeResult call() {
            boolean nonGormEvent = event.getSource() == null || !event.getSource().equals(GormTopicSupport.GORM_SOURCE);
            if (nonGormEvent) {
                persistenceInterceptor.init();
            }

            DefaultEventsRegistry.InvokeResult invokeResult = grailsEventsRegistry.invokeListeners(event);

            if (!nonGormEvent) {
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

        @Override
        public int size() {
            try {
                get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
            return super.size();
        }

    }
}
