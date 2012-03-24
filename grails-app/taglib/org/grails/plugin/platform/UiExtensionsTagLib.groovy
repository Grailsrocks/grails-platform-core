package org.grails.plugin.platform

import org.grails.plugin.platform.util.TagLibUtils

class UiExtensionsTagLib {
    static namespace = "g"
    
    static returnObjectForTags = ['joinClasses']
    
    // @todo OK if the machine stays up over new year this will become invalid...
    def thisYear = new Date()[Calendar.YEAR].toString()

    def grailsUiHelper
    
    def label = { attrs, body ->
        out << "<label"
        def t = getMessageOrBody(attrs, body)
        if (attrs) {
            out << TagLibUtils.attrsToString(attrs)
        }
        out << ">"
        out << t
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
                if (!attrs.value) {
                    attrs.value = text
                }
                out << g.actionSubmit(attrs)
                break;
        }
    }
    
    // @todo move this to TagLibUtils and use messageSource
    protected getMessageOrBody(Map attrs, Closure body) {
        def textCode = attrs.remove('text')
        def textCodeArgs = attrs.remove('textArgs')
        def textFromCode = textCode ? g.message(code:textCode, args:textCodeArgs) : null
        if (textFromCode) {
            textFromCode = textFromCode.encodeAsHTML()
        }
        def v = textFromCode ?: body()
        return v
    }

    def displayMessage = { attrs ->
        def classes = attrs.class ?: ''
        for (scope in [request, flash]) {
            def msgParams = grailsUiHelper.getDisplayMessage(scope)
            if (msgParams) {
                if (attrs.type) {
                    classes = joinClasses(values:[classes, attrs.type])
                } else if (msgParams.type){
                    classes = joinClasses(values:[classes, msgParams.type])
                }
                attrs.code = msgParams.text
                attrs.args = msgParams.args
                attrs.encodeAs = 'HTML'
            
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
    
    def joinClasses = { attrs ->
        StringBuilder res = new StringBuilder()
        def first = true
        attrs.values?.each { v ->
            if (v) {
                if (!first) {
                    res << ' '
                } else {
                    first = false
                }
                res << v
            }
        }
        return res.toString()
    }
    
    def callTag = { attrs, body ->
        def name = attrs.remove('tag')
        def bodyAttr = attrs.remove('bodyContent')
        def (ns, tagName) = TagLibUtils.resolveTagName(name)
        def mergedAttrs
        if (attrs.attrs != null) {
            mergedAttrs = attrs.remove('attrs')
        }
        if (mergedAttrs) {
            mergedAttrs.putAll(attrs)
        } else {
            mergedAttrs = attrs
        }
        def taglib = this[ns]
        out << taglib."${tagName}"(mergedAttrs, bodyAttr ?: body)
    }    
}