/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (stephane.maldini@gmail.com)
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

class HTMLTagLib {
    static namespace = "g"

    static DOCTYPES = [
        'html 4.01':[
            dt:'<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">\n'
        ],
        'html 5':[
            dt:'<!DOCTYPE html>\n'
        ],
        'xhtml 1.0':[
            dt:'<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">\n',
            pre:'<?xml version="1.0" encoding="UTF-8"?>\n',
            htmlAttrs: ' xmlns="http://www.w3.org/1999/xhtml"',
            htmlDefaultAttrs: ['xml:lang':"en", lang:"en"]
        ],
        'xhtml 1.1':[
            dt:'<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">\n',
            pre:'<?xml version="1.0" encoding="UTF-8"?>\n',
            htmlAttrs: ' xmlns="http://www.w3.org/1999/xhtml"',
            htmlDefaultAttrs: ['xml:lang':"en", lang:"en"]
        ]
    ]
    
    def html = { attrs, body ->
        // @todo add caching for these, likely same throughout app for every request - lang attribute aside
        def id = attrs.remove('doctype') ?: 'html 5'
        def dtInfo = DOCTYPES[id]
        if (!dtInfo) {
            throwTagError "Unsupported doctype value [${id}] - specify one of ${DOCTYPES.keySet()}"
        }
        out << dtInfo.pre
        out << dtInfo.dt
        out << "<html"
        out << dtInfo.htmlAttrs
        def htmlAttrs
        if (dtInfo.htmlDefaultAttrs) {
            if (attrs) {
                htmlAttrs = dtInfo.htmlDefaultAttrs.clone()
                htmlAttrs.putAll(attrs)
            } else {
                // don't clone if nothing to add to it
                htmlAttrs = dtInfo.htmlDefaultAttrs
            }
        } else {
            htmlAttrs = attrs
        }
        
        if (htmlAttrs) {
            out << TagLibUtils.attrsToString(htmlAttrs)
        }
        
        out << '>\n'
        out << body()
        out << '\n</html>'
    }
    
}