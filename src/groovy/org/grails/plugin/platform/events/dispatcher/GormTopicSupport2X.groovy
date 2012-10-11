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
package org.grails.plugin.platform.events.dispatcher;

//import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent;


/**
 * @author Stephane Maldini <smaldini@vmware.com>
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
        if(evt != null & returnValue != null){
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
                return entry.value
            }
        }
        
        null
    }

    Object extractEntity(Object source) {
        //workaround for document db and hibernate gorm events
        source.entityObject ?: source?.entityAccess?.entity
    }
}
