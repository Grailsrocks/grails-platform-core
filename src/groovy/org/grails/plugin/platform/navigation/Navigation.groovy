package org.grails.plugin.platform.navigation

import org.grails.plugin.platform.util.PluginUtils
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
            setActivePath(request, makeActivationPath([controller, action]))
        }
    }
    
    String getActivePath(request) {
        request['plugin.platformCore.navigation.activePath']
    }
    
    String getActiveScope(request) {
        def elements = splitActivationPath(getActivePath(request))
        return elements ? elements[0] : null
    }
    
    NavigationScope scopeByName(String name) {
        return rootScopes[name]
    }

    NavigationNode nodeForActivationPath(String path) {
        nodesByActivationPath[path]
    }
    
    void reloadAll() {
        log.info "Reloading navigation structure"
        clearScopes()

        loadControllers()
        loadDSL()
        
        updateCaches()
    }

    void clearScopes() {
        rootScopes = [:]
    }
    
    void updateCaches() {
        nodesByActivationPath = [:]
        for (scope in rootScopes.values()) { 
            for (node in scope.children) {
                nodesByActivationPath[node.activationPath] = node
            }
        }
    }
    
    void loadDSL() {
        // get all the XXXNavigation artefacts and evaluate, with app's last
    }
    
    void loadControllers() {
        def rootScopesNeeded = []
        
        for (art in grailsApplication.controllerClasses) {
            def controllerClass = art.clazz
            def controllerName = GrailsNameUtils.getPropertyName(art.name)
            Class grails2ActionAnnotation = grailsApplication.classLoader.loadClass('grails.web.Action')
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