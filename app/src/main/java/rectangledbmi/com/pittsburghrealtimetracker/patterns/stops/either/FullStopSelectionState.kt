package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either


import java.util.HashMap

import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.StopRenderState

/**
 *
 * DTO for the full selection state for stops.
 *
 * Created by epicstar on 10/9/16.
 * @author Jeremy Jao
 * @author Michael Antonacci
 */

class FullStopSelectionState private constructor(stopRenderStateMap: Map<Int, StopRenderState>) : EitherStopState<Map<Int, StopRenderState>> {

    private val stopRenderStateMap: Map<Int, StopRenderState>

    init {
        this.stopRenderStateMap = HashMap(stopRenderStateMap)
    }

    override fun value(): Map<Int, StopRenderState> {
        return stopRenderStateMap
    }

    companion object {

        @JvmStatic
        fun create(stopRenderStateMap: Map<Int, StopRenderState>): FullStopSelectionState {
            return FullStopSelectionState(stopRenderStateMap)
        }
    }
}
