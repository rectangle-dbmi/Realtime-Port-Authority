package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either;

/**
 * <p>DTO for stop render logic on the map state.</p>
 * <p>Created by epicstar on 10/9/16.</p>
 * @author Jeremy Jao
 * @author Michael Antonacci
 * @since 78
 */

public class MapState implements EitherStopState<Boolean> {

    // only has shouldStopsBeVisible... maybe we want to also add lat longs to this
    private final Boolean shouldStopsBeVisible;

    private MapState(Boolean zoom) {
        this.shouldStopsBeVisible = zoom;
    }

    public static MapState create(Boolean zoom) {
        return new MapState(zoom);
    }

    public Boolean value() {
        return shouldStopsBeVisible;
    }
}
