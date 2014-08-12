package rectangledbmi.pittsburghrealtimepublictransittracker;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Timer;
import java.util.TimerTask;

import rectangledbmi.pittsburghrealtimepublictransittracker.handlers.RequestTask;

public class MapsActivity extends FragmentActivity {

    /**
     * The Google Maps object
     */
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    /**
     * @param savedInstanceState Bundle class variable for the android main
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    /**
     * method for resuming
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
//        new RequestTask(mMap).execute();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                centerMap();
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        mMap.clear();
                        new RequestTask(mMap).execute();
                    }
                });
            }
        };
        timer.schedule(task, 0, 10000); //it executes this every 1000ms
    }

    /**
     * Polls self on the map and then centers the map on self
     */
    private void centerMap() {
        LatLng pittsburgh = new LatLng(40.441, -79.981);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pittsburgh, (float)11.88));
        mMap.setMyLocationEnabled(true);
    }
}
