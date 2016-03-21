package rectangledbmi.com.pittsburghrealtimetracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.Constants;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.PATAPI;
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * This is the main activity of the Realtime Tracker...
 */
public class SelectTransit extends AppCompatActivity implements
        NavigationDrawerFragment.BusListCallbacks,
        SelectionFragment.BusSelectionInteraction {

    private static final String LINES_LAST_UPDATED = "lines_last_updated";

    /**
     * The latitude and longitude of Pittsburgh... used if the app doesn't have a saved state of the camera
     */
    private final static LatLng PITTSBURGH = new LatLng(40.441, -79.981);

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Fragment that contains data from navigation drawer
     */
    private SelectionFragment selectionFragment;


    /**
     * Port Authority API Client made through Retrofit
     *
     * @since 46
     */
    private PATAPI patApiClient;

    /**
     * Reference to the main activity's Layout.
     *
     * @since 55
     */
    private DrawerLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restoreActionBar();
        buildPATAPI();
        setContentView(R.layout.activity_select_transit);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                fragmentManager.findFragmentById(R.id.navigation_drawer);
        checkSDCardData();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mainLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        selectionFragment = (SelectionFragment) fragmentManager.findFragmentById(R.id.map);

        enableHttpResponseCache();

    }

    /**
     * Builds the PAT API Client from a Rest Adapter
     *
     * @since 46
     */
    private void buildPATAPI() {
        // use a date converter
        Gson gson = new GsonBuilder()
                .setDateFormat(Constants.DATE_FORMAT_PARSE)
                .create();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS).build();
        // build the restadapter
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.api_url))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();

        // sets the PAT API
        patApiClient = retrofit.create(PATAPI.class);
    }

    public PATAPI getPatApiClient() {
        return patApiClient;
    }

//    /**
//     * Checks if the network is available
//     * TODO: incorporate this with a dialog to enable internet
//     * @return whether or not the network is available...
//     */
//    private boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager
//                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
//    }

    /**
     * Checks if the stored polylines directory is present and clears if we hit a friday or if the
     * saved day of the week is higher than the current day of the week.
     * @since 32
     */
    private void checkSDCardData() {
        File data = getFilesDir();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Long lastUpdated = sp.getLong(LINES_LAST_UPDATED, -1);
        Timber.d(data.getName());
        if (data.mkdirs())
            Timber.d("Created data storage");
        File lineInfo = new File(data, "/lineinfo");
        if (data.mkdirs())
            Timber.d("created line info folder in storage");
        Timber.d(Long.toString(lastUpdated));
        if (lastUpdated != -1 && ((System.currentTimeMillis() - lastUpdated) / 1000 / 60 / 60) > 24) {
            if (lineInfo.exists()) {
                Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_WEEK);
                Calendar o = Calendar.getInstance();
                o.setTimeInMillis(lastUpdated);
                int oldDay = o.get(Calendar.DAY_OF_WEEK);
                if (day == Calendar.FRIDAY || oldDay >= day) {
                    File[] files = lineInfo.listFiles();
                    sp.edit().putLong(LINES_LAST_UPDATED, System.currentTimeMillis()).apply();
                    if (files != null) {
                        for (File file : files) {
                            if (file.delete())
                                Timber.d("%s deleted", file.getName());
                        }
                    }
                }
            }
        }

        if (lineInfo.listFiles() == null || lineInfo.listFiles().length == 0) {
            sp.edit().putLong(LINES_LAST_UPDATED, System.currentTimeMillis()).apply();
        }
    }

    private void enableHttpResponseCache() {
        try {
            long httpCacheSize = 10485760; // 10 MiB
            File fetch = getExternalCacheDir();
            if (fetch == null) {
                fetch = getCacheDir();
            }
            File httpCacheDir = new File(fetch, "http");
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
            Timber.e("HTTP response cache is unavailable.");
        }
    }

    /**
     * Checks the state of the route on the map. If it is not on the map, the relevant info will be
     * added. Otherwise, it will be unselected on the map
     *
     * @since 43
     * @param route the bus route selected
     */
    @Override
    public void onSelectBusRoute(@NonNull Route route) {
        selectionFragment.onSelectBusRoute(route);
    }

    @Override
    public void onDeselectBusRoute(Route route) {
        selectionFragment.onDeselectBusRoute(route);
    }


    public void restoreActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Throwable e) {
                Snackbar.make(mainLayout,
                        "Material Design bugged out on your device. Please report this to the Play Store Email if this pops up.", Snackbar.LENGTH_SHORT).show();
//                Toast.makeText(this, "Material Design bugged out on your device. Please report this to the Play Store Email if this pops up.", Toast.LENGTH_SHORT).show();
            }
        }
        try {
            ActionBar t = getSupportActionBar();
            if (t != null) t.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Snackbar.make(mainLayout,
                    "Material Design bugged out on your device. Please report this to the Play Store Email if this pops up.", Snackbar.LENGTH_SHORT).show();
        }

    }

    //dunno...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.select_transit, menu);
//            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    //We probably don't need this? Maybe we do
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_select_buses) {
            mNavigationDrawerFragment.openDrawer();
        }
        else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_clear) {
            clearSelection();
        }
        return super.onOptionsItemSelected(item);
    }

    public void clearSelection() {
        File lineInfo = new File(getFilesDir(), "/lineinfo");
        Timber.d("cleared files: %s", lineInfo.getAbsolutePath());
        if(lineInfo.exists()) {
            File[] files = lineInfo.listFiles();
            if(files != null) {
                for(File file : files)
                    file.delete();
            }
        }
        mNavigationDrawerFragment.clearSelection();
        selectionFragment.clearSelection();
    }

    /**
     * Create a toast
     * @since 47
     * @param string - string to show
     * @param length - length to show message
     */
    @Override
    public void showToast(String string, int length) {
        Toast.makeText(this, string, length).show();
    }

    /**
     * General way to make a snackbar
     *
     * @since 46
     * @param string - the string to add to on the
     * @param showLength - how long to show the snackbar
     */
    public void makeSnackbar(@NonNull String string, int showLength) {
        if (string.length() > 0) {

            Snackbar.make(mainLayout,
                    string, showLength
            ).show();
        }

    }

    public void makeSnackbar(@NonNull String string, int showLength, @NonNull String action, @NonNull View.OnClickListener listener) {
        if (string.length() == 0) return;
        Snackbar.make(mainLayout, string, showLength)
                .setAction(action, listener)
                .show();
    }


    public void onBackPressed() {
        if (!mNavigationDrawerFragment.closeDrawer())
            super.onBackPressed();
    }

    @Override
    public void showOkDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SelectTransit.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override @NonNull
    public Route getSelectedRoute(String routeNumber) {
        return mNavigationDrawerFragment.getSelectedRoute(routeNumber);
    }


    @Override @NonNull public Set<String> getSelectedRoutes() {
        return mNavigationDrawerFragment.getSelectedRoutes();
    }


    @NonNull @Override
    public PublishSubject<RouteSelection> getSelectionSubject() {
        return mNavigationDrawerFragment.getListSelectionSubject();
    }

    @Override
    public void openPermissionsPage() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Click Event for the {@link rectangledbmi.com.pittsburghrealtimetracker.R.menu#select_transit}'s Application Details
     * @param item the application details item
     */
    public void onClickAppDetails(MenuItem item) {
        openPermissionsPage();
    }
}
