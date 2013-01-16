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
import org.grails.plugin.platform.events.EventsImpl
import org.grails.plugin.platform.events.dispatcher.GormTopicSupport1X
import org.grails.plugin.platform.events.dispatcher.GormTopicSupport2X
import org.grails.plugin.platform.events.publisher.DefaultEventsPublisher
import org.grails.plugin.platform.events.publisher.GormBridgePublisher
import org.grails.plugin.platform.events.registry.DefaultEventsRegistry

class PlatformCoreGrailsPlugin {
    // the plugin version
    def version = "1.0.RC5"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3 > *"

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/conf/Test*.groovy",
            "grails-app/i18n/test.properties",
            "grails-app/domain/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/controllers/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/services/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/src/groovy/org/grails/plugin/platform/test/**/*.groovy",
            "grails-app/src/java/org/grails/plugin/platform/test/**/*.java",
            "grails-app/views/error.gsp",
            "grails-app/views/test/**/*.gsp"
    ]

    def observe = ['*'] // We observe everything so we can re-apply dynamic methods, conventions etc

    def watchedResources = [
            "file:./grails-app/conf/*Navigation.groovy",
            "file:./grails-app/conf/*Events.groovy",
            "file:./plugins/*/grails-app/conf/*Navigation.groovy",
            "file:./plugins/*/grails-app/conf/*Events.groovy"
    ]

    def artefacts = [getNavigationArtefactHandler(), getEventsArtefactHandler()]

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
            [name: "Stéphane Maldini", email: "smaldini@vmware.com"]
    ]

    def issueManagement = [system: "JIRA", url: "http://jira.grails.org/browse/GPPLATFORMCORE"]

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
        def config = application.config.plugin.platformCore

        // Config API
        grailsPluginConfiguration(org.grails.plugin.platform.config.PluginConfigurationFactory) { bean ->
            bean.factoryMethod = 'getInstance'
            grailsApplication = ref('grailsApplication')
        }

        def deployed = application.warDeployed
        def grailsVersion = application.metadata['app.grails.version']

        // Security API
        if (!config.security.disabled) {
            grailsSecurity(org.grails.plugin.platform.security.SecurityImpl)
        }

        // Injection API
        grailsInjection(org.grails.plugin.platform.injection.InjectionImpl) {
            grailsApplication = ref('grailsApplication')
        }

        // Navigation API
        if (!config.navigation.disabled) {
            grailsNavigation(org.grails.plugin.platform.navigation.NavigationImpl) {
                grailsApplication = ref('grailsApplication')
                grailsConventions = ref('grailsConventions')
            }
        }

        // Convention API
        grailsConventions(org.grails.plugin.platform.conventions.ConventionsImpl) {
            grailsApplication = ref('grailsApplication')
        }

        // UI Helper API
        if (!config.ui.disabled) {
            grailsUiExtensions(org.grails.plugin.platform.ui.UiExtensions)
        }
        
        // Events API
        if (!config.events.disabled) {
            task.executor(id: "grailsTopicExecutor", 'pool-size': config.events.poolSize)

            //init api bean
            grailsEventsRegistry(DefaultEventsRegistry)
            grailsEventsPublisher(DefaultEventsPublisher) {
                grailsEventsRegistry = ref('grailsEventsRegistry')
                persistenceInterceptor = ref("persistenceInterceptor")
                catchFlushExceptions = config.events.catchFlushExceptions
            }

            grailsEvents(EventsImpl) {
                grailsApplication = ref('grailsApplication')
                grailsEventsRegistry = ref('grailsEventsRegistry')
                grailsEventsPublisher = ref('grailsEventsPublisher')
            }

            if (!config.events.gorm.disabled) {
                if (grailsVersion.startsWith('1')) {
                    gormTopicSupport(GormTopicSupport1X)
                } else {
                    gormTopicSupport(GormTopicSupport2X) {
                        translateTable = [
                                'PreInsertEvent': 'beforeInsert', 'PreUpdateEvent': 'beforeUpdate', /*'PreLoadEvent': 'beforeLoad',*/
                                'PreDeleteEvent': 'beforeDelete', 'ValidationEvent': 'beforeValidate', 'PostInsertEvent': 'afterInsert',
                                'PostUpdateEvent': 'afterUpdate', 'PostDeleteEvent': 'afterDelete', /*'PostLoadEvent': 'afterLoad',*/
                                'SaveOrUpdateEvent': 'onSaveOrUpdate'
                        ]
                    }
                }
                grailsEventsGormBridge(GormBridgePublisher) {
                    gormTopicSupport = ref("gormTopicSupport")
                    grailsEvents = ref("grailsEvents")
                }

            }
        }
    }

    def doWithDynamicMethods = { ctx ->
        def config = ctx.grailsApplication.config.plugin.platformCore
        ctx.grailsInjection.initInjections()

        if (!config.events.disabled)
            ctx.grailsEvents.reloadListeners()
    }

    def doWithConfigOptions = {
        legacyPrefix = 'grails.plugin.platform'

        'organization.name'(type: String, defaultValue: 'My Corp (set plugin.platformCore.organization.name)')
        'site.name'(type: String, defaultValue: 'Our App (set plugin.platformCore.site.name)')
        'site.url'(type: String, defaultValue: null)

        'show.startup.info'(type: Boolean, defaultValue: true)

        'navigation.disabled'(type: Boolean, defaultValue: false)

        'events.disabled'(type: Boolean, defaultValue: false)
        'events.poolSize'(type: Integer, defaultValue: 10)
        'events.catchFlushExceptions'(type: Boolean, defaultValue: true)
        'events.gorm.disabled'(type: Boolean, defaultValue: false)

        'security.disabled'(type: Boolean, defaultValue: false)

        'ui.disabled'(type: Boolean, defaultValue: false)
    }

    def doWithConfig = {
    }

    def doWithInjection = { ctx ->
        def config = ctx.grailsApplication.config.plugin.platformCore

        if (!config.security.disabled) {
            register ctx.grailsSecurity.injectedMethods
        }
        if (!config.config.disabled) {
            register ctx.grailsPluginConfiguration.injectedMethods
        }
        if (!config.events.disabled) {
            register ctx.grailsEvents.injectedMethods
        }
        if (!config.ui.disabled) {
            register ctx.grailsUiExtensions.injectedMethods
        }
    }

    def doWithApplicationContext = { applicationContext ->
    }

    def onConfigChange = { event ->
        event.application.mainContext.grailsPluginConfiguration.reload()
    }

    def onChange = { event ->
        def ctx = event.application.mainContext
        def config = event.application.config.plugin.platformCore

        def navArtefactType = getNavigationArtefactHandler().TYPE
        def eventArtefactType = getEventsArtefactHandler().TYPE

        if (event.source instanceof Class) {
            if (!config.navigation.disabled && application.isArtefactOfType(navArtefactType, event.source)) {
                // Update the app with the new class
                event.application.addArtefact(navArtefactType, event.source)
                ctx.grailsNavigation.reload(event.source)

            } else if (!config.events.disabled && application.isArtefactOfType(eventArtefactType, event.source)) {
                event.application.addArtefact(eventArtefactType, event.source)
                ctx.grailsEvents.reloadListeners()
            }
            else if (!config.navigation.disabled && application.isArtefactOfType('Controller', event.source)) {
                ctx.grailsNavigation.reload() // conventions on controller may have changed
            } else if (!config.events.disabled && application.isServiceClass(event.source)) {
                ctx.grailsEvents.reloadListener(event.source)
            }

            // Always do this stuff
            ctx.grailsInjection.applyTo(event.source)

        }
    }


    static getNavigationArtefactHandler() {
        softLoadClass('org.grails.plugin.platform.navigation.NavigationArtefactHandler')
    }

    static getEventsArtefactHandler() {
        softLoadClass('org.grails.plugin.platform.events.EventsArtefactHandler')
    }

    static softLoadClass(String className) {
        try {
            this.getClassLoader().loadClass(className)
        } catch (ClassNotFoundException e) {
            println "ERROR: Could not load $className"
            null
        }
    }
}
