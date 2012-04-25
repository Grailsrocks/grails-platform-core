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