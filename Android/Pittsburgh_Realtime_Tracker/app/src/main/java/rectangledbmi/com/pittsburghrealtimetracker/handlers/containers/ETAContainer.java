package rectangledbmi.com.pittsburghrealtimetracker.handlers.containers;

/**
 * Container class for the ETAs asynctask
 *
 * Created by epicstar on 12/20/14.
 */
public class ETAContainer {
    /**
     * Title of the ETA
     */
    private String title;
    /**
     * Message of the ETA
     */
    private String message;

    public ETAContainer(String title, String message) {
        setTitle(title);
        setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
