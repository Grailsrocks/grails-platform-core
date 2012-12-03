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
import grails.util.Environment

class PlatformCoreFilters {

    def grailsNavigation
    def grailsApplication

    def filters = {
        if (!grailsApplication.config.plugin.platformCore.navigation.disabled) {
            navigationActivator(controller: '*', action: '*') {
                before = {
                    grailsNavigation.setActivePathFromRequest(request, controllerName, actionName)
                    return true
                }
            }
        }
        
        if (Environment.current == Environment.DEVELOPMENT) {
            'platformDev'(uri:'/platform/**'){
                before = {
                    if (controllerName == 'platformTools') {
                        def UA = request.getHeader('User-Agent')
                        // OK need a regex in future... check Lion
                        def OSX = UA.indexOf('OS X 10_7')
                        if (OSX == -1) {
                            // try mountain lion
                            OSX = UA.indexOf('OS X 10_8')
                        }
                        if (OSX > -1) {
                            redirect(mapping:'platformFancy', action:actionName, id:params.id, params:params)
                            return false
                        }
                    }
                }
            }
        }
    }
}
