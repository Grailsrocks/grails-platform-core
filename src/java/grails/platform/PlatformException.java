package grails.platform;


public class PlatformException extends RuntimeException {

    public PlatformException() {
        super();
    }

    public PlatformException(String s) {
        super(s);
    }

    public PlatformException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PlatformException(Throwable throwable) {
        super(throwable);
    }
}
