package po25;

public class PlatformException extends Exception {
    public PlatformException () {

    }

    public PlatformException (String message) {
        super (message);
    }

    public PlatformException (Throwable cause) {
        super (cause);
    }

    public PlatformException (String message, Throwable cause) {
        super (message, cause);
    }
}
