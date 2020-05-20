package rectangledbmi.com.pittsburghrealtimetracker.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.rectanglel.patstatic.errors.ErrorMessage
import com.rectanglel.patstatic.model.PatApiService
import com.rectanglel.patstatic.patterns.polylines.PolylineView
import com.rectanglel.patstatic.patterns.stops.StopView
import com.rectanglel.patstatic.predictions.PredictionsView
import com.rectanglel.patstatic.vehicles.response.BustimeVehicleResponse
import com.rectanglel.patstatic.vehicles.response.Vehicle
import com.rectanglel.patstatic.vehicles.response.VehicleResponse
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subscribers.DisposableSubscriber
import rectangledbmi.com.pittsburghrealtimetracker.BuildConfig
import rectangledbmi.com.pittsburghrealtimetracker.PATTrackApplication.Companion.getRefWatcher
import rectangledbmi.com.pittsburghrealtimetracker.R
import rectangledbmi.com.pittsburghrealtimetracker.patterns.PatternSelection
import rectangledbmi.com.pittsburghrealtimetracker.patterns.PatternViewModel
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering.StopRenderRequest
import rectangledbmi.com.pittsburghrealtimetracker.predictions.PredictionsViewModel
import rectangledbmi.com.pittsburghrealtimetracker.predictions.ProcessedPredictions
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route
import rectangledbmi.com.pittsburghrealtimetracker.ui.selection.ClearSelection
import rectangledbmi.com.pittsburghrealtimetracker.ui.selection.SelectionFragment
import rectangledbmi.com.pittsburghrealtimetracker.utils.ReactiveHelper.isInternetDown
import rectangledbmi.com.pittsburghrealtimetracker.utils.ReactiveHelper.retryIfInternet
import rectangledbmi.com.pittsburghrealtimetracker.vehicles.VehicleBitmap
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

private const val defaultZoom = 11.8f

/**
 *
 * Fragment that holds a map for the buses. This currently holds all logic related to displaying
 * the buses, getStopRenderRequests, and patternSelections on a [GoogleMap] instance
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 */
// remove this suppressed warning when we can start using the stream APIs
class BusMapFragment : SelectionFragment(), ConnectionCallbacks, OnConnectionFailedListener, OnMapReadyCallback, LocationListener, ClearSelection, PolylineView<PatternSelection?>, StopView<StopRenderRequest?>, PredictionsView<ProcessedPredictions?> {

    // region private instance variables
    private var zoomStopVisibility = 0f

    /**
     * The google map object. Make sure to dereference this [.onDestroy]
     */
    private var mMap: GoogleMap? = null

    /**
     * The google map camera position
     */
    private var cameraPosition: CameraPosition? = null
    private var mapView: MapView? = null

    /**
     * Default zoom level
     */
    private var zoom = defaultZoom

    /**
     * Google map cached bus markers
     */
    private var busMarkers: ConcurrentMap<Int, Marker>? = null

    /**
     * client for google map things
     */
    private var googleApiClient: GoogleApiClient? = null

    /**
     * this is an interface that interacts with the
     * [rectangledbmi.com.pittsburghrealtimetracker.ui.selection.NavigationDrawerFragment]
     */
    private var busListInteraction: BusSelectionInteraction? = null

    /**
     * The vehicles subscriptions
     */
    private var vehicleSubscriptions: CompositeDisposable? = null

    /**
     * The subscription to unselect a vehicle from the map
     */
    private var unselectVehicleSubscription: DisposableSubscriber<*>? = null
    private var routeLines: ConcurrentMap<String, List<Polyline>>? = null

    /**
     * Observable that emits individual vehicles onto the map.
     * Instantiated on [.onMapReady] -> [.setupReactiveObjects]
     */
    private var vehicleUpdateFlowable: Flowable<VehicleBitmap>? = null

    /**
     * Observable that emits errors from the vehicle updates
     * Instantiated on [.onMapReady] -> [.setupReactiveObjects]
     */
    private var vehicleErrorFlowable: Flowable<ErrorMessage>? = null

    /**
     * The [retrofit2.Retrofit] instance of the Port Authority TrueTime API
     */
    private var patApiService: PatApiService? = null
    private var selectionSubscription: Disposable? = null

    /**
     * Subscription for the patternSelections
     */
    private var polylineSubscription: Disposable? = null

    /**
     * subscriptions for stops
     */
    private var stopSubscription: Disposable? = null

    /**
     * Hashmap cached google map stops
     */
    private var stops: HashMap<Int, Marker?>? = null

