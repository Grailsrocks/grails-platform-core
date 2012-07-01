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

import grails.events.Listener
import grails.util.GrailsNameUtils
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.plugin.platform.conventions.DSLCallCommand
import org.grails.plugin.platform.conventions.DSLCommand
import org.grails.plugin.platform.conventions.DSLEvaluator
import org.grails.plugin.platform.conventions.DSLNamedArgsCallCommand
import org.grails.plugin.platform.events.publisher.EventsPublisher
import org.grails.plugin.platform.events.registry.EventsRegistry
import org.grails.plugin.platform.util.PluginUtils
import org.springframework.context.ApplicationContext

import java.lang.reflect.Method

class EventsImpl implements Events {

    static final private log = Logger.getLogger(EventsImpl.class)

    EventsRegistry grailsEventsRegistry
    EventsPublisher grailsEventsPublisher
    private ApplicationContext applicationContext
    GrailsApplication grailsApplication

    List<EventDefinition> eventDefinitions

    static final String APP_NAMESPACE = 'app'

    def injectedMethods = { theContext ->

        'controller, domain, service' { Class clazz ->
            //String defaultNamespace = PluginUtils.getNameOfDefiningPlugin(theContext, clazz) ?: APP_NAMESPACE'

            def self = theContext.grailsEvents
            //def config = theContext.grailsApplication.config.plugin.platformCore

            event { String topic, data = null, Map params = null ->
                self.event(null, topic, data, params)
            }
            event { Map args ->
                self.event(args.for ?: null, args.topic, args.data, args.params)
            }
            eventAsync { Map args ->
                self.eventAsync(args.for ?: null, args.topic, args.data, args.params)
            }
            eventAsync { String topic, data = null, Map params = null ->
                self.eventAsync(null, topic, data, params)
            }
            eventAsync { String topic, data, Closure callback, params = null ->
                self.eventAsyncWithCallback(null, topic, data, callback, params)
            }
            eventAsync { String topic, Closure callback, params = null ->
                self.eventAsyncWithCallback(null, topic, null, callback, params)
            }
            copyFrom(self.grailsEventsPublisher, 'waitFor')
            copyFrom(self.grailsEventsRegistry, ['addListener', 'removeListeners', 'countListeners'])
        }
    }

    // We have to use a list here as [] and ... were failing to compile for some WTF reason - MP
    Object[] waitFor(List<EventReply> replies) {
        grailsEventsPublisher.waitFor(replies)
    }

    String addListener(String namespace, String topic, Closure callback) {
        grailsEventsRegistry.addListener(namespace, topic, callback)
    }

    int removeListeners(String callbackId) {
        grailsEventsRegistry.removeListeners(callbackId)
    }

    int countListeners(String callbackId) {
        grailsEventsRegistry.countListeners(callbackId)
    }

    EventReply event(String namespace, String topic) {
        event(namespace, topic, null, null)
    }
    
    EventReply event(String namespace, String topic, data) {
        event(namespace, topic, data, null)
    }
    
    EventReply event(String namespace, String topic, data, Map params) {
        if (log.debugEnabled) {
            log.debug "Sending event of namespace [$namespace] and topic [$topic] with data [${data}] and params [${params}]"
        }
        grailsEventsPublisher.event buildEvent(namespace, topic, data, params)
    }

    EventReply eventAsync(String namespace, String topic) {
        eventAsync(namespace, topic, null, null)
    }

    EventReply eventAsync(String namespace, String topic, data) {
        eventAsync(namespace, topic, data, null)
    }

    EventReply eventAsync(String namespace, String topic, data, Map params) {
        if (log.debugEnabled) {
            log.debug "Sending async event of namespace [$namespace] and topic [$topic] with data [${data}] and params [${params}]"
        }
        grailsEventsPublisher.eventAsync buildEvent(namespace, topic, data, params)
    }

    void eventAsyncWithCallback(String namespace, String topic, Closure callback) {
        eventAsyncWithCallback(namespace, topic, null, callback, null)
    }
    
    void eventAsyncWithCallback(String namespace, String topic, data, Closure callback) {
        eventAsyncWithCallback(namespace, topic, data, callback, null)
    }
    
    void eventAsyncWithCallback(String namespace, String topic, data, Closure callback, Map params) {
        if (log.debugEnabled) {
            log.debug "Sending event of namespace [$namespace] and topic [$topic] with data [${data}] with callback Closure and params [${params}]"
        }
        grailsEventsPublisher.eventAsync buildEvent(namespace, topic, data, params), callback
    }

