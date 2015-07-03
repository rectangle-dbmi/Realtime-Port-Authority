package rectangledbmi.com.pittsburghrealtimetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import rectangledbmi.com.pittsburghrealtimetracker.handlers.BusUpdateTask;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestLine;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestPredictions;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.extend.ETAWindowAdapter;
import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStop;

/**
 * This is the main activity of the Realtime Tracker...
 */
public class SelectTransit extends AppCompatActivity implements
        NavigationDrawerFragment.BusListCallbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback, LocationListener,
        BusUpdateTask.UpdateBusConnectionCallbacks {

    private static final String LINES_LAST_UPDATED = "lines_last_updated";

    /**
     * Saved instance of the buses that are selected
     */
    private final static String BUS_SELECT_STATE = "busesSelected";

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
     * list of buses
     * <p/>
     * public because we want to clear this list...
     */
    private Set<String> buses;

    /**
     * This is the object that updates the UI every 10 seconds
     */
    private Timer timer;

    /**
     * This is the object that creates the action to update the UI
     */
    private TimerTask task;

    /**
     * This is the client that will center the map on the person using the app.
     */
    private GoogleApiClient client;

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
     * Reminds us if the bus task is running or not to update the buses (workaround for asynctask ******)
     */
    private boolean isBusTaskRunning;

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
     * Google location request to give us location-based context
     * @since 42
     */
    LocationRequest gLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_transit);
        checkSDCardData();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        setGoogleApiClient();
        createBusList();
        MapFragment mapFragment = (MapFragment) getFragmentManager()
            .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
            //sets up the map
        inSavedState = false;
        enableHttpResponseCache();
        restoreInstanceState(savedInstanceState);
        isBusTaskRunning = false;
        notCentered = false;
            //        zoom = 15.0f;
