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

/**
 * Public interface for the Plugin Configuration API
 * 
 * Plugins declare the config they support, the expected types, validators and default values
 * This means they do not have to supply/merge default values into Config. 
 *
 */
interface PluginConfiguration {
    
    /**
     * Get pluginConfig for any object, determined by the plugin in which is was defined
     */
    ConfigObject getPluginConfigFor(objectInstance)

    /**
     * Return the plugin-specific ConfigObject for the given plugin
     * @param pluginName the BEAN notation name of the plugin e.g. beanFields
     */
    ConfigObject getPluginConfig(String pluginName);
    
    /**
     * Get information about all the declared plugin config variables
     */
     List<PluginConfigurationEntry> getAllEntries()
}

