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
 * Builder for doWithConfig DSL
 */
class ConfigBuilder {
    Closure _applicationConfig
    Map<String, Closure> _pluginConfigs = [:]
    
    def methodMissing(String name, args) {
        assert args.size() == 1
        assert args[0] instanceof Closure
        if (name == 'application') {
            assert _applicationConfig == null
            _applicationConfig = args[0]
        } else {
            // @todo add support for multiple closures / fail fast if called again?
            assert _pluginConfigs[name] == null
            _pluginConfigs[name] = args[0]
        }

        return null
    }
}