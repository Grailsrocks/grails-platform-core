package org.grails.plugin.platform.events

/**
 * @file
 * @author  Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @date 14/05/12
 
 * @section DESCRIPTION
 *
 * [Does stuff]
 */
 class EventDefinition implements Comparable<EventDefinition>{
     ListenerId listenerId
     String scope = 'app'
     boolean requiresReply = false
     boolean disabled = false
     String definingPlugin

     int score = 0

     int compareTo(EventDefinition t) {
         t.score <=> score
     }
 }
