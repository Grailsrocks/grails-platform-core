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
package org.grails.plugin.platform.injection

import grails.util.GrailsNameUtils
import org.slf4j.LoggerFactory
import org.grails.plugin.platform.util.PluginUtils

class InjectionImpl implements Injection {
    
    final log = LoggerFactory.getLogger(Injection)
    
    def grailsApplication
    
    Map<String, List<Closure>> injectionsByArtefactType = [:]
    
    void initInjections() {
        def plugins = grailsApplication.mainContext.pluginManager.allPlugins
        // @todo what order is this - plugin dependency order?
        plugins.each { p ->
            //def pluginName = GrailsNameUtils.getLogicalPropertyName(p.pluginClass.name, 'GrailsPlugin')
            def inst = p.instance
            if (inst.metaClass.hasProperty(inst, 'doWithInjection')) {
                def injectionDSL = inst.doWithInjection
                register(injectionDSL)
            }
        }
        apply()
    }

    /**
     * Call to apply an injection DSL to artefacts of the application, automatically at startup and reload
     */
    void register(Closure injectionBuilder) {
        def builder = new InjectionBuilder()
        
        def injections = builder.build(injectionBuilder, grailsApplication.mainContext)
        for (typeToClosureApplicators in injections) {
            for (applicator in typeToClosureApplicators.value) {
                registerInjection(typeToClosureApplicators.key, applicator)
            }
        }
    }
    
    void registerInjection(String artefactType, Closure methodApplicator) {
        def injections = injectionsByArtefactType[artefactType]
        if (!injections) {
            injectionsByArtefactType[artefactType] = [methodApplicator]
        } else {
            injections << methodApplicator
        }
    }

    void reset() {
        injectionsByArtefactType.clear()
    }
    
    void apply() {
        if (log.debugEnabled) {
            log.debug "Applying injected methods to all artefacts (${injectionsByArtefactType.keySet()})"
        }
        def allArtefacts = grailsApplication.allArtefacts
        
        for (artefact in allArtefacts) {
            applyTo(artefact)
        }
    }
    
    void applyTo(Class clazz) {
        if (log.debugEnabled) {
            log.debug "Applying injected methods to [${clazz}]"
        }
        def artefactType = grailsApplication.getArtefactType(clazz)
        if(artefactType) {
            def type = GrailsNameUtils.getPropertyName(artefactType.type)
            List<Closure> applicators = injectionsByArtefactType[type]
            List<Closure> globalApplicators = injectionsByArtefactType['*']

            if (log.debugEnabled) {
                log.debug "Applying injected methods for artefact type [$type]"
            }
            for (a in applicators) {
                // @todo do we need to clone always?
                def builder = new InjectionBuilderMethodDelegate(clazz, artefactType, a, grailsApplication.mainContext)
                def methodsToApply = builder.build()
                applyMethodsTo(clazz, methodsToApply)
            }
            if (log.debugEnabled) {
                log.debug "Applying injected methods for all artefact types"
            }
            for (a in globalApplicators) {
                def builder = new InjectionBuilderMethodDelegate(clazz, artefactType, a, grailsApplication.mainContext)
                def methodsToApply = builder.build()
                applyMethodsTo(clazz, methodsToApply)
            }
        }
    }
    
    void applyMethodsTo(Class clazz, List<InjectedMethod> methods) {
        MetaClass mc = clazz.metaClass
        for (m in methods) {
            def pluginName = m.declaringPlugin
            if (log.debugEnabled) {
                log.debug "Plugin [${pluginName}] adding${m.staticMethod ? ' static' : '' } method ${m.name}(${m.code.parameterTypes.name.join(',' )}) to [${clazz.name}]"
            }
            try {
                if (m.staticMethod) {
                    mc.'static'."${m.name}" << m.code
                } else {
                    mc[m.name] << m.code
                }
            } catch (GroovyRuntimeException e) {
                log.warn "Could not inject${m.staticMethod ? ' static' : '' } method ${m.name}(${m.code.parameterTypes.name.join(',' )}) in to [${clazz.name}], a method with that name and argument list already exists"
            }
        }
    }
}
