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
package org.grails.plugin.platform.conventions

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.slf4j.LoggerFactory

/**
 * Builder that evaluates a DSL Closure and produces a structure representing the 
 * method calls and property access, with child nodes for methods taking a closure
 */
class StandardDSLDelegate {

    final __log = LoggerFactory.getLogger(StandardDSLDelegate)

    private List<DSLCommand> __results
    private GrailsApplication grailsApplication

    StandardDSLDelegate(List<DSLCommand> results, GrailsApplication grailsApplication) {
        this.__results = results
        this.grailsApplication = grailsApplication
    }

    private __newBlock(String name, args, Closure body) {
        DSLCommand cmd
        if (this.@__log.debugEnabled) {
            this.@__log.debug "New block name [$name] with args [${args}] (${args?.size()})"
        }
        if ((args?.size() == 1) && args[0] instanceof Map) {
            cmd = new DSLNamedArgsBlockCommand(name: name, arguments: args[0] ?: [])
        } else {
            cmd = new DSLBlockCommand(name: name, arguments: args ?: [])
        }
        List<DSLCommand> results = []
        def nestedDelegate = new StandardDSLDelegate(results, grailsApplication)
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
                    command = new DSLNamedArgsCallCommand(name: name, arguments: args[0])
                } else if (args[0] instanceof Closure) {
                    command = __newBlock(name, null, args[0])
                } else {
                    command = new DSLSetValueCommand(name: name, value: args[0])
                }
            } else {
                if (args[-1] instanceof Closure) {
                    command = __newBlock(name, args[0..args.size() - 2], args[-1])
                } else {
                    command = new DSLCallCommand(name: name, arguments: args)
                }
            }
        } else {
            command = new DSLCallCommand(name: name, arguments: [])
        }

        if (command) {
            this.@__results << command
        } else {
            throw new IllegalArgumentException('Standard DSL Builder does not understand a call to [$name] with [${args}]')
        }
    }

    def propertyMissing(String name, value) {
        this.@__results << new DSLSetValueCommand(name: name, value: value)
        value
    }

    def propertyMissing(String name) {
        if (grailsApplication) {
            switch (name) {
                case 'grailsApplication': return grailsApplication
                    break
                case 'ctx': return grailsApplication.mainContext
                    break
                case 'config': return grailsApplication.config
                    break
            }
        }

        this.@__results << new DSLGetValueCommand(name: name)
    }
}   