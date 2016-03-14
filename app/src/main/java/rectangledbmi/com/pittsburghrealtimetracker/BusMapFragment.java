package rectangledbmi.com.pittsburghrealtimetracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.concurrent.ConcurrentMap;

import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStopCollection;
import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BusMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BusMapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback, LocationListener {

    // region static parts of the fragment

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String CAMERA_POSITION = "cameraPosition";

    /**
     * The
     */
    private static final int CENTER_MAP_LOCATION_CODE = 123;

    /**
     * The latitude and longitude of Pittsburgh... used if the app doesn't have a saved state of the camera
     */
    private final static LatLng PITTSBURGH = new LatLng(40.441, -79.981);

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BusSelectionInteraction mListener;

    public BusMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BusMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BusMapFragment newInstance(String param1, String param2) {
        BusMapFragment fragment = new BusMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    // </editor-fold>

    // <editor-fold desc="Private instance variables">
    private float zoomStopVisibility;

    /**
     * The google map object. Make sure to dereference this {@link #onDestroy()}
     */
    private GoogleMap mMap;

    /**
     * The google map camera position
     */
    private CameraPosition cameraPosition;

    private MapView mapView;

    private float zoom = 11.8f;

    private ConcurrentMap<Integer, Marker> busMarkers;

    private TransitStopCollection transitStopCollection;

    private GoogleApiClient googleApiClient;
    // endregion

    // region Android Fragment LifeCycle
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BusSelectionInteraction) {
            // get the default zoom level of the stops
            mListener = (BusSelectionInteraction) context;
            TypedValue zoomLevelValue = new TypedValue();
            context.getResources().getValue(R.integer.zoom_level, zoomLevelValue, true);
            zoomStopVisibility = zoomLevelValue.getFloat();

        } else {
            RuntimeException e = new RuntimeException(context.toString()
                    + " must implement BusSelectionInteraction");
            Timber.e(e, "Fragment must be attached to Activity that interacts with NavigationDrawerFragment");
            throw e;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        transitStopCollection = new TransitStopCollection();
    }

    @Override
    public void onActivityCreated(Bundle inState) {
        super.onActivityCreated(inState);
        cameraPosition = inState.getParcelable(CAMERA_POSITION);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bus_map, container, false);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        // enable/disable UI to see your current location based on permission changes
        Context context = getContext();
        if (context == null || mMap == null) return;
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission_group.LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED && !mMap.isMyLocationEnabled()) {
            mMap.setMyLocationEnabled(true);
        } else if (permission == PackageManager.PERMISSION_DENIED && mMap.isMyLocationEnabled()) {
            mMap.setMyLocationEnabled(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        mMap = null;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
        mMap = null;
        cameraPosition = null;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        outState.putParcelable(CAMERA_POSITION, mMap.getCameraPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    // endregion

    // region Permission Request Handling

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        switch (requestCode) {
            case CENTER_MAP_LOCATION_CODE:
                Timber.d("Requesting permissions to center the map");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Request has been accepted to center the map");
                    centerMapWithPermissions();
                } else {
                    String message = getString(R.string.center_permissions_denied);
                    Timber.i("Request has been been denied: %s", message);
                    mListener.makeSnackbar(
                            message,
                            Snackbar.LENGTH_LONG,
                            getString(R.string.center_permissions_action),
                            (view) -> mListener.openPermissionsPage());
                }
                break;
        }
    }

    // endregion

    // region Google API Client Callbacks

    /**
     * Called from {@link #onStart()} to connect to the Google APIs. This will get the Google Map object
     * which is handled in {@link #onMapReady(GoogleMap)}
     *
     * @param bundle the saved state
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mMap == null) {
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.i("Google callback suspended with number %d", i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.i("Google Callback Connection failed, %s", connectionResult.toString());
    }
    // endregion

    // region Google Map and Location Callbacks
    /**
     * This is called from {@link #onConnected(Bundle)} to retrieve a non-null map object
     * from the Google Maps API.
     *
     * @param googleMap the google map object
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        centerMap();
    }

    /**
     * <p>Centers the map based on certain criteria:</p>
     * <ul>
     *     <li>If map is recreated, set the previous camera position</li>
     *     <li>Else, center the map on Pittsburgh. If location permissions are allowed,
     *         Run {@link #centerMapWithPermissions()}
     *     </li>
     * </ul>
     */
    private void centerMap() {
        if (mMap == null) return;
        if (cameraPosition != null) { // The camera position of the map is saved from recreation
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            return;
        }
        // if the map is made from a clean creation (not from orientation change, get the current contents
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PITTSBURGH, 11.8f));
        centerMapWithPermissions();
    }

    /**
     * Centers the map if the {@link android.Manifest.permission_group#LOCATION} permissions are granted.
     * If they are not and has never been asked, check to see if location settings should be granted.
     * If they are granted, center the location either on your last known location or on the first
     * location update. Otherwise, don't run it.
     */
    private void centerMapWithPermissions() {
        if (mMap == null || getContext() == null) return;

        // first check if the permission is granted... if not, show why you should if user didn't say
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission_group.LOCATION)) {
                mListener.showOkDialog(
                        getString(R.string.location_permission_message),
                        ((dialog, which) ->
                                requestPermissions(new String[]{Manifest.permission_group.LOCATION}, CENTER_MAP_LOCATION_CODE))
                );
            }
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) { // use the last location if found
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomStopVisibility));
        } else { // request one location update
            LocationRequest gLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setExpirationTime(10000) // set expiration 10 seconds
                    .setNumUpdates(1); // only do one update. needs above call for expiration
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, gLocationRequest, this);
        }
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Centers the map on the user's latest location data.
     * @param location the user's latest location data.
     */
    @Override
    public void onLocationChanged(Location location) {
        /*
        in the future, we may want different behaviors when the FusedLocationApi
        requests location updates. If this needs to happen, we should have
        multiple internal classes that implement LocationListener with different logic in each
         */
        if (location == null || mMap == null || isInPittsburgh(location)) return;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomStopVisibility));
    }
    // endregion

    // region Miscellaneous Private Methods
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
    // endregion
}
