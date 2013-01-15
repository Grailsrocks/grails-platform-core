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
package org.grails.plugin.platform.navigation

import org.grails.plugin.platform.util.PluginUtils
import org.grails.plugin.platform.conventions.*
import grails.util.GrailsNameUtils

import org.slf4j.LoggerFactory

/**
 * Bean that encapsulates the navigation structure of the entire application
 *
 * @todo Auto-set parent title if same action is declared in a child and title specified / vice versa
 * @todo Support CRUD multi-actions correctly out of the box with xxx(controller:y, scaffold:true) and auto-sense scaffold where possible.
 * default item is list, sub item Create, actions list, show, edit, update map activate list, create and saave activate Create
 */
class NavigationImpl implements Navigation {
    
    static LINK_TAG_ATTRIBUTES = ['controller', 'action', 'mapping', 'uri', 'url', 'view']
    
    final log = LoggerFactory.getLogger(NavigationImpl)

    Map<String, NavigationScope> rootScopes
    Map<String, NavigationScope> nodesById
    Map<String, NavigationScope> nodesByControllerAction
    
    def grailsApplication
    def grailsConventions
    
    List<NavigationScope> getScopes() {
        rootScopes.values() as List
    }
    
    void setActivePath(request, String path) {
        if (log.debugEnabled) {
            log.debug "Setting navigation active path for this request to: $path"
        }
        request['plugin.platformCore.navigation.activePath'] = path
        request['plugin.platformCore.navigation.activeNode'] = nodeForId(path)
    }

    void setDefaultScope(request, String scope) {
        request['plugin.platformCore.navigation.defaultScope'] = scope
    }

    String getDefaultScope(request, defaultValue = 'app') {
        request['plugin.platformCore.navigation.defaultScope'] ?: defaultValue
    }

    String getDefaultControllerAction(String controllerName) {
        def artef = grailsApplication.getArtefact('Controller', controllerName)
        if (log.debugEnabled) {
            log.debug "Getting default action for [$controllerName]"
        }
        return artef?.defaultAction ?: 'index' 
    }

    /**
     * Attempt to location the current request's controller and action in the nav graph,
     * looking in "app" scope first, then other scopes
     * If found, the id of that node becomes our active path
     */
    void setActivePathFromRequest(request, controllerName, action) {
        if (log.debugEnabled) {
            log.debug "Setting navigation active path from current request controller/action [$controllerName] and [$action]"
        }
        
        if (controllerName) {
            if (!action) {
                action = getDefaultControllerAction(controllerName)
            }
            
            def path 
            // See if we can reverse map from controller/action to an activation path
            def node = nodeForControllerAction(controllerName, action)
            if (node) {
                path = node.id
            }

            if (log.debugEnabled) {
                log.debug "Setting navigation active path from current request controller/action [$controllerName] and [$action], found node [$node] and setting active path to [$path]"
            }
            if (path) {
                setActivePathWasAuto(request, true)
                setActivePath(request, path)
            }
        }
    }
    
    String getActivePathWasAuto(request) {
        request['plugin.platformCore.navigation.activePath.auto']
    }
    
    void setActivePathWasAuto(request, boolean value) {
        request['plugin.platformCore.navigation.activePath.auto'] = true
    }
    
    String getActivePath(request) {
        request['plugin.platformCore.navigation.activePath']
    }
    
    NavigationScope getActiveNode(request) {
        request['plugin.platformCore.navigation.activeNode']
    }

    NavigationScope getFirstNodeOfPath(String path) {
        getFirstAncestor(path)
    }
    
    NavigationScope getFirstActiveNode(request) {
        getFirstAncestor(getActiveNode(request)?.id)
    }
    
    NavigationScope getFirstAncestor(String path) {
        def parts = splitPath(path)
        if (parts) {
            return nodeForId(parts[0])
        } else {
            return null
        }
    }
    
    NavigationScope scopeByName(String name) {
        return rootScopes[name]
    }

    NavigationScope nodeForId(String path) {
        if (path) {
            if (path?.endsWith(NavigationScope.NODE_PATH_SEPARATOR)) {
                if (path.size() > 1) {
                    path = path[0..-2]
                } else {
                    path = ''
                }
            }
            return nodesById[path]
        } else {
            return null
        }
    }
    
