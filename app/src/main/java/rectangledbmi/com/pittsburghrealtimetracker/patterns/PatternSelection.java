package rectangledbmi.com.pittsburghrealtimetracker.patterns;

import com.google.android.gms.maps.model.LatLng;
import com.rectanglel.patstatic.patterns.polylines.PolylineView;
import com.rectanglel.patstatic.patterns.response.Ptr;

import java.util.List;

/**
 * <p>Data Transfer Object that transfers meta info for {@link com.google.android.gms.maps.model.Polyline}
 * creation from the {@link PatternViewModel}
 * to the {@link PolylineView}.</p>
 *
 * <p>The contract of this class should be that if {@link #isSelected()} is `false`, {@link #getPatterns()}
 * should be `null. Otherwise, it will always not be null.</p>
 *
 * <p>Created by epicstar on 7/30/16.</p>
 * @since 78
 * @author Jeremy Jao
 */
public class PatternSelection {


    private final List<Ptr> patterns;
    private boolean isSelected;
    private final String routeNumber;
    private List<List<LatLng>> latLngs;
    private final int routeColor;

    /**
     * <p>It is recommended to use this constructor when you want to show the polyline of a route number.
     * Otherwise, it breaks the contract of this data-transfer object.</p>
     *
     * <p>Creates a data-transfer object for {@link com.google.android.gms.maps.model.Polyline} creation.</p>
     *
     * @param patterns a list of patterns for polyline metadata
     * @param isSelected whether or not the route is selected
     * @param routeNumber the route number of the polyline
     */
    public PatternSelection(List<Ptr> patterns, boolean isSelected, String routeNumber, int routeColor) {
        this.patterns = patterns;
        this.isSelected = isSelected;
        this.routeNumber = routeNumber;
        this.routeColor = routeColor;
    }

    /**
     * <p>The List of patterns. Each pattern should represent some metadata
     * {@link com.google.android.gms.maps.model.Polyline}s for a selected route.</p>
     *
     * <p>This should always be null if {@link #isSelected()} is `false`. Otherwise, it should almost
     * always not be `null`.</p>
     * @return a list of Patterns for {@link com.google.android.gms.maps.model.Polyline} creation
     */
    public List<Ptr> getPatterns() {
        return patterns;
    }

    /**
     * <p>Tells the {@link PolylineView} whether or not to show the
     * {@link com.google.android.gms.maps.model.Polyline}s for a route.</p>
     *
     * <p>If this is `false`, {@link #getPatterns()} should always be null.</p>
     * @return whether or not to show the {@link com.google.android.gms.maps.model.Polyline}.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     *
     * @return the route number as part of polyline metadata.
     */
    public String getRouteNumber() {
        return routeNumber;
    }

    /**
     * {@link com.google.android.gms.maps.model.Polyline} creation needs a list of latlngs to create.
     * A route has a list of patterns per outbound and inbound route.
     * @return List of latLngs for polyline creation
     */
    public List<List<LatLng>> getLatLngs() {
        return latLngs;
    }

    /**
     * Sets the object for direct {@link com.google.android.gms.maps.model.Polyline} metadata
     * @param latLngs a list of Google Map points for polyline creation
     */
    public void setLatLngs(List<List<LatLng>> latLngs) {
        this.latLngs = latLngs;
    }

    /**
     *
     * @return the route's color
     */
    public int getRouteColor() {
        return routeColor;
    }
}
