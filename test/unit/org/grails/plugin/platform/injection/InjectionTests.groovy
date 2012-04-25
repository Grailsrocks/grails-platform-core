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