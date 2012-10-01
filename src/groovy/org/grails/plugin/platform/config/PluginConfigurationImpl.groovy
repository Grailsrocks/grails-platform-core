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
package org.grails.plugin.platform.config

import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext

import org.slf4j.LoggerFactory

import grails.util.GrailsNameUtils
import grails.util.Environment

import org.grails.plugin.platform.config.PluginConfigurationEntry
import org.grails.plugin.platform.util.ClosureInvokingScript
import org.grails.plugin.platform.util.PluginUtils

import org.codehaus.groovy.grails.plugins.metadata.GrailsPlugin

import org.codehaus.groovy.grails.commons.GrailsDomainClass

/**
 * Bean for declaring and accessing plugin config 
 * 
 * Plugins declare the config they support, the expected types, validators and default values
 * This means they do not have to supply/merge default values into Config. 
 *
 * The values of these settings provided by the user are read from <app>/grails-app/conf/PluginConfig.groovy
 * so that they can be merged in and validated
 *
 * Config priority is in this order:
 * 1. Values supplied by the application in Config.groovy
 * 2. Values supplied in PluginConfig.groovy
 * 3. Default values specified by the declaring plugin
 *
 */
class PluginConfigurationImpl implements PluginConfiguration, ApplicationContextAware {
    
    static PLUGIN_CONFIG_CLASS = "PluginConfig"
    
    final log = LoggerFactory.getLogger(PluginConfiguration)
    
    def grailsApplication
    def pluginManager
    ApplicationContext applicationContext

    protected List<PluginConfigurationEntry> pluginConfigurationEntries = []
    protected Map<String, PluginConfigurationEntry> legacyPluginConfigurationEntries = [:]

    def injectedMethods = {
        def self = this

        // Apply pluginConfig to all artefacts that come from plugins
        '*' { clazz, artefact ->
            def pluginName = PluginUtils.getNameOfDefiningPlugin(applicationContext, clazz)
            if (pluginName) {
                def pluginConf = self.getPluginConfig(pluginName)
                
                getPluginConfig(staticMethod:artefact instanceof GrailsDomainClass) { ->
                    pluginConf
                }
            }
        }
        
    }
    
    /**
     * Get pluginConfig for any object, determined by the plugin in which is was defined
     */
    ConfigObject getPluginConfigFor(objectInstance) {
        def pluginName = PluginUtils.getNameOfDefiningPlugin(applicationContext, objectInstance)

        if (pluginName) {
            return getPluginConfig(pluginName)
        } else {
            return new ConfigObject()
        }
    }
    
    protected ConfigObject loadPluginConfig() {
        // @todo how to load these from plugins, and to make sure they are included in WAR?
        GroovyClassLoader classLoader = new GroovyClassLoader(PluginConfiguration.classLoader)
		ConfigSlurper slurper = new ConfigSlurper(Environment.getCurrent().getName())
		try {
            log.debug "Loading plugin configuration metadata from ${PLUGIN_CONFIG_CLASS} ..."
			return slurper.parse(classLoader.loadClass(PLUGIN_CONFIG_CLASS))
		}
		catch (ClassNotFoundException e) {
            return new ConfigObject()
		}
    }

    /**
     * Set an app config value using a full string path key
     */
    protected void setConfigValueByPath(String fullPath, value, boolean overwriteExisting = true) {
        def config = grailsApplication.config
        
        def parentConfObj
        def path = fullPath.tokenize('.')
        def valueName
        if (path.size() > 1) {
            valueName = path[-1]
            path = path[0..(path.size()-2)]
        } else {
            valueName = path[0]
            path = []
        }

        log.debug "Config path is $path , value name is $valueName"
        
        // Find the last existing element
        path.find { k -> 
            // Find the nearest end point in the config
            def c = config[k]
            if (c instanceof ConfigObject) {
                config = c
                return false
            } else {
                // We should throw here, its an error...
                return true
            }
        }

        if (overwriteExisting || (config instanceof ConfigObject)) {
            config.putAll([(valueName):value])
        }
    }
    
