package org.grails.plugin.platform.events.dispatcher.builder;

import groovy.lang.Closure;

/**
* @author Stephane Maldini <smaldini@doc4web.com>
* @version 1.0
* @file
* @date 17/01/12
* @section DESCRIPTION
* <p/>
* [Does stuff]
*/
public class WhenMapping extends EventMapping {

    private Closure when;

    @Override
    public void setParameter(Object mappedParameter) {
        if (!(mappedParameter instanceof Closure)) {
            throw new IllegalArgumentException("Parameter for mapping [" +
                    MappedEventMethod.WHEN_MAPPING + "] of property [" +
                    getMethodName() + "] of class [" + getOwningClass() + "] must be a Closure");
        }

        when = (Closure) mappedParameter;

        Class<?>[] params = when.getParameterTypes();
        if (params.length != 1) {
            throw new IllegalArgumentException("Parameter for mapping [" + MappedEventMethod.WHEN_MAPPING + "] of property [" + getMethodName() + "] of class [" + getOwningClass() + "] must be a Closure taking 1 parameter (event)");
        }

        super.setParameter(mappedParameter);
    }
}
