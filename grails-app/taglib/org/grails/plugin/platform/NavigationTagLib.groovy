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
package org.grails.plugin.platform

class NavigationTagLib {
    static namespace = "nav"

    static returnObjectForTags = ['activePath', 'activeNode', 'scopeForActivePath']
    
    def grailsNavigation

    def menu = { attrs ->
        println "ATTRS: $attrs"
        def scope = attrs.scope
        if (!scope) {
            scope = 'app'
        }
        println "scope: $scope is a (${scope.getClass()})"
        if (!(scope instanceof String)) {
            println "xxxscope: $scope"
            scope = scope.id
        }
        
        println "scope: $scope"
        if (log.debugEnabled) {
            log.debug "Rendering menu for scope [${scope}]"
        }

        def activeNode = findActiveNode()
        
        def scopeNode = grailsNavigation.scopeByName(scope)
        if (scopeNode) {
            out << "<ul class=\"nav primary\">"
            for (n in scopeNode.children) {
                if (n.visible) {
                    def liClass 
                    if (activeNode == n) {
                        liClass = ' class="active"'
                    }
                    if (!n.enabled) {
                        liClass = ' class="disabled"'
                    }
                    out << "<li${liClass ?: ''}>"
                    def linkArgs = [:]
                    out << g.link(n.linkArgs, g.message(code:n.titleMessageCode, default:n.titleDefault))
                    out << "</li>"
                }
            }
            out << "</ul>"
        }
    }

    def scopeForActivePath = { attrs ->
        grailsNavigation.getDefaultScopeForActivePath(
            attrs.path ?: grailsNavigation.getActivePath(request))
    }
    
    def setActivePath = { attrs ->
        if (attrs.path == null) {
            throwTagError('The [path] attribute is required')
        }
        grailsNavigation.setActivePath(request, 
            attrs.path instanceof List ? grailsNavigation.makeActivationPath(attrs.path) : attrs.path)
    }

    private findActiveNode() {
        grailsNavigation.nodeForActivationPath(grailsNavigation.getActivePath(request))
    }
    
    def activePath = { attrs ->
        grailsNavigation.getActivePath(request)
    }
    
    def activeNode = { attrs ->
        findActiveNode() ?: [id:''] //workaround for 2.0.0 bug
    }
    
    def breadcrumb = { attrs ->
    }
}