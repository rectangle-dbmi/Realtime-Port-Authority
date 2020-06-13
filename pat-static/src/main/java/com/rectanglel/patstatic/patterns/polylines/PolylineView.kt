package com.rectanglel.patstatic.patterns.polylines

import io.reactivex.subscribers.DisposableSubscriber

/**
 *
 * MVVM View that handles the UI State of Polylines.
 *
 * Created by epicstar on 7/29/16.
 * @since 78
 * @author Jeremy Jao and Michael Antonacci
 */
interface PolylineView<R> {
    /**
     * Creates a an [DisposableSubscriber] for observing GUI changes for patterns
     * @return an Observer for polyline creation.
     */
    fun polylineObserver(): DisposableSubscriber<R>
}
