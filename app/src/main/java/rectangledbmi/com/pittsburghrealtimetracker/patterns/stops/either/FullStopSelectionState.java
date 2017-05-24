package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either;


import java.util.HashMap;
import java.util.Map;

import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.StopRenderState;

/**
 * <p>DTO for the full selection state for stops.</p>
 * <p>Created by epicstar on 10/9/16.</p>
 * @author Jeremy Jao
 * @author Michael Antonacci
 */

public class FullStopSelectionState implements EitherStopState<Map<Integer, StopRenderState>> {

    private final Map<Integer, StopRenderState> stopRenderStateMap;

    private FullStopSelectionState(Map<Integer, StopRenderState> stopRenderStateMap) {
        this.stopRenderStateMap = new HashMap<>(stopRenderStateMap);
    }

    public static FullStopSelectionState create(Map<Integer, StopRenderState> stopRenderStateMap) {
        return new FullStopSelectionState(stopRenderStateMap);
    }

    public Map<Integer, StopRenderState> value() {
        return stopRenderStateMap;
    }
}
