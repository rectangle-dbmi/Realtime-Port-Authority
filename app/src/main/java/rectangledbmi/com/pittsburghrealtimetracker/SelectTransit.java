package rectangledbmi.com.pittsburghrealtimetracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.location.Location;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import rectangledbmi.com.pittsburghrealtimetracker.handlers.Constants;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestLine;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestPredictions;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.extend.ETAWindowAdapter;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.PATAPI;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.containers.errors.ErrorMessage;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.containers.vehicles.VehicleBitmap;
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStop;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.BustimeVehicleResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Vehicle;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.VehicleResponse;
import retrofit2.GsonConverterFactory;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * This is the main activity of the Realtime Tracker...
 */
public class SelectTransit extends AppCompatActivity implements
        NavigationDrawerFragment.BusListCallbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback, LocationListener,
        SelectionFragment.BusSelectionInteraction {

    private static final String LINES_LAST_UPDATED = "lines_last_updated";

    /**
     * Saved instance key for the latitude
     */
    private final static String LAST_LATITUDE = "lastLatitude";

    /**
     * Saved instance key for the longitude
     */
    private final static String LAST_LONGITUDE = "lastLongitude";

    /**
     * Saved instance key for the zoom of the map
     */
    private final static String LAST_ZOOM = "lastZoom";

    /**
     * The latitude and longitude of Pittsburgh... used if the app doesn't have a saved state of the camera
     */
    private final static LatLng PITTSBURGH = new LatLng(40.441, -79.981);

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * The Google Maps Fragment that displays literally everything
     */
    private GoogleMap mMap;

    /**
     * longitude of the map
     */
    private double longitude;

    /**
     * latitude of the map
     */
    private double latitude;

    /**
     * longitude of the map
     */
    private float zoom;

    /**
     * This is the googleAPIClient that will center the map on the person using the app.
     */
    private GoogleApiClient googleAPIClient;

    /**
     * This is where the person is when he first opens the app
     */
    private Location currentLocation;

    /**
     * This specifies whether to center the map on the person or not. Used because if we rotate the
     * screen when the app is opened, it will lose the location of the most current location of the
     * map.
     */
    private boolean inSavedState;

    /**
     * This is the store for the busMarkers
     */
    private ConcurrentMap<Integer, Marker> busMarkers;

    /**
     * Map of the route lines
     */
    private ConcurrentMap<String, List<Polyline>> routeLines;

    /**
     * Object that does dynamic bus marker storage
     */
    private TransitStop transitStop;

    /**
     * Boolean to identify if we are not centered
     * @since 42
     */
    private boolean notCentered;

    /**
     * Port Authority API Client made through Retrofit
     *
     * @since 46
     */
    private PATAPI patApiClient;

    /**
     * Subscription service for vehicle updates
     *
     * @since 46
     */
    private Subscription vehicleSubscription;

    /**
     * Subscription for broadcasting vehicle update errors
     *
     * @since 55
     */
    private Subscription vehicleErrorSubscription;

    /**
     * Camera listener reference for Google Maps
     *
     * @since 54
     */
    private GoogleMap.OnCameraChangeListener mMapCameraListener;

    /**
     * Click listener reference for Google Maps
     *
     * @since 54
     */
    private GoogleMap.OnMarkerClickListener mMapMarkerClickListener;

    /**
     * Reference to the main activity's Layout.
     *
     * @since 55
     */
    private DrawerLayout mainLayout;


    /**
     * Location Request Code
     * @since 48
     */
    private static int LOCATION_REQUEST_CODE = 123;

    /**
     * Observable to update bus vehicles on map every 10 seconds.
     */
    private Observable<VehicleBitmap> vehicleUpdateObservable;

    /**
     * Observable to update bus vehicle error messages every 10 seconds.
     */
    private Observable<ErrorMessage> vehicleErrorObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LeakCanary.install(getApplication());
        restoreActionBar();
        setContentView(R.layout.activity_select_transit);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        checkSDCardData();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mainLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        setGoogleApiClient();
        buildPATAPI();
        instantiateMapReferences();
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        //sets up the map
        inSavedState = false;
        enableHttpResponseCache();
        restoreInstanceState(savedInstanceState);
        notCentered = false;
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
        // build the restadapter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.api_url))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
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
        Timber.d("data_storage", data.getName());
        if (data.mkdirs())
            Log.d("sd_card", "Created data storage");
        File lineInfo = new File(data, "/lineinfo");
        if (data.mkdirs())
            Log.d("sd_card", "created line info folder in storage");
        Timber.d("updated time", Long.toString(lastUpdated));
        if (lastUpdated != -1 && ((System.currentTimeMillis() - lastUpdated) / 1000 / 60 / 60) > 24) {
//            Timber.d("update time....", Long.toString((System.currentTimeMillis() - lastUpdated) / 1000 / 60 / 60));
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
                                Timber.d("sd_card", file.getName() + " deleted");
                        }
                    }
                }
            }
        }

        if (lineInfo.listFiles() == null || lineInfo.listFiles().length == 0) {
            sp.edit().putLong(LINES_LAST_UPDATED, System.currentTimeMillis()).apply();
        }
    }

    /**
     * Sets the application google Api Location googleAPIClient
     */
    private void setGoogleApiClient() {
        googleAPIClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
            Timber.d("HTTP_response_cache", "HTTP response cache is unavailable.");
        }
    }

    /**
     * Restores the instance state of the program including the preferences
     *
     * @param savedInstanceState the saved instances of the app
     */
    protected void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Timber.d("savedInstance_restore", "instance saved");
            Timber.d("savedInstance_s", "lat=" + savedInstanceState.getDouble(LAST_LATITUDE));
            inSavedState = true;
            latitude = savedInstanceState.getDouble(LAST_LATITUDE);
            longitude = savedInstanceState.getDouble(LAST_LONGITUDE);
            zoom = savedInstanceState.getFloat(LAST_ZOOM);


        } else {
            Timber.d("savedInstance", "default location instead");

            defaultCameraLocation();
        }
        Timber.d("savedInstance_restore", "saved? " + inSavedState);
        Timber.d("savedInstance_restore", "lat=" + latitude);
        Timber.d("savedInstance_restore", "long=" + longitude);
        if (transitStop == null) {
            transitStop = new TransitStop();
        }

    }

    /**
     * Instantiates the default camera coordinates
     */
    private void defaultCameraLocation() {
        latitude = PITTSBURGH.latitude;
        longitude = PITTSBURGH.longitude;
        zoom = (float) 11.8;
    }

    /**
     * Saves the instances of the app
     * <p/>
     * Right now, it saves the list of buses and the camera position of the map
     *
     * @param savedInstanceState the bundle of saved instances
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
//        Calendar c = Calendar.getInstance();
//        Timber.i("Date", c.get(Calendar.DATE));
//        ArrayList<String> list = new ArrayList<String>(buses.size());
//        list.addAll(buses);
//        savedInstanceState.putStringArrayList(BUS_SELECT_STATE, list);
        if (mMap != null) {
            savedInstanceState.putDouble(LAST_LATITUDE, latitude);
            savedInstanceState.putDouble(LAST_LONGITUDE, longitude);
            savedInstanceState.putFloat(LAST_ZOOM, zoom);
            Timber.d("savedInstance_osi", "saved? " + inSavedState);
            Timber.d("savedInstance_osi", "lat=" + latitude);
            Timber.d("savedInstance_osi", "long=" + longitude);
            Timber.d("savedInstance_osi", "zoom=" + zoom);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        notCentered = true;
        setUpMap();
        centerMap();
        restorePolylines();
    }

    /**
     * initializes the bus list
     * <p/>
     * Codewise most efficient way to pass the buses to the UI updater
     * <p/>
     * However, linear time worst case
     */
    private void instantiateMapReferences() {
        routeLines = new ConcurrentHashMap<>(getResources().getInteger(R.integer.max_checked));
        busMarkers = new ConcurrentHashMap<>(100);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleAPIClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            restoreBuses();
        }
    }

    protected void onPause() {
        Timber.d("SelectTransit onPause");
        removeBuses();
        stopTimer();
        super.onPause();

    }

    @Override
    protected void onStop() {

        Timber.d("SelectTransit onStop");
        googleAPIClient.disconnect();
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        transitStop.destroyStops();
        transitStop = null;
        mMapCameraListener = null;
        mMapMarkerClickListener = null;
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(false);
        }
        super.onDestroy();
    }

    /**
     * Checks the state of the route on the map. If it is not on the map, the relevant info will be
     * added. Otherwise, it will be unselected on the map
     *
     * @since 43
     * @param route the bus route selected
     */
    @Override
    public void onSelectBusRoute(Route route) {
        selectFromList(route);
    }

    @Override
    public void onDeselectBusRoute(Route route) {
        deselectFromList(route);
    }

    /**
     * Adds the route and bus (indirectly via buses) to the map
     *
     * @since 43
     * @param route - the route and its info to be added
     */
    private void selectFromList(Route route) {
        selectPolyline(route);
    }


    /**
     * Adds the bus route line to the map
     *
     * @since 43
     * @param routeInfo - the route and its info to add
     */
    private void selectPolyline(Route routeInfo) {
        String route = routeInfo.getRoute();
        List<Polyline> polylines = routeLines.get(route);

        if (polylines == null || polylines.isEmpty()) {
            new RequestLine(mMap,
                    routeLines,
                    route,
                    routeInfo.getRouteColor(),
                    zoom,
                    R.integer.zoom_level,
                    transitStop, this).execute();
        } else if (!polylines.get(0).isVisible()) {
            setVisiblePolylines(polylines, true);
            transitStop.updateAddRoutes(route, zoom, R.integer.zoom_level);
        }
    }

    /**
     * Removes the bus (indirectly) and the route line if it was on the map
     * @since 43
     * @param route - the route to remove and its info
     */
    private void deselectFromList(Route route) {
        removeBuses();
        Timber.d("removed_bus", route.getRoute());
        deselectPolyline(route.getRoute());
        stopTimer();
    }

    /**
     * Removes the route line if it was on the map
     * @param route - the route to remove by its string
     */
    private void deselectPolyline(String route) {
        List<Polyline> polylines = routeLines.get(route);
        if (polylines != null) {
            if (!polylines.isEmpty() && polylines.get(0).isVisible()) {
                setVisiblePolylines(polylines, false);
                transitStop.removeRoute(route);
            } else {
                routeLines.remove(route);
            }
        }
    }

    /**
     * sets a visible or invisible polylines for a route
     *
     * @param polylines  list of polylines
     * @param visibility whether or not the polylines are visible or not
     */
    private void setVisiblePolylines(List<Polyline> polylines, boolean visibility) {
        for (Polyline polyline : polylines) {
            polyline.setVisible(visibility);
        }
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
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Polls self on the map and then centers the map on Pittsburgh or you if you're in Pittsburgh..
     */
    private void centerMap() {
        Timber.d("location_changed", "centering map in centerMap()");
        if (currentLocation != null) {
            latitude = currentLocation.getLatitude();
            longitude = currentLocation.getLongitude();
            zoom = 15.0f;
            Timber.d("location_changed", "current location set in centerMap()");
        }
        Timber.d("savedInstance", "saved? " + inSavedState);
        Timber.d("savedInstance", "lat=" + latitude);
        Timber.d("savedInstance", "long=" + longitude);
        Timber.d("savedInstance", "zoom=" + zoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }

    /**
     * Set up observables for updating Google Maps. The call tree starts from
     * {@link #onMapReady}.
     *
     * @since 57
     */
    private void setupMapObservables() {

        Observable<BustimeVehicleResponse> vehicleIntervalObservable = Observable
                .interval(0, 10, TimeUnit.SECONDS)
                .filter((aLong) -> mMap != null)
                .flatMap(aLong -> {
                    Timber.d("vehicle_observable", "This ran " + Long.toString(aLong) + " times");
                    return patApiClient.getVehicles(collectionToString(mNavigationDrawerFragment.getSelectedRoutes()), BuildConfig.PAT_API_KEY);
                })
                .map(VehicleResponse::getBustimeResponse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .share();


        vehicleUpdateObservable = vehicleIntervalObservable.flatMap(
                bustimeVehicleResponse -> {
                    Timber.d("vehicle_observable_update", "getting vehicles");
                    return Observable.from(bustimeVehicleResponse.getVehicle());
                })
                .map(makeBitmaps());

        vehicleErrorObservable = vehicleIntervalObservable
                .map(BustimeVehicleResponse::getProcessedErrors)
                .distinctUntilChanged()
                .flatMap(errorMap -> {
                    Timber.d("vehicle_observable_error", "getting vehicle errors");
                    return Observable.from(errorMap.entrySet());
                })
                .map(transformSingleMessage());
    }

    /**
     * Adds markers to map.
     * This is only called when we resume the map
     * This is done in a thread.
     * This will also create the bus update observables.
     */
    protected void setUpMap() {
        Timber.d("setup_map", "If this runs 2+ in activity cycle, this is a problem");
        setMapListeners();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        setupMapObservables();
        restoreBuses();
    }

    /**
     * Restore buses by checking icons then adding them to map
     * @since 50
     */
    public void restoreBuses() {
        clearAndAddToMap();
    }

    /**
     * Creates
     */
    private void createListeners() {
        if (mMapCameraListener == null) {
            mMapCameraListener = cameraPosition -> {
                if (!inSavedState)
                    inSavedState = true;
                latitude = cameraPosition.target.latitude;
                longitude = cameraPosition.target.longitude;
                if (zoom != cameraPosition.zoom) {
                    zoom = cameraPosition.zoom;
                    transitStop.checkAllVisibility(zoom, R.integer.zoom_level);
                }
            };
        }
        if (mMapMarkerClickListener == null) {
            mMapMarkerClickListener = marker -> {

                if (marker != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 400, null);
                    new RequestPredictions(getApplicationContext(), marker, mNavigationDrawerFragment.getSelectedRoutes()).execute(marker.getTitle());
                    return true;
                }
                return false;
            };
        }
    }

    /**
     * set the map listeners
     */
    private void setMapListeners() {
        if (mMap != null) {
            createListeners();
            mMap.setInfoWindowAdapter(new ETAWindowAdapter(getLayoutInflater()));
            mMap.setOnCameraChangeListener(mMapCameraListener);

                /*
                TODO:
                a. Make an XML PullParser for getpredictions (all we need is bus route: <list of 3 times>)
                b. update snippet with the times: marker.setSnippet
                c. Make the snippet follow Google Maps time implementation!!!
                d. getpredictions&stpid=marker.getTitle().<regex on \(.+\)> since this is where the stop id is to get stop id.
                 */
            mMap.setOnMarkerClickListener(mMapMarkerClickListener);
        }
    }

    /**
     * This is the method to restore polylines.
     *
     * @since 43
     */
    protected void restorePolylines() {
        Route currentRoute;
        for (String route : mNavigationDrawerFragment.getSelectedRoutes()) {
            currentRoute = mNavigationDrawerFragment.getSelectedRoute(route);
            selectPolyline(currentRoute);
//            mNavigationDrawerFragment.setTrue(currentRoute.getListPosition());
        }
    }

    /**
     * Stops the bus refresh, then adds buses to the map
     */
    private void clearAndAddToMap() {
        if (mMap != null) {
            Timber.d("stop_add_buses", mNavigationDrawerFragment.getSelectedRoutes().toString());
            stopTimer();
            addBuses();
        }

//        System.out.println("Added buses");
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
     * This creates an observer to either update or add the buses to the map.
     * @return the vehicle update observer
     * @since 55
     */
    private Subscriber<VehicleBitmap> vehicleBusUpdateObservable() {
        return new Subscriber<VehicleBitmap>() {

            private boolean showedErrors = false;

            @Override
            public void onCompleted() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
                String cDateTime = dateFormat.format(new Date());
                Timber.d("Bus map updates finished updates at %s", cDateTime);
            }

            @Override
            public void onError(Throwable e) {
                if (e.getMessage() != null && e.getLocalizedMessage() != null && !showedErrors) {
                    showedErrors = true;
                    if (e instanceof IOException) {
                        showToast(getString(R.string.retrofit_network_error), Toast.LENGTH_SHORT);
                    } else if (e instanceof HttpException) {
                        HttpException http = (HttpException) e;
                        showToast(http.code() + " " + http.message() + ": "
                                + getString(R.string.retrofit_http_error), Toast.LENGTH_SHORT);
                    } else {
                        showToast(getString(R.string.retrofit_conversion_error), Toast.LENGTH_SHORT);
                    }
                    Timber.e("bus_vehicle_error", e.getMessage());
                }
                Timber.e(e.getClass().getName());
                Timber.e("Vehicle Observable error. %s\n%s",
                        e.getClass().getName(),
                        Log.getStackTraceString(e)
                );
            }

            @Override
            public void onNext(VehicleBitmap vehicleBitmap) {
                addOrUpdateMarkers(vehicleBitmap);
            }

            /**
             * Handle vehicle updates and adds...
             * <ul>
             *     <li>add marker if not on {@link SelectTransit#busMarkers} - {@link #addMarker(VehicleBitmap)}</li>
             *     <li>update marker if in {@link SelectTransit#busMarkers} - {@link #updateMarker(Vehicle, Marker)}</li>
             * </ul>
             *
             * @since 46
             * @param vehicleBitmap - vehicle to be added
             */
            private void addOrUpdateMarkers(VehicleBitmap vehicleBitmap) {
                int vid = vehicleBitmap.getVehicle().getVid();
                Marker marker = busMarkers.get(vid);
                if (marker == null) {
                    addMarker(vehicleBitmap);
                } else {
                    updateMarker(vehicleBitmap.getVehicle(), marker);
                }

            }

            /**
             * adds marker not in {@link SelectTransit#busMarkers}
             * @since 46
             * @param vehicleBitmap - the vehicle to add
             */
            private void addMarker(VehicleBitmap vehicleBitmap) {
                Vehicle vehicle = vehicleBitmap.getVehicle();
                Timber.d("marker_add", "adding_marker " + Integer.toString(vehicle.getVid()));
                busMarkers.put(vehicle.getVid(), mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(vehicle.getLat(), vehicle.getLon()))
                                .title(vehicle.getRt() + "(" + vehicle.getVid() + ") " + vehicle.getDes() + (vehicle.isDly() ? " - Delayed" : ""))
                                .draggable(false)
                                .rotation(vehicle.getHdg())
                                .icon(BitmapDescriptorFactory.fromBitmap(vehicleBitmap.getBitmap()))
                                .anchor((float) 0.5, (float) 0.5)
                                .flat(true)
                ));
            }

            /**
             * Updates marker information on map
             * @since 46
             * @param vehicle - vehicle to update
             * @param marker - marker to update
             */
            private void updateMarker(Vehicle vehicle, Marker marker) {
                Timber.d("marker_update", "updating_pointer");
                marker.setTitle(vehicle.getRt() + "(" + vehicle.getVid() + ") " + vehicle.getDes() + (vehicle.isDly() ? " - Delayed" : ""));
                marker.setPosition(new LatLng(vehicle.getLat(), vehicle.getLon()));
                marker.setRotation(vehicle.getHdg());
            }
        };
    }

    /**
     * This is the vehicle update/add Port Authority API observer that will print each processed error
     * into a Toast.
     * @return the vehicle update observer
     * @since 55
     */
    private Subscriber<ErrorMessage> vehicleErrorObserver() {
        Timber.d("vehicle_error_observer", "restarting the vehicle error observer");
        return new Subscriber<ErrorMessage>() {

            @Override
            public void onCompleted() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
                String cDateTime = dateFormat.format(new Date());
                Timber.d("vehicle_error_complete", "Bus map error updates finished updates at " + cDateTime);

            }

            @Override
            public void onError(Throwable e) {
                if (e.getLocalizedMessage() != null)
                    Timber.e("vehicle_error_errs", e.getLocalizedMessage());
                Timber.e("vehicle_error_errs", Log.getStackTraceString(e));
            }

            @Override
            public void onNext(ErrorMessage errorMessage) {
                if (errorMessage != null && errorMessage.getMessage() != null) {
                    showToast(errorMessage.getMessage() +
                                    (errorMessage.getParameters() != null ? ": " + errorMessage.getParameters() : ""),
                            Toast.LENGTH_SHORT);
                }

            }
        };
    }

    /**
     * Creates an anonymous class to make messages more human-readable.
     * @return a closure to create a human-readable {@link ErrorMessage}
     * @since 55
     */
    private Func1<Map.Entry<String, ArrayList<String>>, ErrorMessage> transformSingleMessage() {
        return new Func1<Map.Entry<String, ArrayList<String>>, ErrorMessage>() {

            /**
             * Transforms the original message to a user-readable message.
             * @param originalMessage The original Port Authority API message
             * @return a user-readable message from the original API message
             */
            private String transformMessage(String originalMessage) {
                if (originalMessage != null) {
                    if (originalMessage.contains("No data found for parameter")) {
                        return getString(R.string.no_vehicle_error);
                    } else if (originalMessage.contains("specified") && originalMessage.contains("rt")) {
                        return getString(R.string.no_routes_selected);
                    } else if (originalMessage.contains("Transaction limit for current day has been exceeded")) {
                        return getString(R.string.pat_api_exceeded);
                    } else
                        return originalMessage;
                }
                return null;

            }

            @Override
            public ErrorMessage call(Map.Entry<String, ArrayList<String>> processedMessage) {
                return new ErrorMessage(transformMessage(processedMessage.getKey()), processedMessage.getValue());
            }
        };
    }

    private Func1<Vehicle, VehicleBitmap> makeBitmaps() {
        return new Func1<Vehicle, VehicleBitmap>() {

            private HashMap<String, Bitmap> busIconCache = new HashMap<>(mNavigationDrawerFragment.getAmountSelected());

            @Override
            public VehicleBitmap call(Vehicle vehicle) {
                String routeName = vehicle.getRt();
                if (busIconCache.containsKey(routeName)) {
                    return new VehicleBitmap(vehicle, busIconCache.get(routeName));
                } else {
                    Bitmap icon = makeBitmap(mNavigationDrawerFragment.getSelectedRoute(routeName));
                    busIconCache.put(routeName, icon);
                    return new VehicleBitmap(vehicle, icon);
                }
            }

            private Bitmap makeBitmap(Route route) {
                Bitmap bus_icon = BitmapFactory.decodeResource(getResources(), R.drawable.bus_icon);
                Bitmap busicon = Bitmap.createBitmap(bus_icon.getWidth(), bus_icon.getHeight(), bus_icon.getConfig());
                Canvas canvas = new Canvas(busicon);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColorFilter(new PorterDuffColorFilter(route.getRouteColor(), PorterDuff.Mode.MULTIPLY));
                canvas.drawBitmap(bus_icon, 0f, 0f, paint);
                drawText(canvas, bus_icon, getResources().getDisplayMetrics().density, route.getRoute(), route.getColorAsString());
                return busicon;
            }

            private void drawText(Canvas canvas, Bitmap bus_icon, float fontScale, String routeNumber, String routeColor) {
                int currentColor = Color.parseColor(routeColor);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(isLight(currentColor) ? Color.BLACK : Color.WHITE);
                paint.setTextSize(8 * fontScale);
                Rect fontBounds = new Rect();
                paint.getTextBounds(routeNumber, 0, routeNumber.length(), fontBounds);
                int x = bus_icon.getWidth() / 2;
                int y = (int) ((double) bus_icon.getHeight() / 1.25);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(routeNumber, x, y, paint);
            }

            /**
             * Decides whether or not the color (background color) is light or not.
             * <p>
             * Formula was taken from here:
             * http://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
             *
             * @param color the background color being fed
             * @return whether or not the background color is light or not (.345 is the current threshold)
             * @since 47
             */
            private boolean isLight(int color) {
                return 1.0 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 < .5;
            }
        };
    }

    /**
     * adds buses to map... or else the map will be clear.
     *
     * It updates the map every 10 seconds and makes sure that on the other threads, it will
     * process the Port Authority API call the Reactive way.
     */
    private void addBuses() {
        Timber.d("adding buses", mNavigationDrawerFragment.getSelectedRoutes().toString());

        // run the vehicle updater
        vehicleSubscription = vehicleUpdateObservable
                .subscribe(vehicleBusUpdateObservable());

        // run the vehicle update observer and print it when the error message object changes
        vehicleErrorSubscription = vehicleErrorObservable.subscribe(vehicleErrorObserver());

    }

    /**
     * Removes bus markers from {@link #busMarkers}
     * @param routesOnMap - markers to remove by vid
     */
    private void removeBuses(Set<Integer> routesOnMap) {
        Timber.d("remove_buses", "removing buses");
        if (routesOnMap != null) {
            for (Integer vid : routesOnMap) {
                if (vid != null) {
                    Marker marker = busMarkers.remove(vid);
                    if (marker != null) {
                        Timber.d("remove_buses", Integer.toString(vid) + " removed");
                        marker.remove();
                    }
                }

            }
        }

    }

    /**
     * Removes all buses
     */
    private void removeBuses() {
        if (busMarkers != null) {
            Timber.d("bus_remove", "buses removed");
            for (Marker busMarker : busMarkers.values()) {
                busMarker.remove();
            }
            busMarkers.clear();
        }

    }

    /**
     *
     * @param data - the data in a collection to add
     * @param <T> - Any Object that extends {@link Object}
     * @since 46
     * @return a comma-delim strings of data
     */
    private <T> String collectionToString(Collection<T> data) {
        int size = data.size();
        int i = 0;
        StringBuilder buf = new StringBuilder();
        for (T datum : data) {
            buf.append(datum);
            if (++i < size)
                buf.append(',');
        }
        return buf.toString();
    }

    /**
     * General way to make a snackbar
     *
     * @since 46
     * @param string - the string to add to on the
     * @param showLength - how long to show the snackbar
     */
    private void makeSnackbar(String string, int showLength) {
        if (string != null && string.length() > 0) {

            Snackbar.make(mainLayout,
                    string, showLength
            ).show();
        }

    }

    /**
     * Stops the vehicle subscriptions
     */
    private void stopTimer() {
        if (vehicleSubscription != null)
            vehicleSubscription.unsubscribe();
        if (vehicleErrorSubscription != null)
            vehicleErrorSubscription.unsubscribe();
    }

    /**
     * General method to clear the map.
     */
    protected void clearMap() {
        /*
        I noticed this is probably not a good method to use... you clear choices... but you also must
        clear the navigation drawer
         */
        Timber.d("map_cleared", "map_cleared");
        if (mMap != null) {
            stopTimer();
            mMap.clear();
            routeLines = new ConcurrentHashMap<>(getResources().getInteger(R.integer.max_checked));
            transitStop = new TransitStop();
            removeBuses();
            mNavigationDrawerFragment.clearSelection();
//            mNavigationDrawerFragment.clearSelection();
        }
    }

    public void onBackPressed() {
        if (!mNavigationDrawerFragment.closeDrawer())
            super.onBackPressed();
    }

    private void showOkDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SelectTransit.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    /**
     * Checks if the current location is in the immediate vicinity
     * @param currentLocation The current location.
     * @return whether or not your device is in Pittsburgh
     */
    private boolean isInPittsburgh(Location currentLocation) {
        return currentLocation != null &&
                (
                        (currentLocation.getLatitude() > 39.859673 && currentLocation.getLatitude() < 40.992847) &&
                                (currentLocation.getLongitude() > -80.372815 && currentLocation.getLongitude() < -79.414258)
                );
    }

    /**
     * Give a request to find the location of the user using the ACCESS_FINE_LOCATION permission
     * and Google Play Service's Location APIs.
     *
     * @since 48
     */
    private void requestLocation() {
        LocationRequest gLocationRequest = LocationRequest.create();
        gLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        gLocationRequest.setInterval(1000);
        //        MapsInitializer.initialize(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleAPIClient);
        //        Timber.d("location_changed", "What is going on here");
        if (currentLocation == null) {
            Timber.d("location_changed", "current location is null");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleAPIClient, gLocationRequest, this);
            if (currentLocation != null) {
                if (mMap != null && isInPittsburgh(currentLocation))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15.0f));
            }
        } else if (!inSavedState) {
            if (mMap != null) {
                Timber.d("location_changed", "current location is not null");
                if (isInPittsburgh(currentLocation)) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(
                                    currentLocation.getLatitude(),
                                    currentLocation.getLongitude()),
                            15.0f));
                }

            }

        }
    }

    /**
     * Requests the ACCESS_FINE_LOCATION permission to enable centering Google Maps on the user. If
     * granted, it will request location updates.
     *
     * @since 48
     */
    private void requestLocationPermissions() {
        int hasLocationPermissions = ContextCompat.checkSelfPermission(SelectTransit.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermissions != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(SelectTransit.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showOkDialog(getString(R.string.location_permission_message),
                        (dialog, which) -> ActivityCompat.requestPermissions(SelectTransit.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_REQUEST_CODE));
            }
            ActivityCompat.requestPermissions(SelectTransit.this,
                    new String[]{Manifest.permission_group.LOCATION},
                    LOCATION_REQUEST_CODE);
            return;
        }
        requestLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    mMap != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    /**
     * Part of the GoogleApiClient connection. If it is connected
     *
     * @param bundle the saved state of the app
     */
    @Override
    public void onConnected(Bundle bundle) {
        requestLocationPermissions();
    }

    /**
     * Not sure what to do with this...
     *
     * @param i dunno...
     */
    @Override
    public void onConnectionSuspended(int i) {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleAPIClient, this);
    }

    /**
     * This is where the GoogleApiClient will fail. So far, just have it stored into a log...
     *
     * @param connectionResult The specified code if the GoogleApiClient fails to connect!
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("Google API Error", connectionResult.toString());
//        centerMap();
        Toast.makeText(this, "Google connection failed, please try again later", Toast.LENGTH_SHORT).show();

//        TODO: Perhaps based on the connection result, we can close and make custom error messages.
    }

    @Override
    public void onLocationChanged(Location location) {
        Timber.d("location_changed", "onLocationChanged() " + location.toString());
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15.0f));

        currentLocation = location;
        if(mMap != null && notCentered) {
            if(isInPittsburgh(currentLocation))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15.0f));
            notCentered = false;
        }
    }


    @Override @NonNull public Set<String> getSelectedRoutes() {
        return mNavigationDrawerFragment.getSelectedRoutes();
    }

    @Override public Route getSelectedRoute(@NonNull String routeName) {
        return mNavigationDrawerFragment.getSelectedRoute(routeName);
    }

    @Override
    public void onSelectBusRouteObservable(@NonNull Observable<RouteSelection> routeSelectionObservable) {

    }

    @Override
    public void onUnselectBusRouteObservable(@NonNull Observable<RouteSelection> routeSelectionObservable) {

    }

    public Observable<RouteSelection> getSelectionPublishSubject() {
        return mNavigationDrawerFragment.getListSelectionSubject();
    }
}
