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

import org.grails.plugin.platform.navigation.NavigationScope
import org.grails.plugin.platform.util.TagLibUtils

/**
 * @todo TEST impl custom on nav:menu/breadcrumb
 */
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
    def primary = { attrs, body ->
        if (!attrs.scope) {
            attrs.scope = grailsNavigation.getDefaultScope(request, null)
            if (!attrs.scope) {
                attrs.scope = findScopeForActivationPath(attrs.path)
                grailsNavigation.setDefaultScope(request, attrs.scope) 
            }
        }
        if (!attrs.class) {
            attrs.class = "nav primary"
        }
        out << nav.menu(attrs, body)
    }
    
    /**
     * Render the secondary navigation menu
     * @attr path Optional activation path. If not specified, uses current request's activation path ONLY IF current activation path is in the same scope used for nav:primary
     * @attr scope Optional scope of menu to render. If not specified, uses default scope determined by activation path or "app"
     * @attr class Optional css class for the outer <ul>
     */
    def secondary = { attrs, body ->
        def pathNodes = findNodes(attrs.path)
        if (log.debugEnabled) {
            log.debug "Rendering secondary nav, active nodes are: ${pathNodes?.id}"
        }
        
        // There's only a secondary if something is active
        if (pathNodes?.size()) {
            def currentScope = grailsNavigation.scopeByName(grailsNavigation.getDefaultScope(request))
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
                    out << nav.menu(attrs, body)
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
    def menu = { attrs, body ->
        // @todo remove attributes and pass-through all that are left to <ul>
        def cssClass = attrs.class != null ? attrs.class : 'nav'
        def id = attrs.id ? " id=\"${attrs.id.encodeAsHTML()}\" " : ''
        def scope = attrs.scope
        def depth = attrs.depth != null ? attrs.depth.toInteger() : 1
        def alwaysRenderChildren = attrs.forceChildren == 'true' || attrs.forceChildren.is(Boolean.TRUE)
        if (!scope) {
            def requestPath = attrs.path ?: grailsNavigation.getActivePath(request)
            def pathScope = nav.scopeForActivationPath(path:requestPath)
            scope = pathScope ?: grailsNavigation.getDefaultScope(request)
        }
        if (!(scope instanceof String)) {
            scope = scope.name
        }
        
        if (log.debugEnabled) {
            log.debug "Rendering menu for scope [${scope}]"
        }

        def custom = attrs.custom
        def customBody = (custom == 'true') || custom.is(Boolean.TRUE)

        def activeNodes = findNodes(attrs.path)
        
        def callbackContext = [
            grailsApplication:grailsApplication,
            pageScope:pageScope,
            session:session,
            request:request,
            controllerName:controllerName,
            actionName:actionName,
            flash:flash,
            params:params
        ]
        
        def scopeNode = grailsNavigation.nodeForId(scope)
        if (scopeNode) {
            out << "<ul${id}"
            if (cssClass) {
                out << " class=\"${cssClass.encodeAsHTML()}\""
            }
            out << ">"
            if (log.debugEnabled) {
                log.debug "Rendering menu for scope [${scope}] which has children ${scopeNode.children.name}"
            }
            for (n in scopeNode.children) {
                if (n.isVisible(callbackContext)) {
                    def active = activeNodes.contains(n)
                    def enabled = n.isEnabled(callbackContext)
                    
                    def linkArgs = new HashMap(n.linkArgs) // Clone! naughty g.link changes them otherwise. Naughty g.link!
                    if (customBody) {
                        // Always give custom body a clone of the link args as they can't use the ones from item
                        // Custom body is responsible for rendering nested items
                        out << body([item:n, linkArgs:linkArgs, active:active, enabled:enabled])        
                    } else {
                        def liClass 
                        if (active) {
                            liClass = ' class="active"'
                        }
                        if (!enabled) {
                            liClass = ' class="disabled"'
                        }
                        out << "<li${liClass ?: ''}>"
                        out << g.link(linkArgs, nav.title(item:n, codec:''))

                        if ((active || alwaysRenderChildren) && (depth > 1)) {
                            def nestedAttrs = attrs.clone()
                            nestedAttrs.depth = depth - 1
                            nestedAttrs.scope = n.id
                            out << nav.menu(nestedAttrs, body)
                        }

                        out << "</li>"
                    }
                }
            }
            out << "</ul>"
        } else if (log.debugEnabled) {
            log.debug "Attempt to render menu for scope [${scope}] but there was no navigation node found for that scope."
        }
    }
    
    
    def items = { attrs, body ->
        def scope = attrs.scope
        def node = attrs.node
        if (!scope && !node) {
            scope = grailsNavigation.getDefaultScope(request)
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
    
    private findScopeForActivationPath(path) {
        def rootScope = path ? grailsNavigation.nodeForId(path)?.rootScope : grailsNavigation.getActiveNode(request)?.rootScope
        return rootScope ? rootScope.name : null
    }

    /**
     * Return the name of the root scope of the supplied or default activation path
     * @attr path Optional activation path
     */
    def scopeForActivationPath = { attrs ->
        return findScopeForActivationPath(attrs.path)
    }
    
    /**
     * Return the first node in the activation path specified, or based on current activation path
     * @attr path Optional activation path
     */
    def firstActiveNode = { attrs ->
        def r = findNodes(attrs.path)
        return r.size() ? r[0] : [id:''] // workaround for grails 2 bug
    }

    /**
     * Set a value on the current request. You can set the default scope to make menu/primary default to something other than "app"
     * or the current active path's scope, and you can set the active path for this request.
     * @attr scope Optional - scope to default to
     * @attr path Optional - active path to use
     */
    def set = { attrs ->
        if (attrs.path) {
            grailsNavigation.setActivePath(request, 
                attrs.path instanceof List ? grailsNavigation.makePath(attrs.path) : attrs.path)
        }
        if (attrs.scope) {
            grailsNavigation.setDefaultScope(request, attrs.scope) 
        }
    }

    private List<NavigationScope> findNodes(String activePath) {
        grailsNavigation.nodesForPath(activePath ?: grailsNavigation.getActivePath(request))
    }
    
    private NavigationScope findNode(String activePath) {
        grailsNavigation.nodeForId(activePath ?: grailsNavigation.getActivePath(request))
    }
    
    /**
     * Return the current active path
     */
    def activePath = { attrs ->
        grailsNavigation.getActivePath(request)
    }
    
    /**
     * Return the current active navigation node, or the node specified by the path attribute
     */
    def activeNode = { attrs ->
        findNode(attrs.path) ?: [id:''] //workaround for 2.0.0 null return value bug, can't return null :(
    }

/*
    * Work in progress - Render a breadcrumb, with optional custom rendering
    def breadcrumb = { attrs, body ->
        // @todo remove attributes and pass-through all that are left to <ul>

        def nodes = findNodes(attrs.path)
        def cssClass = attrs.class == null ? 'breadcrumb' : attrs.class
        def id = attrs.id ? " id=\"${attrs.id.encodeAsHTML()}\" " : ''

        def custom = attrs.custom
        def customBody = (custom == 'true') || custom.is(Boolean.TRUE)

        if (!nodes) {
            TagLibUtils.warning('nav:breadcrumb', "No activation path for this request and no path attribute set, or path [${attrs.path}] cannot be resolved")
        } else {
            if (!customBody) {
                out << "<ul${id}"
                if (cssClass) {
                    out << " class=\"${cssClass.encodeAsHTML()}\""
                }
                out << ">"
            }
            def first = true
            int l = nodes.size()
            for (int i = 0; i < l; i++) {
                def n = nodes[i]
                def linkArgsCloned = new HashMap(n.linkArgs)
                if (customBody) {
                    out << body([item:n, linkArgs:linkArgsCloned, first:first, last:i == l-1])
                } else {
                    def text = g.message(code:n.titleMessageCode, default:n.titleDefault)
                    out << "<li>${g.link(linkArgsCloned, text)}</li>"
                }
                first = false
            }
            if (!customBody) {
                out << "</ul>"
            }
        }
    }
*/

    /**
     * Render the i18n title of a navigation item
     * @attr item The navigation item (instance of NavigationItem)
     * @attr codec Optional codec to apply. If none specified defaults to HTML
     */
    def title = { attrs ->
        def item = attrs.item
        def codec = attrs.codec == null ? 'HTML' : attrs.codec
        out << g.message(code:item.titleMessageCode, default:item.titleDefault, encodeAs:codec ?: null)
    }
}