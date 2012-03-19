package org.grails.plugin.platform.navigation

/**
 * Base class for each node in the graph, used to represent the root nodes
 * which cannot themselves link to anything
 */
class NavigationScope {
    private List<NavigationNode> children
        
    private String name

    NavigationScope(Map args) {
        this.children = args.children == null ? [] : args.children
        this.name = args.name
    }
    
    List<NavigationNode> getChildren() {
        this.children
    }
    
    void addNode(NavigationNode node) {
        node.scope = this
        this.children << node
    }
    
    void lockChildren() {
        this.children = Collections.asImmutableList(this.children)
    }

    String getName() {
        this.name
    }
}