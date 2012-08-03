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
package org.grails.plugin.platform

class SystemTagLib {
    static namespace = "p"
    
    static returnObjectForTags = ['config']
    
    def grailsApplication
    def pluginManager
    
    def config = { attrs ->
        def conf = grailsApplication.config.flatten()
        def n = attrs.name
        if (!n) {
            throwTagError "You must specify a [name] attribute containing the full path of the config value you which to output"
        }
        def defaultValue = attrs.'default'
        return !(conf[n] instanceof ConfigObject) ? conf[n] : defaultValue
    }

    def requiresBean = { attrs ->
        def name = attrs.name
        def clazz = attrs['class']
        if (pageScope.variables[name] == null) {
            pageScope.variables[name] = grailsApplication.classLoader.loadClass(clazz).newInstance()
        }
    }
}