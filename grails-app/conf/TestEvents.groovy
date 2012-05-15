import org.grails.plugin.platform.test.SampleService

events = {
    /*
        all listeners for 'testTopic' in SampleService class will have scope 'blah'

        sending events to such topics requires the following call form : event('testTopic', data, [scope:'blah'])
        default scope is : 'app' for app events, 'pluginName' for plugins events
     */
    "testTopic:$SampleService.name" scope:'blah'

    /*
        all listeners for 'testTopic2' in SampleService class defined in method testEvent3 will have scope 'global' (*)

        events sent with any specified scope will trigger this listener : event('testTopic2, data, [scope:'blah']) will
        work as well.
     */
    "testTopic2:$SampleService.name#testEvent3" scope:'*'


    /*
        all listeners for 'testTopic3' will have scope 'app', which is the default anyway. Could have been written
        "testTopic3"(). This is certainly useful for plugins which want to let its listeners to observe app events.
         Otherwise the plugins listeners will observe its owns events ( scope : 'pluginName' ).
     */
    "testTopic3" scope:'app'
}