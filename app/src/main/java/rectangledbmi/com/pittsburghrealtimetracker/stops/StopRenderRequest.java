package rectangledbmi.com.pittsburghrealtimetracker.stops;

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
    private final boolean isVisible;

    public static StopRenderRequest create(Pt stopInfo, boolean isVisible) {
        return new StopRenderRequest(stopInfo, isVisible);
    }

    private StopRenderRequest(Pt stopPt, boolean isVisible) {
        this.stopPt = stopPt;
        this.isVisible = isVisible;
    }

    public Pt getStopPt() {
        return stopPt;
    }

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopRenderRequest)) return false;

        StopRenderRequest that = (StopRenderRequest) o;

        if (isVisible != that.isVisible) return false;
        return stopPt != null ? stopPt.equals(that.stopPt) : that.stopPt == null;

    }

    @Override
    public int hashCode() {
        int result = stopPt != null ? stopPt.hashCode() : 0;
        result = 31 * result + (isVisible ? 1 : 0);
        return result;
    }
}
