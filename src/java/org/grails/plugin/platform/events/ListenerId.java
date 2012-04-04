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
package org.grails.plugin.platform.events;

import groovy.lang.Closure;
import org.grails.plugin.platform.events.registry.EventsRegistry;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 09/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class ListenerId implements Serializable {
    private static final String CLOSURE_METHOD_NAME = "call";
    private static final String ID_CLASS_SEPARATOR = ":";
    private static final String ID_METHOD_SEPARATOR = "#";
    private static final String ID_HASHCODE_SEPARATOR = "@";

    private static final Pattern idRegex = Pattern.compile(
            "([^" + ID_CLASS_SEPARATOR + "]*)?" +
                    "(" + ID_CLASS_SEPARATOR + "([^" + ID_METHOD_SEPARATOR + "]*))?"
                    + "(" + ID_METHOD_SEPARATOR + "([^" + ID_HASHCODE_SEPARATOR + "]*))?"
                    + "(" + ID_HASHCODE_SEPARATOR + "(-?\\d*))?");

    private String className;
    private String methodName;
    private String hashCode;
    private String topic;

    public ListenerId(String topic) {
        this(topic, null, null, null);
    }

    public ListenerId(String topic, String className, String methodName, String hashCode) {
        this.className = className;
        this.methodName = methodName;
        this.hashCode = hashCode;
        if (topic != null && !topic.isEmpty()) {
            this.topic = topic.startsWith(EventsRegistry.GRAILS_TOPIC_PREFIX) ?
                    topic :
                    EventsRegistry.GRAILS_TOPIC_PREFIX + topic;
        }
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    //format : topic:package.Class#method@hashCode
    public String toString() {
        return toStringWithoutHash()
                + (hashCode != null ? ID_HASHCODE_SEPARATOR + hashCode : "");
    }

    // format : topic:package.Class#method
    public String toStringWithoutHash() {
            return (topic != null ? topic : "") + (className != null ? ID_CLASS_SEPARATOR + className : "")
                    + (methodName != null ? ID_METHOD_SEPARATOR + methodName : "");
        }

    static public ListenerId build(String topic, Object target, Method callback) {
        return new ListenerId(topic, target.getClass().getName(), callback.getName(), Integer.toString(target.hashCode()));
    }

    static public ListenerId build(String topic, Closure target) {
        return new ListenerId(topic, target.getClass().getName(), CLOSURE_METHOD_NAME, Integer.toString(target.hashCode()));
    }

    static public ListenerId parse(String id) {
        Matcher parsed = idRegex.matcher(id);
        if (parsed.matches())
            return new ListenerId(parsed.group(1), parsed.group(3), parsed.group(5), parsed.group(7));
        else
            return null;
    }

    public boolean matches(ListenerId listener) {
        Boolean result = null;
        if (this.topic != null) {
            result = this.topic.equals(listener.getTopic());
        }
        if (this.className != null) {
            result = result == null || result;
            result &= this.className.equals(listener.getClassName());
            if (this.methodName != null) {
                result &= this.methodName.equals(listener.getMethodName());
                if (this.hashCode != null) {
                    result &= this.hashCode.equals(listener.getHashCode());
                }
            }
        }

        return result != null && result;
    }

    public boolean equals(String patternId) {
        ListenerId listener = parse(patternId);
        return matches(listener);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListenerId listener = (ListenerId) o;

        return !(className != null ? !className.equals(listener.className) : listener.className != null) &&
                !(hashCode != null ? !hashCode.equals(listener.hashCode) : listener.hashCode != null) &&
                !(methodName != null ? !methodName.equals(listener.methodName) : listener.methodName != null) &&
                !(topic != null ? !topic.equals(listener.topic) : listener.topic != null);

    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (hashCode != null ? hashCode.hashCode() : 0);
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        return result;
    }
}
