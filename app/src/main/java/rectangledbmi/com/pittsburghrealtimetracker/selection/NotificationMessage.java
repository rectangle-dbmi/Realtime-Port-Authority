package rectangledbmi.com.pittsburghrealtimetracker.selection;

/**
 * Class instance to pass an object to create toasts and snackbar messages reactively.
 *
 * @author Jeremy Jao
 * @since 70
 */
public class NotificationMessage {

    public static NotificationMessage create(String message, int length) {
        return new NotificationMessage(message, length);
    }

    private String message;
    private int length;

    private NotificationMessage(String message, int length) {
        setMessage(message);
        setLength(length);
    }

    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public int getLength() {
        return length;
    }

    private void setLength(int length) {
        this.length = length;
    }
}