    /**
     * Return the plugin-specific ConfigObject for the given plugin
     * @param pluginName the BEAN notation name of the plugin e.g. beanFields
     */
    ConfigObject getPluginConfig(String pluginName) {
        grailsApplication.config.plugin[pluginName]
    }
    
    void clearCaches() {
        legacyPluginConfigurationEntries.clear()
        pluginConfigurationEntries.clear()
    }

    void reload() {
        log.debug "Reloading platform configuration..."
        clearCaches()
        applyConfig()
    }
    
    /**
     * Take app config, merge in config from PluginConfig.groovy and then doWithConfig blocks,
     * and validate the whole lot according to doWithConfigOptions
     */
    void applyConfig() {
        log.debug "Applying doWithConfig and doWithConfigOptions..."

        // Get the metadata we need
        loadConfigurationOptions()
        
        // Load up user-supplied plugin configs
        applyAppPluginConfiguration(loadPluginConfig())

        // Copy legacy app config values into plugin namespace
        migrateLegacyConfigValues(true)

        // Let plugins merge in their configs if no explicit setting given by user
        mergeDoWithConfig()
        
        // Copy legacy plugin config values into plugin namespace, if no existing value
        // this is to cover for plugins that use legacy prefixes for plugins that are migrated
        // after the plugin that sets the value is released
        migrateLegacyConfigValues(false)
        
        // Now validate plugin config
        applyPluginConfigurationDefaultValuesAndConstraints()
        log.debug "After applying doWithConfig and doWithConfigOptions, application config is: ${grailsApplication.config}"
        
        verifyConfig()
    }
    
    /**
     * Warn the user if any plugin.x config exists that is not declared by a plugin
     */
    void verifyConfig() {
        def registeredKeys = pluginConfigurationEntries*.fullConfigKey as Set
        def flatAppConf = grailsApplication.config.flatten()
        // @todo we falsely report Map values as invalid config currently as flatten() flattens these too
        flatAppConf.each { k, v ->
            if (k.startsWith('plugin.')) {
                if (!registeredKeys.contains(k) || 
                    ((v instanceof Map) && !registeredKeys.find({ regK -> k.startsWith(regK+'.')})) ) {
                    // @todo should we fail fast here?
                    // @todo support wildcard configoptions
                    log.warn "Your configuration contains a value for [${k}] which is not declared by any plugin"
                }
            }
        }
    }
    
    /** 
     * Find any config values that start with legacy prefixes from doWithConfigOptions
     * and copy them into the correct plugin namespace so that plugins only need to check 
     * the new location
     */
    void migrateLegacyConfigValues(boolean overwriteExisting) {
        def registeredKeys = legacyPluginConfigurationEntries
        def flatAppConf = grailsApplication.config.flatten()
        // Find all configs that are legacy and copy them to the correct plugin scoped config
        flatAppConf.keySet().each { k ->
            def legacyEntry = legacyPluginConfigurationEntries[k]
            if (legacyEntry) {
                def newKey = legacyEntry.fullConfigKey
                def v = flatAppConf.getAt(k)
                // @todo We have to get a new config here as it may have changed during processing
                // Otherwise we get a CME
                def latestConf = grailsApplication.config.flatten()
                def currentValue = latestConf.getAt(newKey)
                if (currentValue != v) {
                    setConfigValueByPath(newKey, v, overwriteExisting)
                    log.warn "Your configuration contains a legacy config value for [${k}]. You should move this to [${newKey}] but it will work for now"
                }
            }
        }
    }
    
    /**
     * Take a Closure and use it as config, returns a ConfigObject
     */
    ConfigObject parseConfigClosure(Closure c) {
        new ConfigSlurper().parse(new ClosureInvokingScript(c))
    }
        
