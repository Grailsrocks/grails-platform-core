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
package org.grails.plugin.platform.controllers

class PlatformToolsController {
    
    def grailsSecurity
    def grailsNavigation
    def grailsEvents
    def grailsUiExtensions
    
    def index = {
    }
    
    def security = {
        [identity:grailsSecurity.userIdentity, info:grailsSecurity.userInfo]
    }
    
    def showPluginConfig = {
        [allPluginConfig:grailsApplication.config.plugin.flatten()]
    }

    def showNavigation = {
        [
            navScopes:grailsNavigation.scopes,
            navNodesById:grailsNavigation.nodesById,
            navNodesByControllerAction:grailsNavigation.nodesByControllerAction
        ]
    }

    def showEvents = {
        [events:grailsEvents.eventDefinitions]
    }

    def showUiExtensions = {
        pluginSession.testKey = 'session testing'
        pluginRequestAttributes.testKey = 'request attribute testing'
        pluginFlash.testKey = 'flash testing, only see this on second load!'
        
        displayMessage text:"This is an alert message", type:'alert'
        
        [
            pluginSessionInfo: grailsUiExtensions.getPluginSession('platformCore').toMap(),
            pluginFlashInfo: grailsUiExtensions.getPluginFlash('platformCore').toMap(),
            pluginRequestInfo: grailsUiExtensions.getPluginRequestAttributes('platformCore').toMap(),
        ]
    }

/*    
    def jsmodel = {
        [book:[title:'Test book title', author:'Test Author']]
    }
*/
}
