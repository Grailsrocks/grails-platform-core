package org.grails.plugin.platform

class UiHelperTagLib {
    static namespace = "g"
    
    def grailsUiHelper
    
    def displayMessage = { attrs ->
        for (scope in [request, flash]) {
            def msgParams = grailsUiHelper.getDisplayMessage(scope)
            if (msgParams) {
                if (attrs.type) {
                    msgType = attrs.type
                }
                attrs.code = msg
                attrs.args = msgArgs
            
                out << "<div class=\"${msgType.encodeAsHTML()}\">"
                out << g.message(attrs)
                out << "</div>"
            }
        }
    }
}