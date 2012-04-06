package org.grails.plugin.platform.conventions

import org.slf4j.LoggerFactory

/**
 * Builder that evaluates a DSL Closure and produces a structure representing the 
 * method calls and property access, with child nodes for methods taking a closure
 */
class StandardDSLDelegate {
    
    final log = LoggerFactory.getLogger(StandardDSLDelegate)
    
    private List<DSLCommand> __results
    
    StandardDSLDelegate(List<DSLCommand> results) {
        this.__results = results
    }
    
    private __newBlock(String name, args, Closure body) {
        DSLCommand cmd
        if ((args?.size() == 1) && args[0] instanceof Map) {
            cmd = new DSLNamedArgsBlockCommand(name: name, arguments: args[0] ?: [])
        } else {
            cmd = new DSLBlockCommand(name: name, arguments: args ?: [])
        }
        List<DSLCommand> results = []
        def nestedDelegate = new StandardDSLDelegate(results)
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = nestedDelegate
        body()
        cmd.children = results
        return cmd        
    }
    
    def methodMissing(String name, args) {
        DSLCommand command
        
        if (args) {
            if (args.size() == 1) {
                if (args[0] instanceof Map) {
                    command = new DSLNamedArgsCallCommand(name: name, arguments:args[0])
                } else if (args[0] instanceof Closure){
                    command = __newBlock(name, null, args[0])
                } else {
                    command = new DSLSetValueCommand(name: name, value:args[0])
                }
            } else {
                if (args[-1] instanceof Closure) {
                    command = __newBlock(name, args[0..args.size()-2], args[-1])
                } else {
                    command = new DSLCallCommand(name: name, arguments:args)
                }
            }
        } else {
            command = new DSLCallCommand(name: name, arguments:[])
        }
        
        if (command) {
            this.@__results << command
        } else {
            throw new IllegalArgumentException('wtf?')
        }
    }
    
    def propertyMissing(String name, value) {
        this.@__results << new DSLSetValueCommand(name: name, value:value)
        value
    }
}   