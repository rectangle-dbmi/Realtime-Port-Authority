package rectangledbmi.com.pittsburghrealtimetracker.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.rectanglel.patstatic.errors.ErrorMessage;
import com.rectanglel.patstatic.model.PatApiService;
import com.rectanglel.patstatic.patterns.polylines.PolylineView;
import com.rectanglel.patstatic.patterns.response.Pt;
import com.rectanglel.patstatic.patterns.stops.StopView;
import com.rectanglel.patstatic.predictions.PredictionsView;
import com.rectanglel.patstatic.vehicles.response.BustimeVehicleResponse;
import com.rectanglel.patstatic.vehicles.response.Vehicle;
import com.rectanglel.patstatic.vehicles.response.VehicleResponse;
import com.squareup.leakcanary.RefWatcher;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subscribers.DisposableSubscriber;
import rectangledbmi.com.pittsburghrealtimetracker.BuildConfig;
import rectangledbmi.com.pittsburghrealtimetracker.PATTrackApplication;
import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.PatternSelection;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.PatternViewModel;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering.StopRenderRequest;
import rectangledbmi.com.pittsburghrealtimetracker.predictions.PredictionsViewModel;
import rectangledbmi.com.pittsburghrealtimetracker.predictions.ProcessedPredictions;
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route;
import rectangledbmi.com.pittsburghrealtimetracker.ui.selection.ClearSelection;
import rectangledbmi.com.pittsburghrealtimetracker.ui.selection.SelectionFragment;
import rectangledbmi.com.pittsburghrealtimetracker.vehicles.VehicleBitmap;
import retrofit2.HttpException;
import timber.log.Timber;

import static rectangledbmi.com.pittsburghrealtimetracker.utils.ReactiveHelper.isInternetDown;
import static rectangledbmi.com.pittsburghrealtimetracker.utils.ReactiveHelper.retryIfInternet;


