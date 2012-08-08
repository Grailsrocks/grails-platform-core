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

class ViewCallbackDelegate {
    def props
    def grailsApplication
    def model

    ViewCallbackDelegate(app, m, Map concreteProps) {
        props = concreteProps
        grailsApplication = app
        model = m
    }

    /**
     * Return a predefined property or bean from the context
     */
    def propertyMissing(String name) {
        if (this.@props.containsKey(name)) {
            return this.@props[name]
        } else if (this.@model[name] != null) {
            return this.@model[name]
        } else {
            return this.@grailsApplication.mainContext.getBean( name )
        }
    }
}
