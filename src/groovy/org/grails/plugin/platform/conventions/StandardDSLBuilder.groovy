package org.grails.plugin.platform.conventions

import org.slf4j.LoggerFactory

/**
 * Builder that evaluates a DSL Closure and produces a structure representing the 
 * method calls and property access, with child nodes for methods taking a closure
 *
 * Just instantiate this and call build() with the DSL closure.
 * Iteratate over the List<DSLCommand> you get back, checking the type of nodes
 * to see what calls were made. 
 * 
 * NOTE: DSLCommand is polymorphic so if using in if/switch statements be careful
 * of ordering - put more specific types FIRST to avoid surprises
 */
class StandardDSLBuilder {
    
    final log = LoggerFactory.getLogger(StandardDSLBuilder)

    List<DSLCommand> build(Closure c, args = null) {
        List<DSLCommand> results = []
        def delegateBuilder = new StandardDSLDelegate(results)
        c.delegate = delegateBuilder
        c.resolveStrategy = Closure.DELEGATE_FIRST
        if (args) {
            c(*args)
        } else {
            c(args)
        }
        return results
    }
}