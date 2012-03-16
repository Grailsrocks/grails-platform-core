/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (stephane.maldini@gmail.com)
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
package org.grails.plugin.platform.events.dispatcher;

//import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent;
import org.grails.plugin.platform.events.registry.EventsRegistry;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 03/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
class GormTopicSupport2X implements GormTopicSupport {

    Map<String, String> translateTable

    void processCancel(Object evt, returnValue){
        if(returnValue != null){
            if(returnValue.getClass().equals(ArrayList)){
                for(Object val in returnValue){
                    if(!val){
                        evt.cancel()
                        break
                    }
                }
            }else if(!((Boolean)returnValue)){
                evt.cancel()
            }
        }
    }

    String convertTopic(Object evt) {
        if(!translateTable)
            return null
        
        for(Map.Entry<String,String> entry in translateTable){
            if(entry.getKey().equalsIgnoreCase(evt.getClass().simpleName)){
                return EventsRegistry.GRAILS_TOPIC_PREFIX+entry.value
            }
        }
        
        return EventsRegistry.GRAILS_TOPIC_PREFIX+evt.getClass().simpleName
    }
}
