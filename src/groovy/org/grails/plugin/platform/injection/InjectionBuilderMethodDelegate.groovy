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

import org.codehaus.groovy.runtime.MethodClosure
import org.grails.plugin.platform.util.PluginUtils

/**
 * Supports simple DSL for adding methods to classes
 */
class InjectionBuilderMethodDelegate {
    Closure applicator
    Class targetClass
    def targetArtefact
    def appContext
    List<InjectedMethod> results = []
    
    InjectionBuilderMethodDelegate(Class clazz, artefact, Closure applicator, appContext) {
        this.applicator = applicator.clone()
        targetClass = clazz
        targetArtefact = artefact
        this.appContext = appContext
    }
    
    List<InjectedMethod> build() {
        applicator.delegate = this
        applicator.resolveStrategy = Closure.DELEGATE_FIRST
        if (applicator.maximumNumberOfParameters == 2) {
            applicator(targetClass, targetArtefact)
        } else {
            applicator(targetClass)
        }
        return results
    }
    
    void copyFrom(bean, String methodName, Map meta = null) {
        copyFrom(bean, [methodName], meta)
    }
    
    void copyFrom(bean, List methodNames, Map meta = null) {
        for (n in methodNames) {
            def pluginName = PluginUtils.getNameOfDefiningPlugin(appContext, bean)
            addMethod(n, new MethodClosure(bean, n), pluginName, meta)
        }
    }
    
    void addMethod(String name, Closure code, String declaringPlugin, Map meta = null) {
        boolean isStatic = meta?.staticMethod ?: false
        results << new InjectedMethod(staticMethod:isStatic, name:name, code:code, declaringPlugin:declaringPlugin)
    }

    def methodMissing(String name, args) {
        def validCall = false
        def code
        def meta
        switch (args.size()) {
            case 2:
                validCall = (args[0] instanceof Map) && (args[1] instanceof Closure)
                meta = args[0]
                code = args[1]
                break;
            case 1:
                validCall = args[0] instanceof Closure
                code = args[0]
                break;
        }
        if (!validCall) {
            throw new IllegalArgumentException(
                "The injection builder expects method calls like someMethodName(Closure code) or someMethodName(Map args, Closure code)")
        }

        def pluginName = PluginUtils.getNameOfDefiningPlugin(appContext, code)

        addMethod(name, code, pluginName, meta)
    }
}