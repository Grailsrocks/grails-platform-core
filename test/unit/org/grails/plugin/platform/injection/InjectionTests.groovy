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
package org.grails.plugin.platform.injection

class InjectionTests extends GroovyTestCase {
    void testApplyMethodsTo() {
        def magicMethods = [
            [name:'testMe', code: { -> 'testMe called'}],
            [name:'testMeStatic', staticMethod:true, code: { -> 'testMe static called'}]
        ]
      
        def obj = new DummyClassForMonkeying()
        shouldFail {
            obj.testMe()
        }
        shouldFail {
            obj.getClass().testMeStatic()
        }
        
        def injections = new InjectionImpl()
        injections.grailsApplication = [
            mainContext:[
                pluginManager:[ 
                    getAllPlugins: { -> [] }
                ]
            ]
        ] // @todo mock grails app
        injections.applyMethodsTo(DummyClassForMonkeying, magicMethods)
    
        def obj2 = new DummyClassForMonkeying()
        assert obj2.testMe() == 'testMe called', "Instance dynamic method was not applied"
        assert obj2.getClass().testMeStatic() == 'testMe static called', "Static dynamic method was not applied"
        
        // @todo reset metaclass
    }
}

class DummyClassForMonkeying {
}