/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (smaldini@vmware.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugin.platform.navigation

/**
 * Immutable encapsulation of a node in the navigation structure
 * Instances of this are shared globally and available to requests so 
 * this must be immutable and threadsafe
 */
class NavigationScope {
    static NODE_PATH_SEPARATOR = '/'
    
    NavigationScope parent
    private List<NavigationScope> children
    private String name

    NavigationScope(Map args) {
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
    
    NavigationScope getRootScope() {
        if (parent) {
            return parent.rootScope
        } else {
            return this
        }
    }
    
    void lockChildren() {
        this.children = Collections.asImmutableList(this.children)
    }

    String getName() {
        this.name
    }

}