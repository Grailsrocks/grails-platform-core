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

/**
 * Interface that plugin must implement to provide security information
 * 
 */
interface SecurityBridge {
    
    /**
     * Implementations must return the name of their security provider 
     * @return A name such as "Spring Security"
     */
    String getProviderName() 

    /**
     * Get user id string i.e. "marcpalmer" of the currently logged in user, from whatever
     * underlying security API is in force
     * @return the user name / identity String or null if nobody is logged in
     */
    String getUserIdentity()

    /**
     * Get user info object containing i.e. email address, other stuff defined by the security implementation
     * @return the implementation's user object or null if nobody is logged in
     */
    def getUserInfo()

    /**
     * Return true if the current logged in user has the specified role
     */
    boolean userHasRole(role)

    /**
     * Can the current user access this object to perform the named action?
     * @param object The object, typically domain but we don't care what
     * @param action Some application-defined action string i.e. "view" or "edit"
     */
    boolean userIsAllowed(object, action)
    
    /**
     * Create a link to the specified security action
     * @param action One of "login", "logout", "signup"
     * @return Must return a Map of arguments to pass to g:link to create the link
     */
    Map createLink(String action)
    
    /** 
     * Determine whether a user with the given id already exists or not
     */
    boolean userExists(identity)

    /**
     * Execute code masquerading as the specified user, for the duration of the Closure block
     * @return Whatever the closure returns
     */
    def withUser(identity, Closure code)
}