    /**
     * Subject for handling zooms
     */
    private var zoomSubject: BehaviorSubject<Float>? = null

    /**
     * subscription for predictions
     */
    private var predictionsSubscription: Disposable? = null

    // endregion
    // region Android Fragment LifeCycle
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BusSelectionInteraction) {
            // get the default zoom level of the getStopRenderRequests
            busListInteraction = context
            val zoomLevelValue = TypedValue()
            context.resources.getValue(R.integer.zoom_level, zoomLevelValue, true)
            zoomStopVisibility = zoomLevelValue.float
        } else {
            val e = RuntimeException((context.toString()
                    + " must implement BusSelectionInteraction"))
            Timber.e(e, "Fragment must be attached to Activity that interacts with NavigationDrawerFragment")
            throw e
        }
    }

    @SuppressLint("UseSparseArrays")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleApiClient = GoogleApiClient.Builder((context)!!)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()

        // set up the getStopRenderRequests collection object and its listeners
        busMarkers = ConcurrentHashMap()
        routeLines = ConcurrentHashMap()
        stops = HashMap()
        zoomSubject = BehaviorSubject.create()
    }

    override fun onActivityCreated(inState: Bundle?) {
        super.onActivityCreated(inState)
        if (inState == null) return
        cameraPosition = inState.getParcelable(CAMERA_POSITION)
        if (cameraPosition != null) {
            zoomSubject?.onNext(cameraPosition!!.zoom)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bus_map, container, false)
        mapView = view.findViewById<View>(R.id.map) as MapView
        mapView?.onCreate(savedInstanceState)
        return view
    }

    override fun onResume() {
        super.onResume()
        //        ServerDownDialogFragment.newInstance().show(getFragmentManager(), getString(R.string.servers_down_title));
        Timber.d("resuming map fragment")
        if (mapView != null) {
            mapView?.onResume()
            Timber.d("resumed map view")
        }
        // enable/disable UI to see your current location based on permission changes
        val context = context
        if (context == null || mMap == null) return
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED && mMap?.isMyLocationEnabled != true) {
            Timber.d("Setting Google map Location Enabled")
            mMap?.isMyLocationEnabled = true
        } else if (permission == PackageManager.PERMISSION_DENIED && mMap?.isMyLocationEnabled == true) {
            Timber.d("Setting Google map Location as disabled")
            mMap?.isMyLocationEnabled = false
        }
        resetMapSubscriptions()
    }

    override fun onStart() {
        super.onStart()
        Timber.d("Starting map fragment")
        if (googleApiClient != null) {
            googleApiClient!!.connect()
            Timber.d("Connecting Google Api client")
        }
    }

    override fun onPause() {
        Timber.d("Pausing Map Fragment")
        if (mapView != null) {
            mapView!!.onPause()
            Timber.d("Pausing Map View")
        }
        pauseMapState()
        super.onPause()
    }

    private fun pauseMapState() {
        if (vehicleSubscriptions != null) {
            vehicleSubscriptions!!.dispose()
            Timber.d("vehicle updater unsubscribed")
        }
        removeBuses()
    }

    override fun onStop() {
        Timber.d("Stopping Bus Fragment")
        if (googleApiClient != null) {
            googleApiClient!!.disconnect()
            Timber.d("disconnecting google map api client")
        }
        super.onStop()
    }

    override fun onDestroy() {
        Timber.d("Destroying Bus Fragment")
        if (activity != null) {
            Timber.d("Adding leakcanary to fragment")
            val refWatcher = getRefWatcher((activity)!!)
            refWatcher?.watch(this)
        }
        if (unselectVehicleSubscription != null) {
            unselectVehicleSubscription?.dispose()
            unselectVehicleSubscription = null
            Timber.d("Unselect Vehicle Event destroyed")
        }
        unsubscribeSubscription(polylineSubscription)
        unsubscribeSubscription(stopSubscription)
        unsubscribeSubscription(predictionsSubscription)
        if (selectionSubscription != null) {
            selectionSubscription?.dispose()
            selectionSubscription = null
        }
        if (mMap != null) {
            with(mMap!!) {
                setInfoWindowAdapter(null)
                setOnCameraIdleListener(null)
                setOnMarkerClickListener(null)
            }
            mMap = null
            Timber.d("Google Map Object destroyed")
        }
        if (mapView != null) {
            mapView!!.onDestroy()
            mapView = null
            Timber.d("Map View destroyed")
        }
        cameraPosition = null
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapView!!.onSaveInstanceState(outState)
        if (mMap != null) {
            outState.putParcelable(CAMERA_POSITION, mMap!!.cameraPosition)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        mapView?.onLowMemory()
        super.onLowMemory()
    }

    override fun onDetach() {
        super.onDetach()
        busListInteraction = null
    }

    // endregion
    // region Permission Request Handling
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CENTER_MAP_LOCATION_CODE -> {
                Timber.d("Requesting permissions to center the map")
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Request has been accepted to center the map")
                    centerMapWithPermissions()
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        busListInteraction?.showOkDialog(
                                getString(R.string.location_permission_message),
                                (DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), CENTER_MAP_LOCATION_CODE) })
                        )
                    } else {
                        val message = getString(R.string.center_permissions_denied)
                        Timber.i("Request has been been denied: %s", message)
                        busListInteraction?.makeSnackbar(
                                message,
                                Snackbar.LENGTH_LONG,
                                getString(R.string.center_permissions_action),
                                View.OnClickListener { _: View? -> busListInteraction?.openPermissionsPage() })
                        if (mMap != null && mMap?.isMyLocationEnabled == true) {
                            // ensure that the the map location is disabled. Android Lint says this is an error so I have to do this extra check to remove the lint check
                            if (ActivityCompat.checkSelfPermission((context)!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((context)!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                mMap?.isMyLocationEnabled = false
                            }
                        }
                    }
                }
            }
        }
    }
    // endregion
    // region Google API Client Callbacks
    /**
     * Called from [.onStart] to connect to the Google APIs. This will get the Google Map object
     * which is handled in [.onMapReady]
     *
     * @param bundle the saved state
     */
    override fun onConnected(bundle: Bundle?) {
        if (mMap == null) {
            Timber.d("Google Map object is null. Getting Google Map Object")
            mapView?.getMapAsync(this)
        }
    }

    override fun onConnectionSuspended(i: Int) {
        Timber.i("Google callback suspended with number %d", i)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Timber.i("Google Callback Connection failed, %s", connectionResult.toString())
    }
    // endregion
    // region Google Map and Location Callbacks
    /**
     * This is called from [.onConnected] to retrieve a non-null map object
     * from the Google Maps API.
     *
     * @param googleMap the google map object
     */
    override fun onMapReady(googleMap: GoogleMap) {
        if (activity == null || busListInteraction?.patApiService == null) return
        mMap = googleMap
        Timber.d("google map object set")
        patApiService = busListInteraction?.patApiService
        Timber.d("PAT API client set")
        // center the map
        if (cameraPosition != null) {
            Timber.d("map was instantiated from a recreation (orientation change, etc.)")
            mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            zoomSubject?.onNext(cameraPosition!!.zoom)
            enableGoogleMapLocation()
        } else {
            Timber.d("Map was instantiated from a clean state. Centering the map on Pittsburgh and possibly on you")
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(PITTSBURGH, defaultZoom))
            centerMapWithPermissions()
        }
        mMap?.setInfoWindowAdapter(ETAWindowAdapter(activity!!.layoutInflater))
        mMap?.setOnCameraIdleListener {
            cameraPosition = mMap?.cameraPosition
            if (zoom != cameraPosition?.zoom) {
                Timber.v("Change zoom state: %f", zoom)
                zoom = cameraPosition?.zoom ?: defaultZoom
                zoomSubject?.onNext(zoom)
            }
        }
        // set up observable information
        setupReactiveObjects()

        // set up predictions onClick
        val predictionsViewModel = PredictionsViewModel((patApiService)!!, resources.getInteger(R.integer.marker_camera_delay))
        mMap?.setOnMarkerClickListener { marker: Marker ->
            mMap?.animateCamera(CameraUpdateFactory.newLatLng(marker.position), resources.getInteger(R.integer.marker_camera_delay), null)
            if (busListInteraction == null) {
                Timber.w("Marker was clicked but cannot interact with bus list fragment")
                return@setOnMarkerClickListener false
            }
            val predictionsSingle: Single<ProcessedPredictions>? = predictionsViewModel
                    .getPredictions(marker, HashSet(busListInteraction?.selectedRoutes?.filterNotNull()!!))
            if (predictionsSingle == null) {
                Timber.w("Marker clicked but no single given")
                return@setOnMarkerClickListener false
            }
            predictionsSubscription = predictionsSingle
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(predictionsObserver())
            true
        }
    }

    /**
     * @return Lambda for displaying connection issues to the user.
     */
    private fun disconnectionMessage(): Consumer<Throwable> {
        return Consumer { throwable: Throwable? ->
            if (isInternetDown((throwable)!!)) {
                Timber.i(throwable, getString(R.string.disconnected_internet))
                busListInteraction?.showToast(getString(R.string.disconnected_internet), Toast.LENGTH_SHORT)
            }
        }
    }

    /**
     * @return Lambda for displaying reconnection to the user.
     */
    private fun reconnectionMessage(): Consumer<Boolean> {
        return Consumer { _: Boolean? ->
            Timber.i(getString(R.string.retrying_vehicles))
            busListInteraction?.showToast(getString(R.string.retrying_vehicles), Toast.LENGTH_SHORT)
        }
    }

    /**
     *
     * Observables created:
     *
     *  * [.vehicleErrorFlowable]
     *  * [.vehicleErrorFlowable]
     *
     *
     * Subscriptions created:
     *
     *  * [.vehicleSubscriptions]
     *  * [.unselectVehicleSubscription]
     *
     */
    private fun setupReactiveObjects() {
        val selectedRoutesFlowable = busListInteraction?.selectedRoutesObservable
                ?.observeOn(Schedulers.io())
        val toggledRoutesFlowable = busListInteraction?.toggledRouteObservable
                ?.observeOn(Schedulers.io())
        setupPolylineObservable(toggledRoutesFlowable)
        val selectionObservable = selectedRoutesFlowable
                ?.replay(1)
        val vehicleIntervalObservable = selectionObservable
                ?.debounce(400, TimeUnit.MILLISECONDS)
                ?.switchMap { routes ->
                    Timber.d("Selecting vehicle observable")
                    if (routes.isEmpty()) {
                        Flowable.just(routes)
                    } else Flowable.interval(0, 10, TimeUnit.SECONDS)
                            .map { aLong: Long? ->
                                if (BuildConfig.DEBUG) {
                                    val msg: String = String.format(Locale.US, "Calling x%d", aLong)
                                    Timber.d(msg)
                                }
                                routes
                            }
                }?.flatMap { routes: Set<String?>? ->
                    if (BuildConfig.DEBUG) {
                        val msg: String = String.format("updating map with %s", routes)
                        Timber.d(msg)
                    }
                    if (routes != null) patApiService?.getVehicles(routes.filterNotNull()) else null
                }?.map(VehicleResponse::bustimeResponse)
                ?.retryWhen { attempt: Flowable<Throwable?> ->
                    attempt
                            .compose(retryIfInternet(disconnectionMessage(), reconnectionMessage()))
                }
                ?.share()
                ?.subscribeOn(Schedulers.computation())
                ?.observeOn(AndroidSchedulers.mainThread())
        vehicleUpdateFlowable = vehicleIntervalObservable
                ?.flatMap { bustimeVehicleResponse: BustimeVehicleResponse ->
                    Timber.d("Iterating through all vehicles to add")
                    Flowable.fromIterable(bustimeVehicleResponse.vehicle)
                }?.map(makeBitmaps())
        vehicleErrorFlowable = vehicleIntervalObservable
                ?.map(BustimeVehicleResponse::processedErrors)
                ?.distinctUntilChanged()
                ?.flatMap { errorMap: HashMap<String, ArrayList<String>> -> Flowable.fromIterable<Map.Entry<String, ArrayList<String>>?>(errorMap.entries) }
                ?.map(transformSingleMessage())
        unselectVehicleSubscription = toggledRoutesFlowable
                ?.skipWhile(Route::isSelected)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.flatMap { route: Route ->
                    Timber.d("removing all %s's", route.route)
                    Flowable.fromIterable<Map.Entry<Int, Marker>>(busMarkers?.entries)
                            .filter { busMarker: Map.Entry<Int, Marker> -> busMarker.value.title.contains((route.route)!!) }
                }
                ?.subscribeWith(object : DisposableSubscriber<Map.Entry<Int?, Marker?>?>() {
                    override fun onError(e: Throwable) {
                        Timber.e(e, "Error happened while trying to unselect")
                    }

                    override fun onComplete() {
                        Timber.d("No longer listening to deselections")
                    }

                    override fun onNext(vehicleMapEntry: Map.Entry<Int?, Marker?>?) {
                        busMarkers?.remove(vehicleMapEntry?.key)
                        vehicleMapEntry?.value?.remove()
                    }
                })
        resetMapSubscriptions()
        selectionSubscription = selectionObservable?.connect()
        busListInteraction?.restoreSelection()
    }

    private fun setupPolylineObservable(routeSelectionObservable: Flowable<Route>?) {
        /*
      Creates a stream for the polyline observable
     */
        val patternViewModel = PatternViewModel(
                (patApiService)!!,
                routeSelectionObservable
        )
        polylineSubscription = patternViewModel.getPatternSelections()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(polylineObserver())
        stopSubscription = patternViewModel.getStopRenderRequests(zoomSubject?.toFlowable(BackpressureStrategy.BUFFER))
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(stopObserver())
    }

    /**
     * Centers the map if the [android.Manifest.permission_group.LOCATION] permissions are granted.
     * If they are not and has never been asked, check to see if location settings should be granted.
     * If they are granted, center the location either on your last known location or on the first
     * location update. Otherwise, don't run it.
     */
    private fun centerMapWithPermissions() {
        if (mMap == null || context == null) return

        // first check if the permission is granted... if not, show why you should if user didn't say
        if (ContextCompat.checkSelfPermission((context)!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), CENTER_MAP_LOCATION_CODE)
            return
        }
        val lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        // use the last location if found and if less than 1 hour
        if ((lastLocation != null) && isInPittsburgh(lastLocation) && (System.currentTimeMillis() - lastLocation.time < 3600000)) {
            Timber.i("Using last location to center map.")
            val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomStopVisibility))
        } else { // request one location update
            Timber.i("Creating 1 location request to center map on you.")
            val gLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setExpirationTime(10000) // set expiration 10 seconds
                    .setNumUpdates(1) // only do one update. needs above call for expiration
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, gLocationRequest, this)
        }
        enableGoogleMapLocation()
    }

    /**
     * Enables the google map location UI settings if the permission is allowed.
     */
    private fun enableGoogleMapLocation() {
        if (ContextCompat.checkSelfPermission((context)!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), CENTER_MAP_LOCATION_CODE)
            return
        }
        mMap?.isMyLocationEnabled = true
    }

    /**
     * Centers the map on the user's latest location data.
     *
     * @param location the user's latest location data.
     */
    override fun onLocationChanged(location: Location) {
        /*
        in the future, we may want different behaviors when the FusedLocationApi
        requests location updates. If this needs to happen, we should have
        multiple internal classes that implement LocationListener with different logic in each
         */
        if (!isInPittsburgh(location) || (mMap == null)) return
        val latLng = LatLng(location.latitude, location.longitude)
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomStopVisibility))
    }
    // endregion
    // region Map State and Observables
    /**
     * Resets the composite subscription for observable things on [.mMap]
     */
    private fun resetMapSubscriptions() {
        Timber.d("Resetting vehicle subscriptions in bus fragment")
        if (vehicleSubscriptions == null || vehicleSubscriptions?.isDisposed == true) {
            vehicleSubscriptions = CompositeDisposable(
                    vehicleUpdateFlowable?.subscribeWith(vehicleUpdateObserver()),
                    vehicleErrorFlowable?.subscribeWith(vehicleErrorObserver())
            )
            Timber.d("Vehicle subscriptions subscribed")
        } else if (BuildConfig.DEBUG) {
            val ex = IllegalStateException("Vehicle state subscription must be unsubscribed.")
            Timber.e(ex, "Vehicle subscription is leaking.")
            throw ex
        }
    }

    /**
     * This is an anonymous function that attaches a route's bitmap to its information
     * from [Vehicle]. This will make an [Observable] emit a [VehicleBitmap].
     *
     * @return the anonymous vehicle information with its associated bitmap
     */
    private fun makeBitmaps(): Function<Vehicle?, VehicleBitmap> {
        return object : Function<Vehicle?, VehicleBitmap> {
            private val busIconCache = HashMap<String?, Bitmap>(busListInteraction?.selectedRoutes?.size ?: 0)
            override fun apply(vehicle: Vehicle): VehicleBitmap {
                val routeName = vehicle.rt
                return if (busIconCache.containsKey(routeName)) {
                    Timber.v("using cached bitmap %s", routeName)
                    VehicleBitmap(vehicle, (busIconCache[routeName])!!)
                } else {
                    Timber.v("creating bitmap %s", routeName)
                    val icon = makeBitmap(busListInteraction?.getSelectedRoute((vehicle.rt)!!))
                    busIconCache[routeName] = icon
                    VehicleBitmap(vehicle, icon)
                }
            }

            private fun makeBitmap(route: Route?): Bitmap {
                val busIcon = BitmapFactory.decodeResource(resources, R.drawable.bus_icon)
                val busicon = Bitmap.createBitmap(busIcon.width, busIcon.height, busIcon.config)
                val canvas = Canvas(busicon)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.colorFilter = PorterDuffColorFilter(route!!.routeColor, PorterDuff.Mode.MULTIPLY)
                canvas.drawBitmap(busIcon, 0f, 0f, paint)
                drawText(canvas, busIcon, resources.displayMetrics.density, route.route, route.colorAsString)
                return busicon
            }

            private fun drawText(canvas: Canvas, bus_icon: Bitmap, fontScale: Float, routeNumber: String?, routeColor: String) {
                val currentColor = Color.parseColor(routeColor)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.color = if (isLight(currentColor)) Color.BLACK else Color.WHITE
                paint.textSize = 8 * fontScale
                val fontBounds = Rect()
                paint.getTextBounds(routeNumber, 0, routeNumber!!.length, fontBounds)
                val x = bus_icon.width / 2
                val y = (bus_icon.height.toDouble() / 1.25).toInt()
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(routeNumber, x.toFloat(), y.toFloat(), paint)
            }

            /**
             * Decides whether or not the color (background color) is light or not.
             *
             *
             * Formula was taken from here:
             * http://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
             *
             * @param color the background color being fed
             * @return whether or not the background color is light or not (.345 is the current threshold)
             * @since 47
             */
            private fun isLight(color: Int): Boolean {
                return 1.0 - ((0.299 * Color.red(color)) + (0.587 * Color.green(color)) + (0.114 * Color.blue(color))) / 255 < .5
            }
        }
    }

    /**
     * Creates an anonymous class to make messages more human-readable.
     *
     * @return a closure to create a human-readable [ErrorMessage]
     * @since 55
     */
    private fun transformSingleMessage(): Function<Map.Entry<String, ArrayList<String>>?, ErrorMessage> {
        return object : Function<Map.Entry<String, ArrayList<String>>?, ErrorMessage> {
            /**
             * Transforms the original message to a user-readable message.
             * @param originalMessage The original Port Authority API message
             * @return a user-readable message from the original API message
             */
            private fun transformMessage(originalMessage: String?): String? {
                return when {
                    originalMessage?.contains("No data found for parameter") == true -> {
                        getString(R.string.no_vehicle_error)
                    }
                    originalMessage?.contains("specified") == true && originalMessage.contains("rt") -> {
                        getString(R.string.no_routes_selected)
                    }
                    originalMessage?.contains("Transaction limit for current day has been exceeded") == true -> {
                        getString(R.string.pat_api_exceeded)
                    }
                    else -> originalMessage
                }
            }

            override fun apply(processedMessage: Map.Entry<String, ArrayList<String>>): ErrorMessage {
                return ErrorMessage((transformMessage(processedMessage.key))!!, processedMessage.value)
            }
        }
    }
    // endregion
    // region Observers
    /**
     * This creates an observer to either update or add the buses to the map.
     *
     * @return the vehicle update observer
     * @since 55
     */
    private fun vehicleUpdateObserver(): DisposableSubscriber<VehicleBitmap?> {
        return object : DisposableSubscriber<VehicleBitmap?>() {
            private var showedErrors = false
            override fun onError(e: Throwable) {
                if (e is SocketTimeoutException) {
                    busListInteraction?.showToast(getString(R.string.retrofit_http_error), Toast.LENGTH_SHORT)
                } else if ((e.message != null) && (e.localizedMessage != null) && !showedErrors) {
                    showedErrors = true
                    if (e is HttpException) {
                        val http = e
                        busListInteraction?.showToast((http.code().toString() + " " + http.message() + ": "
                                + getString(R.string.retrofit_http_error)), Toast.LENGTH_SHORT)
                    } else {
                        Timber.e("Vehicle error not handled.")
                        busListInteraction?.showToast(getString(R.string.retrofit_conversion_error), Toast.LENGTH_SHORT)
                    }
                    Timber.e(e, "bus_vehicle_error")
                }
                Timber.e(e.javaClass.name)
                Timber.e("Vehicle Observable error. %s\n%s",
                        e.javaClass.name,
                        Log.getStackTraceString(e)
                )
            }

            override fun onComplete() {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH)
                val cDateTime = dateFormat.format(Date())
                Timber.d("Bus map updates finished updates at %s", cDateTime)
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            override fun onNext(vehicleBitmap: VehicleBitmap?) {
                if (vehicleBitmap != null) {
                    addOrUpdateMarkers(vehicleBitmap)
                }
            }

            /**
             * Handle vehicle updates and adds...
             *
             *  * add marker if not on [BusMapFragment.busMarkers] - [.addMarker]
             *  * update marker if in [BusMapFragment.busMarkers] - [.updateMarker]
             *
             *
             * @since 46
             * @param vehicleBitmap - vehicle to be added
             */
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            private fun addOrUpdateMarkers(vehicleBitmap: VehicleBitmap) {
                val vid = vehicleBitmap.vehicle.vid
                val marker = busMarkers?.getOrElse(vid, { null })
                if (marker == null) {
                    addMarker(vehicleBitmap)
                } else {
                    updateMarker(vehicleBitmap, marker)
                }
            }

            /**
             * adds marker not in [BusMapFragment.busMarkers]
             * @since 46
             * @param vehicleBitmap - the vehicle to add
             */
            private fun addMarker(vehicleBitmap: VehicleBitmap) {
                val vehicle = vehicleBitmap.vehicle
                Timber.v("marker_add adding_marker %s", vehicle.vid.toString())
                val marker = mMap?.addMarker(MarkerOptions()
                        .position(LatLng(vehicle.lat, vehicle.lon))
                        .title(vehicle.rt + "(" + vehicle.vid + ") " + vehicle.des + (if (vehicle.isDly) " - Delayed" else ""))
                        .draggable(false)
                        .rotation(vehicle.hdg.toFloat())
                        .zIndex(5f)
                        .icon(BitmapDescriptorFactory.fromBitmap(vehicleBitmap.bitmap))
                        .anchor(0.5.toFloat(), 0.5.toFloat())
                        .flat(true))
                marker?.tag = vehicleBitmap.vehicle
                busMarkers?.set(vehicle.vid, marker)
            }

            /**
             * Updates marker information on map
             * @since 46
             * @param vehicleBitmap - vehicle to update
             * @param marker - marker to update
             */
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            private fun updateMarker(vehicleBitmap: VehicleBitmap, marker: Marker) {
                val vehicle = vehicleBitmap.vehicle
                Timber.v("marker_update... updating_pointer")
                marker.title = vehicle.rt + "(" + vehicle.vid + ") " + vehicle.des + (if (vehicle.isDly) " - Delayed" else "")
                marker.position = LatLng(vehicle.lat, vehicle.lon)
                marker.rotation = vehicle.hdg.toFloat()
                if ((Objects.requireNonNull(marker.tag) as Vehicle).rt != vehicle.rt) {
                    Timber.d("changing vehicle %d icon from %s to %s", vehicle.vid, (marker.tag as Vehicle?)?.rt, vehicle.rt)
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(vehicleBitmap.bitmap))
                }
                marker.tag = vehicle
            }
        }
    }

    /**
     * This is the vehicle update/add Port Authority API observer that will print each processed error
     * into a Toast.
     *
     * @return the vehicle update observer
     * @since 55
     */
    private fun vehicleErrorObserver(): DisposableSubscriber<ErrorMessage> {
        return object : DisposableSubscriber<ErrorMessage>() {
            override fun onError(e: Throwable) {
                Timber.e(e, "error in vehicle error observer")
            }

            override fun onComplete() {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH)
                val dateTime = dateFormat.format(Date())
                Timber.d("Bus map error updates finished at %s", dateTime)
            }

            override fun onNext(errorMessage: ErrorMessage) {
                busListInteraction?.showToast(errorMessage.message +
                        (if (errorMessage.parameters != null) ": " + errorMessage.parameters else ""),
                        Toast.LENGTH_SHORT)
            }
        }
    }
    // endregion
    // region Miscellaneous Private Methods
    /**
     * Checks if the current location is in the immediate vicinity
     *
     * @param currentLocation The current location.
     * @return whether or not your device is in Pittsburgh
     */
    private fun isInPittsburgh(currentLocation: Location?): Boolean {
        if (currentLocation == null) {
            return false
        }
        val lat = currentLocation.latitude
        val lon = currentLocation.longitude
        return (lat > 39.859673) && (lat < 40.992847
                ) && (lon > -80.372815) && (lon < -79.414258)
    }

    /**
     * Removes all buses
     */
    private fun removeBuses() {
        listOfNotNull(busMarkers)
            .flatMap(ConcurrentMap<Int, Marker>::values)
            .forEach(Marker::remove)
        Timber.d("buses removed")
        busMarkers?.clear()
    }

    override fun clearSelection() {
        removeBuses()
    }

    /**
     * sets a visible or invisible patternSelections for a route
     *
     * @param polylines  list of patternSelections
     * @param visibility whether or not the patternSelections are visible or not
     */
    private fun setVisiblePolylines(polylines: List<Polyline>, visibility: Boolean) {
        for (polyline: Polyline in polylines) {
            polyline.isVisible = visibility
        }
    }

    override fun polylineObserver(): DisposableSubscriber<PatternSelection?> {
        return object : DisposableSubscriber<PatternSelection?>() {
            override fun onError(e: Throwable) {
                Timber.e(e, "Problem with polyline creation.")
            }

            override fun onComplete() {
                Timber.d("Completed observing on PatternSelection")
            }

            override fun onNext(patternSelection: PatternSelection?) {
                if (mMap == null) {
                    Timber.e("Google Map is null while trying to add patternSelections")
                    return
                }
                val routeLine = routeLines?.get(patternSelection?.routeNumber)
                if (patternSelection?.isSelected == true) {
                    if (routeLine != null) {
                        setVisiblePolylines(routeLine, patternSelection.isSelected)
                    } else {
                        createRouteLine(patternSelection)
                    }
                } else { // is not selected... so remove the polyline
                    for (lineInRoute: Polyline in routeLine!!) {
                        lineInRoute.remove()
                    }
                    routeLines?.remove(patternSelection?.routeNumber)
                }
            }

            private fun createRouteLine(patternSelection: PatternSelection) {
                val latLngList = patternSelection.latLngs
                if (patternSelection.latLngs == null) {
                    Timber.i("Cannot add patterns... should not be null")
                    return
                }
                val polylines: MutableList<Polyline> = ArrayList(patternSelection.latLngs?.size ?: 0)
                for (latLngs: List<LatLng> in latLngList!!) {
                    polylines.add(mMap!!.addPolyline(
                            PolylineOptions()
                                    .addAll(latLngs)
                                    .color(patternSelection.routeColor)
                                    .geodesic(true)
                    ))
                }
                routeLines?.set(patternSelection.routeNumber, polylines)
            }
        }
    }

    private fun clearStops() {
        if (stops == null) {
            Timber.i("Cannot clear all getStopRenderRequests")
            return
        }
        listOfNotNull(stops)
                .flatMap(HashMap<Int, Marker?>::values)
                .forEach { marker -> marker?.remove()}
        Timber.d("buses removed")
        stops?.clear()
    }

    override fun stopObserver(): DisposableSubscriber<StopRenderRequest?> {
        return object : DisposableSubscriber<StopRenderRequest?>() {
            override fun onError(e: Throwable) {
                Timber.e(e, "Stop observer encountered an error")
                clearStops()
            }

            override fun onComplete() {
                Timber.i("Stops have been unsubscribed")
                stops?.clear()
            }

            override fun onNext(stopRenderRequest: StopRenderRequest?) {
                if (mMap == null) {
                    Timber.i("Google Map is null")
                    return
                }
                if (stops == null) {
                    Timber.i("getStopRenderRequests is null")
                    return
                }
                val stopInfo = stopRenderRequest?.stopPt
                var stopMarker = stops!![stopInfo?.stpid]
                if (stopRenderRequest?.isVisible == true) {
                    if (stopMarker == null) {
                        stopMarker = mMap?.addMarker(MarkerOptions()
                                .anchor(.5f, .5f)
                                .title(stopInfo?.title)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop))
                                .visible(true)
                                .flat(false)
                                .zIndex(10f)
                                .draggable(false)
                                .position(LatLng(stopInfo!!.lat, stopInfo.lon))
                                .flat(false)
                        )
                        stopMarker?.tag = stopRenderRequest.stopPt
                        stops?.set(stopRenderRequest.stopPt.stpid, stopMarker)
                    } else {
                        stopMarker.isVisible = true
                    }
                } else if (stopMarker != null) {
                    stopMarker.remove()
                    stops?.remove(stopInfo?.stpid)
                }
            }
        }
    }

    override fun predictionsObserver(): DisposableSingleObserver<ProcessedPredictions> {
        return object : DisposableSingleObserver<ProcessedPredictions>() {
            override fun onError(e: Throwable) {
                Timber.e(e, "Predictions encountered an error")
            }

            override fun onSuccess(processedPredictions: ProcessedPredictions) {
                Timber.i("Showing predictions:\n%s", processedPredictions.predictions)
                val marker = processedPredictions.marker
                marker.snippet = processedPredictions.predictions
                marker.showInfoWindow()
            }
        }
    } // endregion

    companion object {
        private const val CAMERA_POSITION = "cameraPosition"

        /**
         * The permissions request code to unsubscribe the map
         */
        private const val CENTER_MAP_LOCATION_CODE = 123

        /**
         * The latitude and longitude of Pittsburgh... used if the app doesn't have a saved state of the camera
         */
        private val PITTSBURGH = LatLng(40.441, -79.981)
        private fun unsubscribeSubscription(subscription: Disposable?) {
            if (subscription != null && !subscription.isDisposed) {
                subscription.dispose()
            }
        }
    }
}