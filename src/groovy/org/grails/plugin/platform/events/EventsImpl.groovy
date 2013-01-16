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
package org.grails.plugin.platform.events

import grails.events.EventDeclarationException
import grails.events.Listener
import grails.util.GrailsNameUtils
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.plugin.platform.conventions.DSLCallCommand
import org.grails.plugin.platform.conventions.DSLCommand
import org.grails.plugin.platform.conventions.DSLEvaluator
import org.grails.plugin.platform.conventions.DSLNamedArgsCallCommand
import org.grails.plugin.platform.conventions.DSLSetValueCommand
import org.grails.plugin.platform.events.publisher.EventsPublisher
import org.grails.plugin.platform.events.registry.EventsRegistry
import org.grails.plugin.platform.events.utils.EventsUtils
import org.grails.plugin.platform.util.PluginUtils
import org.springframework.context.ApplicationContext

import java.lang.reflect.Method
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class EventsImpl implements Events {

    static final private log = Logger.getLogger(EventsImpl.class)

    EventsRegistry grailsEventsRegistry
    EventsPublisher grailsEventsPublisher
    private ApplicationContext applicationContext
    GrailsApplication grailsApplication

    List<EventDefinition> eventDefinitions

    static final String APP_NAMESPACE = 'app'

    static final dslArgumentsToMap = [EventsPublisher.TIMEOUT, EventsPublisher.ON_ERROR, EventsPublisher.ON_REPLY,
             EventsPublisher.FORK]

    def injectedMethods = { theContext ->

        'controller, domain, service' { Class clazz ->
            String pluginName = PluginUtils.getNameOfDefiningPlugin(theContext, clazz)
            Map pluginParam = [plugin: pluginName]

            def self = theContext.grailsEvents
            //def config = theContext.grailsApplication.config.plugin.platformCore

            event { String topic ->
                self.event(null, topic, null, pluginParam, null)
            }

            event { String topic, Closure callback ->
                self.event(null, topic, null, pluginParam, callback)
            }

            event { String topic, data, Closure callback ->
                self.event(null, topic, data, pluginParam, callback)
            }

            event { String topic, data = null, Map params = null, Closure callback = null ->
                if (pluginName) {
                    params = params ? pluginParam + params : pluginParam
                }
                self.event(null, topic, data, params, callback)
            }

            event { Map args, Closure callback = null ->
                def ns = args.remove('for') ?: args.remove('namespace')
                if (pluginName)
                    args += pluginParam

                self.event(ns, args.remove('topic'), args.remove('data'), args.remove('params') ?: args, callback)
            }

            on { String topic, Closure callback ->
                self.checkNamespace pluginName, null
                self.grailsEventsRegistry.on(null, topic, callback)
            }
            on { String namespace, String topic, Closure callback ->
                self.checkNamespace pluginName, namespace
                self.grailsEventsRegistry.on(namespace, topic, callback)
            }

            copyFrom(self, 'waitFor')
            copyFrom(self.grailsEventsRegistry, ['removeListeners', 'countListeners'])
        }
    }

    private boolean processEventsDefinition(EventMessage message, Map params) {
        EventDefinition definition = eventDefinitions.find {it.topic == message.event && it.namespace == message.namespace}

        if (!definition?.disabled && ((!definition?.filterClass && !definition?.filterClosure) ||
                (definition?.filterClass && message.data in definition.filterClass) ||
                (definition?.filterClosure &&
                        definition.filterClosure.clone().call(definition.filterEventMessage ? message : message.data)))) {

            if (definition) {
                for (key in dslArgumentsToMap) {
                    if (!params.containsKey(key)) params[key] = definition[key]
                }
            }
            true
        } else {
            false
        }
    }

    private void checkNamespace(pluginNs, targetNs, context = null) {
        if (pluginNs && !targetNs) {
            throw new EventDeclarationException("Your plugin $pluginNs must specify the namespace when using events methods or annotations " +
                    (context ?: ''))
        }
    }

    Object[] waitFor(long l, TimeUnit timeUnit, EventReply... replies) throws ExecutionException, InterruptedException, TimeoutException {
        for (reply in replies) {
            reply?.get(l, timeUnit)
        }
        replies
    }

    Object[] waitFor(EventReply... replies) throws ExecutionException, InterruptedException, TimeoutException {
        waitFor(-1l, TimeUnit.NANOSECONDS, replies)
    }

    EventReply event(String namespace, String topic, data = null, Map params = [:], Closure callback = null) {
        def eventMessage = buildEvent(params?.plugin, namespace, topic, data, params)
        if (processEventsDefinition(eventMessage, params)) {
            if (log.debugEnabled) {
                log.debug "Sending event of namespace [$namespace] and topic [$topic] with data [${data}] and params [${params}]"
            }
            def reply
            callback = callback ?: params?.get(EventsPublisher.ON_REPLY) as Closure
            if (params?.containsKey(EventsPublisher.FORK) && !params.remove(EventsPublisher.FORK)) {
                reply = grailsEventsPublisher.event(eventMessage)
                reply.onError = params?.get(EventsPublisher.ON_ERROR) as Closure
                reply.throwError()
                callback?.call(reply)
            } else
                reply = grailsEventsPublisher.eventAsync(eventMessage, params)

            return reply
        }
        null
    }

    EventMessage buildEvent(String pluginName, String namespace, String topic, data, Map params) {
        boolean gormSession = params?.containsKey(EventsPublisher.GORM) ? params.remove(EventsPublisher.GORM) as boolean : true
        namespace = params?.remove(EventsPublisher.NAMESPACE) ?: namespace ?: ListenerId.parse(topic).namespace
        checkNamespace pluginName, namespace

        namespace = namespace ?: APP_NAMESPACE

        new EventMessage(topic, data, namespace, gormSession, params?.remove(EventsPublisher.HEADERS))
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
                    String pluginName = PluginUtils.getNameOfDefiningPlugin(applicationContext, serviceClass)
                    String namespace = annotation.namespace()
                    checkNamespace pluginName, namespace, "-> @Listener $serviceClass.name#$method.name"

                    String topic = annotation.topic() ?: method.name
                    c(namespace ?: APP_NAMESPACE, annotation?.namespace() as boolean, topic, method, serviceClass, annotation.proxySupport())
                }
            }
        }
    }

    EventDefinition matchesDefinition(String topic, Method method, Class serviceClass) {
        for (definition in eventDefinitions) {
            if (definition.topic == topic) {
                log.info "Applying Event definition [$definition.topic] from [$definition.definingPlugin]"
                return definition
            }
        }
        null
    }

    void registerListeners(Collection<Class> serviceClasses) {
//            grailsEventsDispatcher.scanClassForMappings(serviceClass)
        def bean
        eachListener(serviceClasses) {String namespace, boolean hasInlineNamespace,
                                      String topic, Method method, Class serviceClass, boolean proxySupport ->

            def definition = matchesDefinition(topic, method, serviceClass)

            // If there is no match with a known event, or there is a declared event and it is not disabled,
            // add the listener
            log.info "Register event listener $serviceClass.name#$method.name for topic $topic and namespace $namespace and proxy support $proxySupport"
            if (!definition) {
                log.warn "Event listener $serviceClass.name#$method.name declared for topic $topic and namespace $namespace but no such event is declared, you may never receive it"
            }

            bean = applicationContext.getBean(GrailsNameUtils.getPropertyName(serviceClass))
            if (!proxySupport) {
                bean = EventsUtils.unproxy(bean);
            }

            grailsEventsRegistry.on(
                    namespace,
                    topic,
                    bean,
                    method
            )
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
        List<DSLCommand> commands = new DSLEvaluator().evaluate(dsl,grailsApplication)
        String definingPlugin = PluginUtils.getNameOfDefiningPlugin(grailsApplication.mainContext, dsl)
        parseDSL(commands, definingPlugin)
    }

    void loadDSL(Class dslClass) {
        Script dslInstance = dslClass.newInstance() as Script
        dslInstance.binding["grailsApplication"] = grailsApplication
        dslInstance.binding["ctx"] = grailsApplication.mainContext
        dslInstance.binding["config"] = grailsApplication.config
        dslInstance.run()
        def dsl = dslInstance.binding['events'] ? dslInstance.binding['events'] as Closure : null
        if (dsl) {
            registerEvents(dsl)
        } else {
            log.warn "Tried to load events data from artefact [${dslClass}] but no 'events' value was found in the script"
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
                case DSLSetValueCommand:
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

    private addItemFromArgs(String topic, Map arguments, String definingPlugin) {
        if (log.debugEnabled) {
            log.debug "Adding event declared in DSL - topic: ${topic}, arguments: ${arguments}, defined by plugin ${definingPlugin}"
        }
        def definition = new EventDefinition()

        for (key in [EventsPublisher.NAMESPACE, 'requiresReply', 'disabled', 'secured',
                EventsPublisher.TIMEOUT, EventsPublisher.ON_ERROR, EventsPublisher.ON_REPLY,
               EventsPublisher.FORK]) {
            definition[key] = arguments?.remove(key) ?: definition[key]
        }

        if (!definition.namespace) {
            definition.namespace = definingPlugin ?: APP_NAMESPACE
        }

        def filter = arguments?.remove('filter')

        if (filter) {
            if (Closure.isAssignableFrom(filter.getClass())) {
                definition.filterClosure = filter as Closure
                definition.filterEventMessage = EventMessage.isAssignableFrom(Closure.cast(filter).parameterTypes[0])
            }
            if (Class.isAssignableFrom(filter.getClass())) {
                definition.filterClass = filter as Class
            }
        }

        definition.othersAttributes = arguments

        definition.definingPlugin = definingPlugin
        definition.topic = topic

        if (log.debugEnabled) {
            log.debug "Scoring event declared in DSL - definition: ${definition.dump()}"
        }
        int score = 0
        if (!definingPlugin) score += 1
        if (!topic.contains('*')) score += 1
        definition.score = score

        def overridenDefinition = eventDefinitions.find {it.topic == topic}
        eventDefinitions.remove(overridenDefinition)

        eventDefinitions << definition
    }

}