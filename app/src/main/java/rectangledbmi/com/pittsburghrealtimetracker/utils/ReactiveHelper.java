package rectangledbmi.com.pittsburghrealtimetracker.utils;

import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;

import java.io.IOException;
import java.net.SocketTimeoutException;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * <p>Helper methods for shared reactive methods.</p>
 * <p>Created by epicstar on 10/21/16.</p>
 * @author Jeremy Jao
 * @since 80
 */

public class ReactiveHelper {

    /**
     * <p>Use in a {@link Observable#compose(Observable.Transformer)} when listening to state to reconnect
     *    from the internet using {@link Observable#retryWhen(Func1)}</p>
     * @param disconnectionMessage lambda that should print out a disconnection message
     * @param reconnectionMessage lambda that should print out a reconnection message
     * @return a transformer for composing retrying internet
     */
    public static Observable.Transformer<Throwable, Boolean> retryIfInternet(
            Action1<Throwable> disconnectionMessage,
            Action1<Boolean> reconnectionMessage
    ) {
        return throwableObservable -> throwableObservable
                .doOnNext(disconnectionMessage)
                .flatMap(throwable -> {
            // theoretically, this should only resubscribe when internet is back
            if (isInternetDown(throwable)) {
                return ReactiveNetwork
                        .observeInternetConnectivity(
                                new WalledInternetStrategy(),
                                2000,
                                2000,
                                "http://clients3.google.com/generate_204",
                                80,
                                2000,
                                null
                        )
                        .skipWhile(isConnected -> !isConnected)
                        .doOnNext(isConnected -> {
                            if (isConnected) {
                                reconnectionMessage.call(true);
                            }
                            else {
                                Timber.i("Internet is still disconnected in retryWhen.");
                            }
                        });
            }
            // otherwise, just run normal onError
            Timber.i(throwable, "Not retrying since something should be wrong on " +
                    "Port Authority's end.");
            return Observable.error(throwable);
        });
    }


    /**
     * Decides if internet is down on the mobile device.
     * @param throwable the error
     * @return true if internet on the mobile device
     */
    public static boolean isInternetDown(Throwable throwable) {
        /*
         Jeremy Jao: note an edge case where if on a tablet and android says wifi has some sort of spotty
         internet, retrofit/okhttp will still try to send an HTTP request and will throw a Socket Timeout
         Exception. This is bad because the app will say "Internet disconnected please try again later,
         but in reality, most likely, it's still up
         (this is our only we know the Port Authority servers are up or down since they don't give us error responses).
         This means that users will have to restart the app for it to function again.

         I believe that Retrofit should be instead detecting what the Android OS is saying about
         wifi state and should return an IOException so I'm leaving this behavior for now... Perhaps
         to be revisited at some point.
        */
        return (throwable instanceof IOException && (
                !(throwable instanceof SocketTimeoutException))
        );
    }
}
