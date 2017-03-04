package com.rectanglel.patstatic.patterns.stops;

import rx.Observer;

/**
 * <p>View for handling UI things for the getStopRenderRequests on the Port Authority API</p>
 * <p>Created by epicstar on 9/20/16.</p>
 */

public interface StopView<R> {
    Observer<R> stopObserver();
}
