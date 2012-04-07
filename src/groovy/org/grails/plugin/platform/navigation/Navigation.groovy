package org.grails.plugin.platform.navigation

import org.grails.plugin.platform.util.PluginUtils
import org.grails.plugin.platform.conventions.*
import grails.util.GrailsNameUtils

import org.slf4j.LoggerFactory
/**
 * Bean that encapsulates the navigation structure of the entire application
 */
class Navigation {
    
    static LINK_TAG_ATTRIBUTES = ['controller', 'action', 'mapping', 'uri', 'url', 'view']
    
    final log = LoggerFactory.getLogger(Navigation)

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
        nodesById[path]
    }
    
    List<NavigationScope> nodesForPath(String path) {
        if (log.debugEnabled) {
            log.debug "Getting nodesForPath [$path]"
        }
        def node = nodeForId(path)
        def nodes = []
        while (node && !(node instanceof NavigationScope)) {
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
        nodesByControllerAction["$controller:$action"]
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
        // Can't work out how/if we can optimize this at the moment due to overrides etc
        reloadAll()
    }
    
    void clearCaches() {
        nodesByControllerAction = [:]
        nodesById = [:]
    }
    
    void clearScopes() {
        rootScopes = [:]
    }
    
    void updateSortOrder() {
        for (scope in rootScopes.values()) { 
            scope.finalizeItems()
        }
    }

    void updateCaches() {
        for (scope in rootScopes.values()) { 
            for (node in scope.children) {
                updateCachesForItem(node)
            }
        }
    }

    void updateCachesForItem(NavigationItem node) {
        nodesById[node.id] = node
        if (node.linkArgs.controller) {
            nodesByControllerAction["${node.linkArgs.controller}:${node.linkArgs.action}"] = node
        }
        for (child in node.children) {
            updateCachesForItem(child)
        }
    }
    
    void loadDSL(Class dslClass) {
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
                log.debug "Loading navigation artefact [${artefact.clazz}]"
            }
            loadDSL(artefact.clazz)
        }
    }

    void registerNavigation(Closure dsl) {
        clearCaches() // this may hose other stuff
        List<DSLCommand> commands = new DSLEvaluator().evaluate(dsl)
        String definingPlugin = PluginUtils.getNameOfDefiningPlugin(grailsApplication.mainContext, dsl.owner.getClass())
        parseDSL(commands, null, definingPlugin)
        updateCaches()
    }
    
    NavigationItem addItemFromArgs(DSLNamedArgsCallCommand c, NavigationScope parent, String definingPlugin) {
        def linkArgs = [:]
        for (p in LINK_TAG_ATTRIBUTES) {
            if (c.arguments.containsKey(p)) {
                linkArgs[p] = c.arguments[p]
            }
        }
        
        // Inherit controller from parent
        if (!linkArgs.controller && linkArgs.action) {
            linkArgs.controller = parent.linkArgs.controller
        } 
        
        // Workaround lack of "view" support in g:link
        if (linkArgs.view) {
            linkArgs.uri = '/'+linkArgs.remove('view')
        }
        
        def nodeArgs = [
            name:c.name,
            order:c.arguments.order,
            data:c.arguments.data,
            titleDefault:c.arguments.titleText ?: GrailsNameUtils.getNaturalName(c.name),
            linkArgs:linkArgs,
            titleMessageCode:c.arguments.title,
            visible:c.arguments.visible,
            enabled:c.arguments.enabled,
        ]
        def item = new NavigationItem(nodeArgs)
        if (log.debugEnabled) {
            log.debug "Adding item ${item.name} with parent ${parent?.id} with link args ${item.linkArgs}"
        }
        addItem(parent, item)
    }

    void parseDSL(List<DSLCommand> commands, NavigationScope parent, String definingPlugin) {
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
                            if (c.arguments) {
                                throw new IllegalArgumentException( "You cannot define a root scope and pass it arguments. Arguments are for nodes only")
                            }
                            newParent = getOrCreateScope(c.name)
                        } else {
                            // Add this parent node, before the children
                            if (c instanceof DSLBlockCommand) {
                                throw new IllegalArgumentException( "You cannot define a new navigation block that does not have any link arguments")
                            }
                            newParent = addItemFromArgs(c, parent, definingPlugin)
                        }
                        // Now add any children
                        parseDSL(c.children, newParent, definingPlugin)
                    }
                    break;
                case DSLSetValueCommand:
                    throw new IllegalArgumentException( "We don't support property setting or simple method calls in this DSL. Your DSL tried to set [${c.name}] to ${c.value}")
                case DSLCallCommand:
                    throw new IllegalArgumentException( "We don't support property setting or simple method calls in this DSL. Your DSL tried to call [${c.name}] with args ${c.arguments}")
                case DSLNamedArgsCallCommand:
                    if (!parent) {
                        throw new IllegalArgumentException( "We don't support named argument method calls unless you are in a scope. Your DSL tried to call [$c.name]($c.arguments)")
                    } 
                    
                    addItemFromArgs(c, parent, definingPlugin)
                    break;
                default:
                    throw new IllegalArgumentException( "We don't support command type ${c.getClass()}")
            }
        }
    }

    void loadControllers() {
        def rootScopesNeeded = []
        
        Class grails2ActionAnnotation
        try {
            grails2ActionAnnotation = grailsApplication.classLoader.loadClass('grails.web.Action')
        } catch (Throwable t) {
        }

        for (art in grailsApplication.controllerClasses) {
            def controllerClass = art.clazz
            def controllerName = GrailsNameUtils.getPropertyName(art.name)
            def actionNames = grailsConventions.discoverCodeBlockConventions(controllerClass, grails2ActionAnnotation)
            
            log.debug "Found actions $actionNames for controller $controllerName"

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
            }
            
            log.debug "Scope for actions of controller $controllerName is ${scope}"

            def defaultAction = getDefaultControllerAction(controllerClass.name)
            
            def controllerNode = declareControllerNode(
                parent: getOrCreateScope(scope),
                name: controllerName,
                titleDefault: GrailsNameUtils.getNaturalName(controllerName),
                controller: controllerName, 
                action: defaultAction)

            actionNames -= defaultAction
            def n = 0
            for (action in actionNames) {
                declareControllerNode(
                    parent:controllerNode,
                    name: action,
                    titleDefault: GrailsNameUtils.getNaturalName(action),
                    controller:controllerName, 
                    order:n++,
                    action:action)
            }
        }
    }
    
    NavigationItem declareControllerNode(Map args) {
        def path = args.path
        
        def nodeArgs = [
            name:args.name,
            titleDefault:args.titleDefault,
            linkArgs:[controller:args.controller,action:args.action]
        ]
        NavigationItem node = new NavigationItem(nodeArgs)
        if (log.debugEnabled) {
            log.debug "Adding node ${node.id} to parent ${args.parent?.id}"
        }
        addItem(args.parent, node)
    }

    NavigationItem addItem(NavigationScope parent, NavigationItem item) {
        parent.add(item)
        if (nodesById.containsKey(item.id)) {
            parent.remove(item)
            throw new IllegalArgumentException("Cannot add navigation node with id [${item.id}] because an item with the same id already exists")
        }
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