    EventMessage buildEvent(String namespace, String topic, data, Map params) {
        boolean gormSession = params?.containsKey('gormSession') ? params.remove('gormSession') : true
        String _namespace = params?.remove('namespace') ?: namespace

        new EventMessage(topic, data, _namespace, gormSession, params)
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

    void eachListener(Collection<Class> serviceClasses, Closure c) {
        for (Class serviceClass in serviceClasses) {
            for (Method method : serviceClass.declaredMethods) {
                Listener annotation = method.getAnnotation(Listener)
                if (annotation) {
                    String namespace = /*PluginUtils.getNameOfDefiningPlugin(applicationContext, serviceClass) ?:*/
                       annotation?.namespace() ?: APP_NAMESPACE
                    String topic = annotation?.topic() ?: method.name
                    c(namespace, annotation?.namespace() as boolean, topic, method, serviceClass)
                }
            }
        }
    }

    EventDefinition matchesDefinition(String topic, Method method, Class serviceClass) {
        ListenerId targetId = ListenerId.build(null, topic, serviceClass, method)
        for (definition in eventDefinitions) {
            if (definition.listenerId.matches(targetId)) {
                log.info "Applying Event definition [$definition.listenerId] from [$definition.definingPlugin]"
                return definition
            }
        }
        null
    }

    void registerListeners(Collection<Class<?>> serviceClasses) {
//            grailsEventsDispatcher.scanClassForMappings(serviceClass)
        eachListener(serviceClasses) {String namespace, boolean hasInlineNamespace, String topic, Method method, Class serviceClass ->

            def definition = matchesDefinition(topic, method, serviceClass)
            if(!hasInlineNamespace || !definition?.definingPlugin){
                namespace = definition?.namespace ?: namespace
            }
            // If there is no match with a known event, or there is a declared event and it is not disabled,
            // add the listener
            if (!definition || !definition.disabled) {
                log.info "Register event listener $serviceClass.name#$method.name for topic $topic and namespace $namespace"
                if (!definition) {
                    log.warn "Event listener $serviceClass.name#$method.name declared for topic $topic and namespace $namespace but no such event is declared, you may never receive it"
                }

                grailsEventsRegistry.addListener(
                        namespace,
                        topic,
                        applicationContext.getBean(GrailsNameUtils.getPropertyName(serviceClass)),
                        method,
                        definition
                )
            }
        }
    }

    void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
        this.applicationContext = grailsApplication.mainContext
    }

    void clearEventDefinitions() {
        eventDefinitions = []
    }

    void reloadListeners() {
        log.info "Reloading events listeners"

        grailsEventsRegistry.removeListeners('*')
        clearEventDefinitions()
        loadDSL()
        registerListeners(grailsApplication.serviceClasses*.clazz)

    }

    void registerEvents(Closure dsl) {
        List<DSLCommand> commands = new DSLEvaluator().evaluate(dsl)
        String definingPlugin = PluginUtils.getNameOfDefiningPlugin(grailsApplication.mainContext, dsl)
        parseDSL(commands, definingPlugin)
    }

    void loadDSL(Class dslClass) {
        def dslInstance = dslClass.newInstance()
        dslInstance.run()
        def dsl = dslInstance.binding.getVariable('events')
        if (dsl) {
            registerEvents(dsl)
        } else {
            log.warn "Tried to load events data from artefact [${artefact.clazz}] but no 'events' value was found in the script"
        }
    }

    void loadDSL() {
        if (log.debugEnabled) {
            log.debug "Loading events artefacts..."
        }

        for (artefact in grailsApplication.eventsClasses) {
            if (log.debugEnabled) {
                log.debug "Loading events artefact [${artefact.clazz}] (class instance hash: ${System.identityHashCode(artefact.clazz)})"
            }
            loadDSL(artefact.clazz)
        }

        eventDefinitions.sort()
    }

    /**
     * Receives a graph of DSL commend objects and creates the necessary namespaces and items
     *
     * Handles the "magic" inheritance of values and conventions etc.
     */
    protected void parseDSL(List<DSLCommand> commands, String definingPlugin) {
        if (log.debugEnabled) {
            log.debug "Parsing events DSL commands: ${commands}, defined by plugin ${definingPlugin}"
        }
        for (c in commands) {
            switch (c) {
                case DSLCallCommand:
                    addItemFromArgs(c.name, null, definingPlugin)
                    break
                case DSLNamedArgsCallCommand:
                    addItemFromArgs(c.name, c.arguments, definingPlugin)
                    break
                default:
                    throw new IllegalArgumentException("We don't support command type ${c.getClass()}")
            }
        }
    }

    private addItemFromArgs(String listenerPattern, Map arguments, String definingPlugin) {
        if (log.debugEnabled) {
            log.debug "Adding event declared in DSL - listenerPattern: ${listenerPattern}, arguments: ${arguments}, defined by plugin ${definingPlugin}"
        }
        def definition = new EventDefinition()
        definition.namespace = arguments?.remove('namespace')
        if (!definition.namespace) {
            definition.namespace = definingPlugin
        }
        definition.requiresReply = arguments?.remove('requiresReply') ?: definition.requiresReply
        definition.disabled = arguments?.remove('disabled') ?: definition.disabled
        definition.secured = arguments?.remove('secured') ?: definition.secured
        def filter = arguments?.remove('filter')

        if (filter) {
            if (Closure.isAssignableFrom(filter.getClass())) {
                definition.filterClosure = filter
            }
            if (Class.isAssignableFrom(filter.getClass())) {
                definition.filterClass = filter
            }
        }

        definition.othersAttributes = arguments

        definition.definingPlugin = definingPlugin
        definition.listenerId = ListenerId.parse(listenerPattern)

        if (log.debugEnabled) {
            log.debug "Scoring event declared in DSL - definition: ${definition.dump()}"
        }
        int score = 0
        if (definition.listenerId.namespace) score += 1
        if (definition.listenerId.topic) score += 1
        if (definition.listenerId.className) score += 1
        if (definition.listenerId.methodName) score += 1
        if (definition.listenerId.hashCode) score += 1
        if (!definingPlugin) score += 1
        definition.score = score

        def overridenDefinition = eventDefinitions.find {it.listenerId.toString() == listenerPattern}
        eventDefinitions.remove(overridenDefinition)

        eventDefinitions << definition
    }
}