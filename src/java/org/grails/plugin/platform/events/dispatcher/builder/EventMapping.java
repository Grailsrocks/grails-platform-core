package org.grails.plugin.platform.events.dispatcher.builder;

import java.lang.reflect.Method;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 17/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public abstract class EventMapping {
    private String mappedMethodName;
    private Class<?> mappedOwningClass;
    private Object mappedParameter;

    public Object getParameter() {
        return mappedParameter;
    }

    public boolean supports(Method type) {
        return type != null;
    }

    public void setMethodName(String methodName) {
        this.mappedMethodName = methodName;
    }

    public void setOwningClass(Class<?> owningClass) {
        this.mappedOwningClass = owningClass;
    }

    public String getMethodName() {
        return mappedMethodName;
    }

    public Class<?> getOwningClass() {
        return mappedOwningClass;
    }

    public boolean isValid() {
        return true;
    }

    public boolean processBeforeHandler() {
        return true;
    }

    public boolean processAfterHandler() {
        return true;
    }

    public void processInitialization() {
    }

    public void setParameter(Object mappedParameter) {
        this.mappedParameter = mappedParameter;
    }
}