    List<NavigationScope> nodesForPath(String path) {
        if (log.debugEnabled) {
            log.debug "Getting nodesForPath [$path]"
        }
        def node = nodeForId(path)
        def nodes = []
        // We don't return the root scope
        while (node && node.parent) {
            nodes << node
            node = node.parent
        }
        nodes = nodes.reverse()
        if (log.debugEnabled) {
            log.debug "Found nodesForPath [$path]: ${nodes.name}"
        }
        return nodes
    }
    
    NavigationScope getPrimaryScopeFor(path) {
        (path ? getFirstNodeOfPath(path) : getFirstActiveNode(request))?.scope
    }
    
    NavigationScope nodeForControllerAction(String controller, String action) {
        nodesByControllerAction["$controller:$action".toString()]
    }
    
    void reloadAll() {
        log.info "Reloading navigation structure"
        clearScopes()
        clearCaches()
        
        loadDSL()
        loadControllers()
        
        updateSortOrder()
        updateCaches()
    }

    void reload(Class navigationClass) {
        log.info "Reloading navigation structure due to change in ${navigationClass}"

        // Can't work out how/if we can optimize this at the moment due to overrides etc
        reloadAll()
    }
    
    void clearCaches() {
        if (log.debugEnabled) {
            log.debug "Clearing navigation caches"
        }
        nodesByControllerAction = [:]
        nodesById = [:]
    }
    
    void clearScopes() {
        if (log.debugEnabled) {
            log.debug "Clearing navigation scopes"
        }
        rootScopes = [:]
    }
    
    void updateSortOrder() {
        for (scope in rootScopes.values()) { 
            scope.finalizeItems()
        }
    }

    void updateCaches() {
        if (log.debugEnabled) {
            log.debug "Updating navigation caches for root scopes: ${rootScopes.keySet()}"
        }
        for (scope in rootScopes.values()) { 
            for (node in scope.children) {
                updateCachesForItem(node)
            }
        }
    }

    void updateCachesForItem(NavigationItem node) {
        nodesById[node.id] = node
        if (node.linkArgs.controller) {
            assert node.linkArgs.action // We must also have pre-populated with an action
            nodesByControllerAction["${node.linkArgs.controller}:${node.linkArgs.action}".toString()] = node
            // Handle alias
            if (node.actionAliases) {
                for (action in node.actionAliases) {
                    nodesByControllerAction["${node.linkArgs.controller}:${action}".toString()] = node
                }
            }
        }
        for (child in node.children) {
            updateCachesForItem(child)
        }
    }
    
    void loadDSL(Class dslClass) {
        if (log.debugEnabled) {
            log.debug "Loading navigation DSL from calss ${dslClass}"
        }
        def dslInstance = dslClass.newInstance()
        dslInstance.run()
        def dsl = dslInstance.binding.getVariable('navigation')
        if (dsl) {
            registerNavigation(dsl)
        } else {
            log.warn "Tried to load navigation data from artefact [${artefact.clazz}] but no 'navigation' value was found in the script"
        }
    }

    void loadDSL() {
        if (log.debugEnabled) {
            log.debug "Loading navigation artefacts..."
        }
        
        for (artefact in grailsApplication.navigationClasses) {
            if (log.debugEnabled) {
                log.debug "Loading navigation artefact [${artefact.clazz}] (class instance hash: ${System.identityHashCode(artefact.clazz)})"
            }
            loadDSL(artefact.clazz)
        }
    }

    void registerNavigation(Closure dsl) {
        List<DSLCommand> commands = new DSLEvaluator().evaluate(dsl, grailsApplication)
        String definingPlugin = PluginUtils.getNameOfDefiningPlugin(grailsApplication.mainContext, dsl)
        parseDSL(commands, null, definingPlugin)
        updateCaches()
    }
    
    boolean hasNonControllerLinkArgs(Map args) {
        args.containsKey('uri') || 
        args.containsKey('url') || 
        args.containsKey('view') || 
        args.containsKey('mapping')
    }
    
