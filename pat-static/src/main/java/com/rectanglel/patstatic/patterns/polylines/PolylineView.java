package com.rectanglel.patstatic.patterns.polylines;

import io.reactivex.subscribers.DisposableSubscriber;

/**
 * <p>MVVM View that handles the UI State of Polylines.</p>
 * <p>Created by epicstar on 7/29/16.</p>
 * @since 78
 * @author Jeremy Jao and Michael Antonacci
 */
public interface PolylineView<R> {
    /**
     * Creates a an {@link DisposableSubscriber} for observing GUI changes for patterns
     * @return an Observer for polyline creation.
     */
    DisposableSubscriber<R> polylineObserver();
}
