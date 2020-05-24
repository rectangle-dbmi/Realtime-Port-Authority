package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either

import io.reactivex.Observable
import rectangledbmi.com.pittsburghrealtimetracker.patterns.PatternViewModel
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.StopRenderState

/**
 *
 * Sum type for emitting a zoom or selection state from [Observable.merge]
 * in [PatternViewModel.getStopRenderRequests]
 *
 * Created by epicstar on 10/9/16.
 *
 *
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 * @since 78
 */

sealed class EitherStopState
data class FullStopSelectionState(val stopRenderStateMap: Map<Int, StopRenderState>) : EitherStopState()
data class MapState(val shouldStopsBeVisible: Boolean) : EitherStopState()
