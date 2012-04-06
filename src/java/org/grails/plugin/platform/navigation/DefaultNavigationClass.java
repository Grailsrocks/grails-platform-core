package org.grails.plugin.platform.navigation;

import org.codehaus.groovy.grails.commons.*;

public class DefaultNavigationClass extends AbstractGrailsClass implements NavigationClass {
    public DefaultNavigationClass(Class clazz) {
        super(clazz, NavigationArtefactHandler.SUFFIX);
    }
}