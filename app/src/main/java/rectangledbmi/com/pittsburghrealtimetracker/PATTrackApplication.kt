package rectangledbmi.com.pittsburghrealtimetracker

import android.app.Application
import android.util.Log
import timber.log.Timber

/**
 * The application context of the app.
 *
 * Created by epicstar on 3/11/16.
 */
class PATTrackApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashTree())
        }
    }

    private class CrashTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (tag == null || message == null) return

            when (priority) {
                Log.DEBUG, Log.VERBOSE, Log.INFO -> return
                Log.ERROR -> {
                    if (t != null) {
                        Log.e(tag, message, t)
                    } else {
                        Log.e(tag, message)
                    }
                }
                Log.WARN -> {
                    if (t != null) {
                        Log.w(tag, message, t)
                    } else {
                        Log.w(tag, message)
                    }
                }
            }
        }
    }
}
