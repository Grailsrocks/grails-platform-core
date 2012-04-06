package org.grails.plugin.platform.navigation

/**
 * Base class for each node in the graph, used to represent the root nodes
 * which cannot themselves link to anything
 */
class NavigationScope extends NavigationNode {
    NavigationScope(Map args) {
        super(args)
    }
}