package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering

import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.FullStopSelectionState
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.MapState

/**
 *
 * DTO for a pattern view model
 * to store the selection state, map state, and immediate changes for stop visibility.
 *
 * Created by epicstar on 10/8/16.
 * @author Jeremy Jao
 * @since 78
 */

class StopRequestAccumulator private constructor(
        /**
         * @return The most recent selection state for stops
         */
        val fullStopSelectionState: FullStopSelectionState?,
        /**
         * @return The most recent map state
         */
        val mapState: MapState?,
        /**
         * @return the processed list of stops to change
         */
        val stopsToChange: List<StopRenderRequest>) {
    companion object {

        fun create(
                fullStopSelectionState: FullStopSelectionState?,
                mapState: MapState?,
                stopsToChange: List<StopRenderRequest>): StopRequestAccumulator {
            return StopRequestAccumulator(fullStopSelectionState, mapState, stopsToChange)
        }
    }
}
