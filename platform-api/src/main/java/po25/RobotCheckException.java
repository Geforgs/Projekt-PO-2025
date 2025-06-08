package po25;

public class RobotCheckException extends Exception {
    public RobotCheckException () {

    }

    public RobotCheckException (String message) {
        super (message);
    }

    public RobotCheckException (Throwable cause) {
        super (cause);
    }

    public RobotCheckException (String message, Throwable cause) {
        super (message, cause);
    }
}
