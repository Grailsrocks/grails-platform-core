package org.grails.plugin.platform.conventions

import java.lang.reflect.Modifier

import grails.util.GrailsNameUtils

import org.slf4j.LoggerFactory

/**
 * Bean that encapsulates the convention evaluation and overrides
 */
class ConventionsImpl implements Conventions {
    
    final log = LoggerFactory.getLogger(Conventions)

    def grailsApplication

    /**
     * Discover what convention code blocks the target class defines, used to identify
     * pieces of code that must be called as a result of some convention-based stimulus - 
     * for example controller actions.
     *
     * Supports the following representations:
     * - public (or public + final) instance property of type Closure
     * - public (or public + final) instance method with "def" return type and an optional argument list
     */
    List<String> discoverCodeBlockConventions(Class actualClass, Class annotation, boolean allowArgs = true) {
        List<String> namedCodeBlocks = []
        
        Set<String> gettersToIgnore = []
        
        // Get the pre-2.0 syntax closure actions
        for (prop in actualClass.metaClass.properties) {
            def isClosure = (prop.type == Closure)// || (actualClass.newInstance()[prop.name] instanceof Closure)
            if ( isClosure && 
                ((prop.modifiers == Modifier.PUBLIC) || (prop.modifiers == (Modifier.PUBLIC || Modifier.FINAL))) ) {
                namedCodeBlocks << prop.name
                
                gettersToIgnore << "get"+GrailsNameUtils.getClassNameRepresentation(prop.name)
            }
        }
        
        // Get post-2.0 style public methods defined
        if (annotation) {
            for (meth in actualClass.declaredMethods) {
                if ( meth.getAnnotation(annotation) ) {
                    if (allowArgs || (parameterTypes.size() == 0)) {
                        if (!gettersToIgnore.contains(meth.name)) {
                            namedCodeBlocks << meth.name
                        }
                    }
                }
            }
        }
        if (log.debugEnabled) {
            log.debug "Discovered code blocks ${namedCodeBlocks} on artefact ${actualClass}"
        }
        return namedCodeBlocks
    }
}