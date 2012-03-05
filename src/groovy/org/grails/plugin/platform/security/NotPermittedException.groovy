package org.grails.plugin.platform.security

class NotPermittedException extends RuntimeException {
    final target
    final action
    
    NotPermittedException(target, action) {
        super()
        this.target = target
        this.action = action
    }
}