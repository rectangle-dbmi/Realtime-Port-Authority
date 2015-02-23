package rectangledbmi.com.pittsburghrealtimetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestLine;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestPredictions;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestTask;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.extend.ETAWindowAdapter;
import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStop;

/**
 * This is the main activity of the
 */
public class SelectTransit extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String LINES_LAST_UPDATED = "lines_last_updated";

    private static final String BUSLIST_SIZE = "buslist_size";
    /**
     * saved indexes from selection
     */
    private static final String STATE_SELECTED_POSITIONS = "selected_navigation_drawer_positions";
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
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

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
//    private ConcurrentMap<String, Polyline> routeLines;

    private ConcurrentMap<Integer, Marker> busStops;

    private TransitStop transitStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(isNetworkAvailable()) {
            setContentView(R.layout.activity_select_transit);
            checkSDCardData();
            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));

            setGoogleApiClient();
            createBusList();
            //sets up the map
            inSavedState = false;
            enableHttpResponseCache();
            restoreInstanceState(savedInstanceState);
            isBusTaskRunning = false;
            //        zoom = 15.0f;
//        } else {
//
//        }
    }

    /**
     * Checks if the network is available
     * TODO: incorporate this with a dialog to enable internet
     * @return whether or not the network is available...
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

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
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void enableHttpResponseCache() {
        try {
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
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
     * Restores the instance state of the program
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
        savePreferences();
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


    /**
     * initializes the bus list
     * <p/>
     * Codewise most efficient way to pass the buses to the UI updater
     * <p/>
     * However, linear time worst case
     */
    private void createBusList() {
        //This will be changed as things go
        buses = Collections.synchronizedSet(new HashSet<String>(getResources().getInteger(R.integer.max_checked)));

        routeLines = new ConcurrentHashMap<>(getResources().getInteger(R.integer.max_checked));
        busMarkers = new ConcurrentHashMap<>(100);
    }


    /**
     * Sets up map if it is needed
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {

                setUpMap();

                mMap.setInfoWindowAdapter(new ETAWindowAdapter(getLayoutInflater()));
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        if(!inSavedState)
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
//                            final Handler handler = new Handler();
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    new RequestPredictions(mMap, mark, busMarkers.keySet(), transitStop.getStopIds(), getFragmentManager(), buses,
//                                            getApplicationContext()).execute(mark.getTitle());
//                                }
//                            }, 400);
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
    }



    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            setUpMap();
        } else
            setUpMapIfNeeded();
    }

    private void restorePreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getInt(BUSLIST_SIZE, -1) == getResources().getStringArray(R.array.buses).length) {
            buses = sp.getStringSet(BUS_SELECT_STATE, new HashSet<String>(getResources().getInteger(R.integer.max_checked)));
            Log.d("restoring buses", buses.toString());
        } else {
            buses = new HashSet<>(getResources().getInteger(R.integer.max_checked));
        }
    }

    protected void onPause() {
        stopTimer();
        clearMap();
        super.onPause();

    }

    @Override
    protected void onStop() {
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
        sp.edit().putStringSet(BUS_SELECT_STATE, buses).apply();
        sp.edit().putInt(BUSLIST_SIZE, getResources().getStringArray(R.array.buses).length).apply();
    }

    @Override
    protected void onDestroy() {
//        clearMap();
        super.onDestroy();

    }

    /**
     * Gets called from NavigationDrawerFragment's onclick? Supposed to...
     *
     * @param position the list selection selected starting from 0
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        onSectionAttached(position);
    }

    /**
     * Gets called when one of the buses is pressed. Take note routes will always have more than one
     * polyline.
     *
     * @param number which bus in the list is pressed
     */
    public void onSectionAttached(int number) {
        if(mNavigationDrawerFragment.getAmountSelected() >= 0 &&
                mNavigationDrawerFragment.getAmountSelected() <= getResources().getInteger(R.integer.max_checked)) {
            if(mNavigationDrawerFragment.isPositionSelected(number)) {
                selectFromList(number);
            } else {
                deselectFromList(number);
            }
            Log.d("buses_attached", buses.toString());
        }
//        setPolyline(number);
//        setList(getResources().getStringArray(R.array.buses)[number]);
    }


    public void selectFromList(int number) {
        if(buses.add(getResources().getStringArray(R.array.buses)[number])) {
            Log.d("added_bus", getResources().getStringArray(R.array.buses)[number]);
            selectPolyline(number);
        }
    }

    public void deselectFromList(int number) {
        if(buses.remove(getResources().getStringArray(R.array.buses)[number])) {
            Log.d("removed_bus", getResources().getStringArray(R.array.buses)[number]);
            deselectPolyline(number);
        }
    }

    private synchronized void selectPolyline(int number) {
        String route = getResources().getStringArray(R.array.buses)[number];
        int color = Color.parseColor(getResources().getStringArray(R.array.buscolors)[number]);
        List<Polyline> polylines = routeLines.get(route);

        if (polylines == null || polylines.isEmpty()) {
            new RequestLine(mMap, routeLines, route, busStops, color, zoom, Float.parseFloat(getString(R.string.zoom_level)), transitStop, this).execute();
        } else if(!polylines.get(0).isVisible()) {
            setVisiblePolylines(polylines, true);
            transitStop.updateAddRoutes(route, zoom, Float.parseFloat(getString(R.string.zoom_level)));
        }
    }

    private synchronized void deselectPolyline(int number) {
        String route = getResources().getStringArray(R.array.buses)[number];
        List<Polyline> polylines = routeLines.get(route);
        if(polylines != null && polylines.get(0).isVisible()) {
            setVisiblePolylines(polylines, false);
            transitStop.removeRoute(route);
        }
    }

