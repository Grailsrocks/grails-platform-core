package org.grails.plugin.platform.navigation

/**
 * This is the public documented API of the grailsNavigation bean.
 * Call anything else on the impl and you may break.
 */
interface Navigation {

    void setActivePath(request, String path)

    String getActivePath(request)

    void registerNavigation(Closure dsl) 
    
    NavigationItem addItem(NavigationScope parent, NavigationItem item)
}


