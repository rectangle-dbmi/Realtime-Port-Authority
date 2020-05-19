package rectangledbmi.com.pittsburghrealtimetracker.utils

import com.github.pwittchen.reactivenetwork.library.rx2.Preconditions
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingStrategy
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.error.ErrorHandler

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 *
 * Implemented another [InternetObservingStrategy] since
 * [com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingStrategy]
 * does not account for walled garden internet.
 *
 * Created by epicstar on 10/23/16.
 * @author Jeremy Jao
 * @since 80
 */

class WalledInternetStrategy : InternetObservingStrategy {
    override fun observeInternetConnectivity(
            initialIntervalInMs: Int,
            intervalInMs: Int,
            host: String,
            port: Int,
            timeoutInMs: Int,
            socketErrorHandler: ErrorHandler): Observable<Boolean> {
        Preconditions.checkGreaterOrEqualToZero(initialIntervalInMs,
                "initialIntervalInMs is not a positive number")
        Preconditions.checkGreaterThanZero(intervalInMs, "intervalInMs is not a positive number")
        Preconditions.checkNotNullOrEmpty(host, "host is null or empty")
        Preconditions.checkGreaterThanZero(port, "port is not a positive number")
        Preconditions.checkGreaterThanZero(timeoutInMs, "timeoutInMs is not a positive number")
        // socketErrorHandler is null because it's not being used here.
        return Observable.interval(initialIntervalInMs.toLong(), intervalInMs.toLong(), TimeUnit.MILLISECONDS, Schedulers.io())
                .map { _ ->
                    var urlConnection: HttpURLConnection? = null
                    try {
                        val url = URL(host) // "http://clients3.google.com/generate_204"
                        urlConnection = (url.openConnection() as HttpURLConnection).apply {
                            this.instanceFollowRedirects = false
                            this.connectTimeout = timeoutInMs
                            this.readTimeout = timeoutInMs
                            this.useCaches = false
                        }
                        urlConnection.inputStream
                        // We got a valid response, but not from the real google
                        return@map urlConnection.getResponseCode() == 204
                    } catch (e: IOException) {
                        return@map false
                    } finally {
                        urlConnection?.disconnect()
                    }
                }
                .distinctUntilChanged()
    }

    override fun checkInternetConnectivity(host: String, port: Int, timeoutInMs: Int, errorHandler: ErrorHandler): Single<Boolean>? {
        return null
    }

    override fun getDefaultPingHost(): String? {
        return null
    }
}
