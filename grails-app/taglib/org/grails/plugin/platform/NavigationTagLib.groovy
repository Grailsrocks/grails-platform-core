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

import org.grails.plugin.platform.navigation.NavigationNode

class NavigationTagLib {
    static namespace = "nav"

    static returnObjectForTags = ['activePath', 'activeNode', 'scopeForActivationPath', 'firstActiveNode']
    
    def grailsNavigation
    def grailsApplication

    /**
     * Render a primary navigation menu
     * @attr path Optional activation path. If not specified, uses current request's activation path
     * @attr scope Optional scope of menu to render. If not specified, uses default scope determined by activation path or "app"
     */
    def primary = { attrs ->
        if (!attrs.scope) {
            attrs.scope = 'app'
        }
        request['plugin.platformCore.navigation.primaryScope'] = attrs.scope
        out << nav.menu(attrs)
    }
    
    /**
     * Render the secondary navigation menu
     * @attr path Optional activation path. If not specified, uses current request's activation path
     * @attr scope Optional scope of menu to render. If not specified, uses default scope determined by activation path or "app"
     */
    def secondary = { attrs ->
        def activeNodes = findActiveNodes(attrs.path)
        println "Active nodes are: ${activeNodes?.dump()}"
        if (activeNodes?.size() > 1) {
            def currentScope = grailsNavigation.scopeByName(request['plugin.platformCore.navigation.primaryScope'])
            def target = activeNodes[-1]
            println "Active node is in currentScope ${currentScope?.name}?: ${target.inScope(currentScope)}"
            // Only render secondary if the user is actively in a sub-menu of a primary nav option
            if (currentScope && target.inScope(currentScope)) {
                if (activeNodes[0].children) {
                    println "Rendering secondary for: ${target?.dump()}"
                    attrs.scope = activeNodes[0].id
                    out << nav.menu(attrs)
                }
            }
        }
    }
    
    def menu = { attrs ->
        def cssClass = attrs.class != null ? attrs.class : 'nav primary'
        def id = attrs.id ? "id=\"${attrs.id.encodeAsHTML()}\" " : ''
        def scope = attrs.scope
        if (!scope) {
            scope = 'app'
        }
        if (!(scope instanceof String)) {
            scope = scope.name
        }
        
        if (log.debugEnabled) {
            log.debug "Rendering menu for scope [${scope}]"
        }

        def activeNodes = findActiveNodes(attrs.path)
        
        def callbackContext = [grailsApplication:grailsApplication]
        
        def scopeNode = grailsNavigation.nodeForId(scope)
        if (scopeNode) {
            out << "<ul ${id}class=\"${cssClass}\">"
            for (n in scopeNode.children) {
                if (n.isVisible(callbackContext)) {
                    def liClass 
                    if (activeNodes.contains(n)) {
                        liClass = ' class="active"'
                    }
                    if (!n.isEnabled(callbackContext)) {
                        liClass = ' class="disabled"'
                    }
                    out << "<li${liClass ?: ''}>"
                    def linkArgs = n.linkArgs.clone() // Clone! naughty g.link changes them otherwise. Naughty g.link!
                    out << g.link(linkArgs, g.message(code:n.titleMessageCode, default:n.titleDefault))
                    out << "</li>"
                }
            }
            out << "</ul>"
        }
    }
    
    def items = { attrs, body ->
        def scope = attrs.scope
        def node = attrs.node
        if (!scope && !node) {
            scope = 'app'
        }
        if (scope && !(scope instanceof String)) {
            scope = scope.name
        }
        
        def varName = attrs.var
        
        out << "<ul>"
        NavigationNode parentNode = node ?: grailsNavigation.scopeByName(scope)
        for (n in parentNode.children) {
            out << "<li>"
            out << body( (varName ? [(varName):n] : n) )
            if (n.children) {
                out << nav.items([node:n, var:varName], body)
            }
            out << "</li>"
        }
        out << "</ul>"
    }

    def scopeForActivationPath = { attrs ->
        out << "NOT IMPLEMENTED - DO WE NEED THIS?"
//        attrs.path ? grailsNavigation.getScopeForId(attrs.path) : grailsNavigation.getScopeForActiveNode(request)
    }
    
    def firstActiveNode = { attrs ->
        def r = findActiveNodes(attrs.path)
        return r.size() ? r[0] : [id:''] // workaround for grails 2 bug
    }

    def setActivePath = { attrs ->
        if (attrs.path == null) {
            throwTagError('The [path] attribute is required')
        }
        grailsNavigation.setActivePath(request, 
            attrs.path instanceof List ? grailsNavigation.makePath(attrs.path) : attrs.path)
    }

    private List<NavigationNode> findActiveNodes(String activePath) {
        grailsNavigation.nodesForPath(activePath ?: grailsNavigation.getActivePath(request))
    }
    
    private NavigationNode findActiveNode(String activePath) {
        grailsNavigation.nodeForId(activePath ?: grailsNavigation.getActivePath(request))
    }
    
    def activePath = { attrs ->
        grailsNavigation.getActivePath(request)
    }
    
    def activeNode = { attrs ->
        findActiveNode(attrs.path) ?: [id:''] //workaround for 2.0.0 null return value bug
    }
    
    def breadcrumb = { attrs ->
    }
}