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

import org.grails.plugin.platform.security.Security

class SecurityTagLib {
    static namespace = "s" // Or...?
    
    Security grailsSecurity
    
    def identity = { attrs ->
        out << securityIdentity
    }

    def info = { attrs ->
        out << grailsSecurity.userInfo?.getAt(attrs.property)
    }

    def ifLoggedIn = { attrs, body ->
        if (securityIdentity != null) {
            out << body()
        }
    }
    
    def ifNotLoggedIn = { attrs, body ->
        if (securityIdentity == null) {
            out << body()
        }
    }
    
    def ifPermitted = { attrs, body ->
        // @todo using bean + action here we can also implement object permission checks
        grailsSecurity.ifUserHasRole(attrs.role) {
            out << body()
        }
    }

    def ifNotPermitted = { attrs, body ->
        // @todo using bean + action here we can also implement object permission checks
        if (!grailsSecurity.userHasRole(attrs.role)) {
            out << body()
        }
    }
    
    def createLogoutLink = { attrs, body ->
        out << g.createLink(grailsSecurity.createLink('logout'), body)
    }

    def createLoginLink = { attrs, body ->
        out << g.createLink(grailsSecurity.createLink('login'))
    }

    def createSignupLink = { attrs, body ->
        out << g.createLink(grailsSecurity.createLink('signup'))
    }

    def logoutButton = { attrs, body ->
        if (!attrs.kind) {
            attrs.kind = 'anchor'
        }
        attrs.putAll(grailsSecurity.createLink('logout'))
        out << p.button(attrs, body)
    }

    def loginButton = { attrs, body ->
        if (!attrs.kind) {
            attrs.kind = 'anchor'
        }
        attrs.putAll(grailsSecurity.createLink('login'))
        out << p.button(attrs, body)
    }

    def signupButton = { attrs, body ->
        if (!attrs.kind) {
            attrs.kind = 'anchor'
        }
        attrs.putAll(grailsSecurity.createLink('signup'))
        out << p.button(attrs, body)
    }
}