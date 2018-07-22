package rectangledbmi.com.pittsburghrealtimetracker.utils;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.WalledGardenInternetObservingStrategy;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Consumer;
import rectangledbmi.com.pittsburghrealtimetracker.PATTrackApplication;
import timber.log.Timber;

/**
 * <p>Helper methods for shared reactive methods.</p>
 * <p>Created by epicstar on 10/21/16.</p>
 * @author Jeremy Jao
 * @since 80
 */

public class ReactiveHelper {

    /**
     * <p>Use in a {@link Observable#compose(ObservableTransformer)} when listening to state to reconnect
     *    from the internet using {@link Observable#retryWhen(io.reactivex.functions.Function)}</p>
     * @param disconnectionMessage lambda that should print out a disconnection message
     * @param reconnectionMessage lambda that should print out a reconnection message
     * @return a transformer for composing retrying internet
     */
    public static FlowableTransformer<Throwable, Boolean> retryIfInternet(
            Consumer<Throwable> disconnectionMessage,
            Consumer<Boolean> reconnectionMessage
    ) {
        return throwableObservable -> throwableObservable
                .doOnNext(disconnectionMessage)
                .flatMap(throwable -> {
            // theoretically, this should only resubscribe when internet is back
            if (isInternetDown(throwable)) {
                return ReactiveNetwork
                        .observeInternetConnectivity(InternetObservingSettings
                            .strategy(new WalledGardenInternetObservingStrategy())
                            .initialInterval(2000)
                            .interval(2000)
                            .port(80)
                            .timeout(2000)
                            .errorHandler(null)
                            .host("http://clients3.google.com/generate_204")
                            .build())
                    .skipWhile(isConnected -> !isConnected)
                        .doOnNext(isConnected -> {
                            if (isConnected) {
                                reconnectionMessage.accept(true);
                            }
                            else {
                                Timber.i("Internet is still disconnected in retryWhen.");
                            }
                        })
                        .toFlowable(BackpressureStrategy.BUFFER);
            }
            // otherwise, just run normal onError
            Timber.i(throwable, "Not retrying since something should be wrong on " +
                    "Port Authority's end.");
            return Flowable.error(throwable);
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
