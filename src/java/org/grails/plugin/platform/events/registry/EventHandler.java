package org.grails.plugin.platform.events.registry;

import org.grails.plugin.platform.events.ListenerId;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 19/01/12
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public interface EventHandler {

    public ListenerId getListenerId();
    public boolean isUseEventMessage();
}
