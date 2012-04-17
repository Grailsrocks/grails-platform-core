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

import org.grails.plugin.platform.events.dispatcher.DefaultEventsDispatcher
import org.grails.plugin.platform.events.dispatcher.GormTopicSupport1X
import org.grails.plugin.platform.events.dispatcher.GormTopicSupport2X
import org.grails.plugin.platform.events.publisher.DefaultEventsPublisher
import org.grails.plugin.platform.events.registry.DefaultEventsRegistry
import org.springframework.core.io.FileSystemResource

class PlatformCoreGrailsPlugin {
    // the plugin version
    def version = "1.0.M2-SNAPSHOT"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3 > *"
    // the other plugins this plugin depends on
    def dependsOn = [
            resources: '1.1.6 > *'
    ]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/conf/TestResources.groovy",
            "grails-app/conf/TestNavigation.groovy",
            "grails-app/i18n/test.properties",
            "grails-app/domain/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/controllers/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/services/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/src/groovy/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/src/java/org/grails/plugin/platform/test/**/*.java",
            "grails-app/views/error.gsp",
            "grails-app/views/test/**/*.gsp",
    ]

    def observe = ['*'] // We observe everything so we can re-apply dynamic methods, conventions etc

    def watchedResources = [
        "file:./grails-app/conf/*Navigation.groovy",
        "file:./plugins/*/grails-app/conf/*Navigation.groovy"
    ]

    def artefacts = [getNavigationArtefactHandler()]

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
            grailsApplication = ref('grailsApplication')
        }

        def deployed = application.warDeployed
        def grailsVersion = application.metadata['app.grails.version']

        // Security API
        grailsSecurity(org.grails.plugin.platform.security.Security)

        // Injection API
        grailsInjection(org.grails.plugin.platform.injection.InjectionImpl) {
            grailsApplication = ref('grailsApplication')
        }

        // Navigation API
        grailsNavigation(org.grails.plugin.platform.navigation.NavigationImpl) {
            grailsApplication = ref('grailsApplication')
            grailsConventions = ref('grailsConventions')
        }

        // Navigation API
        grailsConventions(org.grails.plugin.platform.conventions.ConventionsImpl) {
            grailsApplication = ref('grailsApplication')
        }

        // Events API
        task.executor(id: "grailsTopicExecutor", 'pool-size': 10)//todo config

        // UI Helper API
        grailsUiHelper(org.grails.plugin.platform.ui.UiHelper) 
        
        //init api bean
        if (grailsVersion.startsWith('1')) {
            gormTopicSupport(GormTopicSupport1X)
        } else {
            gormTopicSupport(GormTopicSupport2X) {
                translateTable = [
                        'PreInsertEvent': 'beforeInsert', 'PreUpdateEvent': 'beforeUpdate', 'PreLoadEvent': 'beforeLoad',
                        'PreDeleteEvent': 'beforeDelete', 'ValidationEvent': 'beforeValidate', 'PostInsertEvent': 'afterInsert',
                        'PostUpdateEvent': 'afterUpdate', 'PostDeleteEvent': 'afterDelete', 'PostLoadEvent': 'afterLoad',
                        'SaveOrUpdateEvent': 'onSaveOrUpdate'
                ]
            }
        }

        grailsEventsRegistry(DefaultEventsRegistry)
        grailsEventsPublisher(DefaultEventsPublisher) {
            grailsEventsRegistry = ref('grailsEventsRegistry')
            taskExecutor = ref('grailsTopicExecutor')
            persistenceInterceptor = ref("persistenceInterceptor")
            gormTopicSupport = ref("gormTopicSupport")
        }
        grailsEventsDispatcher(DefaultEventsDispatcher)

        grailsEvents(org.grails.plugin.platform.events.Events) {
            grailsApplication = ref('grailsApplication')
            grailsEventsRegistry = ref('grailsEventsRegistry')
            grailsEventsPublisher = ref('grailsEventsPublisher')
            grailsEventsDispatcher = ref('grailsEventsDispatcher')
        }
    }

    def doWithDynamicMethods = { ctx ->
        ctx.grailsInjection.initInjections()
        ctx.grailsEvents.initListeners()
    }

    def doWithConfigOptions = {
        'organization.name'(type: String, defaultValue: 'My Corp (set plugin.platformCore.organization.name)')
        'site.name'(type: String, defaultValue: 'Our App (set plugin.platformCore.site.name)')
        'site.url'(type: String, defaultValue: null)

        'show.startup.info'(type: Boolean, defaultValue: true)
    }

    def doWithConfig = {
    }

    def doWithInjection = { ctx ->
        def config = ctx.grailsApplication.config

        register ctx.grailsPluginConfiguration.injectedMethods
        register ctx.grailsSecurity.injectedMethods
        register ctx.grailsEvents.injectedMethods
        register ctx.grailsUiHelper.injectedMethods
    }

    def doWithApplicationContext = { applicationContext ->
    }

    def onConfigChange = { ctx ->
        ctx.grailsPluginConfiguration.reload()
    }
    
    def onChange = { event ->
        def ctx = event.application.mainContext
        def config = event.application.config

        def navArtefactType = getNavigationArtefactHandler().TYPE
        if (application.isArtefactOfType(navArtefactType, event.source)) {

            // Update the app with the new class
            event.application.addArtefact(navArtefactType, event.source)
            ctx.grailsNavigation.reload(event.source)

        } else if (application.isArtefactOfType('Controller', event.source)) {

            ctx.grailsNavigation.reload() // conventions on controller may have changed

        } else {
            switch (event.source) {
                case Class:
                    ctx.grailsInjection.applyTo(event.source)

                    if (application.isServiceClass(event.source)) {
                        ctx.grailsEvents.reloadListener(event.source)
                    }
                    break
            }
        }
    }
    
    
    static getNavigationArtefactHandler() {
        softLoadClass('org.grails.plugin.platform.navigation.NavigationArtefactHandler')
    }

    static softLoadClass(String className) {
        try {
            getClassLoader().loadClass(className)
        } catch (ClassNotFoundException e) {
            println "ERROR: Could not load $className"
            null
        }
    }
}
