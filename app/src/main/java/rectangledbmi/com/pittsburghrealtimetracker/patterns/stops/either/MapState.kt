package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either

/**
 *
 * DTO for stop render logic on the map state.
 *
 * Created by epicstar on 10/9/16.
 * @author Jeremy Jao
 * @author Michael Antonacci
 * @since 78
 */

class MapState private constructor(// only has shouldStopsBeVisible... maybe we want to also add lat longs to this
        private val shouldStopsBeVisible: Boolean) : EitherStopState<Boolean> {

    override fun value(): Boolean {
        return shouldStopsBeVisible
    }

    companion object {

        @JvmStatic
        fun create(zoom: Boolean): MapState {
            return MapState(zoom)
        }
    }
}
