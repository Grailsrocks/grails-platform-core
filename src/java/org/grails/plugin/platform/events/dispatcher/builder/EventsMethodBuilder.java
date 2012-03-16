package org.grails.plugin.platform.events.dispatcher.builder;

import grails.util.GrailsUtil;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.util.BuilderSupport;
import org.grails.plugin.platform.events.Listener;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 17/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class EventsMethodBuilder extends BuilderSupport {
    private Map<String, MappedEventMethod> mappedProperties = new LinkedHashMap<String, MappedEventMethod>();
    private int order = 1;
    private Class<?> targetClass;
    private MetaClass targetMetaClass;

    public EventsMethodBuilder(Object target) {
        this(target.getClass());
    }

    public EventsMethodBuilder(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.targetMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(targetClass);
    }


    @Override
    protected Object doInvokeMethod(String methodName, Object name, Object args) {
        try {
            return super.doInvokeMethod(methodName, name, args);
        } catch (MissingMethodException e) {
            return targetMetaClass.invokeMethod(targetClass, methodName, args);
        }
    }

    @Override
    public Object getProperty(String property) {
        try {
            return super.getProperty(property);
        } catch (MissingPropertyException e) {
            return targetMetaClass.getProperty(targetClass, property);
        }
    }

    @Override
    public void setProperty(String property, Object newValue) {
        try {
            super.setProperty(property, newValue);
        } catch (MissingPropertyException e) {
            targetMetaClass.setProperty(targetClass, property, newValue);
        }

    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Object createNode(Object name, final Map attributes) {
        // we do this so that missing property exception is throw if it doesn't exist

        try {
            final String method = (String) name;
            MappedEventMethod mm;
            if (mappedProperties.containsKey(method)) {
                mm = mappedProperties.get(method);
            } else {
                mm = new MappedEventMethod(targetClass, method);
                final MappedEventMethod _mm = mm;
                ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
                    public void doWith(Method _methodNode) throws IllegalArgumentException, IllegalAccessException {
                        if (_methodNode.getName().equals(method)) {
                            if (_methodNode.getAnnotation(Listener.class) == null) {
                                throw new RuntimeException(targetClass.getName() + "#" + _methodNode.getName() + " is not" +
                                        "a @Listener");
                            }
                            if (_methodNode == null) {
                                throw new MissingMethodException(method, targetClass, new Object[]{attributes}, true);
                            }

                            _mm.setMethodNode(_methodNode);
                            _mm.setOrder(order++);
                            mappedProperties.put(method, _mm);
                            return;
                        }
                    }
                });

            }

            if (mm.getMethodNode() == null) {
                return mm;
            }

            for (Object o : attributes.keySet()) {
                String mappingName = (String) o;
                final Object value = attributes.get(mappingName);
                if (mm.supportsMapping(mappingName)) {
                    mm.applyMapping(mappingName, value);
                } else {
                    if (MappedEventMethod.hasRegisteredMapping(mappingName)) {
                        // constraint is registered but doesn't support this property's type
                        GrailsUtil.warn("Method [" + mm.getMethodName() + "] of domain class " +
                                targetClass.getName() + " has type [" + mm.getMethodNode().getName() +
                                "] and doesn't support mapping [" + mappingName +
                                "]. This mapping will not be checked.");
                    } else {
                        // in the case where the constraint is not supported we still retain meta data
                        // about the constraint in case its needed for other things
                        mm.addMetaMapping(mappingName, value);
                    }
                }
            }

            return mm;
        } catch (InvalidPropertyException ipe) {
            throw new MissingMethodException((String) name, targetClass, new Object[]{attributes});
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Object createNode(Object name, Map attributes, Object value) {
        throw new MissingMethodException((String) name, targetClass, new Object[]{attributes, value});
    }

    @Override
    protected void setParent(Object parent, Object child) {
        // do nothing
    }

    @Override
    protected Object createNode(Object name) {
        return createNode(name, Collections.EMPTY_MAP);
    }

    @Override
    protected Object createNode(Object name, Object value) {
        return createNode(name, Collections.EMPTY_MAP, value);
    }

    public Map<String, MappedEventMethod> getMappedProperties() {
        return mappedProperties;
    }
}
