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

/**
 * Supports simple DSL for adding methods to classes
 */
class InjectionBuilderArtefactTypeDelegate {
    Map<String, List<Closure>> results
    def appCtx
    
    InjectionBuilderArtefactTypeDelegate(Map<String, List<Closure>> res, applicationContext) {
        results = res
        appCtx = applicationContext
    }
    
    void register(Closure nestedDeclarations) {
        def target = nestedDeclarations.clone()
        target.delegate = this
        target(appCtx)
    }
    
    def methodMissing(String name, args) {
        def names = [name]
        if (name.contains(',')) {
            def splitNames = name.tokenize(',')
            names = splitNames*.trim()
        }

        assert args[0] instanceof Closure
        def closure = args[0]

        for (n in names) {
            def beanName = GrailsNameUtils.getPropertyName(n)
            def applicatorList = results[beanName] ?: [] as List<Closure>
            results[beanName] = applicatorList
            applicatorList << closure
        }
    }
}