package rectangledbmi.com.pittsburghrealtimetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestLine;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestTask;

/**
 * This is the main activity of the
 */
public class SelectTransit extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

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
     *
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

//    private ConcurrentMap<String, List<Polyline>> routeLines;
    private ConcurrentMap<String, Polyline> routeLines;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_transit);
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
        restoreInstanceState(savedInstanceState);
        isBusTaskRunning = false;

//        setUpMapIfNeeded();



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

    /**
     * Restores the instance state of the program
     * @param savedInstanceState the saved instances of the app
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        buses = sp.getStringSet(BUS_SELECT_STATE, new HashSet<String>(10));
        if (savedInstanceState != null) {
            inSavedState = true;
//            buses = new HashSet<String>(savedInstanceState.getStringArray(BUS_SELECT_STATE));
//            buses = new HashSet<String>(savedInstanceState.getStringArrayList(BUS_SELECT_STATE));
            latitude = savedInstanceState.getDouble(LAST_LATITUDE);
            longitude = savedInstanceState.getDouble(LAST_LONGITUDE);
            zoom = savedInstanceState.getFloat(LAST_ZOOM);
        } else {
            defaultCameraLocation();
        }

    }
    /**
     * Instantiates the default camera coordinates
     */
    private void defaultCameraLocation() {
        latitude = PITTSBURGH.latitude;
        longitude = PITTSBURGH.longitude;
        zoom = (float)11.88;
    }

    /**
     * Saves the instances of the app
     *
     * Right now, it saves the list of buses and the camera position of the map
     *
     * @param savedInstanceState the bundle of saved instances
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
//        ArrayList<String> list = new ArrayList<String>(buses.size());
//        list.addAll(buses);
//        savedInstanceState.putStringArrayList(BUS_SELECT_STATE, list);
        if(mMap != null) {
            savedInstanceState.putDouble(LAST_LATITUDE, mMap.getCameraPosition().target.latitude);
            savedInstanceState.putDouble(LAST_LONGITUDE, mMap.getCameraPosition().target.longitude);
            savedInstanceState.putFloat(LAST_ZOOM, mMap.getCameraPosition().zoom);
        }

    }



    /**
     * initializes the bus list
     *
     * Codewise most efficient way to pass the buses to the UI updater
     *
     * However, linear time worst case
     */
    private void createBusList() {
        //This will be changed as things go
        buses = Collections.synchronizedSet(new HashSet<String>(getResources().getInteger(R.integer.max_checked)));
        routeLines = new ConcurrentHashMap<String, Polyline>(getResources().getInteger(R.integer.max_checked));
//        routeLines = new ConcurrentHashMap<String, List<Polyline>>(getResources().getInteger(R.integer.max_checked));
    }




    /**
     * Sets up map if it is needed
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
//            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
//                    .getMap();
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
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
        if(mMap != null) {
            setUpMap();
        }
        else
            setUpMapIfNeeded();
    }

    protected void onPause() {
        stopTimer();
        clearMap();
        super.onPause();

    }

    @Override
    protected void onStop() {
        stopTimer();
        savePreferences();
        client.disconnect();
        super.onStop();
    }

    /**
     * Place to save preferences....
     */
    private void savePreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putStringSet(BUS_SELECT_STATE, buses).apply();
        sp.edit().commit();
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        clearMap();
        super.onDestroy();

    }

    /**
     * Gets called from NavigationDrawerFragment's onclick? Supposed to...
     * @param position the list selection selected starting from 0
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        onSectionAttached(position);
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.maps_fragment, MapFragment.newInstance(position + 1))
//                .commit();
    }

    /**
     * Gets called when one of the buses is pressed
     * @param number which bus in the list is pressed
     */
    public void onSectionAttached(int number) {
        setPolyline(number);
        setList(getResources().getStringArray(R.array.buses)[number]);
    }

    private synchronized void setPolyline(int number) {
        String route = getResources().getStringArray(R.array.buses)[number];
        int color = Color.parseColor(getResources().getStringArray(R.array.buscolors)[number]);
        Polyline polyline = routeLines.get(route);
//        List<Polyline> polylines = routeLines.get(route);
        if(polyline == null) {
            new RequestLine(mMap, routeLines, route, color).execute();
        }
        else if(polyline.isVisible()) {
            polyline.setVisible(false);
        }
        else {
            polyline.setVisible(true);
        }
        /*if(polylines == null) {
            System.out.println("polyline was null");
            new RequestLine(mMap, routeLines, route, color).execute();
        }
        else if(polylines.get(0).isVisible()) {
            setVisiblePolylines(polylines, false);
        }
        else
            setVisiblePolylines(polylines, true);*/
    }

    /**
     * sets a visible or invisible polylines for a route
     * @param polylines
     * @param visible
     */
    private void setVisiblePolylines(List<Polyline> polylines, boolean visible) {
        for(Polyline polyline : polylines) {
            polyline.setVisible(visible);
        }
    }

    /**
     * If the selected bus is already in the list, remove it
     * else add it
     *
     * This list will then be passed onto the UI updater if it isn't empty.
     *
     * This is the best way codewise to pass the buses to the UI updater.
     *
     * Worst case is O(n) as we'd have to remove all buses here.
     *
     * we want to also be able to see the bus the instant it loads
     * @param selected the bus string
     */
    private void setList(String selected) {
        //TODO: perhaps look at constant time remove
        //TODO somehow the bus isn't being selected
        if(buses.remove(selected)) {

        }
        else {
            buses.add(selected);
        }
        clearAndAddToMap();
    }

    public void restoreActionBar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);

        }

