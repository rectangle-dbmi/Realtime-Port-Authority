package rectangledbmi.com.pittsburghrealtimetracker;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import timber.log.Timber;

/**
 * The application context of the app.
 *
 * Created by epicstar on 3/11/16.
 */
public class PATTrackApplication extends Application {

    /**
     * Use this for {@link android.support.v4.app.Fragment}s to add a {@link RefWatcher} to look for
     * leaks in {@link LeakCanary}.
     * @param context the application context
     * @return a refWatcher
     */
    public static RefWatcher getRefWatcher(Context context) {
        PATTrackApplication application = (PATTrackApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        refWatcher = LeakCanary.install(this);
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
