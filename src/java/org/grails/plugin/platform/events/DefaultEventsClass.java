package org.grails.plugin.platform.events;

import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
import org.grails.plugin.platform.navigation.NavigationClass;

public class DefaultEventsClass extends AbstractGrailsClass implements NavigationClass {
    public DefaultEventsClass(Class clazz) {
        super(clazz, EventsArtefactHandler.SUFFIX);
    }
}