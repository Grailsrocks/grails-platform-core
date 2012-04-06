package org.grails.plugin.platform.conventions

import org.slf4j.LoggerFactory

/**
 * Builder that evaluates a DSL Closure and produces a structure representing the 
 * method calls and property access, with child nodes for methods taking a closure
 */
class StandardDSLBuilder {
    
    final log = LoggerFactory.getLogger(StandardDSLBuilder)

    List<DSLCommand> build(Closure c) {
        List<DSLCommand> results = []
        def delegateBuilder = new StandardDSLDelegate(results)
        c.delegate = delegateBuilder
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
        return results
    }
}