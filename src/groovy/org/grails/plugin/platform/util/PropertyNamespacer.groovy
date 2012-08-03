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

/**
 * Provides read/write access to a Map, automatically namespacing
 * all keys to provide a safe scoped access to an existing Map-like object
 */
class PropertyNamespacer {
    private delegateMapLikeObject
    private String keyPrefix
    private String keySetMethodName
    
    PropertyNamespacer(String keyPrefix, delegateMapLikeObject, String keySetMethodName) {
        this.delegateMapLikeObject = delegateMapLikeObject
        this.keyPrefix = 'plugin.'+keyPrefix
        this.keySetMethodName = keySetMethodName
    }
    
    def propertyMissing(String name, value) { 
        put(name, value)
    }
    
    def propertyMissing(String name) {
        get(name)
    }
    
    Object get(Object key) {
        this.@delegateMapLikeObject[this.@keyPrefix + key.toString()]
    }

    Object put(Object key, Object value) {
        this.@delegateMapLikeObject[this.@keyPrefix + key.toString()] = value
    }
    
    Set<String> keySet() {
        def allKeys = this.@delegateMapLikeObject."$keySetMethodName"()
        def prefix = this.@keyPrefix
        allKeys.findAll { it.startsWith(prefix) } as Set
    }
    
    Map toMap() {
        Map result = [:]
        for (k in keySet()) {
            def value = this.@delegateMapLikeObject[k]
            result[k] = value
        }
        return result
    }
    
    String toString() {
        "PropertyNamespacer for namespace [${this.@keyPrefix}] with keys: ${keySet()}"
    }
}