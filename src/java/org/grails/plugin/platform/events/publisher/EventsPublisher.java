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
package org.grails.plugin.platform.events.publisher;

import org.grails.plugin.platform.events.EventMessage;
import org.grails.plugin.platform.events.EventReply;

import java.util.Map;

/**
 * @author Stephane Maldini <smaldini@vmware.com>
 * @version 1.0
 * @file
 * @date 02/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public interface EventsPublisher {

    public static final String ON_REPLY = "onReply";
    public static final String ON_ERROR = "onError";
    public static final String FORK = "fork";
    public static final String GORM = "gormSession";
    public static final String NAMESPACE = "namespace";
    public static final String TIMEOUT = "timeout";
    public static final String HEADERS = "headers";

    public EventReply event(final EventMessage event);
    public EventReply eventAsync(final EventMessage event, Map<String, Object> params);
}
