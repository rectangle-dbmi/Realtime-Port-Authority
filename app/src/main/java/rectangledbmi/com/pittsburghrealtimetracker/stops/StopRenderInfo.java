package rectangledbmi.com.pittsburghrealtimetracker.stops;

import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Pt;

/**
 * <p>Rendering info for stopRenderInfos</p>
 * <p>Created by epicstar on 9/20/16.</p>
 * @since 77
 * @author Jeremy Jao
 */
public class StopRenderInfo {
    private final Pt stopInfo;
    private final boolean isVisible;

    public static StopRenderInfo create(Pt stopInfo, boolean isVisible) {
        return new StopRenderInfo(stopInfo, isVisible);
    }

    private StopRenderInfo(Pt stopInfo, boolean isVisible) {
        this.stopInfo = stopInfo;
        this.isVisible = isVisible;
    }


    public Pt getStopInfo() {
        return stopInfo;
    }

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopRenderInfo)) return false;

        StopRenderInfo that = (StopRenderInfo) o;

        if (isVisible != that.isVisible) return false;
        return stopInfo != null ? stopInfo.equals(that.stopInfo) : that.stopInfo == null;

    }

    @Override
    public int hashCode() {
        int result = stopInfo != null ? stopInfo.hashCode() : 0;
        result = 31 * result + (isVisible ? 1 : 0);
        return result;
    }
}