/**
 * <p>Fragment that holds a map for the buses. This currently holds all logic related to displaying
 * the buses, getStopRenderRequests, and patternSelections on a {@link GoogleMap} instance</p>
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 */
@SuppressWarnings("Convert2streamapi")
// remove this suppressed warning when we can start using the stream APIs
public class BusMapFragment extends SelectionFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback, LocationListener, ClearSelection,
        PolylineView<PatternSelection>, StopView<StopRenderRequest>, PredictionsView<ProcessedPredictions> {

    private static final String CAMERA_POSITION = "cameraPosition";

    /**
     * The permissions request code to unsubscribe the map
     */
    private static final int CENTER_MAP_LOCATION_CODE = 123;

    /**
     * The latitude and longitude of Pittsburgh... used if the app doesn't have a saved state of the camera
     */
    private final static LatLng PITTSBURGH = new LatLng(40.441, -79.981);

    public BusMapFragment() {
        // Required empty public constructor
    }

    // region private instance variables
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

    /**
     * Default zoom level
     */
    private float zoom = 11.8f;

    /**
     * Google map cached bus markers
     */
    private ConcurrentMap<Integer, Marker> busMarkers;

    /**
     * client for google map things
     */
    private GoogleApiClient googleApiClient;

    /**
     * this is an interface that interacts with the
     * {@link rectangledbmi.com.pittsburghrealtimetracker.ui.selection.NavigationDrawerFragment}
     */
    private BusSelectionInteraction busListInteraction;

    /**
     * The vehicles subscriptions
     */
    private CompositeDisposable vehicleSubscriptions;

    /**
     * The subscription to unselect a vehicle from the map
     */
    private DisposableSubscriber unselectVehicleSubscription;

    private ConcurrentMap<String, List<Polyline>> routeLines;

    /**
     * Observable that emits individual vehicles onto the map.
     * Instantiated on {@link #onMapReady(GoogleMap)} -> {@link #setupReactiveObjects()}
     */
    private Flowable<VehicleBitmap> vehicleUpdateFlowable;

    /**
     * Observable that emits errors from the vehicle updates
     * Instantiated on {@link #onMapReady(GoogleMap)} -> {@link #setupReactiveObjects()}
     */
    private Flowable<ErrorMessage> vehicleErrorFlowable;

    /**
     * The {@link retrofit2.Retrofit} instance of the Port Authority TrueTime API
     */
    // TODO: take this out and use the PatApiService instead
    private PatApiService patApiService;

    private Disposable selectionSubscription;

    /**
     * Subscription for the patternSelections
     */
    private Disposable polylineSubscription;

    /**
     * subscriptions for stops
     */
    private Disposable stopSubscription;

    /**
     * Hashmap cached google map stops
     */
    private HashMap<Integer, Marker> stops;

    /**
     * Subject for handling zooms
     */
    private BehaviorSubject<Float> zoomSubject;

    /**
     * subscription for predictions
     */
    private Disposable predictionsSubscription;
    // endregion

    // region Android Fragment LifeCycle
    @Override

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BusSelectionInteraction) {
            // get the default zoom level of the getStopRenderRequests
            busListInteraction = (BusSelectionInteraction) context;
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

    @SuppressLint("UseSparseArrays")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // set up the getStopRenderRequests collection object and its listeners
        busMarkers = new ConcurrentHashMap<>();
        routeLines = new ConcurrentHashMap<>();
        stops = new HashMap<>();
        zoomSubject = BehaviorSubject.create();
    }

    @Override
    public void onActivityCreated(Bundle inState) {
        super.onActivityCreated(inState);
        if (inState == null) return;
        cameraPosition = inState.getParcelable(CAMERA_POSITION);
        if (cameraPosition != null) {
            zoomSubject.onNext(cameraPosition.zoom);
        }


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
//        ServerDownDialogFragment.newInstance().show(getFragmentManager(), getString(R.string.servers_down_title));
        Timber.d("resuming map fragment");
        if (mapView != null) {
            mapView.onResume();
            Timber.d("resumed map view");
        }
        // enable/disable UI to see your current location based on permission changes
        Context context = getContext();
        if (context == null || mMap == null) return;
        int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED && !mMap.isMyLocationEnabled()) {
            Timber.d("Setting Google map Location Enabled");
            mMap.setMyLocationEnabled(true);
        } else if (permission == PackageManager.PERMISSION_DENIED && mMap.isMyLocationEnabled()) {
            Timber.d("Setting Google map Location as disabled");
            mMap.setMyLocationEnabled(false);
        }
        resetMapSubscriptions();
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("Starting map fragment");
        if (googleApiClient != null) {
            googleApiClient.connect();
            Timber.d("Connecting Google Api client");
        }

    }

    @Override
    public void onPause() {
        Timber.d("Pausing Map Fragment");
        if (mapView != null) {
            mapView.onPause();
            Timber.d("Pausing Map View");
        }
        pauseMapState();
        super.onPause();
    }

    private void pauseMapState() {
        if (vehicleSubscriptions != null) {
            vehicleSubscriptions.dispose();
            Timber.d("vehicle updater unsubscribed");
        }
        removeBuses();
    }

    @Override
    public void onStop() {
        Timber.d("Stopping Bus Fragment");
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            Timber.d("disconnecting google map api client");
        }
        super.onStop();
    }

    private static void unsubscribeSubscription(Disposable subscription) {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }

    @Override
    public void onDestroy() {
        Timber.d("Destroying Bus Fragment");
        if (getActivity() != null) {
            Timber.d("Adding leakcanary to fragment");
            RefWatcher refWatcher = PATTrackApplication.getRefWatcher(getActivity());
            refWatcher.watch(this);
        }
        if (unselectVehicleSubscription != null) {
            unselectVehicleSubscription.dispose();
            unselectVehicleSubscription = null;
            Timber.d("Unselect Vehicle Event destroyed");
        }
        unsubscribeSubscription(polylineSubscription);
        unsubscribeSubscription(stopSubscription);
        unsubscribeSubscription(predictionsSubscription);

        if (selectionSubscription != null) {
            selectionSubscription.dispose();
            selectionSubscription = null;
        }

        if (mMap != null) {

            mMap.setInfoWindowAdapter(null);
            mMap.setOnCameraIdleListener(null);
            mMap.setOnMarkerClickListener(null);
            mMap = null;
            Timber.d("Google Map Object destroyed");
        }
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
            Timber.d("Map View destroyed");
        }
        cameraPosition = null;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        if (outState != null && mMap != null) {
            outState.putParcelable(CAMERA_POSITION, mMap.getCameraPosition());
        }
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
        busListInteraction = null;
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
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        busListInteraction.showOkDialog(
                                getString(R.string.location_permission_message),
                                ((dialog, which) ->
                                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CENTER_MAP_LOCATION_CODE))
                        );
                    } else {
                        String message = getString(R.string.center_permissions_denied);
                        Timber.i("Request has been been denied: %s", message);
                        busListInteraction.makeSnackbar(
                                message,
                                Snackbar.LENGTH_LONG,
                                getString(R.string.center_permissions_action),
                                (view) -> busListInteraction.openPermissionsPage());
                        if (mMap != null && mMap.isMyLocationEnabled()) {
                            // ensure that the the map location is disabled. Android Lint says this is an error so I have to do this extra check to remove the lint check
                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                mMap.setMyLocationEnabled(false);
                            }

                        }
                    }
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
            Timber.d("Google Map object is null. Getting Google Map Object");
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
        if (getActivity() == null || busListInteraction.getPatApiService() == null) return;
        mMap = googleMap;
        Timber.d("google map object set");
        patApiService = busListInteraction.getPatApiService();
        Timber.d("PAT API client set");
        // center the map
        if (cameraPosition != null) {
            Timber.d("map was instantiated from a recreation (orientation change, etc.)");
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            zoomSubject.onNext(cameraPosition.zoom);
            enableGoogleMapLocation();
        } else {
            Timber.d("Map was instantiated from a clean state. Centering the map on Pittsburgh and possibly on you");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PITTSBURGH, 11.8f));
            centerMapWithPermissions();
        }

        mMap.setInfoWindowAdapter(new ETAWindowAdapter(getActivity().getLayoutInflater()));

        mMap.setOnCameraIdleListener(() -> {
            cameraPosition = mMap.getCameraPosition();
            if (zoom != cameraPosition.zoom) {
                Timber.v("Change zoom state: %f", zoom);
                zoom = cameraPosition.zoom;
                zoomSubject.onNext(zoom);
            }
        });
        // set up observable information
        setupReactiveObjects();

        // set up predictions onClick
        PredictionsViewModel predictionsViewModel = new PredictionsViewModel(patApiService, getResources().getInteger(R.integer.marker_camera_delay));
        mMap.setOnMarkerClickListener((marker) -> {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), getResources().getInteger(R.integer.marker_camera_delay), null);
            if (busListInteraction == null) {
                Timber.w("Marker was clicked but cannot interact with bus list fragment");
                return false;
            }
            Single<ProcessedPredictions> predictionsSingle = predictionsViewModel
                    .getPredictions(marker, new HashSet<>(busListInteraction.getSelectedRoutes()));
            if (predictionsSingle == null) {
                Timber.w("Marker clicked but no single given");
                return false;
            }
            predictionsSubscription = predictionsSingle
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(predictionsObserver());
            return true;
        });
    }

    /**
     * @return Lambda for displaying connection issues to the user.
     */
    private Consumer<Throwable> disconnectionMessage() {
        return throwable -> {
            if (isInternetDown(throwable)) {
                Timber.i(throwable, getString(R.string.disconnected_internet));
                if (busListInteraction != null) {
                    busListInteraction.showToast(getString(R.string.disconnected_internet), Toast.LENGTH_SHORT);
                }
            }
        };
    }

    /**
     * @return Lambda for displaying reconnection to the user.
     */
    private Consumer<Boolean> reconnectionMessage() {
        return aBoolean -> {
            Timber.i(getString(R.string.retrying_vehicles));
            if (busListInteraction != null) {
                busListInteraction.showToast(getString(R.string.retrying_vehicles), Toast.LENGTH_SHORT);
            }
        };
    }

    /**
     * <p>Observables created:</p>
     * <ul>
     * <li>{@link #vehicleErrorFlowable}</li>
     * <li>{@link #vehicleErrorFlowable}</li>
     * </ul>
     * <p>Subscriptions created:</p>
     * <ul>
     * <li>{@link #vehicleSubscriptions}</li>
     * <li>{@link #unselectVehicleSubscription}</li>
     * </ul>
     */
    private void setupReactiveObjects() {
        Flowable<Set<String>> selectedRoutesFlowable = busListInteraction.getSelectedRoutesObservable()
                .observeOn(Schedulers.io());
        Flowable<Route> toggledRoutesFlowable = busListInteraction.getToggledRouteObservable()
                .observeOn(Schedulers.io());

        setupPolylineObservable(toggledRoutesFlowable);
        ConnectableFlowable<Set<String>> selectionObservable = selectedRoutesFlowable
                .replay(1);
        //noinspection Convert2Lambda
        Flowable<BustimeVehicleResponse> vehicleIntervalObservable = selectionObservable
                .debounce(400, TimeUnit.MILLISECONDS)
                .switchMap(new Function<Set<String>, Flowable<Set<String>>>() {
                    @Override
                    public Flowable<Set<String>> apply(Set<String> routes) throws Exception {
                        Timber.d("Selecting vehicle observable");
                        if (routes.isEmpty()) {
                            return Flowable.just(routes);
                        }
                        return Flowable.interval(0, 10, TimeUnit.SECONDS)
                            .map(aLong -> {
                                if (BuildConfig.DEBUG) {
                                    String msg = String.format(Locale.US, "Calling x%d", aLong);
                                    Timber.d(msg);
                                }
                                return routes;
                            });
                    }
                }).flatMap(routes -> {
                    if (BuildConfig.DEBUG) {
                        String msg = String.format("updating map with %s", routes);
                        Timber.d(msg);
                    }
                    return patApiService.getVehicles(routes);
                }).map(VehicleResponse::getBustimeResponse)
                .retryWhen(attempt -> attempt
                        .compose(retryIfInternet(disconnectionMessage(), reconnectionMessage()))
                )
                .share()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());

        vehicleUpdateFlowable = vehicleIntervalObservable
                .flatMap(bustimeVehicleResponse -> {
                    Timber.d("Iterating through all vehicles to add");
                    return Flowable.fromIterable(bustimeVehicleResponse.getVehicle());
                }).map(makeBitmaps());

        vehicleErrorFlowable = vehicleIntervalObservable
                .map(BustimeVehicleResponse::getProcessedErrors)
                .distinctUntilChanged()
                .flatMap(errorMap -> Flowable.fromIterable(errorMap.entrySet()))
                .map(transformSingleMessage());

        unselectVehicleSubscription = toggledRoutesFlowable
                .skipWhile(Route::isSelected)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(route -> {
                    Timber.d("removing all %s's", route.getRoute());
                    return Flowable.fromIterable(busMarkers.entrySet())
                            .filter(busMarker -> busMarker.getValue().getTitle().contains(route.getRoute()));
                })
                .subscribeWith(new DisposableSubscriber<Map.Entry<Integer,Marker>>() {
                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error happened while trying to unselect");
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("No longer listening to deselections");
                    }

                    @Override
                    public void onNext(Map.Entry<Integer, Marker> vehicleMapEntry) {
                        busMarkers.remove(vehicleMapEntry.getKey());
                        vehicleMapEntry.getValue().remove();
                    }
                });

        resetMapSubscriptions();
        selectionSubscription = selectionObservable.connect();
        busListInteraction.restoreSelection();
    }

    private void setupPolylineObservable(Flowable<Route> routeSelectionObservable) {
        /*
      Creates a stream for the polyline observable
     */
        PatternViewModel patternViewModel = new PatternViewModel(
                patApiService,
                routeSelectionObservable
        );
        polylineSubscription = patternViewModel.getPatternSelections()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(polylineObserver());

        stopSubscription = patternViewModel.getStopRenderRequests(zoomSubject.toFlowable(BackpressureStrategy.BUFFER))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(stopObserver());

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
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CENTER_MAP_LOCATION_CODE);
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        // use the last location if found and if less than 1 hour
        if (lastLocation != null && isInPittsburgh(lastLocation) && System.currentTimeMillis() - lastLocation.getTime() < 3600000) {
            Timber.i("Using last location to center map.");
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomStopVisibility));
        } else { // request one location update
            Timber.i("Creating 1 location request to center map on you.");
            LocationRequest gLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setExpirationTime(10000) // set expiration 10 seconds
                    .setNumUpdates(1); // only do one update. needs above call for expiration
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, gLocationRequest, this);
        }
        enableGoogleMapLocation();
    }

    /**
     * Enables the google map location UI settings if the permission is allowed.
     */
    private void enableGoogleMapLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CENTER_MAP_LOCATION_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Centers the map on the user's latest location data.
     *
     * @param location the user's latest location data.
     */
    @Override
    public void onLocationChanged(Location location) {
        /*
        in the future, we may want different behaviors when the FusedLocationApi
        requests location updates. If this needs to happen, we should have
        multiple internal classes that implement LocationListener with different logic in each
         */
        if (location == null || !isInPittsburgh(location) || mMap == null) return;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomStopVisibility));
    }
    // endregion

    // region Map State and Observables

    /**
     * Resets the composite subscription for observable things on {@link #mMap}
     */
    private void resetMapSubscriptions() {
        Timber.d("Resetting vehicle subscriptions in bus fragment");
        if (vehicleSubscriptions == null || vehicleSubscriptions.isDisposed()) {
            vehicleSubscriptions = new CompositeDisposable(
                    vehicleUpdateFlowable.subscribeWith(vehicleUpdateObserver()),
                    vehicleErrorFlowable.subscribeWith(vehicleErrorObserver())
            );
            Timber.d("Vehicle subscriptions subscribed");
        } else if (BuildConfig.DEBUG) {
            IllegalStateException ex = new IllegalStateException("Vehicle state subscription must be unsubscribed.");
            Timber.e(ex, "Vehicle subscription is leaking.");
            throw ex;
        }

    }

    /**
     * This is an anonymous function that attaches a route's bitmap to its information
     * from {@link Vehicle}. This will make an {@link Observable} emit a {@link VehicleBitmap}.
     *
     * @return the anonymous vehicle information with its associated bitmap
     */
    private Function<Vehicle, VehicleBitmap> makeBitmaps() {
        return new Function<Vehicle, VehicleBitmap>() {

            private HashMap<String, Bitmap> busIconCache = new HashMap<>(busListInteraction.getSelectedRoutes().size());

            @Override
            public VehicleBitmap apply(Vehicle vehicle) {
                String routeName = vehicle.getRt();
                if (busIconCache.containsKey(routeName)) {
                    Timber.v("using cached bitmap %s", routeName);
                    return new VehicleBitmap(vehicle, busIconCache.get(routeName));
                } else {
                    Timber.v("creating bitmap %s", routeName);
                    Bitmap icon = makeBitmap(busListInteraction.getSelectedRoute(vehicle.getRt()));
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
     *
     * @return a closure to create a human-readable {@link ErrorMessage}
     * @since 55
     */
    private Function<Map.Entry<String, ArrayList<String>>, ErrorMessage> transformSingleMessage() {
        return new Function<Map.Entry<String, ArrayList<String>>, ErrorMessage>() {

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
            public ErrorMessage apply(Map.Entry<String, ArrayList<String>> processedMessage) {
                return new ErrorMessage(transformMessage(processedMessage.getKey()), processedMessage.getValue());
            }
        };
    }
    // endregion

    // region Observers

    /**
     * This creates an observer to either update or add the buses to the map.
     *
     * @return the vehicle update observer
     * @since 55
     */
    private DisposableSubscriber<VehicleBitmap> vehicleUpdateObserver() {
        return new DisposableSubscriber<VehicleBitmap>() {

            private boolean showedErrors = false;

            @Override
            public void onError(Throwable e) {
                if (e instanceof SocketTimeoutException) {
                    busListInteraction.showToast(getString(R.string.retrofit_http_error), Toast.LENGTH_SHORT);
                } else if (e.getMessage() != null && e.getLocalizedMessage() != null && !showedErrors) {
                    showedErrors = true;
                    if (e instanceof HttpException) {
                        HttpException http = (HttpException) e;
                        busListInteraction.showToast(http.code() + " " + http.message() + ": "
                                + getString(R.string.retrofit_http_error), Toast.LENGTH_SHORT);
                    } else {
                        Timber.e("Vehicle error not handled.");
                        busListInteraction.showToast(getString(R.string.retrofit_conversion_error), Toast.LENGTH_SHORT);
                    }
                    Timber.e(e, "bus_vehicle_error");
                }
                Timber.e(e.getClass().getName());
                Timber.e("Vehicle Observable error. %s\n%s",
                        e.getClass().getName(),
                        Log.getStackTraceString(e)
                );
            }

            @Override
            public void onComplete() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
                String cDateTime = dateFormat.format(new Date());
                Timber.d("Bus map updates finished updates at %s", cDateTime);
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onNext(VehicleBitmap vehicleBitmap) {
                addOrUpdateMarkers(vehicleBitmap);
            }

            /**
             * Handle vehicle updates and adds...
             * <ul>
             *     <li>add marker if not on {@link BusMapFragment#busMarkers} - {@link #addMarker(VehicleBitmap)}</li>
             *     <li>update marker if in {@link BusMapFragment#busMarkers} - {@link #updateMarker(VehicleBitmap, Marker)}</li>
             * </ul>
             *
             * @since 46
             * @param vehicleBitmap - vehicle to be added
             */
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            private void addOrUpdateMarkers(VehicleBitmap vehicleBitmap) {
                int vid = vehicleBitmap.getVehicle().getVid();
                Marker marker = busMarkers.get(vid);
                if (marker == null) {
                    addMarker(vehicleBitmap);
                } else {
                    updateMarker(vehicleBitmap, marker);
                }

            }

            /**
             * adds marker not in {@link BusMapFragment#busMarkers}
             * @since 46
             * @param vehicleBitmap - the vehicle to add
             */
            private void addMarker(VehicleBitmap vehicleBitmap) {
                Vehicle vehicle = vehicleBitmap.getVehicle();
                Timber.v("marker_add adding_marker %s", Integer.toString(vehicle.getVid()));
                Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(vehicle.getLat(), vehicle.getLon()))
                                .title(vehicle.getRt() + "(" + vehicle.getVid() + ") " + vehicle.getDes() + (vehicle.isDly() ? " - Delayed" : ""))
                                .draggable(false)
                                .rotation(vehicle.getHdg())
                                .zIndex(5)
                                .icon(BitmapDescriptorFactory.fromBitmap(vehicleBitmap.getBitmap()))
                                .anchor((float) 0.5, (float) 0.5)
                                .flat(true));
                marker.setTag(vehicleBitmap.getVehicle());
                busMarkers.put(vehicle.getVid(), marker);
            }

            /**
             * Updates marker information on map
             * @since 46
             * @param vehicleBitmap - vehicle to update
             * @param marker - marker to update
             */
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            private void updateMarker(VehicleBitmap vehicleBitmap, Marker marker) {
                Vehicle vehicle = vehicleBitmap.getVehicle();
                Timber.v("marker_update... updating_pointer");
                marker.setTitle(vehicle.getRt() + "(" + vehicle.getVid() + ") " + vehicle.getDes() + (vehicle.isDly() ? " - Delayed" : ""));
                marker.setPosition(new LatLng(vehicle.getLat(), vehicle.getLon()));
                marker.setRotation(vehicle.getHdg());
                if (!((Vehicle) Objects.requireNonNull(marker.getTag())).getRt().equals(vehicle.getRt())) {
                    Timber.d("changing vehicle %d icon from %s to %s", vehicle.getVid(), ((Vehicle) marker.getTag()).getRt(), vehicle.getRt());
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(vehicleBitmap.getBitmap()));
                }
                marker.setTag(vehicle);

            }
        };
    }

    /**
     * This is the vehicle update/add Port Authority API observer that will print each processed error
     * into a Toast.
     *
     * @return the vehicle update observer
     * @since 55
     */
    private DisposableSubscriber<ErrorMessage> vehicleErrorObserver() {
        return new DisposableSubscriber<ErrorMessage>() {
            @Override
            public void onError(Throwable e) {
                Timber.e(e, "error in vehicle error observer");
            }

            @Override
            public void onComplete() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);
                String dateTime = dateFormat.format(new Date());
                Timber.d("Bus map error updates finished at %s", dateTime);
            }

            @Override
            public void onNext(ErrorMessage errorMessage) {
                if (errorMessage != null && errorMessage.getMessage() != null) {
                    busListInteraction.showToast(errorMessage.getMessage() +
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
     *
     * @param currentLocation The current location.
     * @return whether or not your device is in Pittsburgh
     */
    private boolean isInPittsburgh(Location currentLocation) {
        if (currentLocation == null){
            return false;
        }

        double lat = currentLocation.getLatitude();
        double lon = currentLocation.getLongitude();
        return lat > 39.859673 && lat < 40.992847
                && lon > -80.372815 && lon < -79.414258;
    }

    /**
     * Removes all buses
     */
    private void removeBuses() {
        if (busMarkers != null) {
            Timber.d("buses removed");
            //noinspection Convert2streamapi
            for (Marker busMarker : busMarkers.values()) {
                busMarker.remove();
            }
            busMarkers.clear();
        }

    }

    @Override
    public void clearSelection() {
        removeBuses();
    }

    /**
     * sets a visible or invisible patternSelections for a route
     *
     * @param polylines  list of patternSelections
     * @param visibility whether or not the patternSelections are visible or not
     */
    private void setVisiblePolylines(List<Polyline> polylines, boolean visibility) {
        for (Polyline polyline : polylines) {
            polyline.setVisible(visibility);
        }
    }

    @Override
    public DisposableSubscriber<PatternSelection> polylineObserver() {
        return new DisposableSubscriber<PatternSelection>() {
            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Problem with polyline creation.");
            }

            @Override
            public void onComplete() {
                Timber.d("Completed observing on PatternSelection");
            }

            @Override
            public void onNext(PatternSelection patternSelection) {
                if (mMap == null) {
                    Timber.e("Google Map is null while trying to add patternSelections");
                    return;
                }
                List<Polyline> routeLine = routeLines.get(patternSelection.getRouteNumber());
                if (patternSelection.isSelected()) {
                    if (routeLine != null) {
                        setVisiblePolylines(routeLine, patternSelection.isSelected());
                    } else {
                        createRouteLine(patternSelection);
                    }
                } else { // is not selected... so remove the polyline
                    for (Polyline lineInRoute : routeLine) {
                        lineInRoute.remove();
                    }
                    routeLines.remove(patternSelection.getRouteNumber());
                }
            }

            private void createRouteLine(PatternSelection patternSelection) {
                List<List<LatLng>> latLngList = patternSelection.getLatLngs();
                if (patternSelection.getLatLngs() == null) {
                    Timber.i("Cannot add patterns... should not be null");
                    return;
                }
                List<Polyline> polylines = new ArrayList<>(patternSelection.getLatLngs().size());
                for (List<LatLng> latLngs : latLngList) {
                    polylines.add(mMap.addPolyline(
                            new PolylineOptions()
                                    .addAll(latLngs)
                                    .color(patternSelection.getRouteColor())
                                    .geodesic(true)
                    ));
                }
                routeLines.put(patternSelection.getRouteNumber(), polylines);
            }
        };
    }

    private void clearStops() {
        if (stops == null) {
            Timber.i("Cannot clear all getStopRenderRequests");
            return;
        }
        for (Marker marker : stops.values()) {
            marker.remove();
        }
        stops.clear();
    }

    @Override
    public DisposableSubscriber<StopRenderRequest> stopObserver() {
        return new DisposableSubscriber<StopRenderRequest>() {
            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Stop observer encountered an error");
                clearStops();
            }

            @Override
            public void onComplete() {
                Timber.i("Stops have been unsubscribed");
                if (stops != null) {
                    stops.clear();
                }
            }

            @Override
            public void onNext(StopRenderRequest stopRenderRequest) {
                if (mMap == null) {
                    Timber.i("Google Map is null");
                    return;
                }
                if (stops == null) {
                    Timber.i("getStopRenderRequests is null");
                    return;
                }
                Pt stopInfo = stopRenderRequest.getStopPt();
                Marker stopMarker = stops.get(stopInfo.getStpid());
                if (stopRenderRequest.isVisible()) {
                    if (stopMarker == null) {
                        stopMarker = mMap.addMarker(new MarkerOptions()
                                .anchor(.5f, .5f)
                                .title(stopInfo.getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop))
                                .visible(true)
                                .flat(false)
                                .zIndex(10)
                                .draggable(false)
                                .position(new LatLng(stopInfo.getLat(), stopInfo.getLon()))
                                .flat(false)
                        );
                        stopMarker.setTag(stopRenderRequest.getStopPt());
                        stops.put(stopRenderRequest.getStopPt().getStpid(), stopMarker);
                    }
                    else {
                        stopMarker.setVisible(true);
                    }
                } else if (stopMarker != null) {
                    stopMarker.remove();
                    stops.remove(stopInfo.getStpid());
                }
            }
        };
    }

    @Override
    public DisposableSingleObserver<ProcessedPredictions> predictionsObserver() {
        return new DisposableSingleObserver<ProcessedPredictions>() {
            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Predictions encountered an error");
            }

            @Override
            public void onSuccess(ProcessedPredictions processedPredictions) {
                Timber.i("Showing predictions:\n%s", processedPredictions.getPredictions());
                Marker marker = processedPredictions.getMarker();
                marker.setSnippet(processedPredictions.getPredictions());
                marker.showInfoWindow();
            }
        };
    }
    // endregion

}
