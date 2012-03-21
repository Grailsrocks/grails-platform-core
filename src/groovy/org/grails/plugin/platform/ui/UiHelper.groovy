package org.grails.plugin.platform.ui

import org.springframework.web.context.request.RequestContextHolder as RCH

/**
 * Helper methods for common UI features
 */
class UiHelper {
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
        def args = [:]
        if (scope[UiConstants.DISPLAY_MESSAGE]) {
            args.msg = scope[UiConstants.DISPLAY_MESSAGE]
            args.msgArgs = scope[UiConstants.DISPLAY_MESSAGE_ARGS]
            args.msgType = scope[UiConstants.DISPLAY_MESSAGE_TYPE]
        }
        return args
    }
    
    void displayMessage(String text, request = RCH.requestAttributes.request) {
        request[UiConstants.DISPLAY_MESSAGE] = text
    }

    void displayMessage(Map args, request = RCH.requestAttributes.request) {
        request[UiConstants.DISPLAY_MESSAGE] = args.text
        request[UiConstants.DISPLAY_MESSAGE_ARGS] = args.args
        request[UiConstants.DISPLAY_MESSAGE_TYPE] = args.type
    }

    void displayFlashMessage(String text, flash = RCH.requestAttributes.flashScope) {
        flash[UiConstants.DISPLAY_MESSAGE] = msg
    }

    void displayFlashMessage(Map args, flash = RCH.requestAttributes.flashScope) {
        flash[UiConstants.DISPLAY_MESSAGE] = args.text
        flash[UiConstants.DISPLAY_MESSAGE_ARGS] = args.args
        flash[UiConstants.DISPLAY_MESSAGE_TYPE] = args.type
    }
}