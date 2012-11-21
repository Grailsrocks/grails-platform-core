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
interface Security {
    boolean hasProvider()

    /**
     * Execute the closure pretending to be the user id specified
     */
    def withUser(identity, Closure code)

    /**
     * Get user id string i.e. "marcpalmer" of the currently logged in user, from whatever
     * underlying security API is in force
     */
    String getUserIdentity()

    /** 
     * Determine whether a user with the given id already exists or not
     */
    boolean userExists(identity)

    /**
     * Get user info object i.e. email address, other stuff defined by the security implementation
     * @return An object of completely unknown type. Only for use if you know the security provider
     */
    def getUserInfo()
    
    /**
     * Test if the user has any of the listed roles
     * @param roleOrRoles A list of roles or a single role
     * @return true if the user has any of the roles
     */
    boolean userHasAnyRole(roleOrRoles)

    /**
     * Test if the user has all the listed roles
     * @param roleOrRoles A list of roles or a single role
     * @return true if the user has all of the roles
     */
    boolean userHasAllRoles(roleOrRoles)

    /**
     * Can the current user access this object to perform the named action?
     * @param object The object, typically domain but we don't care what
     * @param action Some application-defined action string i.e. "view" or "edit"
     */
    boolean userIsAllowed(object, action)

    /**
     * Run the closure if userIsAllowed returns true for the object and action
     * otherwise throw exception
     * @return the return value of the closure
     * @throws NotPermittedException
     */
    def requirePermission(object, action, Closure code) throws NotPermittedException
    
    def ifUserHasRole(role, Closure code)

    /**
     * Can the current user access this object to perform the named action?
     * @param object The object, typically domain but we don't care what
     * @param action Some application-defined action string i.e. "view" or "edit"
     */
    def ifUserIsAllowed(object, action, Closure code)
    
    /**
     * Create a link to the specified security action
     * @param action One of "login", "logout", "signup"
     * @return Must return a Map of arguments to pass to g:link to create the link
     */
    Map createLink(String action)
    
}