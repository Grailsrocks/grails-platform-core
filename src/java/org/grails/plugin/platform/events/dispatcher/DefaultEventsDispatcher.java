package org.grails.plugin.platform.events.dispatcher;

import groovy.lang.Closure;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.grails.plugin.platform.events.EventObject;
import org.grails.plugin.platform.events.dispatcher.builder.EventsMethodBuilder;
import org.grails.plugin.platform.events.dispatcher.builder.MappedEventMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 16/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class DefaultEventsDispatcher implements EventsDispatcher {

    public final static String PROPERTY_NAME = "events";

    public Map<String, Map<String, MappedEventMethod>> getMappedMethodsByClass() {
        return mappedMethodsByClass;
    }

    private Map<String, Map<String, MappedEventMethod>> mappedMethodsByClass =
            new HashMap<String, Map<String, MappedEventMethod>>();

    public boolean when(EventObject event, String className, String methodName) {
        return false;
    }

    public void scanClassForMappings(Class<?> clazz) {
        EventsMethodBuilder delegate = new EventsMethodBuilder(clazz);

        Closure c = (Closure) GrailsClassUtils.getStaticPropertyValue(clazz, PROPERTY_NAME);

        if (c != null) {
            c = (Closure) c.clone();
            c.setResolveStrategy(Closure.DELEGATE_ONLY);
            c.setDelegate(delegate);
            c.call();

            mappedMethodsByClass.put(clazz.getName(), delegate.getMappedProperties());
        }
    }

    public void scanClassesForMappings(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            scanClassForMappings(clazz);
        }
    }
}
