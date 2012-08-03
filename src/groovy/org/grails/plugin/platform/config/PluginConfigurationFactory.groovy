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
 * This is really ugly hack to allow us to init before spring context is done.
 * We have to be able to init before Spring so that merged config is available
 * everywhere it should be.
 */
class PluginConfigurationFactory {
    static private PluginConfiguration instance = new PluginConfigurationImpl()
    
    static PluginConfiguration getInstance() {
        instance
    }
}