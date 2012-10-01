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
package org.grails.plugin.platform

import org.grails.plugin.platform.util.TagLibUtils
import org.grails.plugin.platform.util.PluginUtils
import grails.util.GrailsNameUtils

class UiExtensionsTagLib {
    static namespace = "p"
    
    static returnObjectForTags = ['joinClasses']
    
    // @todo OK if the machine stays up over new year this will become invalid...
    def thisYear = new Date()[Calendar.YEAR].toString()

    def grailsUiExtensions
    
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
        if (log.debugEnabled) {
            log.debug "p:button called with $attrs"
        }
        def kind = attrs.remove('kind') ?: 'button'
        def text = getMessageOrBody(attrs, body)
        def dis = attrs.remove('disabled')?.toBoolean()
        switch (kind) {
            case 'button':
                out << "<button"
                if (dis) {
                    attrs.disabled = "disabled"
                }
                if (attrs) {
                    out << TagLibUtils.attrsToString(attrs)
                }
                out << ">${text}</button>"
                break;
            case 'anchor':
                if (!attrs.'class') {
                    attrs.'class' = "button"
                }
                if (dis) {
                    attrs.'class' = p.joinClasses(values:["disabled", attrs.'class'])
                }
                out << g.link(attrs, text)
                break;
            case 'submit':
                attrs.value = text
                if (dis) {
                    attrs.disabled = "disabled"
                }
                out << g.actionSubmit(attrs)
                break;
        }
    }
    
    // @todo move this to TagLibUtils and use messageSource
    protected getMessageOrBody(Map attrs, Closure body) {
        def textCode = attrs.remove('text')
        def textCodeArgs = attrs.remove('textArgs')
        def textScope = attrs.remove('textScope')
        def textPlugin = attrs.remove('textPlugin')
        def textFromCode = textCode ? p.text(
            code:textCode, 
            default:null, // force null so we can detect it and only execute body if need be
            scope:textScope, 
            plugin:textPlugin, 
            args:textCodeArgs) : null
        if (textFromCode?.toString()) {
            return textFromCode.encodeAsHTML()
        } else {
            // Use body but IF it is blank, fall back to smart i18n code as a visible placeholder
            return body() ?: p.text(
                code:textCode, 
                scope:textScope,
                plugin:textPlugin, 
                args:textCodeArgs)
        }
    }

    def displayMessage = { attrs ->
        def classes = attrs.class ?: ''
        def classPrefix = attrs.cssPrefix ?: ''
        def searchScopes = [
            grailsUiExtensions.getPluginRequestAttributes('platformCore'), 
            grailsUiExtensions.getPluginFlash('platformCore')
        ]
        for (scope in searchScopes) {
            def msgParams = grailsUiExtensions.getDisplayMessage(scope)
            if (msgParams) {
                if (attrs.type) {
                    classes = joinClasses(values:[classes, classPrefix+attrs.type])
                } else if (msgParams.type){
                    classes = joinClasses(values:[classes, classPrefix+msgParams.type])
                }
                def msgAttrs = [
                    code: msgParams.text,
                    args: msgParams.args,
                    encodeAs:'HTML'
                ]
            
                out << "<div class=\"${classes.encodeAsHTML()}\">"
                // Message is already namespaced
                out << g.message(msgAttrs)
                out << "</div>"
            }
        }
    }
    
    def smartLink = { attrs, body ->
        def con = attrs.controller ?: controllerName
        def defaultAction = 'index' // make this ask the artefact which is default
        def act = attrs.action ?: defaultAction
        attrs.text = "action.${con}.${act}"
        def text = getMessageOrBody(attrs, body)
        out << g.link(attrs, text)
    }
    
    def organization = { attrs ->
        def codec = attrs.encodeAs ?: 'HTML'
        def s = pluginConfig.organization.name
        out << (codec != 'none' ? s."encodeAs$codec"() : s)
    }
    
    def siteName = { attrs ->
        def codec = attrs.encodeAs ?: 'HTML'
        def s = pluginConfig.site.name
        out << (codec != 'none' ? s."encodeAs$codec"() : s)
    }
    
    def siteLink = { attrs ->
        attrs.url = p.siteURL(attrs)
        out << g.link(attrs) { 
            out << p.siteName([:])
        }
    }
    
    def siteURL = { attrs ->
        def linkArgs = pluginConfig.site.url ? 
            [url:pluginConfig.site.url] : 
            [absolute:true, uri:'/']

        out << g.createLink(linkArgs)
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
    
    /**
     * Allows a GSP to call another tag, passing it attributes at runtime without listing them
     * in the GSP
     */
    def callTag = { attrs, body ->
        def name = attrs.remove('tag')
        if (name.indexOf('.') != -1) {
            throwTagError "The [tag] attribute of [p:callTag] must use the colon namespace form, not period form"
        }
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
    
    /**
     * Write out an attribute and value only if the value is non-null
     * @attr value Value of the attribute
     * @attr name Name of the attribute i.e. "width"
     */
    def attrIfSet = { attrs ->
        if (attrs.value != null) {
            out << "${attrs.name}=\"${attrs.value.encodeAsHTML()}\""
        }
    }
    
    
    /** 
     * Get i18n text string, like g:message but with some attrib changes and code namespaced by plugin that declared GSP
     * Attributes:
     * @attr code The i18n code
     * @attr args The i18n args (optional)
     * 
     * Body is the default text if code does not resolve.
     */
    def text = { attrs, body ->
        if (log.debugEnabled) {
            log.debug "p:text called with attrs $attrs"
        }
        def i18nscope = attrsToTextScope(attrs) ?: pageScope['plugin.platformCore.ui.text.scope']
        if (!i18nscope) {
            def pluginPath = pageScope.pluginContextPath
            def pluginPathMatcher = pluginPath =~ '/plugins/(.+)-[\\d]+.*$'
            def appPlugin = PluginUtils.findAppPlugin(grailsApplication.mainContext)
            if (pluginPathMatcher.matches() || appPlugin) {
                def pluginName = pluginPathMatcher.matches() ? 
                    GrailsNameUtils.getPropertyNameForLowerCaseHyphenSeparatedName(pluginPathMatcher[0][1]) :
                    appPlugin.name
                i18nscope = "plugin.${pluginName}"
            }
        }

        def defaultText = attrs.containsKey('default') ? attrs.default : body()
        if (log.debugEnabled) {
            log.debug "p:text default text will be [$defaultText]"
        }
        def codes = attrs.error ? attrs.error.codes : (attrs.codes ?: [attrs.code])
        if (!codes && !defaultText) {
            throwTagError "The attributes [codes], [code] and [default] - as well as the body are all empty. This tag is for rendering text!"
        }

        if (i18nscope) {
            for (code in codes) {    
                def namespacedCode = "${i18nscope}.${code}"
                if (log.debugEnabled) {
                    log.debug "Resolving scoped i18n message from scope [${i18nscope}] using code [${namespacedCode}]"
                }
                if (!defaultText && !attrs.containsKey('default')) {
                    defaultText = namespacedCode
                }

                if (log.debugEnabled) {
                    log.debug "Attempting to resolve scoped i18n message code [${namespacedCode}]"
                }
                def msg = g.message(code:namespacedCode, args:attrs.args, default:null, encodeAs:attrs.encodeAs)
                if (log.debugEnabled) {
                    log.debug "Attempt to resolve scoped i18n message code [${namespacedCode}] yielded: ${msg}"
                }
                if (msg) {
                    if (log.debugEnabled) {
                        log.debug "Resolved scoped i18n message code [${namespacedCode}] and returning ${msg}"
                    }
                    out << msg
                    return
                }
            }
            if (log.debugEnabled) {
                log.debug "Failed to resolve scoped i18n message codes, returning default ${defaultText}"
            }
            if (defaultText) {
                out << defaultText
            }

        } else {
            if (!defaultText && !attrs.containsKey('default') ) {
                defaultText = codes[0]
            }            

            for (code in codes) {    
                if (log.debugEnabled) {
                    log.debug "Attempting to resolve i18n message code [${code}]"
                }
                def msg = g.message(code:code, args:attrs.args, default:null, encodeAs:attrs.encodeAs)
                if (log.debugEnabled) {
                    log.debug "Attempt to resolve unscoped i18n message code [${code}] yielded: ${msg}"
                }
                if (msg) {
                    out << msg
                    return
                }
            }
            if (defaultText) {
                out << defaultText
            }
        }
    }

    private String attrsToTextScope(attrs) {
        attrs.plugin ? "plugin.${attrs.plugin}" : attrs.scope
    }

    /**
     * Set the scope of p:text i18n codes for the duration of this request
     * Used by GSPs in apps that override plugin GSPs, or just to scope all the i18n safely in an scenario
     */
    def textScope = { attrs ->
        def scope = attrsToTextScope(attrs)
        pageScope['plugin.platformCore.ui.text.scope'] = scope ?: null
    }
 
    def dummyText = { attrs -> 
        def n = attrs.size ? attrs.size.toString().toInteger() : 0
        if (!n) {
            n = 1
        }
        int i = request['plugin.platformCore.ipsum.counter'] ?: (int)0
        while (n-- > 0) {
            out << '<p>'
            out << DUMMY_TEXT[i++]
            out << '</p>'
            if (i >= DUMMY_TEXT.size()) {
                i = 0
            }
        }
        request['plugin.platformCore.ipsum.counter'] = i
    }

    @Lazy List DUMMY_TEXT = """
Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nam eu nulla. Donec lobortis purus vel urna. Nunc laoreet lacinia nunc. In volutpat sodales ipsum. Sed vestibulum. Integer in ante. Sed posuere ligula rhoncus erat. Fusce urna dui, sollicitudin ac, pulvinar quis, tincidunt et, risus. Quisque a nunc eget nibh interdum fringilla. Fusce dapibus odio in est. Nunc egestas mauris ac leo. Nullam orci.
Morbi volutpat leo in ligula. Integer vel magna. Quisque ut magna et nisi bibendum sagittis. Fusce elit ligula, sodales sit amet, tincidunt in, ullamcorper condimentum, lectus. Aliquam ut massa. Suspendisse dolor. Cras quam augue, consectetuer id, auctor ut, tincidunt a, velit. Quisque euismod tortor sed nulla. Nunc dapibus, nisi et iaculis feugiat, leo ipsum venenatis enim, a nonummy magna ante vitae diam. Proin sapien. Duis eleifend. Praesent tempor velit molestie neque. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Sed mollis justo eget augue. Donec tempus, urna a congue ultrices, lacus magna convallis nulla, non ultrices metus justo et purus. In leo lorem, dapibus at, volutpat sed, posuere a, justo. Donec varius, erat in placerat pharetra, lorem est gravida erat, nec accumsan turpis erat sed velit. Duis malesuada, lacus sit amet dictum lobortis, arcu velit sodales lectus, ac mattis ipsum lacus in magna. Curabitur sed ante ac enim consequat porttitor. Suspendisse bibendum turpis a magna.
Fusce interdum. Maecenas eu elit sed nulla dignissim interdum. Sed laoreet. Aenean pede. Phasellus porta. Ut dictum nonummy diam. Sed a leo. Cras ullamcorper nibh. Sed laoreet. Praesent vehicula suscipit ligula. Morbi ullamcorper.
Nam justo augue, dictum a, hendrerit in, ultricies in, leo. Nullam eleifend. Duis tempor ipsum vitae diam. Curabitur felis dui, bibendum vitae, luctus quis, volutpat sed, orci. Cras vulputate ullamcorper ante. Sed congue libero in orci. Vivamus a odio ac sapien dignissim posuere. Sed mollis ipsum id libero. Quisque vitae justo. Nulla vitae mauris. Phasellus convallis ligula in nulla. Morbi aliquet, velit ac semper iaculis, mi odio dignissim diam, id dapibus eros metus id nisi. Nulla vitae sapien. Nulla ligula. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Curabitur non nisl id ante egestas dapibus. Sed mollis ornare ipsum. In id enim dignissim erat viverra vulputate.
Aenean sit amet massa. Nam mattis enim ut elit. Phasellus pretium ornare lorem. Maecenas non orci. Fusce cursus eleifend mi. Suspendisse egestas, sem id pellentesque nonummy, lacus odio scelerisque est, in aliquet erat libero sed velit. Aenean volutpat, risus eget malesuada pulvinar, neque felis malesuada tellus, quis interdum nisl ante et dui. Sed quis elit ac leo interdum viverra. Ut nonummy ultricies est. Sed arcu enim, luctus sed, ornare sed, vestibulum at, eros. Donec malesuada urna a risus. Vestibulum nonummy tincidunt elit. Sed faucibus suscipit erat.
Nunc mollis. Aliquam erat volutpat. Aliquam quis ante in ipsum auctor viverra. Donec sapien. Duis rhoncus placerat massa. Aenean justo urna, egestas eu, tempus vitae, rutrum in, pede. Quisque luctus. Sed nulla. Maecenas in tellus ut ipsum ullamcorper placerat. In tempor venenatis libero. Duis bibendum pharetra neque. Donec porta. Integer ipsum. Morbi in orci. Phasellus id turpis. Sed massa. Suspendisse rhoncus quam vitae sapien laoreet fringilla. Ut fermentum. Nam eu purus eu massa rutrum congue.
Praesent tincidunt, odio et rutrum sagittis, orci dolor iaculis nisl, ut tempus nisi mi a lacus. Quisque erat lorem, cursus et, molestie ac, sollicitudin tincidunt, orci. Ut eget est. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nam aliquet mauris id dolor. Ut eget sapien ac diam dignissim convallis. Sed pulvinar. Aenean tincidunt aliquam nisi. Nulla pharetra. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Vestibulum pretium, dolor vitae ultrices ullamcorper, mi erat bibendum erat, id sagittis quam urna sed nisi.
In convallis porttitor nisi. Mauris et mi a diam pulvinar pellentesque. Morbi sed pede feugiat arcu fringilla dignissim. Praesent tellus velit, accumsan in, venenatis et, malesuada eget, turpis. Donec molestie, nisl non adipiscing scelerisque, metus erat blandit neque, non adipiscing nisi elit at ligula. Pellentesque sodales varius diam. Fusce scelerisque. Vestibulum ultricies aliquam eros. Donec vel felis. Nullam ut erat non leo gravida gravida. Aliquam eu odio. Mauris nibh mi, tincidunt ac, porttitor non, euismod in, nibh. Maecenas tempor malesuada nisi. In hac habitasse platea dictumst. Donec vel purus interdum lorem ullamcorper molestie. Mauris convallis nulla ac lectus. Sed vel lectus. Curabitur sapien magna, rhoncus vitae, aliquet vel, tempor ut, nisi. Cras elementum elit at neque.
In velit purus, pulvinar vitae, pharetra a, mattis consequat, neque. Etiam justo nisl, auctor nec, aliquet eu, iaculis quis, velit. Aenean metus nisl, posuere vel, faucibus vel, tincidunt elementum, purus. Nulla facilisi. Praesent venenatis urna vel nibh. Nunc tellus elit, viverra eu, porta nec, elementum a, lectus. In eleifend. Mauris malesuada, est vitae pellentesque rutrum, risus ipsum placerat metus, eu scelerisque elit arcu quis lacus. Maecenas pulvinar nunc at elit. Mauris metus felis, elementum non, auctor non, tincidunt vitae, lorem. Vivamus semper magna sit amet neque. Sed tristique augue nec nunc.
Nulla pretium massa sed sem viverra venenatis. In congue sem eget purus consequat consectetuer. Sed euismod erat eget neque. Proin turpis. Sed id nulla vel magna consectetuer laoreet. Aenean pulvinar scelerisque erat. Quisque eget augue vel risus convallis congue. Praesent tortor nunc, ultricies a, rutrum vitae, venenatis at, turpis. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos hymenaeos. Curabitur accumsan venenatis diam. In hac habitasse platea dictumst. Cras faucibus ligula in leo. Aenean mattis, felis mollis vestibulum semper, velit tortor semper dui, sed interdum arcu magna eu lectus. Nunc nibh neque, vestibulum eu, ornare ut, congue in, est. Sed consequat leo. Donec et quam commodo magna dapibus placerat. Aenean condimentum. Mauris volutpat, nisi vel ultrices porttitor, lectus magna iaculis mauris, vel facilisis magna nibh eget neque. Vestibulum consequat, lectus vel ultrices accumsan, purus velit hendrerit neque, sit amet mollis velit lacus ac orci.\
""".encodeAsHTML().tokenize('\n')*.trim()
}