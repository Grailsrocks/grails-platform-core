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

import grails.util.Environment
import org.slf4j.LoggerFactory

class TagLibUtils {
    static final log = LoggerFactory.getLogger(TagLibUtils)

    static final String EMPTY = ''
    
    static valueToGroovy(v, boolean quoteString = false) {
        def vString
        if (v instanceof Map) {
            vString = new StringBuilder("[")
            boolean first = true
            for (entry in v) {
                if (!first) {
                    vString <<= ','
                }
                first = false
                vString << "${entry.key}: ${valueToGroovy(entry.value, true)}"
            }
            vString << ']'
        } else if (v instanceof Collection) {
            vString = new StringBuilder("[")
            boolean first = true
            for (entry in v) {
                if (!first) {
                    vString <<= ','
                }
                first = false
                vString << valueToGroovy(entry.value, true)
            }
            vString << ']'
        } else if (v instanceof Number) {
            vString = '\${v}'
        } else if (v instanceof Boolean) {
            vString = '${'+v+'}'
        } else {
            vString = quoteString ? "'${v}'" : v.toString()
        }
        return vString
    }

    /**
     * Convert a Map of attributes to a HTML attribute list String
     * @param attrs The map of attributes
     * @return A string of the form: x="y" p="q" 
     */ 
    static attrsToString(Map attrs) {
        final resultingAttributes = []
        for (e in attrs.entrySet()) {
            def v = e.value
            if (v != null) {
                def vString = valueToGroovy(v)
                resultingAttributes << "${e.key}=\"${vString}\""
            }
        }
        return resultingAttributes ? " ${resultingAttributes.join(' ')}" : EMPTY
    }
    
    /**
     * Convert a value of unknown type into a Set of values. If it is already a Set, nothing is done.
     * If it is a String, it will be split on commas and each value trimmed, and put into a Set
     * Collections are converted to sets. Anything else causes an error.
     */
    static attrSetOfItems(String attrName, value, Set defaultValue = null) {
        if (value) {
            if (value instanceof Set) {
                return value 
            } else if (value instanceof String) {
                return value.split(',')*.trim() as Set
            } else if (value instanceof Collection) {
                return value as Set
            } else {
                throw new IllegalArgumentException("Tag [$attrName] expected a comma-delimited string, collection or Set")
            }
        } 
        return defaultValue
    }

    /**
     * Convert a value of unknown type into a List of values. If it is already a List, nothing is done.
     * If it is a String, it will be split on commas and each value trimmed, and put into a List
     * Collections are converted to sets. Anything else causes an error.
     */
    static attrListOfItems(String attrName, stringOrListValue, List defaultValue = null) {
        if (stringOrListValue) {
            if (stringOrListValue instanceof List) {
                return stringOrListValue
            } else if (stringOrListValue instanceof String) {
                return stringOrListValue.split(',')*.trim() as List
            } else if (!(stringOrListValue instanceof List) && (stringOrListValue instanceof Collection)) {
                return (stringOrListValue as List).sort() // at least have reproducible ordering
            } else {
                throw new IllegalArgumentException("Tag [$attrName] expected a comma-delimited string, collection or Set")
            }
        } 
        return defaultValue
    }
    
    /**
     * Resolve a tag string of the form x:yyyyy into a tag namespace and tag name, with optional no-namespacing for
     * implicit g: tags
     */
    static resolveTagName(String name) {
        def parts = name.tokenize(':')
        def ns
        def tagName
        switch (parts.size()) {
            case 1: 
                ns = "g"
                tagName = parts[0]
                break;
            case 2:
                ns = parts[0]
                tagName = parts[1]
                break;
            default:
                throwTagError "The name needs to have a g: namespace tag name or a 'namespace:tagName' value"
                break;
        }
        return [ns, tagName]
    }
    
    
    /**
     * Generate a request-unique id
     */
    static String newUniqueId(request) {
        def id = request.'plugin.platformCore.request.id.counter' ?: 0
        request.'plugin.platformCore.request.id.counter' = ++id
        return id
    }
    
    static void warning(String tagName, String message) {
        if (Environment.current == Environment.DEVELOPMENT) {
            log.warn "Tag [$tagName]: $message"
        }
    }

    static executeViewCallback(context, Closure callback) {
        
    }
}