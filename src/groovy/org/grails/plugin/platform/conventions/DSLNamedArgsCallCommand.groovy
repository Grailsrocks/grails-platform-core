package org.grails.plugin.platform.conventions

/**
 * Encapsulate a DSL command that is a regular method invocation
 *
 * something a:b, p:q
 */
class DSLNamedArgsCallCommand extends DSLCommand {
    Map arguments
}