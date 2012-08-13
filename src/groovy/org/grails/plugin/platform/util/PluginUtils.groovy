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
package org.grails.plugin.platform.util

import org.codehaus.groovy.grails.plugins.metadata.GrailsPlugin
import grails.util.GrailsNameUtils

class PluginUtils {
    /**
     * Find the GrailsPlugin instance for this app, in the case you are run-app'ing a plugin
     * @return the GrailsPlugin of the current plugin being run, or null if it is not a plugin
     */
    static findAppPlugin(applicationContext) {
        def allPlugins = applicationContext.pluginManager.allPlugins
        allPlugins.find { it.basePlugin }
    }
    
    /** 
     * Work out which Grails plugin (if any) defined the class of the object supplied
     */
    static String getNameOfDefiningPlugin(applicationContext, def object) {
        def originalObject = object
        Class clazz
        if (object instanceof Class) {
            clazz = object
        } else {
            while (object instanceof Closure) {
                object = object.owner
            }
            clazz = object.getClass()
        } 

        def appPlugin = findAppPlugin(applicationContext)
        def pluginAnnotation = clazz.getAnnotation(GrailsPlugin)

        if (pluginAnnotation || appPlugin) {
            return pluginAnnotation ? 
                GrailsNameUtils.getPropertyNameForLowerCaseHyphenSeparatedName(pluginAnnotation.name()) : 
                appPlugin.name
        } else {
            return null
        }
    }
}