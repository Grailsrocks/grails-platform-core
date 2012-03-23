package org.grails.plugin.platform

class UiExtensionsTagLib {
    static namespace = "g"
    
    // @todo OK if the machine stays up over new year this will become invalid...
    def thisYear = new Date()[Calendar.YEAR].toString()

    def grailsUiHelper
    
    def label = { attrs, body ->
        out << "<label "
        if (attrs) {
            out << TagLibUtils.attrsToString(attrs)
        }
        out << ">"
        out << getMessageOrBody(attrs, body)
        out << "</label>"
    }
    
    def button = { attrs, body ->
        def kind = attrs.remove('kind') ?: 'button'
        def text = getMessageOrBody(attrs, body)
        switch (kind) {
            case 'button':
                out << "<button"
                if (attrs) {
                    out << TagLibUtils.attrsToString(attrs)
                }
                out << ">${text}</button>"
                break;
            case 'anchor':
                if (!attrs.'class') {
                    attrs.'class' = "button"
                }
                out << g.link(attrs, text)
                break;
            case 'submit':
                attrs.value = text
                out << g.actionSubmit(attrs)
                break;
        }
    }
    
    // @todo move this to TagLibUtils and use messageSource
    protected getMessageOrBody(Map attrs, Closure body) {
        def textCode = attrs.remove('text')
        def v = textCode ? g.message(code:textCode, encodeAs:'HTML') : body()
        return v
    }

    def displayMessage = { attrs ->
        def classes = attrs.class ?: ''
        for (scope in [request, flash]) {
            def msgParams = grailsUiHelper.getDisplayMessage(scope)
            if (msgParams) {
                if (attrs.type) {
                    classes = classes + " ${attrs.type}"
                }
                attrs.code = msg
                attrs.args = msgArgs
            
                out << "<div class=\"${classes.encodeAsHTML()}\">"
                out << g.message(attrs)
                out << "</div>"
            }
        }
    }
    
    def smartLink = { attrs ->
        def con = attrs.controller ?: controllerName
        def defaultAction = 'index' // make this ask the artefact which is default
        def act = attrs.action ?: defaultAction
        def text = g.message(code:"action.${con}.${act}", encodeAs:'HTML')
        out << g.link(attrs, text)
    }
    
    def company = { attrs ->
        def codec = attrs.encodeAs ?: 'HTML'
        def s = pluginConfig.company.name
        out << (codec != 'none' ? s."encodeAs$codec"() : s)
    }
    
    def siteName = { attrs ->
        def codec = attrs.encodeAs ?: 'HTML'
        def s = pluginConfig.site.name
        out << (codec != 'none' ? s."encodeAs$codec"() : s)
    }
    
    def siteLink = { attrs ->
        out << g.link(absolute:'true', uri:'/') { 
            out << g.siteName()
        }
    }
    
    def siteURL = { attrs ->
        out << g.createLink(absolute:'true', uri:'/')
    }
    
    def year = { attrs ->
        out << thisYear
    }        
}