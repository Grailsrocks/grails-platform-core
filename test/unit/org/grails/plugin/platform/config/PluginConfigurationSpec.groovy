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
package org.grails.plugin.platform.config

import grails.test.*
import spock.lang.*
import grails.plugin.spock.UnitSpec
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.grails.plugin.platform.util.ClosureInvokingScript

class PluginConfigurationSpec extends UnitSpec {

    @Unroll("Expected #resultPath set to #resultValue from a closure")
    def "closure config loads into a ConfigObject"() {
        given:
        def pc = new PluginConfiguration()
        def mockConfig = new ConfigObject()
        def mockApp = [
            config: mockConfig
        ]
        pc.grailsApplication = mockApp
        
        when:
        def loadedConf = pc.parseConfigClosure(configBlock)
        def flatConf = loadedConf.flatten()
        
        then:
        flatConf[resultPath] == resultValue
        
        
        where:
        configBlock                       | resultPath    | resultValue
        { it -> a = 'test' }              | 'a'           | 'test'
        { it -> a.b = 'test' }            | 'a.b'         | 'test'
        { it -> a { b = 'test' } }        | 'a.b'         | 'test'
        { it -> a { b.c = 'test' } }      | 'a.b.c'       | 'test'
        { it -> a { b { c = 'test' } } }  | 'a.b.c'       | 'test'
    }
    
    @Unroll("Expected #resultPath set to #resultValue from a closure")
    def "external plugin config file loads and merges correctly"() {
        given:
        def pc = new PluginConfiguration()
        def mockConfig = new ConfigObject()
        def mockApp = [
            config: mockConfig
        ]
        pc.grailsApplication = mockApp
        pc.pluginConfigurationEntries = [
            [fullConfigKey:'plugin.a'],
            [fullConfigKey:'plugin.a.b'],
            [fullConfigKey:'plugin.a.b.c']
        ]

        when:
        pc.grailsApplication.config = new ConfigSlurper().parse(new ClosureInvokingScript(appConf))
        pc.applyAppPluginConfiguration( new ConfigSlurper().parse(new ClosureInvokingScript(pluginConf)) )
        def flatConf = pc.grailsApplication.config.flatten()
        
        then:
        (resultValue != null ? (flatConf[resultPath] == resultValue) : flatConf[resultPath].size() == 0)
        
        
        // Here we check that only settings that are declared are set, and values set already by 
        // the application are not overwritten
        where:
        appConf     | pluginConf                               | resultPath           | resultValue
        { it -> }   | { it -> plugin.a = 'test' }              | 'plugin.a'           | 'test'
        { it -> }   | { it -> plugin.a.b = 'test' }            | 'plugin.a.b'         | 'test'
        { it -> }   | { it -> a.b = 'test' }                   | 'a.b'                | null
        { it -> }   | { it -> plugin { a { b = 'test' } } }    | 'plugin.a.b'         | 'test'
        { it -> plugin.a = 'good' }     | { it -> plugin.a = 'test' }              | 'plugin.a'           | 'good'
        { it -> plugin.a.b = 'good' }   | { it -> plugin.a.b = 'test' }            | 'plugin.a.b'         | 'good'
        { it -> a.b = 'good' }          | { it -> a.b = 'test' }                   | 'a.b'                | 'good'
    }
    
}
