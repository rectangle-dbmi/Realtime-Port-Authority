package rectangledbmi.com.pittsburghrealtimetracker

import android.app.Application
import android.util.Log
import timber.log.Timber

/**
 * Application application class
 *
 * @author Jeremy Jao
 */
public class PATTrackApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashTree())
        }
    }

    private class CrashTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
            when(priority) {
                Log.DEBUG, Log.VERBOSE, Log.INFO -> return
            }
            super.log(priority, tag, message, t)

        }

    }
}

