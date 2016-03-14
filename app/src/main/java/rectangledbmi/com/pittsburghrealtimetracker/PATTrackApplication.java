package rectangledbmi.com.pittsburghrealtimetracker;

import android.app.Application;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

/**
 * The application context of the app.
 *
 * Created by epicstar on 3/11/16.
 */
public class PATTrackApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashTree());
        }
    }

    private static class CrashTree extends Timber.Tree {

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if(priority == Log.DEBUG || priority == Log.VERBOSE || priority == Log.INFO) return;
            if(tag == null || message == null) return;

            if(priority == Log.ERROR) {
                if(t != null) {
                    Log.e(tag, message, t);
                } else {
                    Log.e(tag, message);
                }
            } else if(priority == Log.WARN) {
                if(t != null) {
                    Log.w(tag, message, t);
                } else {
                    Log.w(tag, message);
                }
            }
        }
    }
}
