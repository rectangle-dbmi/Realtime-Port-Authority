package rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.errorcontainers;

import java.util.ArrayList;
import java.util.HashMap;

import rx.Observable;

/**
 * This is a base class for handling all errors in the Port Authority API.
 *
 * @author Jeremy Jao
 * @since 49
 */
public class PATAPIErrors extends Observable<ErrorMessage> {

    protected HashMap<String, ArrayList<String>> errors;

    /**
     * Creates an Observable with a Function to execute when it is subscribed to.
     * <p>
     * <em>Note:</em> Use {@link #create(OnSubscribe)} to create an Observable, instead of this constructor,
     * unless you specifically have a need for inheritance.
     *
     * @param f {@link OnSubscribe} to be executed when a Subscriber is called is called
     */
    protected PATAPIErrors(OnSubscribe<ErrorMessage> f) {
        super(f);
    }


}
