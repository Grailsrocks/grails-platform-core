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
package org.grails.plugin.platform.events;

import org.grails.plugin.platform.events.dispatcher.GormTopicSupport;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Stephane Maldini <smaldini@vmware.com>
 * @version 1.0
 * @file
 * @date 30/12/11
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class EventMessage<D> implements Serializable {
    final private String namespace;
    final private String event;
    final private D data;
    final private Boolean gormSession;
    final private Map<String, String> headers;

    public EventMessage(String event, D data) {
        this(event, data, null);
    }

    public EventMessage(String event, D data, String namespace) {
        this(event, data, namespace, namespace == null || !namespace.equals(GormTopicSupport.GORM_SOURCE));
    }

    public EventMessage(String event, D data, String namespace, boolean gormSession) {
        this(event, data, namespace, gormSession, null);
    }

    public EventMessage(String event, D data, String namespace, boolean gormSession, Map<String,String> headers) {
        this.event = event;
        this.data = data;
        this.namespace = namespace;
        this.gormSession = gormSession;
        this.headers = headers;
    }

    public D getData() {
        return data;
    }


    public String getNamespace() {
        return namespace;
    }


    public String getEvent() {
        return event;
    }


    public boolean isGormSession() {
        return gormSession;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
