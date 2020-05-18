package rectangledbmi.com.pittsburghrealtimetracker

import android.app.Application
import android.content.Context
import android.util.Log

import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

import timber.log.Timber

/**
 * The application context of the app.
 *
 * Created by epicstar on 3/11/16.
 */
class PATTrackApplication : Application() {

    private var refWatcher: RefWatcher? = null

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        refWatcher = LeakCanary.install(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashTree())
        }
    }

    private class CrashTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
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

    companion object {

        /**
         * Use this for [android.support.v4.app.Fragment]s to add a [RefWatcher] to look for
         * leaks in [LeakCanary].
         * @param context the application context
         * @return a refWatcher
         */
        @JvmStatic
        fun getRefWatcher(context: Context): RefWatcher? {
            val application = context.applicationContext as PATTrackApplication
            return application.refWatcher
        }
    }
}
