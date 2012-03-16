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
package org.grails.plugin.platform.events.publisher;

import groovy.lang.Closure;
import org.grails.plugin.platform.events.EventObject;
import org.grails.plugin.platform.events.EventReply;

import java.util.concurrent.ExecutionException;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 02/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public interface EventsPublisher {

    public final static String GORM_EVENT_SOURCE = "gorm";

    public EventReply event(final EventObject event);
    public EventReply eventAsync(final EventObject event);
    public void eventAsync(final EventObject event, Closure onComplete);
    public Object[] waitFor(EventReply... replies) throws ExecutionException, InterruptedException;
}
