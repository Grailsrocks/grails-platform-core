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

class PlatformCoreBootStrap {
    def grailsNavigation
    def grailsApplication

    def init = {
        applicationStartupInfo()

        if (!pluginConfig?.navigation.disabled) {
            grailsNavigation.reloadAll()
        }
    }
    
    def destroy = {
        
    }
    
    void applicationStartupInfo() {
        if (pluginConfig?.show.startup.info) {
            def w = 70
            println '='*w
            def name = grailsApplication.metadata.'app.name'
            def ver = grailsApplication.metadata.'app.version'
            def welcome = "Application: "+(ver ? "$name $ver" : name)

            println ''
            println welcome.center(w)
            println(('-'*welcome.length()).center(w))
            println ''
            println "  Environment: ${Environment.current}"
            println '  Database configuration: '
            def ds = grailsApplication.config.dataSource
            println "    Hibernate DDL mode: ${ds.dbCreate}"
            if (ds.jndiName) {
                println "    JNDI: ${ds.jndiName}"
            } else {
                println "    URL: ${ds.url}"
                println "    Driver: ${ds.driverClassName}"
                println "    User: ${ds.username}"
            }

            println '='*w
        }
    }
}
