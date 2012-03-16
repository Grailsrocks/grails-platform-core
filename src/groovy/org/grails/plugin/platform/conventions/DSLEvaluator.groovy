package org.grails.plugin.platform.conventions

import org.slf4j.LoggerFactory

/**
 * Evaluates a DSL and returns the command structure of it
 */
class DSLEvaluator {
    
    final log = LoggerFactory.getLogger(DSLEvaluator)

    def grailsApplication

    List<DSLCommand> evaluate(Closure c) {
        def builder = new StandardDSLBuilder()
        builder.build(c)
    }
}