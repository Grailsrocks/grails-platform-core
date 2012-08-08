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

import org.grails.plugin.platform.util.ViewCallbackDelegate

/**
 * Immutable encapsulation of an item in the navigation structure
 * Instances of this are shared globally and available to requests so 
 * this must be immutable and threadsafe
 */
class NavigationItem extends NavigationScope {
    private Integer order
    
    private String titleDefault
    
    private Map linkArgs
    private List actionAliases
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
        this.linkArgs = args.linkArgs.asImmutable()
        this.actionAliases = args.actionAliases
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
        getRootScope().name == scopeName
    }
    
    boolean inScope(NavigationScope scope) {
        getRootScope().name == scope.name
    }
    
    /**
     * Get any application-supplied data that was declared for this item
     * Used for info like icon-names, alt text and so on - custom rendering usage
     */
    Map getData() {
        this.data
    }

    List getActionAliases() {
        this.actionAliases
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
            return invokeCallback(this.visibleClosure, context)
        } else {
            return this.visible
        }
    }
    
    boolean isEnabled(context) {
        if (this.enabledClosure != null) {
            return invokeCallback(this.enabledClosure, context)
        } else {
            return this.enabled
        }
    }
    
    protected invokeCallback(Closure c, context) {
        def delegate = new ViewCallbackDelegate(context.grailsApplication, context.pageScope, context)
        
        Closure cloneOfClosure = c.clone()
        cloneOfClosure.delegate = delegate
        cloneOfClosure.resolveStrategy = Closure.DELEGATE_FIRST
        return cloneOfClosure()
    }
}