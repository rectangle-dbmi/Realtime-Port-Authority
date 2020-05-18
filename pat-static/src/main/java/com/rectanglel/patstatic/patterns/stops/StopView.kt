package com.rectanglel.patstatic.patterns.stops

import io.reactivex.subscribers.DisposableSubscriber

/**
 *
 * View for handling UI things for the getStopRenderRequests on the Port Authority API
 *
 * Created by epicstar on 9/20/16.
 */

interface StopView<R> {
    fun stopObserver(): DisposableSubscriber<R>
}