    void realizeLinkArguments(String itemName, Map linkArgs, NavigationScope parent) {
        boolean isControllerLinking = !hasNonControllerLinkArgs(linkArgs)
        boolean itemNameUsed = false
        
        // Inherit controller from parent
        if (isControllerLinking && !linkArgs.controller) {
            // inherit controller
            if (parent instanceof NavigationItem) {
                linkArgs.controller = parent.linkArgs.controller
            } else {
                // item name becomes controller name
                linkArgs.controller = itemName
                itemNameUsed = true
            }
        }  
        
        if (isControllerLinking && !linkArgs.action) {
            if (!itemNameUsed) {
                linkArgs.action = itemName // Use node name as action if we have controller
                itemNameUsed = true
            } else {
                linkArgs.action = getDefaultControllerAction(linkArgs.controller)
            }
        }
        
        // Workaround lack of "view" support in g:link
        if (!isControllerLinking && linkArgs.view) {
            linkArgs.uri = '/'+linkArgs.remove('view')
        }
    }
    
    NavigationItem addItemFromArgs(String name, Map arguments, NavigationScope parent, String definingPlugin) {
        def actionList

        // See if we have a list of actions to register - first is parent, subsequent are children
        if (arguments.action) {
            if (arguments.action instanceof List) {
                actionList = arguments.action
            } else {
                actionList = arguments.action.split(',')*.trim()
            }
        }
        
        def childActionList
        if (actionList) {
            childActionList = actionList
            arguments.action = actionList[0]
        }

        // Now we know we have just a single action at most, resolve args
        def linkArgs = [:]
        for (p in LINK_TAG_ATTRIBUTES) {
            if (arguments.containsKey(p)) {
                linkArgs[p] = arguments[p]
            }
        }
        
        realizeLinkArguments(name, linkArgs, parent)

        // @todo In future, cache the generated URL for the given linkArgs, using linkGenerator (? reload problems)
        def nodeArgs = [
            name:name,
            order:arguments.order,
            data:arguments.data,
            actionAliases:arguments.actionAliases,
            titleDefault:arguments.titleText ?: GrailsNameUtils.getNaturalName(name),
            linkArgs:linkArgs,
            titleMessageCode:arguments.title,
            visible:arguments.visible,
            enabled:arguments.enabled,
        ]
        def item = new NavigationItem(nodeArgs)
        if (log.debugEnabled) {
            log.debug "Adding item ${item.name} with parent ${parent?.id} with args $nodeArgs"
        }
        
        // Create the node
        def primaryNode = addItem(parent, item)
        
        // Now if it has any auto-children do those
        if (childActionList?.size() > 1) {
            int n = 0
            for (action in childActionList) {
                def actionArgs = [:]
                actionArgs.action = action
                actionArgs.parent = primaryNode
                actionArgs.name = action
                actionArgs.titleDefault = GrailsNameUtils.getNaturalName(action)
                actionArgs.controller = primaryNode.linkArgs.controller
                actionArgs.order = action == primaryNode.linkArgs.action ? Integer.MIN_VALUE : n++

                declareControllerNode(actionArgs)
            }
        }
        return primaryNode
    }

