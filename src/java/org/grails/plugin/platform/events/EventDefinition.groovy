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
package org.grails.plugin.platform.events

/**
 * @file
 * @author Stephane Maldini <smaldini@vmware.com>
 * @version 1.0
 * @date 14/05/12

 * @section DESCRIPTION
 *
 * [Does stuff]
 */

class EventDefinition implements Comparable<EventDefinition> {
    String topic
    String namespace

    Class filterClass = null;
    Closure filterClosure = null;
    boolean filterEventMessage = false;

    boolean requiresReply = false
    boolean disabled = false
    boolean secured = false
    String definingPlugin
    Map othersAttributes

    Boolean fork = true
    Closure onError = null
    Closure onReply = null
    Long timeout = null

    int score = 0

    int compareTo(EventDefinition t) {
        t.score <=> score
    }
}
