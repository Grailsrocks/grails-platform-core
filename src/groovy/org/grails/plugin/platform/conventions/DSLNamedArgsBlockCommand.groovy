package org.grails.plugin.platform.conventions

/**
 * Encapsulate a DSL command that represents a nested block of commands
 *
 * something(x:y, p:q) {
 *    other = foo   
 *    bar a:b
 * }
 */
class DSLNamedArgsBlockCommand extends DSLNamedArgsCallCommand {
    List<DSLCommand> children
}