//    private synchronized void setPolyline(int number) {
//        String route = getResources().getStringArray(R.array.buses)[number];
//        int color = Color.parseColor(getResources().getStringArray(R.array.buscolors)[number]);
//        List<Polyline> polylines = routeLines.get(route);
//
//        if (polylines == null || polylines.isEmpty()) {
//            new RequestLine(mMap, routeLines, route, busStops, color, zoom, Float.parseFloat(getString(R.string.zoom_level)), transitStop, this).execute();
//        } else if (polylines.get(0).isVisible()) {
//            setVisiblePolylines(polylines, false);
//            transitStop.removeRoute(route);
//        } else {
//            setVisiblePolylines(polylines, true);
//            transitStop.updateAddRoutes(route, zoom, Float.parseFloat(getString(R.string.zoom_level)));
//        }
//    }

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

    /**
     * If the selected bus is already in the list, remove it
     * else add it
     * <p/>
     * This list will then be passed onto the UI updater if it isn't empty.
     * <p/>
     * This is the best way codewise to pass the buses to the UI updater.
     * <p/>
     * Worst case is O(n) as we'd have to remove all buses here.
     * <p/>
     * we want to also be able to see the bus the instant it loads
     *
     * @param selected the bus string
     */
    private void setList(String selected) {
        //TODO: perhaps look at constant time remove
        //TODO somehow the bus isn't being selected
        if (!buses.remove(selected)) {
            buses.add(selected);
        }
    }

    public void restoreActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Throwable e) {
                Toast.makeText(this, "Material Design bugged out on your device. Please report this to the Play Store Email if this pops up.", Toast.LENGTH_LONG).show();
            }
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        if (currentLocation != null && !inSavedState) {

            double currentLatitude = currentLocation.getLatitude();
            double currentLongitude = currentLocation.getLongitude();
            // case where you are inside Pittsburgh...
            if ((currentLatitude > 39.859673 && currentLatitude < 40.992847) &&
                    (currentLongitude > -80.372815 && currentLongitude < -79.414258)) {
                latitude = currentLatitude;
                longitude = currentLongitude;
                zoom = (float) 15.0;
            } else {
                zoom = 11.82f;
            }
        } /*else {
            zoom = 11.82f;
        }*/
        Log.d("savedInstance", "saved? " + inSavedState);
        Log.d("savedInstance", "lat="+latitude);
        Log.d("savedInstance", "long="+longitude);
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
        mMap.setMyLocationEnabled(true);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getInt(BUSLIST_SIZE, -1) == getResources().getStringArray(R.array.buses).length) {
//            buses.clear();
            clearAndAddToMap();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    restorePolylines();
                }
            }, 100);

        }
    }

    /**
     * This is the method to restore polylines....
     */
    protected void restorePolylines() {
        Log.d("polylines restoring", "zoom= " + zoom);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getInt(BUSLIST_SIZE, -1) == getResources().getStringArray(R.array.buses).length) {
            Set<String> selected = sp.getStringSet(STATE_SELECTED_POSITIONS, null);
            if (selected != null) {
                for (String select : selected) {
                    int position = Integer.parseInt(select);
                    selectPolyline(position);
                    mNavigationDrawerFragment.setTrue(position);
                }
            }
        } else {
            Toast.makeText(this, "New buses were added. Please re-select your buses", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Stops the bus refresh, then adds buses to the map
     */
    protected synchronized void clearAndAddToMap() {
        Log.d("stop_add_buses", buses.toString());
        stopTimer();
        addBuses();
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
                    RequestTask req;

                    public void run() {
                        if (!buses.isEmpty()) {
//                            clearMap();

                            req = new RequestTask(mMap, buses, busMarkers, context);
//                            req = new RequestTask(mMap, buses, context);
                            req.execute();
                        } else
                            clearMap();
                    }
                });
            }
        };
        if (!buses.isEmpty()) {
            timer.schedule(task, 0, 10000); //it executes this every 10000ms
        } else
            clearMap();
    }


    private synchronized void removeBuses() {
        if (busMarkers != null) {
            for (Marker busMarker : busMarkers.values()) {
                busMarker.remove();
            }
            busMarkers = null;
        }

    }

    /**
     * Stops the timer task
     */
    private synchronized void stopTimer() {
        removeBuses();
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
        busMarkers = null;
        if (mMap != null) {
            routeLines = new ConcurrentHashMap<>(getResources().getInteger(R.integer.max_checked));
            transitStop = new TransitStop();
            mMap.clear();
            mNavigationDrawerFragment.getDrawerListView().clearChoices();
        }
    }

    /**
     * @return The list of buses that are selected
     */
    protected void clearBuses() {
        Log.d("clearing buses", "buses cleared");
        buses.clear();
    }

    public void onBackPressed() {
        if (!mNavigationDrawerFragment.closeDrawer())
            super.onBackPressed();
    }

    /**
     * Part of the GoogleApiClient connection. If it is connected
     *
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        /*
      This is the location request using FusedLocationAPI to get the person's last known location
     */
        LocationRequest gLocationRequest = LocationRequest.create();
        gLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        gLocationRequest.setInterval(1000);
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        if (currentLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, gLocationRequest, this);
            if (currentLocation != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            }

        }
//        System.out.println("Location: " + currentLocation);
        centerMap();
    }

    /**
     * Not sure what to do with this...
     *
     * @param i dunno...
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * This is where the GoogleApiClient will fail. So far, just have it stored into a log...
     *
     * @param connectionResult The specified code if the GoogleApiClient fails to connect!
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Google API Error", connectionResult.toString());
        Toast.makeText(this, "Google connection failed, please try again later", Toast.LENGTH_LONG).show();
//        TODO: Perhaps based on the connection result, we can close and make custom error messages.
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            currentLocation = location;
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
}