    /**
     * Load cross-plugin doWithConfig configuration and merge into main app config
     */
    void mergeDoWithConfig() {
        if (log.debugEnabled) {
            log.debug "About to merge plugin configs into main Config which is currently: ${grailsApplication.config}"
        }

        def plugins = pluginManager.allPlugins
        // @todo what order is this - plugin dependency order?
        plugins.each { p ->
            def pluginName = GrailsNameUtils.getLogicalPropertyName(p.pluginClass.name, 'GrailsPlugin')
            def inst = p.instance
            if (inst.metaClass.hasProperty(inst, 'doWithConfig')) {
                def confDSL = inst.doWithConfig.clone()

                if (log.debugEnabled) {
                    log.debug "Getting doWithConfig configuration metadata for plugin ${pluginName}"
                }
                def builder = new ConfigBuilder()
                confDSL.delegate = builder
                confDSL(grailsApplication.config.clone()) // Deep clone app config to avoid side effects of querying existing config values
                
                def newConf
                
                // Merge in any non-namespaced app config
                if (builder._applicationConfig) {
                    newConf = parseConfigClosure(builder._applicationConfig)
                } else {
                    newConf = new ConfigObject()
                }
                
                if (newConf.size()) {
                    if (log.debugEnabled) {
                        log.debug "Plugin ${pluginName} added application configuration settings: ${newConf}"
                    }
                }
                
                // @todo run these in plugin dependency order? If two plugins set something on another plugin, which one wins?
                builder._pluginConfigs.each { confPluginName, code -> 
                    // @todo Do we need to a add safety check to prevent plugins configuring themselves (creates ordering problems?)
                    // @todo verify its a real plugin with exposed config
                    def pluginConf = parseConfigClosure(code)
                    newConf.plugin."$confPluginName" = pluginConf

                    if (pluginConf.size()) {
                        if (log.debugEnabled) {
                            log.debug "Plugin ${pluginName} modified plugin configuration for plugin ${confPluginName}: ${pluginConf}"
                        }
                    }
                }

                // Now merge all this into main config, so that values are only replaced
                // if the application did not already set something explicitly
                if (log.debugEnabled) {
                    log.debug "Plugin ${pluginName} config changes being merged into main config: ${newConf}"
                }

                // Do an empty-safe ConfigObject merge
                mergeConfigs(newConf, grailsApplication.config)
            }
        }
    }

    /**
     * Copies values from other into config, only if no value already exists in config
     * Impl works around bug with other containing empty ConfigObject that obliterate non-Map valus in config
     */
    ConfigObject safeConfigMerge(Map config, Map other) {
        for(entry in other) {
            def configEntry = config[entry.key]
            // Non-map and map values that have not been set are just copied
            if(configEntry == null) {
                config[entry.key] = entry.value
                continue
            }
            else {
                // We can only merge maps
                if(configEntry instanceof Map) {
                    if (configEntry.size() > 0 && entry.value instanceof Map) {
                        // recurse
                        safeConfigMerge(configEntry, entry.value)
                    } // We can just assign if empty in config
                    else if (configEntry.size() == 0) {
                        config[entry.key] = entry.value
                    }
                }
            }
        }
        return config
    }

    /**
     * Merge new into old, working around the problems with merging ConfigObjects that already have
     * values or already have empty nodes in the old one
     */
    void mergeConfigs(ConfigObject newConfig, ConfigObject oldConfig) {
        def merged = safeConfigMerge( newConfig, oldConfig)

        if (log.debugEnabled) {
            log.debug "Config merged with app config: ${merged}"
        }

        // put all the merged keys into main config
        if (log.debugEnabled) {
            log.debug "Config changes replacing main config which is: ${oldConfig}"
        }

        oldConfig.putAll( merged)

        if (log.debugEnabled) {
            log.debug "Config changes merged into main config resulted in: ${oldConfig}"
        }
    }

