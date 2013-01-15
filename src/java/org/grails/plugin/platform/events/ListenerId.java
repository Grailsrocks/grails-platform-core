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

import groovy.lang.Closure;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Stephane Maldini <smaldini@vmware.com>
 * @version 1.0
 * @file
 * @date 09/01/12
 * @section DESCRIPTION
 * <p/>
 * format : namespace://topic:package.Class#method@hashCode
 */
public class ListenerId implements Serializable {
    private static final String CLOSURE_METHOD_NAME = "call";
    private static final String ID_CLASS_SEPARATOR = ":";
    private static final String ID_METHOD_SEPARATOR = "#";
    private static final String ID_HASHCODE_SEPARATOR = "@";
    public static final String NAMESPACE_WILDCARD = "*";
    public static final String ID_NAMESPACE_SEPARATOR = "://";

    /*private static final Pattern idRegex = Pattern.compile(
            "([^" + ID_NAMESPACE_SEPARATOR + "]*)?" +
                    "("+ID_NAMESPACE_SEPARATOR +")?"+
                    "([^" + ID_CLASS_SEPARATOR + "]*)?" +
                    "(" + ID_CLASS_SEPARATOR + "([^" + ID_METHOD_SEPARATOR + "]*))?"
                    + "(" + ID_METHOD_SEPARATOR + "([^" + ID_HASHCODE_SEPARATOR + "]*))?"
                    + "(" + ID_HASHCODE_SEPARATOR + "(-?\\d*))?");
*/
    private String className;
    private String methodName;
    private String hashCode;
    private String topic;

    private String namespace;

    public ListenerId(String namespace, String topic) {
        this(namespace, topic, null, null, null);
    }

    public ListenerId(String namespace, String topic, String className, String methodName, String hashCode) {
        this.className = className;
        this.methodName = methodName;
        this.hashCode = hashCode;
        this.namespace = namespace;
        if (topic != null && !topic.equals("")) {
            this.topic = topic;
        }
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
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

    //format : namespace://topic:package.Class#method@hashCode
    public String toString() {
        return toStringWithoutHash()
                + (hashCode != null ? ID_HASHCODE_SEPARATOR + hashCode : "");
    }

    // format : namespace://topic:package.Class#method
    public String toStringWithoutHash() {
        return (namespace != null ? namespace + ID_NAMESPACE_SEPARATOR : "") + (topic != null ? topic : "") + (className != null ? ID_CLASS_SEPARATOR + className : "")
                + (methodName != null ? ID_METHOD_SEPARATOR + methodName : "");
    }

    static public ListenerId build(String namespace, String topic, Object target, Method callback) {
        return new ListenerId(namespace, topic, target.getClass().getName(), callback.getName(), Integer.toString(target.hashCode()));
    }

    static public ListenerId build(String namespace, String topic, Class target, Method callback) {
        return new ListenerId(namespace, topic, target.getName(), callback.getName(), null);
    }

    static public ListenerId build(String namespace, String topic, Closure target) {
        return new ListenerId(namespace, topic, target.getClass().getName(), CLOSURE_METHOD_NAME, Integer.toString(target.hashCode()));
    }

    static public ListenerId parse(String id) {
        //Matcher parsed = idRegex.matcher(id);
        if (id != null) {
            int namespaceIndex = id.indexOf(ID_NAMESPACE_SEPARATOR);
            String _namespace = namespaceIndex != -1 ? id.substring(0, namespaceIndex) : null;
            id = namespaceIndex != -1 ? id.substring(namespaceIndex + 3, id.length()) : id;

            int classIndex = id.indexOf(ID_CLASS_SEPARATOR);
            String _topic = id.substring(0, classIndex != -1 ? classIndex : id.length());
            id = classIndex != -1 ? id.substring(classIndex + 1, id.length()) : id;

            int methodIndex = id.indexOf(ID_METHOD_SEPARATOR);
            String _class = classIndex != -1 ? id.substring(0, methodIndex != -1 ? methodIndex : id.length()) : null;
            id = methodIndex != -1 ? id.substring(methodIndex + 1, id.length()) : id;

            int hashcodeIndex = id.indexOf(ID_HASHCODE_SEPARATOR);
            String _method = methodIndex != -1 ? id.substring(0, hashcodeIndex != -1 ? hashcodeIndex : id.length()) : null;
            String _hashcode = hashcodeIndex != -1 ? id.substring(hashcodeIndex + 1, id.length()) : null;

            return new ListenerId(
                    _namespace,
                    _topic,
                    _class,
                    _method,
                    _hashcode
            );
        }

        return null;
    }

    public static boolean matchesTopic(String source, String target, boolean checkTargetTopic){
        int topicIdx = source.indexOf(NAMESPACE_WILDCARD);
        int targetTopicIdx = target.indexOf(NAMESPACE_WILDCARD);
        return checkTargetTopic && targetTopicIdx != -1 && source.startsWith(target.substring(0, targetTopicIdx)) ||
                        topicIdx != -1 && target.startsWith(source.substring(0, topicIdx)) ||
                        source.equals(target);
    }

    public static boolean matchesNamespace(String source, String target, boolean checkTargetTopic){
        return source.equals(NAMESPACE_WILDCARD) ||
                checkTargetTopic && target.equals(NAMESPACE_WILDCARD) ||
                source.equalsIgnoreCase(target);
    }

    public boolean matches(ListenerId target) {
        Boolean result = null;

        if (this.namespace != null && target.getNamespace() != null) {
            result = matchesNamespace(this.namespace, target.getNamespace(), true);
        }

        if (this.topic != null) {
            result = result == null || result;
            result &= matchesTopic(this.topic, target.getTopic(), true);
        }

        if (this.className != null) {
            result = result == null || result;
            result &= this.className.equals(target.getClassName());
            if (this.methodName != null) {
                result &= this.methodName.equals(target.getMethodName());
                if (this.hashCode != null) {
                    result &= this.hashCode.equals(target.getHashCode());
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
                !(namespace != null ? !namespace.equals(listener.namespace) : listener.namespace != null) &&
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
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        return result;
    }
}
