package rectangledbmi.com.pittsburghrealtimetracker.patterns

import com.google.android.gms.maps.model.LatLng
import com.rectanglel.patstatic.patterns.polylines.PolylineView
import com.rectanglel.patstatic.patterns.response.Ptr

/**
 *
 * Data Transfer Object that transfers meta info for [com.google.android.gms.maps.model.Polyline]
 * creation from the [PatternViewModel]
 * to the [PolylineView].
 *
 *
 * The contract of this class should be that if [.isSelected] is `false`, [.getPatterns]
 * should be `null. Otherwise, it will always not be null.
 *
 *
 * Created by epicstar on 7/30/16.
 * @since 78
 * @author Jeremy Jao
 */
class PatternSelection
/**
 *
 * It is recommended to use this constructor when you want to show the polyline of a route number.
 * Otherwise, it breaks the contract of this data-transfer object.
 *
 *
 * Creates a data-transfer object for [com.google.android.gms.maps.model.Polyline] creation.
 *
 * @param patterns a list of patterns for polyline metadata
 * @param isSelected whether or not the route is selected
 * @param routeNumber the route number of the polyline
 * @param routeColor the route color of the polyline
 */
(
        /**
         *
         * The List of patterns. Each pattern should represent some metadata
         * [com.google.android.gms.maps.model.Polyline]s for a selected route.
         *
         *
         * This should always be null if [.isSelected] is `false`. Otherwise, it should almost
         * always not be `null`.
         * @return a list of Patterns for [com.google.android.gms.maps.model.Polyline] creation
         */
        val patterns: List<Ptr>?,
        /**
         *
         * Tells the [PolylineView] whether or not to show the
         * [com.google.android.gms.maps.model.Polyline]s for a route.
         *
         *
         * If this is `false`, [.getPatterns] should always be null.
         * @return whether or not to show the [com.google.android.gms.maps.model.Polyline].
         */
        val isSelected: Boolean,
        /**
         *
         * @return the route number as part of polyline metadata.
         */
        val routeNumber: String,
        /**
         *
         * @return the route's color
         */
        val routeColor: Int) {
    /**
     * [com.google.android.gms.maps.model.Polyline] creation needs a list of latlngs to create.
     * A route has a list of patterns per outbound and inbound route.
     */
    var latLngs: List<List<LatLng>>? = null
}