    /**
     * Load plugin config settigns from PluginConfig.groovy and merge into main app config
     */
    void applyAppPluginConfiguration(ConfigObject appPluginConfig) {
        log.debug "Applying user-supplied plugin configuration..."
        def conf = grailsApplication.config
        def flatConf = conf.flatten()
        def pluginConf = appPluginConfig
        def pluginConfFlat = pluginConf.flatten()
        def registeredKeys = pluginConfigurationEntries*.fullConfigKey as Set
        
        pluginConfFlat.each { key, pluginConfValue ->
            if (!registeredKeys.contains(key)) {
                log.warn "Skipping plugin configuration entry ${key}, no plugin configuration has been declared for this."
                return
            }
            // 1. see if there is already an entry defined by the app
            def scopedKey = key
            log.debug "Applying plugin configuration entry ${scopedKey}"
            def value = flatConf[scopedKey]
            def newValue 
            def valueChanged = false
            if (value instanceof ConfigObject) {
                log.debug "Applying plugin configuration entry ${scopedKey}, no value defined by application"
                // 2. if not, see if there is one in the generated plugin conf file
                if (!(pluginConfFlat[scopedKey] instanceof ConfigObject)) {
                    // 3. set the plugin config value into the main config
                    newValue = pluginConfValue
                    valueChanged = true
                    log.debug "Using user-supplied value for ${scopedKey}: [$newValue]"
                }
                if (valueChanged) {
                    // set main config value
                    log.debug "Updating application config for ${scopedKey} to [$newValue]"
                    setConfigValueByPath(scopedKey, newValue)
                    value = newValue
                }
            }
        }
    }
    
    /**
     * Apply plugin-supplied config defaults for declared config values if values are missing
     * and then validate
     */
    void applyPluginConfigurationDefaultValuesAndConstraints() {
        log.debug "Applying plugin configuration constraints..."
        def conf = grailsApplication.config
        def flatConf = conf.flatten()
        
        // Temporarily, we will assum they called register() at start of doWithSpring
        pluginConfigurationEntries.each { entry ->
            def scopedKey = entry.fullConfigKey
            log.debug "Applying plugin configuration entry ${scopedKey}"
            def value = flatConf[scopedKey]
            if ((value instanceof ConfigObject) && value.size() == 0) {
                log.debug "Using plugin default value for ${scopedKey}: [${entry.defaultValue}]"
                value = entry.defaultValue
                setConfigValueByPath(scopedKey, value)
            }

            if (entry.type && (value != null) && !(value instanceof ConfigObject)) { 
                if (!entry.type.isAssignableFrom(value.getClass())) {
                    log.error "Invalid plugin configuration value [${value}] for [$scopedKey], reverting to default value [${entry.defaultValue}] - the value in config is not compatible with the type: ${entry.type}"
                    setConfigValueByPath(scopedKey, entry.defaultValue)
                }
            }
            
            // apply validator
            if (entry.validator) {
                log.debug "Applying plugin config validator for ${scopedKey} to [$value]"
                def msg = entry.validator.call(value)
                if (msg != null) {
                    // @todo Do we fail fast? Probably not, we may want interaction
                    log.error "Invalid plugin configuration value for [$scopedKey], reverting to default value [${entry.defaultValue}] - cause: ${msg}"
                    // Revert to default value
                    setConfigValueByPath(scopedKey, entry.defaultValue)
                }
            }
        }
    }
    
    /**
     * Load up all the doWithConfigOptions metadata
     */
    protected void loadConfigurationOptions() {
        log.debug "Loading plugin configuration metadata..."
        def plugins = pluginManager.allPlugins
        plugins.each { p ->
            def inst = p.instance
            if (inst.metaClass.hasProperty(inst, 'doWithConfigOptions')) {
                log.debug "Getting plugin configuration metadata for plugin ${p.name}"
                def builder = new ConfigOptionsBuilder(
                    pluginName:GrailsNameUtils.getLogicalPropertyName(p.pluginClass.name, 'GrailsPlugin'))
    
                def code = inst.doWithConfigOptions.clone()
                code.delegate = builder
                code()
                
                log.debug "Plugin configuration metadata for plugin ${p.name} yielded entries: ${builder.entries*.fullConfigKey}"
                pluginConfigurationEntries.addAll(builder.entries)

                for (entry in builder.entries) {
                    if (entry.legacyPrefix) {
                        legacyPluginConfigurationEntries[entry.legacyConfigKey] = entry
                    }
                }
            }
        }
    }
    
    /**
     * Get information about all the declared plugin config variables
     */
    List<PluginConfigurationEntry> getAllEntries() {
        pluginConfigurationEntries
    }
}