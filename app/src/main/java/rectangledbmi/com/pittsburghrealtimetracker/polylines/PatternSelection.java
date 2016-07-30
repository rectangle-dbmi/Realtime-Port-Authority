package rectangledbmi.com.pittsburghrealtimetracker.polylines;

import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Ptr;

/**
 * <p>Data Transfer Object that transfers meta info for {@link com.google.android.gms.maps.model.Polyline}
 * creation from the {@link PolylineViewModel} to the {@link PolylineView}.</p>
 *
 * <p>The contract of this class should be that if {@link #isSelected()} is `false`, {@link #getPatterns()}
 * should be `null. Otherwise, it will always not be null.</p>
 *
 * <p>Created by epicstar on 7/30/16.</p>
 * @since 76
 * @author Jeremy Jao
 */
public class PatternSelection {

    private List<Ptr> patterns;
    private boolean isSelected;
    private String routeNumber;

    /**
     * <p>It is recommended to use this constructor when you want to unselect a polyline for a route
     * since it keeps {@link #getPatterns()} null. We will not the pattern information when unselecting
     * a polyline.</p>
     * <p>Creates a data-transfer object for {@link com.google.android.gms.maps.model.Polyline} creation.</p>
     * @param isSelected
     * @param routeNumber
     */
    public PatternSelection(boolean isSelected, String routeNumber) {
        this(null, isSelected, routeNumber);
    }

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
    public PatternSelection(List<Ptr> patterns, boolean isSelected, String routeNumber) {
        this.patterns = patterns;
        this.isSelected = isSelected;
        this.routeNumber = routeNumber;
    }

    /**
     * <p>The List of patterns. Each pattern should represent a list of
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
}
