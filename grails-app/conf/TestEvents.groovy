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
//import org.grails.plugin.platform.test.Book

events = {
    /*
        all listeners for 'testTopic' in SampleService class will have namespace 'blah'

        sending events to such topics requires the following call form : event('testTopic', data, [namespace:'blah'])
        default namespace is : 'app' for app events, 'pluginName' for plugins events
     */
    "sampleHello" namespace:'lal'

    /*
        all listeners for 'testTopic2' in SampleService class defined in method testEvent3 will have namespace 'global' (*)

        events sent with any specified namespace will trigger this listener : event('testTopic2, data, [namespace:'blah']) will
        work as well.
     */
    //"sampleHello:$SampleService.name#testEvent3" namespace:'*'


    /*
        all listeners for 'testTopic3' will have namespace 'app', which is the default anyway. Could have been written
        "testTopic3"(). This is certainly useful for plugins which want to let its listeners to observe app events.
         Otherwise the plugins listeners will observe its owns events ( namespace : 'pluginName' ).
     */
    //"sampleHello namespace:'app'

    "afterLoad" namespace: 'gorm'
}