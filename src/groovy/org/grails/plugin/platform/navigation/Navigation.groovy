package org.grails.plugin.platform.navigation

import org.grails.plugin.platform.util.PluginUtils
import org.grails.plugin.platform.conventions.*
import grails.util.GrailsNameUtils

import org.slf4j.LoggerFactory
/**
 * Bean that encapsulates the navigation structure of the entire application
 */
class Navigation {
    
    static ACTIVE_PATH_SEPARATOR = ':'
    
    final log = LoggerFactory.getLogger(Navigation)

    Map<String, NavigationScope> rootScopes
    Map<String, NavigationScope> nodesByActivationPath
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
    }
    
    void setActivePathFromRequest(request, controller, action) {
        if (log.debugEnabled) {
            log.debug "Setting navigation active path from current request controller/action [$controller] and [$action]"
        }
        
        if (controller) {
            if (!action) {
                def artef = grailsApplication.getArtefact('Controller', controller)
                if (artef) {
                    action = artef.defaultAction
                }
            }
            
            def path 
            // See if we can reverse map from controller/action to an activation path
            def node = nodeForControllerAction(controller, action)
            if (node) {
                path = node.activationPath
            }
            // If not, we build a default activation path from controller/action pair
            if (!path) {
                path = makeActivationPath([controller, action])
            }
            setActivePath(request, path)
        }
    }
    
    String getActivePath(request) {
        request['plugin.platformCore.navigation.activePath']
    }
    
    /**
     * Reverse-lookup the current active path to find out what the default scope
     * would be based on the node found for that activation path.
     * If multiple nodes have same path, only the last one will be found
     */
    String getDefaultScopeForActivePath(String path) {
        def node = nodeForActivationPath(path)
        def scope
        if (node) {
            scope = node.parent
        }
        return scope?.id
    }
    
    NavigationScope scopeByName(String name) {
        return rootScopes[name]
    }

    NavigationNode nodeForActivationPath(String path) {
        nodesByActivationPath[path]
    }
    
    NavigationNode nodeForControllerAction(String controller, String action) {
        nodesByControllerAction["$controller:$action"]
    }
    
    void reloadAll() {
        log.info "Reloading navigation structure"
        clearScopes()
        clearCaches()
        
        loadDSL()
        loadControllers()
        
        updateCaches()
    }

    void clearCaches() {
        nodesByControllerAction = [:]
        nodesByActivationPath = [:]
    }
    
    void clearScopes() {
        rootScopes = [:]
    }
    
    void updateCaches() {
        for (scope in rootScopes.values()) { 
            for (node in scope.children) {
                nodesByActivationPath[node.activationPath] = node
                if (node.linkArgs.controller) {
                    nodesByControllerAction["${node.linkArgs.controller}:${node.linkArgs.action}"] = node
                }
            }
        }
    }
    
    void loadDSL() {
        // get all the XXXNavigation artefacts and evaluate, with app's last
    }

    // @todo temporary dangerous method, remove this later when artefacts implemented
    void registerNavigation(Closure dsl) {
        clearCaches() // this may hose other stuff
        List<DSLCommand> commands = new DSLEvaluator().evaluate(dsl)
        parseDSL(commands, null)
        updateCaches()
    }
    
    void parseDSL(List<DSLCommand> commands, NavigationScope scope) {
        for (c in commands) {
            switch (c) {
                case DSLSetValueCommand:
                case DSLCallCommand:
                    throw new IllegalArgumentException( "We don't support property setting or simple method calls in this DSL. Your DSL tried to set [${c.name}] to ${c.value}")
                    break;
                case DSLNamedArgsCallCommand:
                    if (!scope) {
                        throw new IllegalArgumentException( "We don't support named argument method calls unless you are in a scope. Your DSL tried to call [$c.name]($c.arguments)")
                    } 
                    def linkArgs = [:]
                    for (p in ['controller', 'action', 'mapping', 'uri', 'url', 'view']) {
                        if (c.arguments.containsKey(p)) {
                            linkArgs[p] = c.arguments[p]
                        }
                    }
                    def nodeArgs = [
                        id:c.name,
                        titleDefault:c.arguments.titleText ?: GrailsNameUtils.getNaturalName(c.name),
                        activationPath:c.arguments.activationPath ?: makeActivationPath([c.name]),
                        linkArgs:linkArgs,
                        titleMessageCode:c.arguments.title,
                        visible:c.arguments.visible,
                        enabled:c.arguments.enabled
                    ]
                    def node = new NavigationNode(nodeArgs)
                    if (log.debugEnabled) {
                        log.debug "Adding node ${node.id} to scope ${scope.id} with link args ${node.linkArgs}"
                    }
                    scope.addChild(node)
                    break;
                case DSLBlockCommand:
                    if (log.debugEnabled) {
                        log.debug "DSL block [${c.name}]"
                    }
                    if (scope) {
                        throw new IllegalArgumentException( "You cannot nest scopes - declare scopes at the top level only")
                    }
                    if (c.name == 'overrides') {
                        // 
                    } else {
                        def newScope = getOrCreateScope(c.name)
                        if (newScope) {
                            parseDSL(c.children, newScope)
                        } else {
                            
                        }
                    }
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
            
            def definingPluginName = PluginUtils.getNameOfDefiningPlugin(grailsApplication.mainContext, controllerClass)
            
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
                        scope = 'plugin/'+definingPluginName
                }
            }
            
            log.debug "Scope for actions of controller $controllerName is ${scope}"
            for (a in actionNames) {
                // @todo ONLY do this if the controller/action has not already been mapped by already
                declareControllerNode(
                    scopeName:scope,
                    path:[controllerName], 
                    controller:controllerName, 
                    action:a)
            }
        }
    }
    
    void declareControllerNode(Map args) {
        def path = args.path
        def scope = getOrCreateScope(args.scopeName)
        
        for (pathElement in path) {
            def nodeArgs = [
                parent:scope,
                id:args.controller+'.'+args.action,
                titleDefault:GrailsNameUtils.getNaturalName(args.action),
                activationPath:makeActivationPath(args.path + [args.action]),
                linkArgs:[controller:args.controller,action:args.action]
            ]
            def node = new NavigationNode(nodeArgs)
            if (log.debugEnabled) {
                log.debug "Adding node ${node.activationPath} to scope ${scope.id}"
            }
            scope.addChild(node)
        }
    }
    
    String makeActivationPath(List<String> elements) {
        elements.join(ACTIVE_PATH_SEPARATOR)
    }
    
    def splitActivationPath(String path) {
        path?.split(ACTIVE_PATH_SEPARATOR)
    }
    
    NavigationScope getOrCreateScope(String name) {
        def scope = rootScopes[name]
        if (!scope) {
            if (log.debugEnabled) {
                log.debug "Creating root scope [$name]"
            }
            scope = new NavigationScope(id:name)
            rootScopes[name] = scope
        }
        return scope
    }
}