    /**
     * Receives a graph of DSL commend objects and creates the necessary scopes and items
     *
     * Handles the "magic" inheritance of values and conventions etc.
     */
    protected void parseDSL(List<DSLCommand> commands, NavigationScope parent, String definingPlugin) {
        if (log.debugEnabled) {
            log.debug "Parsing navigation DSL commands: ${commands} in parent ${parent?.name}, defined by plugin ${definingPlugin}"
        }
        for (c in commands) {
            switch (c) {
                case DSLNamedArgsBlockCommand:
                case DSLBlockCommand:
                    if (!parent && (c.name == 'overrides')) {
                        throw new IllegalArgumentException( "Sorry but the 'overrides' block is not yet implemented")
                    } else {
                        if (c.name == 'overrides') {
                            throw new IllegalArgumentException( "Sorry but the 'overrides' block is not valid except at the scope level")
                        }
                        // Are we creating a top-level scope?
                        def newParent
                        if (!parent) {
                            if (c.arguments && 
                                !((c.arguments.size() == 1) && c.arguments.containsKey('global')) ) {
                                throw new IllegalArgumentException( "You cannot define a root scope and pass it arguments. Arguments are for nodes only")
                            }
                            def newScopeName = definingPlugin && !c.arguments.global ? "plugin.$definingPlugin.${c.name}" : c.name
                            newParent = getOrCreateScope(newScopeName)
                        } else {
                            // Add this parent node, before the children

                            def args
                            
                            // If there are no args, name = controller name
                            if (c instanceof DSLBlockCommand) {
                                args = [controller:c.name]
                            } else {
                                args = c.arguments
                            }
                            
                            // We do not support '*' publicly
                            if (args.action == '*') {
                                args.remove('action')
                            }
                            
                            newParent = addItemFromArgs(c.name, args, parent, definingPlugin)
                        }
                        // Now add any children
                        parseDSL(c.children, newParent, definingPlugin)
                    }
                    break;
                case DSLSetValueCommand:
                    throw new IllegalArgumentException( "We don't support property setting or simple method calls in this DSL. Your DSL tried to set [${c.name}] to ${c.value}")
                case DSLCallCommand:
                    // Create an implicit action/controller node
                    if (!c.arguments) {
                        def args = [:]
                        if (!parent) {
                            // name is controller, use default action
                            args.controller = c.name
                        } else {
                            // inherit controller if parent is a node
                            if (parent instanceof NavigationItem) {
                                args.controller = parent.linkArgs.controller
                                // parent may not link with a controller, in which case we use node name as controller
                                if (!args.controller) {
                                    args.controller = c.name
                                } else {
                                    // but if parent had a controller we use action
                                    args.action = c.name
                                }
                            }
                        }
                        addItemFromArgs(c.name, args, parent, definingPlugin)
                    } else {
                        throw new IllegalArgumentException( "We don't support method calls with positional arguments in this DSL. Your DSL tried to call [${c.name}] with ${c.arguments}")
                    }
                    break;
                case DSLNamedArgsCallCommand:
                    if (!parent) {
                        throw new IllegalArgumentException( "We don't support named argument method calls unless you are in a scope. Your DSL tried to call [$c.name]($c.arguments)")
                    } 
                    
                    addItemFromArgs(c.name, c.arguments, parent, definingPlugin)
                    break;
                default:
                    throw new IllegalArgumentException( "We don't support command type ${c.getClass()}")
            }
        }
    }

    /**
     * Load the available controller actions and if no declaration exists already, auto-register them
     * in the navigation system.
     * 
     * Supports "navigationScope" static property convention which can contain the scope name to put the controller into
     * Note however that this will be namespaced to plugin.<pluginName>/<navigationScopeValue> if the artefact comes from
     * a plugin.
     *
     * Controllers are added with the default action as a new scope, with all actions as sub items
     *
     * @todo Does not detect actions under Grails 1.3.x yet.
     */
    void loadControllers() {
        def rootScopesNeeded = []
        
        for (art in grailsApplication.controllerClasses) {
            def controllerClass = art.clazz
            def controllerName = GrailsNameUtils.getPropertyName(art.name)

            // Check if we already have an explicit mapping for anything in this controller
            def controllerPrefix = controllerName+':'
            if (nodesByControllerAction.keySet().find { k -> k.startsWith(controllerPrefix) }) {
                log.debug "Skipping auto-register of controller $controllerName, manual declarations exist"
                continue
            }
            
            def definingPluginName = PluginUtils.getNameOfDefiningPlugin(grailsApplication.mainContext, controllerClass)
            
            log.debug "Controller for navigation is defined in plugin [${definingPluginName}]"
            def scope = controllerClass.metaClass.hasProperty(null, 'navigationScope') ? controllerClass.navigationScope : null
            
            if (!scope) {
                switch (definingPluginName) {
                    case 'platformCore':
                        scope = "dev"
                        break
                    case null:
                        scope = "app"
                        break
                    default: 
                        scope = "plugin.$definingPluginName"
                }
            } else {
                // If convention supplied, namespace it if controller is in a plugin
                if (definingPluginName) {
                    scope = makePath(["plugin.$definingPluginName",scope])
                }
            }
            
            log.debug "Scope for actions of controller $controllerName is ${scope}"

            registerControllerActions(controllerName, controllerClass, scope)
        }
    }

