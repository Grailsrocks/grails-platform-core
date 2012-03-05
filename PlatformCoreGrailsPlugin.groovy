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

import org.springframework.core.io.FileSystemResource

class PlatformCoreGrailsPlugin {
    // the plugin version
    def version = "1.0.M1-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [
            resources: '1.1.6 > *'
    ]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/conf/TestResources.groovy",
            "grails-app/i18n/test.properties",
            "grails-app/domain/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/controllers/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/src/groovy/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/src/java/org/grails/plugin/platform/test/**/*.java",
            "grails-app/views/error.gsp",
            "grails-app/views/test/**/*.gsp",
    ]

    def observe = ['*'] // We observe everything so we can re-apply dynamic methods, conventions etc

    def title = "Plugin Platform Core"
    def author = "Marc Palmer"
    def authorEmail = "marc@grailsrocks.com"
    def description = '''\
Grails Plugin Platform Core APIs
'''

    def loadBefore = ['core'] // Before rest of beans are initialized
    def loadAfter = ['logging'] // After logging though, we need that

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/platform-core"

    def license = "APACHE"

    def organization = [name: "Grailsrocks", url: "http://grailsrocks.com/"]

    def developers = [
            [name: "Marc Palmer", email: "marc@grailsrocks.com"],
            [name: "Stéphane Maldini", email: "stephane.maldini@gmail.com"]
    ]

    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPPLATFORMCORE" ]

    def scm = [url: "https://github.com/Grailsrocks/grails-platform-core"]

    boolean platformInitialized

    void initPlatform(application) {
        if (!platformInitialized) {
            // The terrible things we have to do...
            def hackyInstance = org.grails.plugin.platform.config.PluginConfigurationFactory.instance
            hackyInstance.grailsApplication = application
            hackyInstance.pluginManager = org.codehaus.groovy.grails.plugins.PluginManagerHolder.pluginManager
            hackyInstance.applyConfig()

            // Trigger doPlatformBuildInit(pluginManager)
        }

        platformInitialized = true
    }

    /**
     * This happens only when building app, or in dev
     */
    def doWithWebDescriptor = { xml ->
        initPlatform(application)
    }

    /**
     * This happens all the time, but dWWD may not have run if we're in a WAR
     */
    def doWithSpring = {        
        xmlns task: "http://www.springframework.org/schema/task"

        initPlatform(application)

        // Config API
        grailsPluginConfiguration(org.grails.plugin.platform.config.PluginConfigurationFactory) { bean ->
            bean.factoryMethod = 'getInstance'
        }

        def deployed = application.warDeployed
        def grailsVersion = application.metadata['app.grails.version']

        // Security API
        grailsSecurity(org.grails.plugin.platform.security.Security)

        // Injection API
        grailsInjection(org.grails.plugin.platform.injection.Injection) {
            grailsApplication = ref('grailsApplication')
        }
    }

    def doWithDynamicMethods = { ctx ->
        ctx.grailsInjection.initInjections()
    }

    def doWithConfigOptions = {
    }

    def doWithConfig = {
    }

    def doWithInjection = { ctx ->
        def config = ctx.grailsApplication.config

        register ctx.grailsPluginConfiguration.injectedMethods
        register ctx.grailsSecurity.injectedMethods
    }

    def doWithApplicationContext = { applicationContext ->
    }

    def onChange = { event ->
        def ctx = event.application.mainContext
        def config = event.application.config
        switch (event.source) {
            case Object:
                ctx.grailsInjection.applyTo(event.source)
                break
        }
    }
}
