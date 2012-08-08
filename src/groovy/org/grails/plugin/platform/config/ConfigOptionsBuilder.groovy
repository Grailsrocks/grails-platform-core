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
 * Builder for the doWithConfigOptions DSL
 * Supports simple method calls like this:
 *
 * someConfigValue(default:99)
 * 'someOther.value.name.here'(type:String)
 *
 * Also supports a single property set to set the prefix to copy values from old pre-platform config:
 *
 * legacyPrefix = "searchable"
 */
class ConfigOptionsBuilder {
    List<PluginConfigurationEntry> entries = []

    String pluginName
    
    String legacyPrefix
    
    def methodMissing(String name, args) {
        def e = new PluginConfigurationEntry()
        assert args.size() <= 1
        e.plugin = pluginName
        e.key = name
        if (args.size()) {
            def params = args[0]
            e.defaultValue = params.defaultValue
            e.type = params.type
            e.legacyPrefix = legacyPrefix
            if (params.validator instanceof Closure) {
                e.validator = params.validator
            }
        }
        entries << e
        return null
    }
}

