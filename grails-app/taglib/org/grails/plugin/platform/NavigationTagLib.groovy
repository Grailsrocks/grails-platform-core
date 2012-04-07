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

import org.grails.plugin.platform.navigation.NavigationScope

class NavigationTagLib {
    static namespace = "nav"

    static returnObjectForTags = ['activePath', 'activeNode', 'scopeForActivationPath', 'firstActiveNode']
    
    def grailsNavigation
    def grailsApplication

    /**
     * Render a primary navigation menu
     * @attr path Optional activation path. If not specified, uses current request's activation path
     * @attr scope Optional scope of menu to render. If not specified, uses default scope determined by activation path or "app"
     * @attr class Optional css class for the outer <ul>
     */
    def primary = { attrs ->
        if (!attrs.scope) {
            attrs.scope = 'app'
        }
        if (!attrs.class) {
            attrs.class = "nav primary"
        }
        request['plugin.platformCore.navigation.primaryScope'] = attrs.scope
        out << nav.menu(attrs)
    }
    
    /**
     * Render the secondary navigation menu
     * @attr path Optional activation path. If not specified, uses current request's activation path ONLY IF current activation path is in the same scope used for nav:primary
     * @attr scope Optional scope of menu to render. If not specified, uses default scope determined by activation path or "app"
     * @attr class Optional css class for the outer <ul>
     */
    def secondary = { attrs ->
        def pathNodes = findNodes(attrs.path)
        if (log.debugEnabled) {
            log.debug "Rendering secondary nav, active nodes are: ${pathNodes?.id}"
        }
        
        // There's only a secondary if something is active
        if (pathNodes?.size()) {
            def currentScope = grailsNavigation.scopeByName(request['plugin.platformCore.navigation.primaryScope'])
            def target = pathNodes[-1]
            if (log.debugEnabled) {
                log.debug "Rendering secondary nav, active node is in currentScope ${currentScope?.name}?: ${target.inScope(currentScope)}"
            }
            // Only render secondary if the user is actively in a sub-menu of a primary nav option
            // or if they explicitly passed us a nav path to render secondary for
            if (attrs.path || (currentScope && target.inScope(currentScope))) {
                if (pathNodes[0].children) {
                    if (log.debugEnabled) {
                        log.debug "Rendering secondary for: ${target?.dump()}"
                    }
                    // Amend the scope to the correct node
                    attrs.scope = pathNodes[0].id
                    if (attrs.class == null) {
                        attrs.class = "nav secondary"
                    }
                    out << nav.menu(attrs)
                }
            }
        }
    }
    
    /**
     * Render a menu for a given scope and path
     * @attr scope Optional scope to render menu for. Defaults to "app", but could be any valid scope i.e. "app/messages/archive"
     * @attr path Optional activation path indicating what is currently active.
     * @attr class Optional
     */
    def menu = { attrs ->
        def cssClass = attrs.class != null ? attrs.class : 'nav'
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

        def activeNodes = findNodes(attrs.path)
        
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
        NavigationScope parentNode = node ?: grailsNavigation.scopeByName(scope)
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
        attrs.path ? grailsNavigation.nodeForId(attrs.path)?.rootScope.name : grailsNavigation.getActiveNode(request)?.rootScope.name
    }
    
    def firstActiveNode = { attrs ->
        def r = findNodes(attrs.path)
        return r.size() ? r[0] : [id:''] // workaround for grails 2 bug
    }

    def setActivePath = { attrs ->
        if (attrs.path == null) {
            throwTagError('The [path] attribute is required')
        }
        grailsNavigation.setActivePath(request, 
            attrs.path instanceof List ? grailsNavigation.makePath(attrs.path) : attrs.path)
    }

    private List<NavigationScope> findNodes(String activePath) {
        grailsNavigation.nodesForPath(activePath ?: grailsNavigation.getActivePath(request))
    }
    
    private NavigationScope findNode(String activePath) {
        grailsNavigation.nodeForId(activePath ?: grailsNavigation.getActivePath(request))
    }
    
    def activePath = { attrs ->
        grailsNavigation.getActivePath(request)
    }
    
    def activeNode = { attrs ->
        findNode(attrs.path) ?: [id:''] //workaround for 2.0.0 null return value bug
    }
    
    def breadcrumb = { attrs ->
    }
}