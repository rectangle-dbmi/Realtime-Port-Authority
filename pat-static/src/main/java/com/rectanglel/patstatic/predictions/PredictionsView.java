package com.rectanglel.patstatic.predictions;

import rx.SingleSubscriber;

/**
 * <p>Defines the View for Predictions</p>
 * <p>Created by epicstar on 10/14/16.</p>
 * @author Jeremy Jao
 * @since 78
 */

public interface PredictionsView<R> {
    SingleSubscriber<R> predictionsObserver();
}
