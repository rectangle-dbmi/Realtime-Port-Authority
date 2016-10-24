package rectangledbmi.com.pittsburghrealtimetracker.utils;

import com.github.pwittchen.reactivenetwork.library.Preconditions;
import com.github.pwittchen.reactivenetwork.library.internet.observing.InternetObservingStrategy;
import com.github.pwittchen.reactivenetwork.library.internet.socket.SocketErrorHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * <p>Implemented another {@link InternetObservingStrategy} since
 *    {@link com.github.pwittchen.reactivenetwork.library.internet.observing.strategy.DefaultInternetObservingStrategy}
 *    does not account for walled garden internet.</p>
 * <p>Created by epicstar on 10/23/16.</p>
 * @author Jeremy Jao
 * @since 80
 */

public class WalledInternetStrategy implements InternetObservingStrategy {
    @Override
    public Observable<Boolean> observeInternetConnectivity(
            int initialIntervalInMs,
            int intervalInMs,
            String host,
            int port,
            int timeoutInMs,
            SocketErrorHandler socketErrorHandler)
    {
        Preconditions.checkGreaterOrEqualToZero(initialIntervalInMs,
                "initialIntervalInMs is not a positive number");
        Preconditions.checkGreaterThanZero(intervalInMs, "intervalInMs is not a positive number");
        Preconditions.checkNotNullOrEmpty(host, "host is null or empty");
        Preconditions.checkGreaterThanZero(port, "port is not a positive number");
        Preconditions.checkGreaterThanZero(timeoutInMs, "timeoutInMs is not a positive number");
        // socketErrorHandler is null because it's not being used here.
        return Observable.interval(initialIntervalInMs, intervalInMs, TimeUnit.MILLISECONDS, Schedulers.io())
                .map(aLong -> {
                    HttpURLConnection urlConnection = null;
                    try {
                        URL url = new URL(host); // "http://clients3.google.com/generate_204"
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setInstanceFollowRedirects(false);
                        urlConnection.setConnectTimeout(timeoutInMs);
                        urlConnection.setReadTimeout(timeoutInMs);
                        urlConnection.setUseCaches(false);
                        urlConnection.getInputStream();
                        // We got a valid response, but not from the real google
                        return urlConnection.getResponseCode() == 204;
                    } catch (IOException e) {
                        return false;
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }
                })
                .distinctUntilChanged();
    }
}
