package rectangledbmi.com.pittsburghrealtimetracker.predictions;

import com.google.android.gms.maps.model.Marker;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import com.rectanglel.patstatic.model.PatApiService;
import com.rectanglel.patstatic.patterns.response.Pt;
import com.rectanglel.patstatic.predictions.PredictionsType;
import com.rectanglel.patstatic.vehicles.response.Vehicle;

import io.reactivex.Single;
import timber.log.Timber;

/**
 * <p>ViewModel for getting predictions.</p>
 * <p>Created by epicstar on 10/13/16.</p>
 * @author Jeremy Jao
 * @since 78
 */

public class PredictionsViewModel {

    private final PatApiService patApiService;
    private final int delay;

    public PredictionsViewModel(PatApiService patApiService, int delay) {
        Timber.d("Initializing PredictionsViewModel");
        this.patApiService = patApiService;
        this.delay = delay;
    }

    /**
     * Creates a single emission assuming that a marker and selected routes are fed
     * @param marker the marker
     * @param selectedRoutes the selected routes
     * @return a single emission to get predictions
     */
    public Single<ProcessedPredictions> getPredictions(Marker marker, HashSet<String> selectedRoutes) {
        if (marker == null || selectedRoutes == null) {
            Timber.w("No prediction info available");
            return null;
        }
        PredictionsType predictionsType = (PredictionsType) marker.getTag();
        int id = predictionsType.getId();
        if (predictionsType instanceof Vehicle) { // get vehicle predictions if marker is a vehicle
            Timber.d("Getting vehicle predictions");
            return patApiService.getVehiclePredictions(id)
                    .map(prds -> ProcessedPredictions.create(marker, predictionsType, prds))
                    .delay(delay, TimeUnit.MILLISECONDS);
        } else if (predictionsType instanceof Pt) { // get stop predictions if marker is a Stop/Pt
            Timber.d("Getting stop predictions");
            return patApiService.getStopPredictions(id, selectedRoutes)
                    .map(prds -> ProcessedPredictions.create(marker, predictionsType, prds))
                    .delay(delay, TimeUnit.MILLISECONDS);
        }
        Timber.w("Not getting predictions because of unknown prediction info");
        return null;
    }
}
