package org.grails.plugin.platform.conventions

/**
 * Encapsulate a DSL command that represents a nested block of commands
 *
 * something {
 *    other = foo   
 *    bar a:b
 * }
 * -or with arguments-
 *
 * something(x:y, p:q) {
 *    other = foo   
 *    bar a:b
 * }
 */
class DSLBlockCommand extends DSLCommand {
    List<DSLCommand> children
    List arguments
}