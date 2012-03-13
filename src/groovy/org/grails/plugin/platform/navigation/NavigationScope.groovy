package org.grails.plugin.platform.navigation

/**
 * Base class for each node in the graph, used to represent the root nodes
 * which cannot themselves link to anything
 */
class NavigationScope {
    private List<NavigationNode> children
        
    private String id

    NavigationScope(Map args) {
        this.children = args.children == null ? [] : args.children
        this.id = args.id
    }
    
    List<NavigationNode> getChildren() {
        this.children
    }
    
    void addChild(NavigationNode child) {
        this.children << child
    }
    
    void lockChildren() {
        this.children = Collections.asImmutableList(this.children)
    }

    String getId() {
        this.id
    }
}