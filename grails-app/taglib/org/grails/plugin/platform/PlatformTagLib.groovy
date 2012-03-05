package org.grails.plugin.platform

class PlatformTagLib {
    static namespace = "p"
    
    def uiOverlay = { attrs, body ->
        out << r.require(module:'plugin.platformCore.tools')
        
        out << g.render(plugin:'platformCore', template:'/platformTools/uiOverlay', model:[bodyContent:body])
    }
}