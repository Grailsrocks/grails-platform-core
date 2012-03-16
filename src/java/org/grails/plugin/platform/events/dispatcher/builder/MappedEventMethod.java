package org.grails.plugin.platform.events.dispatcher.builder;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 17/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class MappedEventMethod {

    public final static String WHEN_MAPPING = "when";
    protected static Map<String, List<Class<? extends EventMapping>>> mappings = new HashMap<String, List<Class<? extends EventMapping>>>();

    static {
        mappings.put(WHEN_MAPPING, new ArrayList<Class<? extends EventMapping>>() {{
            add(WhenMapping.class);
        }});
    }

    protected static final Log LOG = LogFactory.getLog(MappedEventMethod.class);

    // move these to subclass

    protected String methodName;
    protected Method methodNode;

    protected Map<String, EventMapping> appliedMappings = new LinkedHashMap<String, EventMapping>();
    protected Class<?> owningClass;
    private BeanWrapper bean;

    private int order; // what order to property appears in
    @SuppressWarnings("rawtypes")
    private Map attributes = Collections.EMPTY_MAP; // a map of attributes of property
    private Map<String, Object> metaMappings = new HashMap<String, Object>();

    public MappedEventMethod(Class<?> clazz, String methodName) {
        this(clazz, methodName, null);
    }
    public MappedEventMethod(Class<?> clazz, String methodName, Method methodNode) {
        owningClass = clazz;
        this.methodName = methodName;
        this.methodNode = methodNode;
        bean = new BeanWrapperImpl(this);
    }

    public void setMethodNode(Method methodNode) {
        this.methodNode = methodNode;
    }

    public static void removeMapping(String name, Class mappingClass) {
        Assert.hasLength(name, "Argument [name] cannot be null");

        List<Class<? extends EventMapping>> objects = getOrInitializeMapping(name);
        objects.remove(mappingClass);
    }

    public static void removeMapping(String name) {
        Assert.hasLength(name, "Argument [name] cannot be null");

        List<Class<? extends EventMapping>> objects = getOrInitializeMapping(name);
        objects.clear();
    }

    public static void registerNewMapping(String name, Class<? extends EventMapping> mappingClass) {
        Assert.hasLength(name, "Argument [name] cannot be null");
        if (mappingClass == null || !EventMapping.class.isAssignableFrom(mappingClass)) {
            throw new IllegalArgumentException("Argument [mappingClass] with value [" + mappingClass +
                    "] is not a valid mapping");
        }

        List<Class<? extends EventMapping>> objects = getOrInitializeMapping(name);
        objects.add(mappingClass);
    }

    private static List<Class<? extends EventMapping>> getOrInitializeMapping(String name) {
        List<Class<? extends EventMapping>> objects = mappings.get(name);
        if (objects == null) {
            objects = new ArrayList<Class<? extends EventMapping>>();
            mappings.put(name, objects);
        }
        return objects;
    }


    public static boolean hasRegisteredMapping(String mappingName) {
        return mappings.containsKey(mappingName);
    }

    /**
     * @return Returns the appliedMappings.
     */
    public Collection<EventMapping> getAppliedMappings() {
        return appliedMappings.values();
    }

    /**
     * Obtains an applied mapping by name.
     *
     * @param name The name of the mapping
     * @return The applied mapping
     */
    public EventMapping getAppliedMapping(String name) {
        return appliedMappings.get(name);
    }

    /**
     * @param mappingName The name of the mapping to check
     * @return Returns true if the specified mapping name is being applied to this property
     */
    public boolean hasAppliedMapping(String mappingName) {
        return appliedMappings.containsKey(mappingName);
    }

    /**
     * @return Returns the methodNode.
     */
    public Method getMethodNode() {
        return methodNode;
    }

    /**
     * @return Returns the order.
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order The order to set.
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @SuppressWarnings("rawtypes")
    public Map getAttributes() {
        return attributes;
    }

    @SuppressWarnings("rawtypes")
    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    /**
     * Checks with this MappedMethod instance supports applying the specified mapping.
     *
     * @param mappingName The name of the mapping
     * @return True if the mapping is supported
     */
    public boolean supportsMapping(String mappingName) {

        if (!mappings.containsKey(mappingName)) {
            return bean.isWritableProperty(mappingName);
        }

        try {
            EventMapping c = instantiateMapping(mappingName, false);
            return c != null && c.supports(methodNode);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Exception thrown instantiating mapping [" + mappingName +
                    "] to class [" + owningClass + "]", e);
            throw new RuntimeException("Exception thrown instantiating  mapping [" + mappingName +
                    "] to class [" + owningClass + "]");
        }
    }

    /**
     * Applies a mapping for the specified name and consraint value.
     *
     * @param mappingName       The name of the mapping
     * @param constrainingValue The constraining value
     * @throws RuntimeException Thrown when the specified mapping is not supported by this ConstrainedProperty. Use <code>supportsMapping(String mappingName)</code> to check before calling
     */
    public void applyMapping(String mappingName, Object constrainingValue) {

        if (mappings.containsKey(mappingName)) {
            if (constrainingValue == null) {
                appliedMappings.remove(mappingName);
            } else {
                try {
                    EventMapping c = instantiateMapping(mappingName, true);
                    if (c != null) {
                        c.setParameter(constrainingValue);
                        appliedMappings.put(mappingName, c);
                    }
                } catch (Exception e) {
                    LOG.error("Exception thrown applying mapping [" + mappingName +
                            "] to class [" + owningClass + "] for value [" + constrainingValue + "]: " + e.getMessage(), e);
                    throw new RuntimeException("Exception thrown applying mapping [" + mappingName +
                            "] to class [" + owningClass + "] for value [" + constrainingValue + "]: " + e.getMessage(), e);
                }
            }
        } else if (bean.isWritableProperty(mappingName)) {
            bean.setPropertyValue(mappingName, constrainingValue);
        } else {
            throw new RuntimeException("Mapping [" + mappingName + "] is not supported for property [" +
                    methodName + "] of class [" + owningClass + "] with type [" + methodNode + "]");
        }
    }

    private EventMapping instantiateMapping(String mappingName, boolean validate) throws InstantiationException, IllegalAccessException {
        List<Class<? extends EventMapping>> candidateMappings = mappings.get(mappingName);

        for (Class<? extends EventMapping> mappingFactory : candidateMappings) {
            EventMapping m = mappingFactory.newInstance();

            m.setOwningClass(owningClass);
            m.setMethodName(methodName);

            if (validate && m.isValid()) {
                return m;
            } else if (!validate) {
                return m;
            }

        }
        return null;
    }

    /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(owningClass)
                .append(methodName)
                .append(methodNode)
                .append(appliedMappings)
                .toString();
    }

    /**
     * Adds a meta mappings which is a non-validating informational mapping.
     *
     * @param name  The name of the mapping
     * @param value The value
     */
    public void addMetaMapping(String name, Object value) {
        metaMappings.put(name, value);
    }

    /**
     * Obtains the value of the named meta mapping.
     *
     * @param name The name of the mapping
     * @return The value
     */
    public Object getMetaMappingValue(String name) {
        return metaMappings.get(name);
    }

    public String getMethodName() {
        return methodName;
    }

}
