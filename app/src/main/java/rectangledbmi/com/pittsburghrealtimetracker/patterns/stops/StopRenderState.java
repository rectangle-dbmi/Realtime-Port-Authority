package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops;

import com.rectanglel.patstatic.patterns.response.Pt;

/**
 * <p>Rendering info for holding selection states for stops</p>
 * <p>Created by epicstar on 9/20/16.</p>
 * @since 78
 * @author Jeremy Jao
 * @author Michael Antonacci
 */
public class StopRenderState {
    private final Pt stopPt;
    private final int routeCount;

    public static StopRenderState create(Pt stopInfo, int routeCount) {
        return new StopRenderState(stopInfo, routeCount);
    }

    private StopRenderState(Pt stopPt, int routeCount) {
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
        if (!(o instanceof StopRenderState)) return false;

        StopRenderState that = (StopRenderState) o;

        //noinspection SimplifiableIfStatement
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
