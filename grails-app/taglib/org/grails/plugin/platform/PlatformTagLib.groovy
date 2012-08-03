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
        out << "<div class=\"tag-output\"><strong>Markup:</strong><pre>\n"+b.encodeAsHTML()+"</pre></div>"
        out << "</div>"
    }
}