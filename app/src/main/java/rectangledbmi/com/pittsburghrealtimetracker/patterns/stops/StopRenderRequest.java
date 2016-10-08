package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops;

import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Pt;

/**
 * <p>Rendering info for getStopRenderRequests</p>
 * <p>Created by epicstar on 9/20/16.</p>
 * @since 77
 * @author Jeremy Jao
 * @author Michael Antonacci
 */
public class StopRenderRequest {
    private final Pt stopPt;
    private final int routeCount;

    public static StopRenderRequest create(Pt stopInfo, int routeCount) {
        return new StopRenderRequest(stopInfo, routeCount);
    }

    private StopRenderRequest(Pt stopPt, int routeCount) {
        this.stopPt = stopPt;
        this.routeCount = routeCount;
    }

    public Pt getStopPt() {
        return stopPt;
    }

    public int routeCount() {
        return routeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopRenderRequest)) return false;

        StopRenderRequest that = (StopRenderRequest) o;

        if (routeCount != that.routeCount) return false;
        return stopPt != null ? stopPt.equals(that.stopPt) : that.stopPt == null;

    }

    @Override
    public int hashCode() {
        int result = stopPt != null ? stopPt.hashCode() : 0;
        result = 31 * result + routeCount;
        return result;
    }
}
