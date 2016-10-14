package rectangledbmi.com.pittsburghrealtimetracker.predictions;

import java.util.concurrent.TimeUnit;

import rectangledbmi.com.pittsburghrealtimetracker.model.PatApiService;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Pt;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Vehicle;
import rx.Observable;
import timber.log.Timber;

/**
 * <p>ViewModel for getting predictions.</p>
 * <p>Created by epicstar on 10/13/16.</p>
 */

public class PredictionsViewModel {

    private final PatApiService patApiService;
    private final int delay;

    public PredictionsViewModel(PatApiService patApiService, int delay) {
        Timber.d("Initializing PredictionsViewModel");
        this.patApiService = patApiService;
        this.delay = delay;
    }

    public Observable<ProcessedPredictions> getPredictions(Observable<PredictionsInfo> predictionInfoObservable) {
        if (predictionInfoObservable == null) {
            Timber.w("No prediction info available");
            return null;
        }
        return predictionInfoObservable
                .flatMap(predictionInfo -> {
                    PredictionsType predictionsType = predictionInfo.getPredictionType();
                    int id = predictionsType.getId();
                    if (predictionsType instanceof Vehicle) {
                        Timber.d("Getting vehicle predictions");
                        return patApiService.getVehiclePredictions(id)
                                .map(prds -> ProcessedPredictions.create(predictionInfo.getMarker(), predictionsType, prds));
                    } else if (predictionsType instanceof Pt) {
                        Timber.d("Getting stop predictions");
                        return patApiService.getStopPredictions(id, predictionInfo.getSelectedRoutes())
                                .map(prds -> ProcessedPredictions.create(predictionInfo.getMarker(), predictionsType, prds));
                    }
                    Timber.w("Not getting predictions because of unknown prediction info");
                    return Observable.never();
                }).delay(delay, TimeUnit.MILLISECONDS);
    }
}