//        ActionBar actionBar = getSupportActionBar();
//        assert actionBar != null;
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(mTitle);
//        actionBar.show();
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
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_select_transit, container, false);
//            return rootView;
//        }
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//            ((SelectTransit) activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
//        }
//    }

    /**
     * Polls self on the map and then centers the map on Pittsburgh or you if you're in Pittsburgh..
     */
    private void centerMap() {
        System.out.println("Centering Map");
        if(currentLocation != null && !inSavedState) {

            double currentLatitude = currentLocation.getLatitude();
            double currentLongitude = currentLocation.getLongitude();
            // case where you are inside Pittsburgh...
            if((currentLatitude > 39.859673 && currentLatitude < 40.992847) &&
                    (currentLongitude > -80.372815 && currentLongitude < -79.414258)) {
                latitude = currentLatitude;
                longitude = currentLongitude;
                zoom = (long)15.00;

            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
        mMap.setMyLocationEnabled(true);

        }

    /**
     * Adds markers to map
     * This is done in a thread.
     *
     */
    protected void setUpMap() {
        clearMap();
        clearAndAddToMap();
    }

    /**
     * Stops the bus refresh, then adds buses to the map
     */
    protected synchronized void clearAndAddToMap() {
        stopTimer();
        addBuses();
    }

    /**
     * adds buses to map. or else the map will be clear...
     */
    protected synchronized void addBuses() {

        final Handler handler = new Handler();
        timer = new Timer();
        final Context context = this;
        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    RequestTask req;
                    public void run() {
                        if(!buses.isEmpty()) {
//                            clearMap();

                            req = new RequestTask(mMap, buses, busMarkers, context);
//                            req = new RequestTask(mMap, buses, context);
                            while(isBusTaskRunning);
                            req.execute();
                        }
                        else
                            clearMap();
                    }
                });
            }
        };
        if(!buses.isEmpty()) {
            timer.schedule(task, 0, 10000); //it executes this every 10000ms
        }
        else
            clearMap();
    }


    private synchronized void removeBuses() {
        if(busMarkers != null) {
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

        if(task != null) {
            task.cancel();
        }
    }

    /**
     * General method to clear the map.
     */
    protected void clearMap() {
        busMarkers = null;
        if(mMap != null) {
            routeLines = new ConcurrentHashMap<String, Polyline>(getResources().getInteger(R.integer.max_checked));
//            routeLines = new ConcurrentHashMap<String, List<Polyline>>(getResources().getInteger(R.integer.max_checked));
            mMap.clear();
        }
        System.out.println("Cleared map...");
    }

    /**
     * @return The list of buses that are selected
     */
    protected void clearBuses() {
        buses.clear();
    }

    public void onBackPressed() {
        if(!mNavigationDrawerFragment.closeDrawer())
            super.onBackPressed();
    }

    /**
     * Part of the GoogleApiClient connection. If it is connected
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
        if(currentLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, gLocationRequest, this);
            if(currentLocation != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            }

        }
//        System.out.println("Location: " + currentLocation);
        centerMap();
    }

    /**
     * Not sure what to do with this...
     * @param i dunno...
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * This is where the GoogleApiClient will fail. So far, just have it stored into a log...
     * @param connectionResult The specified code if the GoogleApiClient fails to connect!
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Google API Location Services Error", connectionResult.toString());
/*        switch(connectionResult.getErrorCode()) {
            case (ConnectionResult.API_UNAVAILABLE) : {

            }
        }*/
//        TODO: Perhaps based on the connection result, we can close and make custom error messages.
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            currentLocation = location;
        }
    }

    public void setBusMarkers(ConcurrentMap<Integer, Marker> busMarkers) {
//        System.out.println("Old busmarker: " + busMarkers);
        this.busMarkers = busMarkers;
//        System.out.println("New busmarker: " + busMarkers);
    }

    public void setBusTaskRunning(boolean isBusTaskRunning) {
        this.isBusTaskRunning = isBusTaskRunning;
    }

    public boolean isBusTaskRunning() {
        return isBusTaskRunning;
    }
}
