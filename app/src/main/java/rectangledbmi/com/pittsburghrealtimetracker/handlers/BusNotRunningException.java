package rectangledbmi.com.pittsburghrealtimetracker.handlers;

/**
 * Created by epicstar on 8/23/14.
 */
public class BusNotRunningException extends Exception {
    public BusNotRunningException() {
        super();
    }

    public BusNotRunningException(String e) {
        super(e);
    }
}
