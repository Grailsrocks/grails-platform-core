package org.grails.plugin.platform.navigation

/**
 * Immutable encapsulation of an item in the navigation structure
 * Instances of this are shared globally and available to requests so 
 * this must be immutable and threadsafe
 */
class NavigationItem extends NavigationScope {
    private Integer order
    
    private String titleDefault
    
    private Map linkArgs
    private String titleMessageCode
    
    private boolean visible = true
    private Closure visibleClosure
    
    private boolean enabled = true
    private Closure enabledClosure
    
    private Map data

    NavigationItem(Map args) {
        super(args)
        this.order = args.order
        this.data = args.data != null ? args.data : Collections.EMPTY_MAP
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
    
    /**
     * Get any application-supplied data that was declared for this item
     * Used for info like icon-names, alt text and so on - custom rendering usage
     */
    Map getData() {
        this.data
    }
    
    Integer getOrder() {
        this.order
    }

    void setOrder(Integer v) {
        this.order = v
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