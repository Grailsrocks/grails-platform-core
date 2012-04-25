package org.grails.plugin.platform.conventions

class UnsupportedDSLOperationException extends RuntimeException {
    DSLCommand command
    
    UnsupportedDSLOperationException(String msg, DSLCommand command) {
        super(msg)
        this.command = command
    }
}