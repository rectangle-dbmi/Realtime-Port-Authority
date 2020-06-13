package com.rectanglel.patstatic.predictions

import io.reactivex.SingleObserver

/**
 *
 * Defines the View for Predictions
 *
 * Created by epicstar on 10/14/16.
 * @author Jeremy Jao
 * @since 78
 */

interface PredictionsView<R> {
    fun predictionsObserver(): SingleObserver<R>
}
