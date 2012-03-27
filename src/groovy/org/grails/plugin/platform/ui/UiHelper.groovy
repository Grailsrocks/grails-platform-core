package org.grails.plugin.platform.ui

import org.slf4j.LoggerFactory

import org.springframework.web.context.request.RequestContextHolder as RCH

/**
 * Helper methods for common UI features
 */
class UiHelper {
    final log = LoggerFactory.getLogger(UiHelper)

    def injectedMethods = {
        def self = this
        
        controller { clazz ->
            displayMessage { String msg ->
                self.displayMessage(msg, delegate.request)
            }
            displayMessage { Map args ->
                self.displayMessage(args, delegate.request)
            }
            displayFlashMessage { String msg ->
                self.displayFlashMessage(msg, delegate.flash)
            }
            displayFlashMessage { Map args ->
                self.displayFlashMessage(args, delegate.flash)
            }
        }
    }

    Map getDisplayMessage(scope) {
        if (log.debugEnabled) {
            log.debug "Getting display message from scope: ${scope}"
        }
        def args = [:]
        if (scope[UiConstants.DISPLAY_MESSAGE]) {
            args.text = scope[UiConstants.DISPLAY_MESSAGE]
            args.args = scope[UiConstants.DISPLAY_MESSAGE_ARGS]
            args.type = scope[UiConstants.DISPLAY_MESSAGE_TYPE]
        }
        if (log.debugEnabled) {
            log.debug "Found display message from scope [${scope}]: ${args}"
        }
        return args
    }
    
    void displayMessage(String text, request = RCH.requestAttributes.request) {
        if (log.debugEnabled) {
            log.debug "Setting display message text: ${text}"
        }
        request[UiConstants.DISPLAY_MESSAGE] = text
    }

    void displayMessage(Map args, request = RCH.requestAttributes.request) {
        if (log.debugEnabled) {
            log.debug "Setting display message args: ${args}"
        }
        request[UiConstants.DISPLAY_MESSAGE] = args.text
        request[UiConstants.DISPLAY_MESSAGE_ARGS] = args.args
        request[UiConstants.DISPLAY_MESSAGE_TYPE] = args.type
    }

    void displayFlashMessage(String text, flash = RCH.requestAttributes.flashScope) {
        if (log.debugEnabled) {
            log.debug "Setting display flash message text: ${text}"
        }
        flash[UiConstants.DISPLAY_MESSAGE] = msg
    }

    void displayFlashMessage(Map args, flash = RCH.requestAttributes.flashScope) {
        if (log.debugEnabled) {
            log.debug "Setting display flash message args: ${args}"
        }
        flash[UiConstants.DISPLAY_MESSAGE] = args.text
        flash[UiConstants.DISPLAY_MESSAGE_ARGS] = args.args
        flash[UiConstants.DISPLAY_MESSAGE_TYPE] = args.type
    }
}