package rectangledbmi.com.pittsburghrealtimetracker.predictions;

import com.google.android.gms.maps.model.Marker;

import java.util.Collection;
import java.util.HashSet;

/**
 * <p>Container for prediction type and selected routes emission.</p>
 * <p>Created by epicstar on 10/14/16.</p>
 */

public class PredictionsInfo {
    private final Marker marker;
    private final Collection<String> selectedRoutes;

    public static PredictionsInfo create(Marker marker, Collection<String> selectedRoutes) {
        return new PredictionsInfo(marker, selectedRoutes);
    }

    private PredictionsInfo(Marker marker, Collection<String> selectedRoutes) {
        this.marker = marker;
        this.selectedRoutes = new HashSet<>(selectedRoutes);
    }

    public Marker getMarker() {
        return marker;
    }

    PredictionsType getPredictionType() {
        return (PredictionsType) marker.getTag();
    }

    Collection<String> getSelectedRoutes() {
        return selectedRoutes;
    }
}
