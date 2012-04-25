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

class PluginConfigurationTests extends GroovyTestCase {
    void testSettingConfigPathsNoExistingConf() {
        def pc = new PluginConfigurationImpl()
        
        def config = new ConfigObject()
        def mockApp = [
            config: config
        ]
        
        pc.grailsApplication = mockApp
        
        pc.setConfigValueByPath('a.b.c.enabled', true)
        assertTrue config.a instanceof ConfigObject
        assertTrue config.a.b instanceof ConfigObject
        assertTrue config.a.b.c instanceof ConfigObject
        println "config.a.b.c: "+config.a.b.c?.dump()
        
        assertTrue config.a.b.c.enabled instanceof Boolean
        assertTrue config.a.b.c.enabled
    }

    void testSettingConfigPathsPartiallyExistingConf() {
        def pc = new PluginConfigurationImpl()
        
        def config = new ConfigObject()
        config.a = new ConfigObject()
        config.a.b = new ConfigObject()
        config.a.b.something = "leave me alone"
        
        def mockApp = [
            config: config
        ]
        
        pc.grailsApplication = mockApp
        
        pc.setConfigValueByPath('a.b.c.enabled', true)
        assertTrue config.a instanceof ConfigObject
        assertTrue config.a.b instanceof ConfigObject
        assertEquals "leave me alone", config.a.b.something
        assertTrue config.a.b.c instanceof ConfigObject
        assertTrue config.a.b.c.enabled instanceof Boolean
        assertTrue config.a.b.c.enabled
    }
    
    
    def testDoWithConfigOptionsClosuresLoad() {
        // dummy validator
        def val1 = { v -> }

        def input = [
            [ plugins:[ [ name:'x', opts: { 'a.b'() }] ], 
                entries:[[plugin:'x', key:'a.b', defaultValue:null, validator: null]] ],
            [ plugins:[ [name:'x', opts: { 'a.b'(defaultValue:'boo') }] ],
                entries:[[plugin:'x', key:'a.b', defaultValue:'boo', validator: null]] ],
            [plugins:[ [name:'x', opts: { 'a.b'(validator: val1) }] ], 
                entries:[[plugin:'x', key:'a.b', defaultValue:null, validator: val1]] ],
            [plugins:[ [name:'x', opts: { 'a.b'(defaultValue:'x'); 'p.q'(defaultValue:'y');  }]  ],
                entries: [  [plugin:'x', key:'a.b', defaultValue:'x', validator: null], 
                            [plugin:'x', key:'p.q', defaultValue:'y', validator: null]] ]
        ]
        
        assertDoWithConfigEntries(input)
    }
    
    private assertDoWithConfigEntries(input) {
      
       input.each { inp ->
           def mockPluginManager = [
               allPlugins: inp.plugins.collect({ p -> [
                   pluginClass:[name:p.name], 
                   instance:new DummyGrailsPluginWithConfigOptions(doWithConfigOptions:p.opts)
               ] }) 
           ] 

           def pc = new PluginConfigurationImpl()

           pc.pluginManager = mockPluginManager
           pc.loadConfigurationOptions()

           inp.entries.each { e ->
               assertTrue "${e} was not found", null != pc.pluginConfigurationEntries.find { ce -> 
                   (ce.plugin == e.plugin) && 
                   (ce.fullConfigKey == "plugin.${e.plugin}.${e.key}".toString()) && 
                   (ce.key == e.key) && 
                   (ce.defaultValue == e.defaultValue) && 
                   (ce.validator == e.validator)
               }
           }
           pc.pluginConfigurationEntries.size() == inp.entries.size()
       }        
    }
}

class DummyGrailsPluginWithConfigOptions {
    def doWithConfigOptions
}