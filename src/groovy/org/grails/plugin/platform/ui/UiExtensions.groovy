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
package org.grails.plugin.platform.ui

import org.slf4j.LoggerFactory

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.context.request.RequestContextHolder as RCH

import org.grails.plugin.platform.util.PluginUtils
import org.grails.plugin.platform.util.PropertyNamespacer

/**
 * Helper methods for common UI features
 */
class UiExtensions implements ApplicationContextAware {
    final log = LoggerFactory.getLogger(UiExtensions)

    static final String SESSION_WRAPPER_KEY = 'plugin.platformCore.plugin.session.wrapper';
    static final String FLASH_WRAPPER_KEY = 'plugin.platformCore.plugin.flash.wrapper';
    static final String REQUEST_WRAPPER_KEY = 'plugin.platformCore.plugin.request.wrapper';
    
    ApplicationContext applicationContext

    def injectedMethods = {
        def self = this
        
        'controller, tagLib' { clazz ->
            
            def pluginName = PluginUtils.getNameOfDefiningPlugin(applicationContext, clazz)

            displayMessage { String msg ->
                self.displayMessage(msg, pluginName)
            }
            displayMessage { Map args ->
                self.displayMessage(args, pluginName)
            }
            displayFlashMessage { String msg ->
                self.displayFlashMessage(msg, pluginName)
            }
            displayFlashMessage { Map args ->
                self.displayFlashMessage(args, pluginName)
            }

            if (pluginName) {
                getPluginSession() { ->
                    self.getPluginSession(pluginName)
                }
                getPluginFlash() { ->
                    self.getPluginFlash(pluginName)
                }
                getPluginRequestAttributes() { ->
                    self.getPluginRequestAttributes(pluginName)
                }
            }
        }
    }

    PropertyNamespacer getPluginSession(String pluginName) {
        def req = RCH.requestAttributes.session
        def wrapper = req[SESSION_WRAPPER_KEY]
        if (!wrapper) {
            def session = RCH.requestAttributes.session
            wrapper = new PropertyNamespacer(pluginName+'.', session, 'getAttributeNames')
            req[SESSION_WRAPPER_KEY] = wrapper
        } 
        return wrapper
    }
    
    PropertyNamespacer getPluginFlash(String pluginName) {
        def req = RCH.requestAttributes.flashScope
        def wrapper = req[FLASH_WRAPPER_KEY]
        if (!wrapper) {
            def flash = RCH.requestAttributes.flashScope
            wrapper = new PropertyNamespacer(pluginName+'.', flash, 'keySet')
            req[FLASH_WRAPPER_KEY] = wrapper
        } 
        return wrapper
    }
    
    PropertyNamespacer getPluginRequestAttributes(String pluginName) {
        def req = RCH.requestAttributes.currentRequest
        def wrapper = req[REQUEST_WRAPPER_KEY]
        if (!wrapper) {
            wrapper = new PropertyNamespacer(pluginName+'.', req, 'getAttributeNames')
            req[REQUEST_WRAPPER_KEY] = wrapper
        } 
        return wrapper
    }
    
    Map getDisplayMessage(scope) {
        if (log.debugEnabled) {
            log.debug "Getting display message from scope: ${scope.toString()}"
        }
        def args = [:]
        if (scope[UiConstants.DISPLAY_MESSAGE]) {
            args.text = scope[UiConstants.DISPLAY_MESSAGE]
            args.args = scope[UiConstants.DISPLAY_MESSAGE_ARGS]
            args.type = scope[UiConstants.DISPLAY_MESSAGE_TYPE]
        }
        if (log.debugEnabled) {
            if (args) {
                log.debug "Found display message from scope [${scope.toString()}]: ${args}"
            }
        }
        return args
    }
    
    void displayMessage(String text, String pluginName = null) {
        if (log.debugEnabled) {
            log.debug "Setting display message text: ${text}"
        }
        getPluginRequestAttributes('platformCore')[UiConstants.DISPLAY_MESSAGE] = pluginName ? "plugin.${pluginName}.${text}" : text
    }

    void displayMessage(Map args, String pluginName = null) {
        if (log.debugEnabled) {
            log.debug "Setting display message args: ${args}"
        }
        def reqAttribs = getPluginRequestAttributes('platformCore')
        reqAttribs[UiConstants.DISPLAY_MESSAGE] = pluginName ? "plugin.${pluginName}.${args.text}" : args.text
        reqAttribs[UiConstants.DISPLAY_MESSAGE_ARGS] = args.args
        reqAttribs[UiConstants.DISPLAY_MESSAGE_TYPE] = args.type
    }

    void displayFlashMessage(String text, String pluginName = null) {
        if (log.debugEnabled) {
            log.debug "Setting display flash message text: ${text}"
        }
        getPluginFlash('platformCore')[UiConstants.DISPLAY_MESSAGE] = pluginName ? "plugin.${pluginName}.${text}" : text
    }

    void displayFlashMessage(Map args, String pluginName = null) {
        if (log.debugEnabled) {
            log.debug "Setting display flash message args: ${args}"
        }
        def flash = getPluginFlash('platformCore')
        flash[UiConstants.DISPLAY_MESSAGE] = pluginName ? "plugin.${pluginName}.${args.text}" : args.text
        flash[UiConstants.DISPLAY_MESSAGE_ARGS] = args.args
        flash[UiConstants.DISPLAY_MESSAGE_TYPE] = args.type
    }
}