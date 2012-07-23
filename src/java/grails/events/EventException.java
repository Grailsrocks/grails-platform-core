package grails.events;


import grails.platform.PlatformException;

public class EventException extends PlatformException {
    public EventException(String s) {
        super(s);
    }
}
