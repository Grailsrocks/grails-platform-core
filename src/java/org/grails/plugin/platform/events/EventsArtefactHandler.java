package org.grails.plugin.platform.events;

import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;

public class EventsArtefactHandler extends ArtefactHandlerAdapter {

    static public final String TYPE = "Events";
    static public final String SUFFIX = "Events";

    public EventsArtefactHandler() {
        super(TYPE, EventsClass.class, DefaultEventsClass.class, SUFFIX, true);
    }

    @Override
    public String getPluginName() {
        return "platformCore";
    }
}