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
    
    def methodMissing(String name, args) {
        def e = new PluginConfigurationEntry()
        assert args.size() <= 1
        e.plugin = pluginName
        e.key = name
        if (args.size()) {
            def params = args[0]
            e.defaultValue = params.defaultValue
            e.type = params.type
            if (params.validator instanceof Closure) {
                e.validator = params.validator
            }
        }
        entries << e
        return null
    }
}

