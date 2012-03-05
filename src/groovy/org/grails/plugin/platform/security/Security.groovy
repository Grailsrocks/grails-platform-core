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
package org.grails.plugin.platform.security

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.slf4j.LoggerFactory
import org.codehaus.groovy.grails.commons.GrailsDomainClass

/**
 * Bean for registering and accessing security information
 * 
 * A security-provider plugin must be installed
 *
 */
class Security implements ApplicationContextAware {
    
    final log = LoggerFactory.getLogger(Security)

    SecurityBridge grailsSecurityBridge 
    ApplicationContext applicationContext
    
    void setApplicationContext(ApplicationContext context) {
        this.@applicationContext = context
        if (context.containsBean('grailsSecurityBridge')) {
            grailsSecurityBridge = context.getBean('grailsSecurityBridge')
        }
    }
    
    def injectedMethods = { 
        def self = this
        'controller, service, domain, tagLib' { Class clazz, artefact ->
            getSecurityIdentity(staticMethod:artefact instanceof GrailsDomainClass) { ->
                self.getUserIdentity()
            }
            getSecurityInfo(staticMethod:artefact instanceof GrailsDomainClass) {  ->
                self.getUserInfo()
            }
            copyFrom(self, 'withUser', 'userHasAnyRole', 'userHasAllRoles', 'userIsAllowed')
        }
    }

    boolean hasProvider() {
        getSecurityBridge() != null
    }
    
    /**
     * Execute the closure pretending to be the user id specified
     */
    def withUser(identity, Closure code) {
        getSecurityBridge(true).withUser(identity, code)
    }

    /**
     * Get user id string i.e. "marcpalmer" of the currently logged in user, from whatever
     * underlying security API is in force
     */
    String getUserIdentity() {
        getSecurityBridge()?.userIdentity
    }

    /**
     * Get user info object i.e. email address, other stuff defined by the security implementation
     * @return An object of completely unknown type. Only for use if you know the security provider
     */
    def getUserInfo() {
        getSecurityBridge()?.userInfo
    }

    def userHasAnyRole(roleOrRoles) {
        def roles = roleOrRoles instanceof Collection ? roleOrRoles : [roleOrRoles]
        roles.any { r ->
            getSecurityBridge()?.userHasRole(r)
        }
    }

    def userHasAllRoles(roleOrRoles) {
        def roles = roleOrRoles instanceof Collection ? roleOrRoles : [roleOrRoles]
        roles.every { r ->
            getSecurityBridge()?.userHasRole(r)
        }
    }

    /**
     * Can the current user access this object to perform the named action?
     * @param object The object, typically domain but we don't care what
     * @param action Some application-defined action string i.e. "view" or "edit"
     */
    def userIsAllowed(object, action) {
        getSecurityBridge()?.userIsAllowed(object, action)
    }

    /**
     * Run the closure if userIsAllowed returns true for the object and action
     * otherwise throw exception
     * @return the return value of the closure
     * @throws NotPermittedException
     */
    def requirePermission(object, action, Closure code) throws NotPermittedException {
        if (userIsAllowed(object, action)) {
            return code()
        } else {
            throw new NotPermittedException(object, action)
        }
    }
    
    def ifUserHasRole(role, Closure code) {
        getSecurityBridge()?.userHasRole(role) ? code() : null
    }

    /**
     * Can the current user access this object to perform the named action?
     * @param object The object, typically domain but we don't care what
     * @param action Some application-defined action string i.e. "view" or "edit"
     */
    def ifUserIsAllowed(object, action, Closure code) {
        def allowed = getSecurityBridge()?.userIsAllowed(object, action)
        allowed ? code() : null
    }
    
    SecurityBridge getSecurityBridge(boolean throwIfNone = false) {
        if (!grailsSecurityBridge) {
            def msg = """\
An attempt was made to use the grailsSecurity bean, but there is no security bridge \
implementation defined. You must install a security plugin and/or provide \
a grailsSecurityBridge bean."""
            if (throwIfNone) {
                throw new IllegalArgumentException(msg)
            } else if (log.warnEnabled) {
                log.warn msg
            }
        }
        grailsSecurityBridge
    }
    
    /**
     * Create a link to the specified security action
     * @param action One of "login", "logout", "signup"
     * @return Must return a Map of arguments to pass to g:link to create the link
     */
    Map createLink(String action) {
        getSecurityBridge().createLink(action)
    }
    
}