//        } else {
//
//        }
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
        Log.d("data_storage", data.getName());
        if(!data.exists())
            data.mkdirs();
        File lineInfo = new File(data, "/lineinfo");
        if(!data.exists())
            data.mkdirs();
        Log.d("updated time", Long.toString(lastUpdated));
        if(lastUpdated != -1 && ((System.currentTimeMillis() - lastUpdated) / 1000 / 60 / 60) > 24) {
//            Log.d("update time....", Long.toString((System.currentTimeMillis() - lastUpdated) / 1000 / 60 / 60));
            if(lineInfo.exists()) {
                Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_WEEK);
                Calendar o = Calendar.getInstance();
                o.setTimeInMillis(lastUpdated);
                int oldDay = o.get(Calendar.DAY_OF_WEEK);
                if(day == Calendar.FRIDAY || oldDay >= day) {
                    File[] files = lineInfo.listFiles();
                    sp.edit().putLong(LINES_LAST_UPDATED, System.currentTimeMillis()).apply();
                    if(files != null) {
                        for (File file : files) {
                            file.delete();
                        }
                    }
                }
            }
        }

        if(lineInfo.listFiles() == null || lineInfo.listFiles().length == 0) {
           sp.edit().putLong(LINES_LAST_UPDATED, System.currentTimeMillis()).apply();
        }
    }

    /**
     * Sets the application google Api Location client
     */
    private void setGoogleApiClient() {
        client = new GoogleApiClient.Builder(getApplicationContext())
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
            if(fetch == null) {
                fetch = getCacheDir();
            }
            File httpCacheDir = new File(fetch, "http");
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
            Log.d("HTTP_response_cache", "HTTP response cache is unavailable.");
        }
    }

    /**
     * Restores the instance state of the program including the preferences
     *
     * @param savedInstanceState the saved instances of the app
     */
    protected void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Log.d("savedInstance_restore", "instance saved");
            Log.d("savedInstance_s", "lat="+savedInstanceState.getDouble(LAST_LATITUDE));
            inSavedState = true;
            latitude = savedInstanceState.getDouble(LAST_LATITUDE);
            longitude = savedInstanceState.getDouble(LAST_LONGITUDE);
            zoom = savedInstanceState.getFloat(LAST_ZOOM);


        } else {
            Log.d("savedInstance", "default location instead");

            defaultCameraLocation();
        }
        Log.d("savedInstance_restore", "saved? " + inSavedState);
        Log.d("savedInstance_restore", "lat="+latitude);
        Log.d("savedInstance_restore", "long="+longitude);
        if (transitStop == null) {
            transitStop = new TransitStop();
        }
        restorePreferences();

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
//        Log.i("Date", c.get(Calendar.DATE));
//        ArrayList<String> list = new ArrayList<String>(buses.size());
//        list.addAll(buses);
//        savedInstanceState.putStringArrayList(BUS_SELECT_STATE, list);
        if (mMap != null) {
            savedInstanceState.putDouble(LAST_LATITUDE, latitude);
            savedInstanceState.putDouble(LAST_LONGITUDE,longitude);
            savedInstanceState.putFloat(LAST_ZOOM, zoom);
            Log.d("savedInstance_osi", "saved? " + inSavedState);
            Log.d("savedInstance_osi", "lat="+latitude);
            Log.d("savedInstance_osi", "long="+longitude);
            Log.d("savedInstance_osi", "zoom=" + zoom);
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
    private void createBusList() {
        routeLines = new ConcurrentHashMap<>(getResources().getInteger(R.integer.max_checked));
        busMarkers = new ConcurrentHashMap<>(100);
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(!isNetworkAvailable()) {
//            DataRequiredDialog dialog = new DataRequiredDialog();
//            dialog.show(getSupportFragmentManager(), "data required");
//        }
//        if (mMap != null) {
//            setUpMap();
//        } else
////            setUpMapIfNeeded();
//        if(buses != null) {
            clearAndAddToMap();
//        }
    }

    /**
     * Restores the the set of buses....
     */
    private void restorePreferences() {
        Log.d("restoring buses", "Attempting to restore buses.");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        buses = new HashSet<String>(sp.getStringSet(BUS_SELECT_STATE, Collections.synchronizedSet(new HashSet<String>(getResources().getInteger(R.integer.max_checked)))));
    }

    protected void onPause() {
        Log.d("main_destroy", "SelectTransit onPause");
        savePreferences();
        stopTimer();
        super.onPause();

    }

    @Override
    protected void onStop() {

        Log.d("main_destroy", "SelectTransit onStop");
        client.disconnect();
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
        super.onStop();
    }

    /**
     * Place to save preferences....
     */
    private void savePreferences() {
        Log.d("saving buses", buses.toString());
        Log.d("selected size", Integer.toString(buses.size()));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spe = sp.edit();
        spe.putStringSet(BUS_SELECT_STATE, buses).apply();
//        sp.edit().putInt(BUSLIST_SIZE, getResources().getStringArray(R.array.buses).length).apply();
        spe.apply();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        clearMap();
//
//
//    }

//    /**
//     * Gets called from NavigationDrawerFragment's onclick? Supposed to...
//     *
//     * @param position the list selection selected starting from 0
//     */
//    @Override
//    public void onNavigationDrawerItemSelected(int position) {
//        // update the main content by replacing fragments
//        onSectionAttached(position);
//    }
//
//    /**
//     * Gets called when one of the buses is pressed. Take note routes will always have more than one
//     * polyline.
//     *
//     * @param number which bus in the list is pressed
//     */
//    public void onSectionAttached(int number) {
//        if(mNavigationDrawerFragment.getAmountSelected() >= 0 &&
//                mNavigationDrawerFragment.getAmountSelected() <= getResources().getInteger(R.integer.max_checked)) {
//            if(mNavigationDrawerFragment.isPositionSelected(number)) {
//                selectFromList(number);
//            } else {
//                deselectFromList(number);
//            }
//            Log.d("buses_attached", buses.toString());
//        }
////        setPolyline(number);
////        setList(getResources().getStringArray(R.array.buses)[number]);
//    }

    /**
     * Checks the state of the route on the map. If it is not on the map, the relevant info will be
     * added. Otherwise, it will be unselected on the map
     *
     * @since 43
     * @param route the bus route selected
     */
    @Override
    public void onBusRouteSelected(Route route) {
        if (mNavigationDrawerFragment.getAmountSelected() >= 0 &&
                mNavigationDrawerFragment.getAmountSelected() <= getResources().getInteger(R.integer.max_checked)) {
            if (mNavigationDrawerFragment.isPositionSelected(route.getListPosition()))
                selectFromList(route);
            else
                deselectFromList(route);
        }
    }

    /**
     * Adds the route and bus (indirectly via buses) to the map
     *
     * @since 43
     * @param route - the route and its info to be added
     */
    private void selectFromList(Route route) {
        buses.add(route.getRoute());
        selectPolyline(route);
    }

    /**
     * Adds the bus route line to the map
     *
     * @since 43
     * @param routeInfo - the route and its info to add
     */
    private synchronized void selectPolyline(Route routeInfo) {
        String route = routeInfo.getRoute();
        List<Polyline> polylines = routeLines.get(route);

        if (polylines == null || polylines.isEmpty()) {
            new RequestLine(mMap, routeLines, route, routeInfo.getRouteColor(), zoom, Float.parseFloat(getString(R.string.zoom_level)), transitStop, this).execute();
        } else if(!polylines.get(0).isVisible()) {
            setVisiblePolylines(polylines, true);
            transitStop.updateAddRoutes(route, zoom, Float.parseFloat(getString(R.string.zoom_level)));
        }
    }

    /**
     * Removes the bus (indirectly) and the route line if it was on the map
     * @since 43
     * @param route - the route to remove and its info
     */
    private void deselectFromList(Route route) {
        buses.remove(route.getRoute());
        Log.d("removed_bus", route.getRoute());
        deselectPolyline(route.getRoute());
    }

    /**
     * Removes the route line if it was on the map
     * @param route - the route to remove by its string
     */
    private synchronized void deselectPolyline(String route) {
        List<Polyline> polylines = routeLines.get(route);
        if(polylines != null) {
            if(!polylines.isEmpty() && polylines.get(0).isVisible()) {
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
                Snackbar.make(findViewById(android.R.id.content),
                "Material Design bugged out on your device. Please report this to the Play Store Email if this pops up.", Snackbar.LENGTH_LONG).show();
//                Toast.makeText(this, "Material Design bugged out on your device. Please report this to the Play Store Email if this pops up.", Toast.LENGTH_LONG).show();
            }
        }
        try {
            ActionBar t = getSupportActionBar();
            if(t != null) t.setDisplayHomeAsUpEnabled(true);
        } catch(NullPointerException e) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Material Design bugged out on your device. Please report this to the Play Store Email if this pops up.", Snackbar.LENGTH_LONG).show();
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
            restoreActionBar();
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

        if(currentLocation != null) {
            latitude = currentLocation.getLatitude();
            longitude = currentLocation.getLongitude();
            zoom = 15.0f;
            Log.d("location_changed", "current location set in centerMap()");
        }
        Log.d("savedInstance", "saved? " + inSavedState);
        Log.d("savedInstance", "lat="+latitude);
        Log.d("savedInstance", "long=" + longitude);
        Log.d("savedInstance", "zoom=" + zoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }

    /**
     * Adds markers to map.
     * This is only called when we resume the map
     * This is done in a thread.
     */
    protected void setUpMap() {
//        System.out.println("restore...");
//        clearMap();
        setMapListeners();
        mMap.setMyLocationEnabled(true);
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        if (sp.getInt(BUSLIST_SIZE, -1) == getResources().getStringArray(R.array.buses).length) {
//            buses.clear();
        clearAndAddToMap();
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    restorePolylines();
//                }
//            }, 100);

//        }
    }


    /**
     * set the map listeners
     */
    private void setMapListeners() {
        // Do a null check to confirm that we have not already instantiated the map.
//        if (mMap == null) {
////            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
//            // Check if we were successful in obtaining the map.
        if (mMap != null) {

            mMap.setInfoWindowAdapter(new ETAWindowAdapter(getLayoutInflater()));
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (!inSavedState)
                        inSavedState = true;
                    latitude = cameraPosition.target.latitude;
                    longitude = cameraPosition.target.longitude;
                    if (zoom != cameraPosition.zoom) {
                        zoom = cameraPosition.zoom;

                        transitStop.checkAllVisibility(zoom, Float.parseFloat(getString(R.string.zoom_level)));
                    }
                }
            });

                /*
                TODO:
                a. Make an XML PullParser for getpredictions (all we need is bus route: <list of 3 times>)
                b. update snippet with the times: marker.setSnippet
                c. Make the snippet follow Google Maps time implementation!!!
                d. getpredictions&stpid=marker.getTitle().<regex on \(.+\)> since this is where the stop id is to get stop id.
                 */
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    if (marker != null) {
//                            final Marker mark = marker;
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 400, null);
                        new RequestPredictions(getApplicationContext(), marker, buses).execute(marker.getTitle());


//                            String message = "Stop 1:\tPRDTM\nStop 2:\tPRDTM";
//                            String title = "Bus";
//                            showDialog(message, title);

                        return true;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * This is the method to restore polylines.
     *
     * @since 43
     */
    protected void restorePolylines() {
        Route currentRoute;
        for(String route : buses) {
            currentRoute = mNavigationDrawerFragment.getSelectedRoute(route);
            selectPolyline(currentRoute);
            mNavigationDrawerFragment.setTrue(currentRoute.getListPosition());
        }
    }

    /**
     * Stops the bus refresh, then adds buses to the map
     */
    protected synchronized void clearAndAddToMap() {
        if (mMap != null) {
            Log.d("stop_add_buses", buses.toString());
            stopTimer();
            addBuses();
        }

//        System.out.println("Added buses");
    }

    /**
     * adds buses to map. or else the map will be clear...
     */
    protected synchronized void addBuses() {
        Log.d("adding buses", buses.toString());
        final Handler handler = new Handler();
        timer = new Timer();
        final Context context = this;
        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    BusUpdateTask req;

                    public void run() {
                        if (!buses.isEmpty()) {
//                            clearMap();

                            req = new BusUpdateTask(mMap, buses, busMarkers, context);
//                            req = new BusUpdateTask(mMap, buses, context);
                            req.execute();
                        }
                    }
                });
            }
        };
        if (!buses.isEmpty()) {
            timer.schedule(task, 0, 10000); //it executes this every 10000ms
        }
    }


    private synchronized void removeBuses() {
        if (busMarkers != null) {
            Log.d("bus_remove", "buses removed");
            for (Marker busMarker : busMarkers.values()) {
                busMarker.remove();
            }
//            busMarkers.clear();
            busMarkers = null;
        }

    }

    /**
     * Stops the timer task
     */
    private synchronized void stopTimer() {
//        removeBuses();
        // wait for the bus task to finish!

        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        if (task != null) {
            task.cancel();
        }
    }

    /**
     * General method to clear the map.
     */
    protected void clearMap() {
        /*
        I noticed this is probably not a good method to use... you clear choices... but you also must
        clear the navigation drawer
         */
        Log.d("map_cleared", "map_cleared");
        if (mMap != null) {
            mMap.clear();
            routeLines = new ConcurrentHashMap<>(getResources().getInteger(R.integer.max_checked));
            transitStop = new TransitStop();
            removeBuses();
            clearBuses();
            mNavigationDrawerFragment.clearSelection();
//            mNavigationDrawerFragment.clearSelection();
        }
    }

    /**
     * The list of buses that are selected
     */
    protected void clearBuses() {
        if(buses != null) {
            Log.d("bus_remove", "buses cleared");
            buses.clear();
        }
    }

    public void onBackPressed() {
        if (!mNavigationDrawerFragment.closeDrawer())
            super.onBackPressed();
    }

    /**
     * Part of the GoogleApiClient connection. If it is connected
     *
     * @param bundle the saved state of the app
     */
    @Override
    public void onConnected(Bundle bundle) {
        /*
      This is the location request using FusedLocationAPI to get the person's last known location
     */
        gLocationRequest = LocationRequest.create();
        gLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        gLocationRequest.setInterval(1000);
//        MapsInitializer.initialize(this);
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(client);
//        Log.d("location_changed", "What is going on here");
        if (currentLocation == null) {
            Log.d("location_changed", "current location is null");
            LocationServices.FusedLocationApi.requestLocationUpdates(client, gLocationRequest, this);
            if (currentLocation != null) {
//                LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
                if (mMap != null)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15.0f));
            }
        } else if (!inSavedState) {
//            latitude = currentLocation.getLatitude();
//            longitude = currentLocation.getLongitude();
//            zoom = 15.0f;
            if (mMap != null)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15.0f));
        }
    }

    /**
     * Not sure what to do with this...
     *
     * @param i dunno...
     */
    @Override
    public void onConnectionSuspended(int i) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    }

    /**
     * This is where the GoogleApiClient will fail. So far, just have it stored into a log...
     *
     * @param connectionResult The specified code if the GoogleApiClient fails to connect!
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Google API Error", connectionResult.toString());
//        centerMap();
        Toast.makeText(this, "Google connection failed, please try again later", Toast.LENGTH_LONG).show();

//        TODO: Perhaps based on the connection result, we can close and make custom error messages.
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("location_changed", "onLocationChanged() " + location.toString());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15.0f));

        currentLocation = location;
        if(mMap != null && notCentered) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15.0f));
            notCentered = false;
        }
    }

    public void setBusMarkers(ConcurrentMap<Integer, Marker> busMarkers) {
        this.busMarkers = busMarkers;
    }

    public void setBusTaskRunning(boolean isBusTaskRunning) {
        this.isBusTaskRunning = isBusTaskRunning;
    }

    public boolean isBusTaskRunning() {
        return isBusTaskRunning;
    }

//    @Override
//    public void onBusInfoRetrieved(List<Bus> bl) {
//
//            Log.d("onPostExecute_task_st", "Task starting after thread ran");
//
//            if(bl == null || bl.isEmpty()) {
//                Toast.makeText(this, "Routes not found. Either there is no route information, no internet connection, or the API Call limit has been exceeded.", Toast.LENGTH_LONG).show();
//
//            } else {
//
//                ConcurrentMap<Integer, Marker> newBusMarkers = new ConcurrentHashMap<>(bl.size());
//                LatLng latlng;
//
//                for (Bus bus : bl) {
////                    Log.d("bus_info", bus.toString());
//                    latlng = new LatLng(bus.getLat(), bus.getLon());
////                    if (busMarkers != null) {
//                    updateMarker(bus, latlng, newBusMarkers);
////                    } else {
////                        addNewMarker(bus, latlng, newBusMarkers);
////                    }
//                }
//                removeOldBuses();
//                setBusMarkers(newBusMarkers);
//            }
//            Log.d("onPostExecute task_stop", "Task has stopped running");
//            setBusTaskRunning(false);
//    }
//
//    /**
//     * Updates the marker already on the map
//     *
//     * @param newBusMarkers the new bus list
//     * @param bus           the bus being updated
//     * @param latlng        the LatLng of the bus's location
//     */
//    private synchronized void updateMarker(Bus bus, LatLng latlng, ConcurrentMap<Integer, Marker> newBusMarkers) {
//        try {
//            Log.d("up_mark_enter", "entered_marker");
//            Marker mark = null;
//            if(busMarkers != null)
//                mark = busMarkers.remove(bus.getVid());
//            if (mark != null) {
//                Log.d("marker_update", "updating_pointer");
//                mark.setTitle(bus.getRt() + "(" + bus.getVid() + ") " + bus.getDes() + (bus.isDly() ? " - Delayed" : "" ));
//                mark.setPosition(latlng);
//                mark.setRotation(bus.getHdg());
//                mark.setSnippet("Speed: " + bus.getSpd());
//                newBusMarkers.putIfAbsent(bus.getVid(), mark);
//
//            } else {
//                addNewMarker(bus, latlng, newBusMarkers);
//            }
//        } catch (NullPointerException e) {
//            Log.e("bus_nullpointer", "Something went wrong while updating...");
//        } catch (IllegalArgumentException e) {
//            Log.e("bus_illegalargument", "Somehow the marker is missing");
//
//        }
//    }
//
//    /**
//     * Add a new marker to the map
//     *
//     * @param bus           the bus being added
//     * @param latlng        the location of the bus
//     * @param newBusMarkers the new bus list
//     */
//    private synchronized void addNewMarker(Bus bus, LatLng latlng, ConcurrentMap<Integer, Marker> newBusMarkers) {
////        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bus_template);
////        Paint paint = new Paint();
////        ColorFilter colorFilter = new LightingColorFilter(Color.RED, 0);
////        paint.setColorFilter(colorFilter);
////        Canvas canvas = new Canvas();
////        bitmap = canvas.drawBitmap(bitmap,);
//        Log.d("marker_add", "adding_marker");
//        MarkerOptions marker = new MarkerOptions()
//                .position(latlng)
//                .title(bus.getRt() + "(" + bus.getVid() + ") " + bus.getDes() + (bus.isDly() ? " - Delayed" : ""))
//                .snippet("Speed: " + bus.getSpd())
//                .draggable(false)
//                .rotation(bus.getHdg())
//                .icon(BitmapDescriptorFactory.fromResource(getDrawable(bus.getRt())))
//                .anchor((float) 0.453125, (float) 0.25)
//                .flat(true);
//        try {
//            Log.d("new_bus", newBusMarkers.toString());
//            Log.d("new_markeroptions", marker.getPosition().toString());
//            Log.d("new_markeroptions", marker.getTitle());
//            Log.d("new_markeroptions", marker.getSnippet());
//            Log.d("new_markeroptions", Float.toString(marker.getRotation()));
//            Log.d("new_mMap", mMap.toString());
////            Marker mapMarker = mMap.addMarker(marker);
////            mapMarker.setIcon(BitmapDescriptorFactory.fromAsset(bus.getRt() + ".png"));
//            newBusMarkers.put(bus.getVid(), mMap.addMarker(marker));
//        } catch (NullPointerException e) {
//            Log.e("null_new_bus", "mMap or marker is null");
//        }
//    }
//
//    /**
//     * Removes the no longer visible buses to the list.
//     */
//    private synchronized void removeOldBuses() {
//        if (busMarkers != null) {
//            for (Marker marker : busMarkers.values()) {
//                marker.remove();
//            }
//        }
//    }
//
//    private int getDrawable(String route) {
//        return getResources().getIdentifier("bus_" + route.toLowerCase(), "drawable", getPackageName());
////        return context.getResources().getDrawable(resourceId);
//    }
}
