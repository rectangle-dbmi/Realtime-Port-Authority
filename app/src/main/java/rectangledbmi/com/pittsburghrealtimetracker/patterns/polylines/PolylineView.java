package rectangledbmi.com.pittsburghrealtimetracker.patterns.polylines;

import rx.Observer;

/**
 * <p>MVVM View that handles the UI State of Polylines.</p>
 * <p>Created by epicstar on 7/29/16.</p>
 * @since 77
 * @author Jeremy Jao and Michael Antonacci
 */
public interface PolylineView {
    /**
     * Creates a an {@link Observer} for {@link com.google.android.gms.maps.model.Polyline} creation
     * info from the {@link rectangledbmi.com.pittsburghrealtimetracker.patterns.PatternViewModel}.
     * @return an Observer for {@link com.google.android.gms.maps.model.Polyline} creation.
     */
    Observer<PatternSelection> polylineObserver();
}
