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
import grails.util.Environment

class PlatformCoreUrlMappings {
    static mappings = {
        if ( Environment.current == Environment.DEVELOPMENT) {
            "/platform"(controller:'platformTools', action:'index')
            "/platform/security/$id?"(controller:'platformTools', action:'security')
            "/platform/ui-extensions/$id?"(controller:'platformTools', action:'showUiExtensions')
            "/platform/events"(controller:'platformTools', action:'showEvents')
            "/platform/plugin-config/$id?"(controller:'platformTools', action:'showPluginConfig')
            //"/platform/js"(controller:'platformTools', action:'jsmodel')
            "/platform/navigation"(controller:'platformTools', action:'showNavigation')
    	}
    	
	}
}