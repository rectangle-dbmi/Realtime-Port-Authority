package rectangledbmi.com.pittsburghrealtimetracker.predictions;

import rx.Observer;

/**
 * <p>Defines the View for Predictions</p>
 * <p>Created by epicstar on 10/14/16.</p>
 * @author Jeremy Jao
 * @since 77
 */

public interface PredictionsView {
    Observer<ProcessedPredictions> predictionsObserver();
}
