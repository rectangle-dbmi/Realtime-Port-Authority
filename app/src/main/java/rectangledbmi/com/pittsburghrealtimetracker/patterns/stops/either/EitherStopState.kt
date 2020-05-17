package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either

import io.reactivex.Flowable
import io.reactivex.Observable
import rectangledbmi.com.pittsburghrealtimetracker.patterns.PatternViewModel

/**
 *
 * Workaround for emitting a zoom or selection state from [Observable.merge]
 * in [PatternViewModel.getStopRenderRequests]
 *
 * Created by epicstar on 10/9/16.
 *
 * @param <T> the class type that is encapsulated in this interface
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 * @since 78
</T> */
interface EitherStopState<T> {
    fun value(): T
}
