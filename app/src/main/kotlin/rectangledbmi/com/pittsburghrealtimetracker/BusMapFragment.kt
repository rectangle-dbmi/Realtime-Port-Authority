package rectangledbmi.com.pittsburghrealtimetracker

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import rectangledbmi.com.pittsburghrealtimetracker.handlers.extend.ETAWindowAdapter
import rectangledbmi.com.pittsburghrealtimetracker.world.Route
import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStop
import timber.log.Timber


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
     * This is the object that decides the visibility of bus stop markers.
     * Note that this has to be null in [onDestroy].
     */
    var transitStop: TransitStop? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transitStop = TransitStop()
        setGoogleApiClient()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        var zoomLevelValue: TypedValue = TypedValue()
        context!!.resources.getValue(R.integer.zoom_level, zoomLevelValue, true)
        zoomStopVisibility = zoomLevelValue.float

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bus_map, container, false)
        val mapView = view.findViewById(R.id.map) as MapView
        mapView.getMapAsync(this)
        return view
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onSelectBusRoute(route: Route) {
        if (mMap == null) {
            Timber.e("Map is not instantiated")
            return
        }
    }

    override fun onDeselectBusRoute(route: Route) {
        if (mMap == null) {
            Timber.e("Map is not instantiated")
            return
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMap()
    }

    override fun onDestroy() {
        transitStop?.destroyStops()
        transitStop = null
        super.onDestroy()
    }

    private fun setupMap() {
        Timber.d("setting up map")
        transitStop = TransitStop()
        mMap!!.setInfoWindowAdapter(ETAWindowAdapter(activity.layoutInflater))
        mMap!!.setOnMarkerClickListener { marker: Marker ->
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(marker.position),400, null)
            true
        }
        mMap!!.setOnCameraChangeListener { cameraPosition: CameraPosition ->
            if(zoom != cameraPosition.zoom) {
                zoom = cameraPosition.zoom
                transitStop?.checkAllVisibility(zoom, zoomStopVisibility)
            }
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


}
