package rectangledbmi.com.pittsburghrealtimetracker

import android.content.Context
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import rectangledbmi.com.pittsburghrealtimetracker.handlers.extend.ETAWindowAdapter
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.containers.errors.ErrorMessage
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.containers.vehicles.VehicleBitmap
import rectangledbmi.com.pittsburghrealtimetracker.world.Route
import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStop
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.BustimeVehicleResponse
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Vehicle
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.VehicleResponse
import retrofit2.HttpException
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SelectionFragment.BusSelectionInteraction] interface
 * to handle interaction events.
 * Use the [BusMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BusMapFragment :
        SelectionFragment(),
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    companion object DefaultMapProperties {
        private var PITTSBURGH: LatLng = LatLng(40.441, -79.981)

        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param param1 Parameter 1.
         * *
         * @param param2 Parameter 2.
         * *
         * @return A new instance of fragment BusMapFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): BusMapFragment {
            val fragment = BusMapFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private var mMap: GoogleMap? = null

    private var googleAPIClient: GoogleApiClient? = null

    private var zoom: Float = 11.8f

    private var zoomStopVisibility: Float = 15.0f

    /**
     * Subscription for bus vehicle update errors
     */
    private var vehicleSubscription: Subscription? = null

    private var vehicleErrorSubscription: Subscription? = null

    private var busMarkers: ConcurrentMap<Int, Marker>? = null

    /**
     *
     */
    private var routeLines: ConcurrentMap<String, List<Polyline>>? = null

    /**
     * This is the object that decides the visibility of bus stop markers.
     * Note that this has to be null in [onDestroy].
     */
    var transitStop: TransitStop? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        var zoomLevelValue: TypedValue = TypedValue()
        context!!.resources.getValue(R.integer.zoom_level, zoomLevelValue, true)
        zoomStopVisibility = zoomLevelValue.float

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transitStop = TransitStop()
        setGoogleApiClient()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bus_map, container, false)
        val mapView = view.findViewById(R.id.map) as MapView
        mapView.getMapAsync(this)
        return view
    }

    override fun onPause() {
        stopVehicleSubsciptions()
        super.onPause()
    }

    override fun onDestroy() {
        transitStop?.destroyStops()
        transitStop = null
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
    }
    override fun onConnectionFailed(p0: ConnectionResult?) {
        throw UnsupportedOperationException()
    }

    override fun onConnectionSuspended(p0: Int) {
        throw UnsupportedOperationException()
    }

    override fun onConnected(p0: Bundle?) {
        throw UnsupportedOperationException()
    }

    private fun setGoogleApiClient() {
        googleAPIClient = GoogleApiClient.Builder(activity.applicationContext)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMap()
    }

    private fun setupMap() {
        Timber.d("setting up map")
        transitStop = TransitStop()
        mMap?.setInfoWindowAdapter(ETAWindowAdapter(activity.layoutInflater))
        mMap?.setOnMarkerClickListener { marker: Marker ->
            mMap?.animateCamera(CameraUpdateFactory.newLatLng(marker.position),400, null)
            true
        }
        mMap?.setOnCameraChangeListener { cameraPosition: CameraPosition ->
            if(zoom != cameraPosition.zoom) {
                zoom = cameraPosition.zoom
                transitStop?.checkAllVisibility(zoom, zoomStopVisibility)
            }
        }
    }

    override fun onSelectBusRoute(route: Route) {
        stopVehicleSubsciptions()
        if (mMap == null) {
            Timber.e("Map is not instantiated")
            return
        }
    }

    override fun onDeselectBusRoute(route: Route) {
        stopVehicleSubsciptions()
        if (mMap == null) {
            Timber.e("Map is not instantiated")
            return
        }


    }

    /**
     * Checks if the current location is in the immediate vicinity
     * @param currentLocation The current location.
     * @return whether or not your device is in Pittsburgh
     */
    private fun isInPittsburgh(currentLocation: Location): Boolean {
        return currentLocation.latitude > 38.859673 &&
                currentLocation.latitude < 40.992847 &&
                currentLocation.longitude > -80.372815 &&
                currentLocation.longitude < -79.414258
    }

    /**
     * Stops the vehicle subscriptions
     */
    private fun stopVehicleSubsciptions() {
        vehicleSubscription?.unsubscribe()
        vehicleErrorSubscription?.unsubscribe()
    }

    private fun getSelectedRoutes(): Set<String> {
        return busDrawerInteractor.selectedRoutes
    }

    /**
     * @param data - the data in a collection to add
     * *
     * @param  - Any Object that extends [Object]
     * *
     * @since 46
     * *
     * @return a comma-delim strings of data
     */
    private fun <T> collectionToString(data: Collection<T>): String {
        val size = data.size
        var i = 0
        val buf = StringBuilder()
        for (datum in data) {
            buf.append(datum)
            if (++i < size)
                buf.append(',')
        }
        return buf.toString()
    }

    private fun getVehicleIntervalObservable(): Observable<BustimeVehicleResponse> {
        return Observable
            .interval(0, 10, TimeUnit.SECONDS)
            .filter { aLong: Long -> mMap != null && getSelectedRoutes().size > 0 }
            .flatMap { aLong: Long ->
                Timber.d("Updating vehicles ${aLong}x")
                if(BuildConfig.DEBUG) {
                    Toast.makeText(context, "Updating vehicles ${aLong}x", Toast.LENGTH_SHORT)
                }
                busDrawerInteractor.patApiClient.getVehicles(
                        collectionToString(busDrawerInteractor.selectedRoutes),
                        BuildConfig.PAT_API_KEY
                )
            }.map(VehicleResponse::getBustimeResponse)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .share()
    }

    private fun getVehicleUpdateObservable(vehicleIntervalObservable: Observable<BustimeVehicleResponse>): Observable<VehicleBitmap> {
        return vehicleIntervalObservable
            .flatMap { bustimeVehicleResponse ->
                Timber.d("getting vehicles")
                Observable.from(bustimeVehicleResponse.vehicle)
            }.map(makeBitmaps())
    }

    /**
     * Creates a closure for bitmaps for the map.
     * @return [Func1<Vehicle, VehicleBitmap>] The anonymous "function" to make the bitmaps
     */
    private fun makeBitmaps(): Func1<Vehicle, VehicleBitmap> {
        return object : Func1<Vehicle, VehicleBitmap> {

            private val busIconCache = HashMap<String, Bitmap>(busDrawerInteractor.selectedRoutes.size)

            override fun call(vehicle: Vehicle): VehicleBitmap {
                val routeName = vehicle.rt
                if (busIconCache.containsKey(routeName)) {
                    return VehicleBitmap(vehicle, busIconCache[routeName])
                } else {
                    val icon = makeBitmap(busDrawerInteractor.getSelectedRoute(routeName)!!)
                    busIconCache.put(routeName, icon)
                    return VehicleBitmap(vehicle, icon)
                }
            }

            private fun makeBitmap(route: Route): Bitmap {
                var bus_icon = BitmapFactory.decodeResource(resources, R.drawable.bus_icon)
                var busicon = Bitmap.createBitmap(bus_icon.width, bus_icon.height, bus_icon.config)
                var canvas = Canvas(busicon)
                var paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.setColorFilter(PorterDuffColorFilter(route.routeColor, PorterDuff.Mode.MULTIPLY))
                canvas.drawBitmap(bus_icon, 0f, 0f, paint)
                drawText(canvas, bus_icon, resources.displayMetrics.density, route.route, route.colorAsString)
                return busicon
            }

            private fun drawText(canvas: Canvas, bus_icon: Bitmap, fontScale: Float, routeNumber: String, routeColor: String) {
                var currentColor = Color.parseColor(routeColor)
                var paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.color = if (isLight(currentColor)) Color.BLACK else Color.WHITE
                paint.textSize = 8 * fontScale
                var fontBounds = Rect()
                paint.getTextBounds(routeNumber, 0, routeNumber.length, fontBounds)
                var x = bus_icon.width / 2
                var y = (bus_icon.height.toDouble() / 1.25).toInt()
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(routeNumber, x.toFloat(), y.toFloat(), paint)
            }

            /**
             * Decides whether or not the color (background color) is light or not.
             *
             *
             * Formula was taken from here:
             * http://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android

             * @param color the background color being fed
             * *
             * @return whether or not the background color is light or not (.345 is the current threshold)
             * *
             * @since 47
             */
            private fun isLight(color: Int): Boolean {
                return 1.0 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 < .5
            }
        }
    }

    private fun vehicleUpdateObserver(): Subscriber<VehicleBitmap> {
        return object : Subscriber<VehicleBitmap>() {

            private var showedErrors: Boolean = false;

            override fun onError(e: Throwable?) {
                if (e?.message != null && e?.message != null && !showedErrors) {
                    showedErrors = true
                    if (e is IOException) {
                        busDrawerInteractor.showToast(e.message as String, Toast.LENGTH_SHORT)
                    } else if (e is HttpException) {
                        var http: HttpException = e
                        busDrawerInteractor.showToast("${http.code()} ${http.message()}: ${getString(R.string.retrofit_http_error)}", Toast.LENGTH_SHORT)
                    } else {
                        busDrawerInteractor.showToast(getString(R.string.retrofit_conversion_error), Toast.LENGTH_SHORT)
                    }
                    Timber.e("bus observable vehicle error: ${e?.message}")
                }
                Timber.e("vehicle observable error. ${e?.javaClass?.name}\n${Log.getStackTraceString(e)}")
            }

            override fun onCompleted() {
                var dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH)
                var cDateTime = dateFormat.format(Date())
                Timber.d("vehicle_error_complete", "Bus map error updates finished updates at " + cDateTime)

            }

            /**
             * Handle vehicle updates and adds...
             * <ul>
             *     <li>add marker if not on {SelectTransit#busMarkers} - {@link #addMarker(VehicleBitmap)}</li>
             *     <li>update marker if in {@link SelectTransit#busMarkers} - {@link #updateMarker(Vehicle, Marker)}</li>
             * </ul>
             *
             * @since 46
             * @param vehicleBitmap - vehicle to be added
             */
            override fun onNext(vehicleBitmap: VehicleBitmap) {
                var vid: Int = vehicleBitmap.vehicle.vid
                var marker: Marker? = busMarkers?.get(vid)
                if(marker == null)
                    addMarker(vehicleBitmap)
                else
                    updateMarker(vehicleBitmap.vehicle, marker)
            }

            /**
             * adds marker not in {@link SelectTransit#busMarkers}
             * @param vehicleBitmap - the vehicle to add
             */
            private fun addMarker(vehicleBitmap: VehicleBitmap) {
                var vehicle: Vehicle = vehicleBitmap.vehicle
                var delayed: String
                if(vehicle.isDly)
                    delayed = " - Delayed"
                else
                    delayed = ""
                Timber.d("Adding marker ${vehicleBitmap.vehicle.vid}")
                busMarkers?.put(vehicle.vid, mMap?.addMarker(MarkerOptions()
                    .position(LatLng(vehicle.lat, vehicle.lon))
                    .title("${vehicle.rt} (${vehicle.vid}) ${vehicle.des}$delayed")
                    .draggable(false)
                    .rotation(vehicle.hdg.toFloat())
                    .icon(BitmapDescriptorFactory.fromBitmap(vehicleBitmap.bitmap))
                    .anchor(.5f, .5f)
                    .flat(true))
                )
            }

            /**
             * Updates marker information on map
             * @param vehicle - vehicle to update
             * @param marker - marker to update
             */
            private fun updateMarker(vehicle: Vehicle, marker: Marker) {
                var delayed: String
                if(vehicle.isDly)
                    delayed = " - Delayed"
                else
                    delayed = ""
                Timber.d("Updating vehicle ${vehicle.vid}}")
                marker.title = "${vehicle.rt} (${vehicle.vid}) ${vehicle.des}$delayed"
                marker.position = LatLng(vehicle.lat, vehicle.lon)
                marker.rotation = vehicle.hdg.toFloat()
            }
        }
    }

    /**
     * Transforms PAT API messages to something more readable.
     */
    private fun transformSingleMessage(): Func1<Map.Entry<String, ArrayList<String>>, ErrorMessage> {
        return object : Func1<Map.Entry<String, ArrayList<String>>, ErrorMessage> {

            private fun transformMessage(originalMessage: String?): String? {
                if(originalMessage != null) {
                    if (originalMessage.contains("No data found for parameter")) {
                        return getString(R.string.no_vehicle_error)
                    } else if (originalMessage.contains("specified") && originalMessage.contains("rt")) {
                        return getString(R.string.no_routes_selected)
                    } else if (originalMessage.contains("Transaction limit for current day has been exceeded")) {
                        return getString(R.string.pat_api_exceeded)
                    }
                }
                return null
            }

            override fun call(processedMessage: Map.Entry<String, ArrayList<String>>?): ErrorMessage? {
                return ErrorMessage(transformMessage(processedMessage?.key), processedMessage?.value)
            }

        }
    }

    /**
     * THis is the vehicle observer to update the UI for error messages from PAT's system.
     */
    private fun vehicleErrorObserver(): Subscriber<ErrorMessage> {
        return object : Subscriber<ErrorMessage>() {
            override fun onCompleted() {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH)
                val cDateTime = dateFormat.format(Date())
                Timber.d("vehicle_error_complete", "Bus map error updates finished updates at " + cDateTime)
            }

            override fun onNext(errorMessage: ErrorMessage?) {
                if (errorMessage != null && errorMessage.message != null) {
                    busDrawerInteractor.showToast(errorMessage.message + if (errorMessage.message != null) ": " + errorMessage.message else "",
                            Toast.LENGTH_SHORT)
                }
            }

            override fun onError(e: Throwable?) {
                if (e?.message != null)
                    Timber.e(e?.message)
                Timber.e(Log.getStackTraceString(e))
            }

        }
    }

}
