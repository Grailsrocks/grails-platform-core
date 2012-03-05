package org.grails.plugin.platform

class PlatformTagLib {
    static namespace = "p"
    
    def uiOverlay = { attrs, body ->
        out << r.require(module:'plugin.pluginPlatform.tools')
        
        out << g.render(plugin:'pluginPlatform', template:'/platformTools/uiOverlay', model:[bodyContent:body])
    }
}