package org.grails.plugin.platform.navigation

/**
 * Immutable encapsulation of a node in the navigation structure
 * Instances of this are shared globally and available to requests so 
 * this must be immutable and threadsafe
 */
class NavigationNode {
    private NavigationScope scope
        
    private String id
    private int order
    
    private String activationPath
    private String titleDefault
    
    private Map linkArgs
    private String titleMessageCode
    
    private boolean visible = true
    private Closure visibleClosure
    
    private boolean enabled = true
    private Closure enabledClosure

    NavigationNode(Map args) {
        this.id = args.id
        this.scope = args.scope
        this.order = args.order ?: 0
        this.activationPath = args.activationPath
        this.linkArgs = args.linkArgs
        this.titleMessageCode = args.titleMessageCode
        this.titleDefault = args.titleDefault
        if (!titleMessageCode) {
            titleMessageCode = "nav.${this.id}"
        }
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
    
    NavigationScope getScope() {
        this.scope
    }

    String getId() {
        this.id
    }
    
    int getOrder() {
        this.order
    }

    boolean getLeafNode() {
        this.leafNode
    }

    String getActivationPath() {
        this.activationPath
    }
    
    String getTitleMessageCode() {
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