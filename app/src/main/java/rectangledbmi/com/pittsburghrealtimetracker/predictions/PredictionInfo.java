package rectangledbmi.com.pittsburghrealtimetracker.predictions;

import java.util.Collection;
import java.util.HashSet;

/**
 * <p>Container for prediction type and selected routes emission.</p>
 * <p>Created by epicstar on 10/14/16.</p>
 */

public class PredictionInfo {
    private final PredictionType predictionType;
    private final Collection<String> selectedRoutes;

    public static PredictionInfo create(PredictionType predictionType, Collection<String> selectedRoutes) {
        return new PredictionInfo(predictionType, selectedRoutes);
    }

    private PredictionInfo(PredictionType predictionType, Collection<String> selectedRoutes) {
        this.predictionType = predictionType;
        this.selectedRoutes = new HashSet<>(selectedRoutes);
    }

    public PredictionType getPredictionType() {
        return predictionType;
    }

    public Collection<String> getSelectedRoutes() {
        return selectedRoutes;
    }
}
