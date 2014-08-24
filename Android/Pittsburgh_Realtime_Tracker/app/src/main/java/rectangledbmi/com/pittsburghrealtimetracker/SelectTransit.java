package rectangledbmi.com.pittsburghrealtimetracker;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestTask;


public class SelectTransit extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

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
     */
    private List<String> buses;

    /**
     * This is the object that updates the UI every 10 seconds
     */
    private Timer timer;

    /**
     * This is the object that creates the action to update the UI
     */
    private TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_transit);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        createBusList();
        restoreInstanceState(savedInstanceState);
        //sets up the map
        setUpMapIfNeeded();

    }

    /**
     * Restores the instance state of the program
     * @param savedInstanceState the saved instances of the app
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            buses = savedInstanceState.getStringArrayList(BUS_SELECT_STATE);
            if(mMap != null) {
                latitude = savedInstanceState.getDouble(LAST_LATITUDE);
                longitude = savedInstanceState.getDouble(LAST_LONGITUDE);
                zoom = savedInstanceState.getFloat(LAST_ZOOM);
            }
            else
                defaultCameraLocation();
        }
        else
            defaultCameraLocation();
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
        savedInstanceState.putStringArrayList(BUS_SELECT_STATE, (ArrayList<String>)buses);
        if(mMap != null) {
            savedInstanceState.putDouble(LAST_LATITUDE, mMap.getCameraPosition().target.latitude);
            savedInstanceState.putDouble(LAST_LONGITUDE, mMap.getCameraPosition().target.longitude);
            savedInstanceState.putFloat(LAST_ZOOM, mMap.getCameraPosition().zoom);
        }
    }



    /**
     * initializes the bus list
     */
    private void createBusList() {
        //This will be changed as things go
        buses = new ArrayList<String>(getResources().getInteger(R.integer.number_of_buses));
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
                centerMap();
                setUpMap();
            }
        }
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
        super.onPause();
        stopTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    /**
     * Gets called from NavigationDrawerFragment's onclick? Supposed to...
     * @param position the list selection selected starting from 1
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
        switch (number) {
            case 0:
                setList(getString(R.string.title_section1));
                break;
            case 1:
                setList(getString(R.string.title_section2));
                break;
            case 2:
                setList(getString(R.string.title_section3));
                break;
            case 3:
                setList(getString(R.string.title_section4));
                break;
            case 4:
                setList(getString(R.string.title_section5));
                break;
            case 5:
                setList(getString(R.string.title_section6));
                break;
            case 6:
                setList(getString(R.string.title_section7));
                break;
            case 7:
                setList(getString(R.string.title_section8));
                break;
        }
    }

    /**
     * If the selected bus is already in the list, remove it
     * else add it
     *
     * we want to also be able to see the bus the instant it loads
     * @param selected the bus string
     */
    private void setList(String selected) {
        //TODO: perhaps look at constant time remove
        //TODO somehow the bus isn't being selected
        if(!buses.remove(selected))
            buses.add(selected);
        //TODO Need to be able to refresh the buses instantly however
        //the issue here is that the thread is not killable
//        setUpMap();
        setUpMap();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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
        if (id == R.id.action_settings) {
            return true;
        }
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
     * Polls self on the map and then centers the map on Pittsburgh
     */
    private void centerMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Adds markers to map
     * This is done in a thread.
     *
     * TODO this isn't working since it keeps running and not interrupting
     */
    private void setUpMap() {
        final Handler handler = new Handler();
//        stopThread();
//        updateUI = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                while (!Thread.interrupted()) {
//                    try {
//                        Thread.sleep(10000);
//                        handler.post(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                mMap.clear();
//                                new RequestTask(mMap, buses).execute();
//
//                            }
//                        });
//                    } catch (Exception e) {
//                        // TODO: handle exception
//                    }
//                }
//            }
//        });
//        if(buses != null && !buses.isEmpty())
//            updateUI.start();
        stopTimer();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        RequestTask req;
                        if(!buses.isEmpty()) {
                            mMap.clear();
                            req = new RequestTask(mMap, buses);
                            req.execute();
                        }
                    }
                });
            }
        };
        if(!buses.isEmpty())
            timer.schedule(task, 0, 10000); //it executes this every 1000ms
    }

    /**
     * Stops the timer task
     */
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        if(task != null) {
            task.cancel();
        }
    }

}
