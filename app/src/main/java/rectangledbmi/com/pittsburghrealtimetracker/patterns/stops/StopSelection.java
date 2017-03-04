package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops;

import com.rectanglel.patstatic.patterns.response.Pt;

import java.util.Collection;

/**
 * <p>Immediate selection info for getStopRenderRequests.</p>
 * <p>Created by epicstar on 9/20/16.</p>
 * @author Jeremy Jao
 */
public class StopSelection {
    private final Collection<Pt> StopPts;
    private final String routeNumber;
    private final boolean isSelected;

    public static StopSelection create(Collection<Pt> pts,
                                       String routeNumber,
                                       boolean isSelected) {
        return new StopSelection(pts, routeNumber, isSelected);
    }

    private StopSelection(Collection<Pt> pts, String routeNumber, boolean isSelected) {
        this.StopPts = pts;
        this.routeNumber = routeNumber;
        this.isSelected = isSelected;
    }

    public Collection<Pt> getStopPts() {
        return StopPts;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopSelection)) return false;

        StopSelection that = (StopSelection) o;

        if (isSelected != that.isSelected) return false;
        if (StopPts != null ? !StopPts.equals(that.StopPts) : that.StopPts != null)
            return false;
        return routeNumber != null ? routeNumber.equals(that.routeNumber) : that.routeNumber == null;

    }

    @Override
    public int hashCode() {
        int result = StopPts != null ? StopPts.hashCode() : 0;
        result = 31 * result + (routeNumber != null ? routeNumber.hashCode() : 0);
        result = 31 * result + (isSelected ? 1 : 0);
        return result;
    }
}
