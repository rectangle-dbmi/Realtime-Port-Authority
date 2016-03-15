package rectangledbmi.com.pittsburghrealtimetracker;

import android.Manifest;
import android.content.Context;
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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import rectangledbmi.com.pittsburghrealtimetracker.handlers.RequestLine;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.extend.ETAWindowAdapter;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.PATAPI;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.containers.errors.ErrorMessage;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.containers.vehicles.VehicleBitmap;
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStopCollection;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.BustimeVehicleResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Vehicle;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.VehicleResponse;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
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
     * The permissions request code to unsubscribe the map
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

    /**
     * The vehicles subscriptions
     */
    private CompositeSubscription vehicleSubscriptions;

    /**
     * The subscription to unselect a vehicle from the map
     */
    private Subscription unselectVehicleSubscription;

    private ConcurrentMap<String,List<Polyline>> routeLines;

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

    /**
     * Observable that emits individual vehicles onto the map.
     * Instantiated on {@link #onMapReady(GoogleMap)} -> {@link #setupReactiveObjects()}
     */
    private Observable<VehicleBitmap> vehicleUpdateObservable;

    /**
     * Observable that emits errors from the vehicle updates
     * Instantiated on {@link #onMapReady(GoogleMap)} -> {@link #setupReactiveObjects()}
     */
    private Observable<ErrorMessage> vehicleErrorObservable;

    /**
     * The {@link retrofit2.Retrofit} instance of the Port Authority TrueTime API
     */
    private PATAPI patApiClient;
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
        if (vehicleSubscriptions == null || vehicleSubscriptions.isUnsubscribed()) {
            resetVehicleSubscriptions();
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
        if (vehicleSubscriptions != null) {
            vehicleSubscriptions.clear();
            vehicleSubscriptions.unsubscribe();
        }
        removeBuses();
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
        unselectVehicleSubscription.unsubscribe();
        unselectVehicleSubscription = null;
        if (mMap != null) {
            mMap.setInfoWindowAdapter(null);
            mMap.setOnCameraChangeListener(null);
            mMap.setOnMarkerClickListener(null);
            mMap = null;
        }

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
        if (getActivity() == null || mListener.getPatApiClient() == null) return;
        patApiClient = mListener.getPatApiClient();
        mMap = googleMap;

        // center the map
        if (cameraPosition != null) {
            Timber.d("map was instantiated from a recreation (orientation change, etc.)");
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            Timber.d("Map was instantiated from a clean state. Centering the map on Pittsburgh and possibly on you");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PITTSBURGH, 11.8f));
            centerMapWithPermissions();
        }
        // set up the stops collection object and its listeners
        transitStopCollection = new TransitStopCollection();
        busMarkers = new ConcurrentHashMap<>();
        routeLines = new ConcurrentHashMap<>();


        mMap.setInfoWindowAdapter(new ETAWindowAdapter(getActivity().getLayoutInflater()));
        mMap.setOnMarkerClickListener((marker) -> {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 400, null);
            return true;
        });

        mMap.setOnCameraChangeListener(cameraPosition -> {
            if (zoom != cameraPosition.zoom) {
                zoom = cameraPosition.zoom;
                transitStopCollection.checkAllVisibility(zoom, zoomStopVisibility);
            }
        });

        // set up observable information
        setupReactiveObjects();
    }

    /**
     * This is the method to restore polylines.
     *
     * @since 43
     */
    protected void restorePolylines() {
        Route currentRoute;
        for (String route : mListener.getSelectedRoutes()) {
            currentRoute = mListener.getSelectedRoute(route);
            selectPolyline(currentRoute);
//            mNavigationDrawerFragment.setTrue(currentRoute.getListPosition());
        }
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
                    transitStopCollection, getContext()).execute();
        } else if (!polylines.get(0).isVisible()) {
            setVisiblePolylines(polylines, true);
            transitStopCollection.updateAddRoutes(route, zoom, R.integer.zoom_level);
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

    /**
     * Removes the route line if it was on the map
     * @param route - the route to remove by its string
     */
    private void deselectPolyline(String route) {
        List<Polyline> polylines = routeLines.get(route);
        if (polylines != null) {
            if (!polylines.isEmpty() && polylines.get(0).isVisible()) {
                setVisiblePolylines(polylines, false);
                transitStopCollection.removeRoute(route);
            } else {
                routeLines.remove(route);
            }
        }
    }

    /**
     * <p>Observables created:</p>
     * <ul>
     *     <li>{@link #vehicleErrorObservable}</li>
     *     <li>{@link #vehicleErrorObservable}</li>
     * </ul>
     * <p>Subscriptions created:</p>
     * <ul>
     *     <li>{@link #vehicleSubscriptions}</li>
     *     <li>{@link #unselectVehicleSubscription}</li>
     * </ul>
     */
    private void setupReactiveObjects() {
        Observable<RouteSelection> selectionObservable = mListener.getSelectionObservable().share();

        Observable<BustimeVehicleResponse> vehicleIntervalObservable = selectionObservable
                .debounce(200, TimeUnit.MILLISECONDS)
                .switchMap(routeSelection -> Observable.interval(10, TimeUnit.SECONDS)
                        .map(aLong -> {
                            if (BuildConfig.DEBUG) {
                                String msg = String.format("updating vehicles: %d", aLong);
                                Timber.d(msg);
                                mListener.showToast(msg, Toast.LENGTH_SHORT);
                            }
                            return aLong;
                        }).concatMap(aLong -> {
                            if (BuildConfig.DEBUG) {
                                String msg = String.format("#%d: updating map with %s", aLong, routeSelection);
                                Timber.d(msg);
                                mListener.showToast(msg, Toast.LENGTH_SHORT);
                            }
                            return patApiClient.getVehicles(collectionToString(routeSelection.getSelectedRoutes()), BuildConfig.PAT_API_KEY);
                        }))
                .map(VehicleResponse::getBustimeResponse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .share();

        vehicleUpdateObservable = vehicleIntervalObservable
                .concatMap(bustimeVehicleResponse -> {
                    Timber.d("Getting vehicles in observable");
                    return Observable.from(bustimeVehicleResponse.getVehicle());
                }).map(makeBitmaps());

        vehicleErrorObservable = vehicleIntervalObservable
                .map(BustimeVehicleResponse::getProcessedErrors)
                .distinctUntilChanged()
                .concatMap( errorMap -> Observable.from(errorMap.entrySet()))
                .map(transformSingleMessage());

        unselectVehicleSubscription = selectionObservable.map(RouteSelection::getToggledRoute)
                .skipWhile(Route::isSelected)
                .concatMap(route -> Observable.from(busMarkers.entrySet())
                        .filter(busMarker -> busMarker.getValue().getTitle().contains(route.getRoute())))
                .subscribe(new Observer<Map.Entry<Integer, Marker>>() {
                    @Override
                    public void onCompleted() {
                        Timber.d("No longer listening to deselections");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error happened while trying to unselect");
                    }

                    @Override
                    public void onNext(Map.Entry<Integer, Marker> vehicleMapEntry) {
                        busMarkers.remove(vehicleMapEntry.getKey());
                        vehicleMapEntry.getValue().remove();
                    }
                });

        resetVehicleSubscriptions();
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

    public void deselectFromList(Route route) {
        Timber.d("removed_bus %s", route.getRoute());
        deselectPolyline(route.getRoute());
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

    // region Map State and Observables

    /**
     * Resets the composite subscription for vehicles
     */
    private void resetVehicleSubscriptions() {
        if(vehicleSubscriptions != null) {
            vehicleSubscriptions = new CompositeSubscription(
                    vehicleUpdateObservable.subscribe(vehicleUpdateObserver()),
                    vehicleErrorObservable.subscribe(vehicleErrorObserver())
            );
        }

    }

    /**
     * This is an anonymous function that attaches a route's bitmap to its information
     * from {@link Vehicle}. This will make an {@link rx.Observable} emit a {@link VehicleBitmap}.
     * @return the anonymous vehicle information with its associated bitmap
     */
    private Func1<Vehicle, VehicleBitmap> makeBitmaps() {
        return new Func1<Vehicle, VehicleBitmap>() {

            private HashMap<String, Bitmap> busIconCache = new HashMap<>(mListener.getSelectedRoutes().size());

            @Override
            public VehicleBitmap call(Vehicle vehicle) {
                String routeName = vehicle.getRt();
                if (busIconCache.containsKey(routeName)) {
                    return new VehicleBitmap(vehicle, busIconCache.get(routeName));
                } else {
                    Bitmap icon = makeBitmap(mListener.getSelectedRoute(vehicle.getRt()));
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
    // endregion

    // region Observers
    /**
     * This creates an observer to either update or add the buses to the map.
     * @return the vehicle update observer
     * @since 55
     */
    private Observer<VehicleBitmap> vehicleUpdateObserver() {
        return new Observer<VehicleBitmap>() {

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
                        mListener.showToast(getString(R.string.retrofit_network_error), Toast.LENGTH_SHORT);
                    } else if (e instanceof HttpException) {
                        HttpException http = (HttpException) e;
                        mListener.showToast(http.code() + " " + http.message() + ": "
                                + getString(R.string.retrofit_http_error), Toast.LENGTH_SHORT);
                    } else {
                        mListener.showToast(getString(R.string.retrofit_conversion_error), Toast.LENGTH_SHORT);
                    }
                    Timber.e("bus_vehicle_error: %s", e.getMessage());
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
             *     <li>add marker if not on {@link BusMapFragment#busMarkers} - {@link #addMarker(VehicleBitmap)}</li>
             *     <li>update marker if in {@link BusMapFragment#busMarkers} - {@link #updateMarker(Vehicle, Marker)}</li>
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
             * adds marker not in {@link BusMapFragment#busMarkers}
             * @since 46
             * @param vehicleBitmap - the vehicle to add
             */
            private void addMarker(VehicleBitmap vehicleBitmap) {
                Vehicle vehicle = vehicleBitmap.getVehicle();
                Timber.d("marker_add adding_marker %s", Integer.toString(vehicle.getVid()));
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
                Timber.d("marker_update... updating_pointer");
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
    private Observer<ErrorMessage> vehicleErrorObserver() {
        return new Observer<ErrorMessage>() {
            @Override
            public void onCompleted() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
                String dateTime = dateFormat.format(new Date());
                Timber.d("Bus map error updates finished at %s", dateTime);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "error in vehicle error observer");
            }

            @Override
            public void onNext(ErrorMessage errorMessage) {
                if (errorMessage != null && errorMessage.getMessage() != null) {
                    mListener.showToast(errorMessage.getMessage() +
                                    (errorMessage.getParameters() != null ? ": " + errorMessage.getParameters() : ""),
                            Toast.LENGTH_SHORT);
                }
            }
        };
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

    /**
     * Removes all buses
     */
    private void removeBuses() {
        if (busMarkers != null) {
            Timber.d("buses removed");
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
    // endregion

}
