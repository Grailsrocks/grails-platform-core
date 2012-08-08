package org.grails.plugin.platform.events.utils;

import org.springframework.aop.TargetClassAware;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

/**
 * Created with IntelliJ IDEA.
 * User: smaldini
 * Date: 8/3/12
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventsUtils {

    static public Object unproxy(Object candidate) {

        if (candidate instanceof Advised) {
            try {
                return ((Advised) candidate).getTargetSource().getTarget();
            } catch (Exception e) {
            }
        }
        return candidate;
    }
}
