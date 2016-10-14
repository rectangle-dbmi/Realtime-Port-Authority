package rectangledbmi.com.pittsburghrealtimetracker.predictions;

import java.util.Collection;

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

    private PatApiService patApiService;

    public PredictionsViewModel(PatApiService patApiService) {
        Timber.d("Initializing PredictionsViewModel");
        this.patApiService = patApiService;
    }

    public Observable<ProcessedPrediction> getPredictions(Observable<PredictionInfo> predictionInfoObservable) {
        if (predictionInfoObservable == null) {
            Timber.w("No prediction info available");
            return null;
        }
        return predictionInfoObservable
                .flatMap(predictionInfo -> {
                    PredictionType predictionType = predictionInfo.getPredictionType();
                    int id = predictionType.getId();
                    if (predictionType instanceof Vehicle) {
                        Timber.d("Getting vehicle predictions");
                        return patApiService.getVehiclePredictions(id)
                                .map(prds -> ProcessedPrediction.create(predictionType, prds));
                    } else if (predictionType instanceof Pt) {
                        Timber.d("Getting stop predictions");
                        return patApiService.getStopPredictions(id, predictionInfo.getSelectedRoutes())
                                .map(prds -> ProcessedPrediction.create(predictionType, prds));
                    }
                    Timber.w("Not getting predictions because of unknown prediction info");
                    return Observable.never();
                });
    }
}
