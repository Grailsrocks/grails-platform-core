package org.grails.plugin.platform.events.publisher;

import org.apache.log4j.Logger;
import org.grails.plugin.platform.events.EventMessage;
import org.grails.plugin.platform.events.EventReply;
import org.grails.plugin.platform.events.dispatcher.GormTopicSupport;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ReflectionUtils;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 29/05/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class GormBridgePublisher implements ApplicationListener {

    private GormTopicSupport gormTopicSupport;
    private EventsPublisher grailsEventsPublisher;

    private final static Logger log = Logger.getLogger(GormBridgePublisher.class);
    static final private String GORM_EVENT_PACKAGE = "org.grails.datastore.mapping.engine.event";

    public void setGrailsEventsPublisher(EventsPublisher grailsEventsPublisher) {
        this.grailsEventsPublisher = grailsEventsPublisher;
    }

    public void setGormTopicSupport(GormTopicSupport gormTopicSupport) {
        this.gormTopicSupport = gormTopicSupport;
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        //fixme horrible hack to support grails 1.3.x
        if (applicationEvent.getClass().getName().startsWith(GORM_EVENT_PACKAGE)) {
            String topic = gormTopicSupport.convertTopic(applicationEvent);
            log.debug("sending " + applicationEvent + " to topic " + topic);
            EventReply reply = grailsEventsPublisher.event(new EventMessage(topic,
                    ReflectionUtils.invokeMethod(
                            ReflectionUtils.findMethod(applicationEvent.getClass(), "getEntityObject"),
                            applicationEvent
                    ), GormTopicSupport.GORM_SOURCE, false));
            try {
                gormTopicSupport.processCancel(applicationEvent, reply.getValues());
            } catch (Exception e) {
                throw new RuntimeException(e);//shouldn't happen as its sync event
            }
        }
    }
}
