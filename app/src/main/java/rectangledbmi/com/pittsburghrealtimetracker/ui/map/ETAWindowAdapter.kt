package rectangledbmi.com.pittsburghrealtimetracker.ui.map

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

import rectangledbmi.com.pittsburghrealtimetracker.R

/**
 *
 * This is the adapter class for the marker option window.
 *
 * Created by epicstar on 12/22/14.
 */
class ETAWindowAdapter(private val inflater: LayoutInflater) : GoogleMap.InfoWindowAdapter {
    private var infoWindow: View? = null

    override fun getInfoWindow(marker: Marker): View? {
        return null //should always return null
    }

    @SuppressLint("InflateParams")
    override fun getInfoContents(marker: Marker): View {
        val infoWindow = infoWindow ?: inflater.inflate(R.layout.map_eta_infowindow, null)

        val title = infoWindow.findViewById<View>(R.id.title) as TextView
        val snippet = infoWindow.findViewById<View>(R.id.snippet) as TextView
        title.text = marker.title
        snippet.text = marker.snippet

        return infoWindow as View
    }
}
