package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering;

import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.FullStopSelectionState;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.MapState;

/**
 * <p>DTO for a pattern view model
 *    to store the selection state, map state, and immediate changes for stop visibility.</p>
 * <p>Created by epicstar on 10/8/16.</p>
 * @author Jeremy Jao
 * @since 78
 */

public class StopRequestAccumulator {
    private final FullStopSelectionState fullStopSelectionState;
    private final MapState mapState;
    private final List<StopRenderRequest> stopsToChange;

    private StopRequestAccumulator(FullStopSelectionState fullStopSelectionState, MapState mapState, List<StopRenderRequest> stopsToChange) {
        this.fullStopSelectionState = fullStopSelectionState;
        this.mapState = mapState;
        this.stopsToChange = stopsToChange;
    }

    public static StopRequestAccumulator create(
            FullStopSelectionState fullStopSelectionState,
            MapState mapState,
            List<StopRenderRequest> stopsToChange) {
        return new StopRequestAccumulator(fullStopSelectionState, mapState, stopsToChange);
    }

    /**
     * @return The most recent selection state for stops
     */
    public FullStopSelectionState getFullStopSelectionState() {
        return fullStopSelectionState;
    }

    /**
     * @return The most recent map state
     */
    public MapState getMapState() {
        return mapState;
    }

    /**
     * @return the processed list of stops to change
     */
    public List<StopRenderRequest> getStopsToChange() {
        return stopsToChange;
    }
}
