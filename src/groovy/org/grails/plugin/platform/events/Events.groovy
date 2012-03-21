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
package org.grails.plugin.platform.events

import grails.util.GrailsNameUtils
import java.lang.reflect.Method
import org.apache.log4j.Logger
import org.grails.plugin.platform.events.publisher.EventsPublisher
import org.grails.plugin.platform.events.registry.EventsRegistry
import org.grails.plugin.platform.util.PluginUtils
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.codehaus.groovy.grails.plugins.metadata.GrailsPlugin
import org.grails.plugin.platform.events.dispatcher.DefaultEventsDispatcher
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware

class Events implements GrailsApplicationAware {

    static final private log = Logger.getLogger(Events.class)

    EventsRegistry grailsEventsRegistry
    EventsPublisher grailsEventsPublisher
    DefaultEventsDispatcher grailsEventsDispatcher
    private ApplicationContext applicationContext

    def injectedMethods = { theContext ->
        println "In events injections, theContext is $theContext"

        'controller, domain, service' { Class clazz ->
            Class definingPlugin = PluginUtils.getNameOfDefiningPlugin(theContext, clazz)
            String scope = definingPlugin?.name()
            def self = theContext.grailsEvents
            
            event {String topic, data = null, Map params = null ->
                self._event(scope, topic, data, params)
            }
            eventAsync {String topic, data = null, Map params = null ->
                self._eventAsync(scope, topic, data, params)
            }
            eventAsync {String topic, data, Closure callback, params = null ->
                self._eventAsyncClosure(scope, topic, data, callback, params)
            }
            eventAsync {String topic, Closure callback ,  params = null ->
                self._eventAsyncClosure(scope, topic, null, callback, params)
            }
            copyFrom(self.grailsEventsPublisher, 'waitFor')
            copyFrom(self.grailsEventsRegistry, 'addListener', 'removeListeners', 'countListeners')
        }
    }

    EventReply _event(String scope, String topic, data = null, Map params = null) {
        if (log.debugEnabled) {
            log.debug "Sending event of scope [$scope] and topic [$topic] with data [${data}] and params [${params}]"
        }
        grailsEventsPublisher.event(new EventObject(source: scope, event: topic, data: data))
    }

    EventReply _eventAsync(String scope, String topic, data = null, Map params = null) {
        if (log.debugEnabled) {
            log.debug "Sending async event of scope [$scope] and topic [$topic] with data [${data}] and params [${params}]"
        }
        grailsEventsPublisher.eventAsync(new EventObject(source: scope, event: topic, data: data))
    }

    void _eventAsyncClosure(String scope, String topic, data, Closure callback,  Map params = null) {
        if (log.debugEnabled) {
            log.debug "Sending event of scope [$scope] and topic [$topic] with data [${data}] with callback Closure and params [${params}]"
        }
        grailsEventsPublisher.eventAsync(new EventObject(source: scope, event: topic, data: data), callback)
    }

    void reloadListener(Class serviceClass) {
        clearEvents(serviceClass)
        registerListeners([serviceClass])
    }

    void clearEvents(Class serviceClass) {
        log.info "Clear event listeners from $serviceClass"
        def removedListeners = grailsEventsRegistry.removeListeners(":$serviceClass.name")
        log.info "events removed : $removedListeners"
    }

    void registerListeners(Collection<Class<?>> serviceClasses) {
        for (Class<?> serviceClass in serviceClasses) {
//            grailsEventsDispatcher.scanClassForMappings(serviceClass)
            for (Method method: serviceClass.declaredMethods) {
                Listener annotation = method.getAnnotation(Listener)
                if (annotation) {
                    log.info "Register event listener $serviceClass.name#$method.name for topic ${annotation.value() ?: method.name}"
                    grailsEventsRegistry.addListener(
                            annotation.value() ?: method.name,
                            applicationContext.getBean(GrailsNameUtils.getPropertyName(serviceClass)),
                            method
                    )
                }
            }
        }
    }

    void setGrailsApplication(org.codehaus.groovy.grails.commons.GrailsApplication grailsApplication) {
        this.applicationContext = grailsApplication.mainContext
        registerListeners(grailsApplication.serviceClasses*.clazz)
    }
}