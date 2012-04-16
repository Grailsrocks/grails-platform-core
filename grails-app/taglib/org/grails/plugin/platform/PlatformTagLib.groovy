package org.grails.plugin.platform

import org.grails.plugin.platform.util.TagLibUtils

class PlatformTagLib {
    static namespace = "p"
    
    def uiOverlay = { attrs, body ->
        out << r.require(module:'plugin.platformCore.tools')
        
        out << g.render(plugin:'platformCore', template:'/platformTools/uiOverlay', model:[bodyContent:body])
    }
    
    def tagDemo = { attrs, body ->
        if (!attrs.tag) return
        
        def (ns, tagName) = TagLibUtils.resolveTagName(attrs.tag)
        def bodyAlone = body()
        def tagAttrs = attrs.clone()
        tagAttrs.remove('tag')
        def markup = "<${ns}:${tagName}${TagLibUtils.attrsToString(tagAttrs)}"
        if (bodyAlone) {
            markup += ">${bodyAlone.encodeAsHTML()}</${ns}:${tagName}>"
        } else {
            markup += "/>"
        }
        def b = p.callTag(attrs, body)
        out << "<div class=\"tag-demo\">"
        out << "<div class=\"tag-source\"><strong>GSP code:</strong><pre>\n"+markup.encodeAsHTML()+"</pre></div>"
        out << "<div class=\"tag-result\"><strong>Result:</strong> ${b}</div>"
        out << "<div class=\"tag-output\"><strong>Output:</strong><pre>\n"+b.encodeAsHTML()+"</pre></div>"
        out << "</div>"
    }
}