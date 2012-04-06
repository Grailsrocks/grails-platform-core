package org.grails.plugin.platform.navigation

/**
 * Immutable encapsulation of a node in the navigation structure
 * Instances of this are shared globally and available to requests so 
 * this must be immutable and threadsafe
 */
class NavigationItem extends NavigationNode {
    private int order
    
    private String titleDefault
    
    private Map linkArgs
    private String titleMessageCode
    
    private boolean visible = true
    private Closure visibleClosure
    
    private boolean enabled = true
    private Closure enabledClosure
    
    NavigationItem(Map args) {
        super(args)
        this.order = args.order ?: 0
        this.linkArgs = args.linkArgs
        this.titleMessageCode = args.titleMessageCode
        this.titleDefault = args.titleDefault
        if (args.visible == null) {
            args.visible = true
        }
        this.visible = args.visible instanceof Closure ? false : args.visible
        this.visibleClosure = args.visible instanceof Closure ? args.visible : null
        if (args.enabled == null) {
            args.enabled = true
        }
        this.enabled = args.enabled instanceof Closure ? false : args.enabled
        this.enabledClosure = args.enabled instanceof Closure ? args.enabled : null
    }

    boolean inScope(String scopeName) {
        getScope().name == scopeName
    }
    
    boolean inScope(NavigationScope scope) {
        getScope().name == scope.name
    }
    
    int getOrder() {
        this.order
    }

    boolean getLeafNode() {
        this.leafNode
    }

    String getTitleMessageCode() {
        if (!this.titleMessageCode) {
            def safeId = id.replaceAll(NODE_PATH_SEPARATOR, '.')
            titleMessageCode = "nav.${safeId}" // captures original id, so i18n continues to work even if moved in hierarchy
        }
        this.titleMessageCode
    }
    
    String getTitleDefault() {
        this.titleDefault
    }
    
    Closure getVisibleClosure() {
        visibleClosure
    }
    
    Closure getEnabledClosure() {
        enabledClosure
    }
    
    boolean isVisible(context) {
        if (this.visibleClosure != null) {
            return this.visibleClosure(context)
        } else {
            return this.visible
        }
    }
    
    boolean isEnabled(context) {
        if (this.enabledClosure != null) {
            return this.enabledClosure(context)
        } else {
            return this.enabled
        }
    }
}