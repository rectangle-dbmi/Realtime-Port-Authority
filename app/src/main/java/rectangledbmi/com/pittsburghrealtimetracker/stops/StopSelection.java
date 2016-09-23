package rectangledbmi.com.pittsburghrealtimetracker.stops;

import java.util.Collection;

import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Pt;

/**
 * <p>Selection Info for stopRenderInfos.</p>
 * <p>Created by epicstar on 9/20/16.</p>
 * @author Jeremy Jao
 */
public class StopSelection {
    private final Collection<Pt> stopInfoCollection;
    private final String routeNumber;
    private final boolean isSelected;

    public static StopSelection create(Collection<Pt> stopInfoCollection,
                                       String routeNumber,
                                       boolean isSelected) {
        return new StopSelection(stopInfoCollection, routeNumber, isSelected);
    }

    private StopSelection(Collection<Pt> stopInfoCollection, String routeNumber, boolean isSelected) {
        this.stopInfoCollection = stopInfoCollection;
        this.routeNumber = routeNumber;
        this.isSelected = isSelected;
    }

    public Collection<Pt> getStopInfoCollection() {
        return stopInfoCollection;
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
        if (stopInfoCollection != null ? !stopInfoCollection.equals(that.stopInfoCollection) : that.stopInfoCollection != null)
            return false;
        return routeNumber != null ? routeNumber.equals(that.routeNumber) : that.routeNumber == null;

    }

    @Override
    public int hashCode() {
        int result = stopInfoCollection != null ? stopInfoCollection.hashCode() : 0;
        result = 31 * result + (routeNumber != null ? routeNumber.hashCode() : 0);
        result = 31 * result + (isSelected ? 1 : 0);
        return result;
    }
}
