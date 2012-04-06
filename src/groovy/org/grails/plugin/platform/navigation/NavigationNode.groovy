package org.grails.plugin.platform.navigation

/**
 * Immutable encapsulation of a node in the navigation structure
 * Instances of this are shared globally and available to requests so 
 * this must be immutable and threadsafe
 */
class NavigationNode {
    static NODE_PATH_SEPARATOR = '#'
    
    NavigationNode parent
    private List<NavigationItem> children
    private String name

    NavigationNode(Map args) {
        this.children = args.children == null ? [] : args.children
        this.name = args.name
        this.parent = args.parent
    }
    
    List<NavigationItem> getChildren() {
        this.children
    }
    
    String getId() {
        parent ? this.parent.id+NODE_PATH_SEPARATOR+this.name : this.name
    }
    
    /**
     * Called when all loading has been done, to sort all of the node lists
     */
    void finalizeItems() {
        this.children = this.children.sort { a, b -> a.order <=> b.order }
        for (n in children) {
            n.finalizeItems()
        }
    }

    NavigationItem add(NavigationItem node) {
        if (node.order == null) {
            int orderValue = children ? children[-1].order+1 : 0 
            node.order = orderValue
        }
        node.parent = this
        this.children << node
        return node
    }
    
    NavigationItem remove(NavigationItem node) {
        node.parent = null
        this.children.remove(node)
        return node
    }
    
    NavigationScope getScope() {
        if (!(parent instanceof NavigationScope)) {
            return parent.scope
        } else {
            return parent
        }
    }
    
    void lockChildren() {
        this.children = Collections.asImmutableList(this.children)
    }

    String getName() {
        this.name
    }

}