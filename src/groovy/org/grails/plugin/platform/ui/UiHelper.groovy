/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (stephane.maldini@gmail.com)
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