package org.grails.plugin.platform.navigation;

import org.codehaus.groovy.grails.commons.*;
import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;

public class NavigationArtefactHandler extends ArtefactHandlerAdapter {

    static public final String TYPE = "Navigation";
    static public final String SUFFIX = "Navigation";
    
    public NavigationArtefactHandler() {
        super(TYPE, NavigationClass.class, DefaultNavigationClass.class, SUFFIX, true);
    }
    
    @Override
    public String getPluginName() {
        return "platformCore";
    }
}