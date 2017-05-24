package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering;

import com.rectanglel.patstatic.patterns.response.Pt;

/**
 * <p>Rendering info for getStopRenderRequests</p>
 * <p>Created by epicstar on 9/20/16.</p>
 * @since 78
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
}