    /**
     * Auto-register controller actions, returning the parent (default/primary) item
     */
    NavigationItem registerControllerActions(String controllerName, Class controllerClass, String scope) {
        if (log.debugEnabled) {
            log.debug "Registering controller actions for [$controllerName] (class: ${controllerClass ?: 'unknown'}) in scope ${scope}"
        }
        if (!controllerClass) {
            def artef = grailsConventions.findArtefactBySimpleClassName(
                    GrailsNameUtils.getClassNameRepresentation(controllerName)+'Controller', 'Controller')
            controllerClass = artef?.clazz            
            if (!controllerClass) {
                log.error "Cannot register navigation for controller [$controllerName], no controller artefact exists with this name"
                return
            }
        }
        
        List actionsToAdd = []
        String defaultAction = getDefaultControllerAction(controllerClass.name)

        Class grails2ActionAnnotation
        try {
            grails2ActionAnnotation = grailsApplication.classLoader.loadClass('grails.web.Action')
        } catch (Throwable t) {
        }

        def actionNames = grailsConventions.discoverCodeBlockConventions(controllerClass, grails2ActionAnnotation)
        if (log.debugEnabled) {
            log.debug "Found auto actions $actionNames for controller $controllerName"
        }
        // @todo ONLY ADD THOSE WITH "GET" ALLOWED METHOD
        actionsToAdd.addAll(actionNames.sort())
        
        log.debug "Registering actions $actionsToAdd for controller $controllerName"

        def controllerArgs = [:]
        controllerArgs.parent = getOrCreateScope(scope)
        controllerArgs.name = controllerName
        controllerArgs.titleText = GrailsNameUtils.getNaturalName(controllerName)
        controllerArgs.controller = controllerName
        controllerArgs.action = defaultAction
        def controllerNode = declareControllerNode(controllerArgs)

        def n = 0
        
        for (action in actionsToAdd) {
            declareControllerNode(
                parent:controllerNode,
                name: action,
                titleText: GrailsNameUtils.getNaturalName(action),
                controller:controllerName, 
                order: action == defaultAction ? Integer.MIN_VALUE : n++,
                action:action)
        }
        
        return controllerNode
    }
    
    NavigationItem declareControllerNode(Map args) {
        if (log.debugEnabled) {
            log.debug "Declaring controller nav item: $args"
        }
        def path = args.path
        
        def nodeArgs = args.clone()
        nodeArgs.titleMessageCode = nodeArgs.remove('title')
        nodeArgs.titleDefault = nodeArgs.remove('titleText')
        nodeArgs.linkArgs = [controller:args.controller,action:args.action]
        for (a in LINK_TAG_ATTRIBUTES) {
            nodeArgs.remove(a)
        }
        
        NavigationItem node = new NavigationItem(nodeArgs)
        if (log.debugEnabled) {
            log.debug "Adding convention item ${node.id} to parent ${args.parent?.id}"
        }
        addItem(args.parent, node)
    }

    NavigationItem addItem(NavigationScope parent, NavigationItem item) {
        parent.add(item)
        if (nodesById.containsKey(item.id)) {
            def itemWas = item.id
            parent.remove(item)
            throw new IllegalArgumentException("Cannot add navigation node with id [${itemWas}] because an item with the same id already exists")
        }
        updateCachesForItem(item)
        return item
    }
    
    String makePath(List<String> elements, String definingPluginName = null) {
        def p = elements.join(NavigationScope.NODE_PATH_SEPARATOR)
        return definingPluginName ? "plugin.${definingPluginName}." + p : p
    }
    
    def splitPath(String path) {
        path ? path.split(NavigationScope.NODE_PATH_SEPARATOR) : Collections.EMPTY_LIST
    }
    
    NavigationScope getOrCreateScope(String name) {
        def scope = rootScopes[name]
        if (!scope) {
            if (log.debugEnabled) {
                log.debug "Creating scope [$name]"
            }
            scope = new NavigationScope(name:name)
            rootScopes[name] = scope
            nodesById[scope.id] = scope
        }
        return scope
    }
}