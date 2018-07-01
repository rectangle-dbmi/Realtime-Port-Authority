package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.PatternViewModel;

/**
 * <p>Workaround for emitting a zoom or selection state from {@link Observable#merge(Iterable)}
 *    in {@link PatternViewModel#getStopRenderRequests(Flowable)} </p>
 * <p>Created by epicstar on 10/9/16.</p>
 *
 * @param <T> the class type that is encapsulated in this interface
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 * @since 78
 */
public interface EitherStopState<T> {
    T value();
}
