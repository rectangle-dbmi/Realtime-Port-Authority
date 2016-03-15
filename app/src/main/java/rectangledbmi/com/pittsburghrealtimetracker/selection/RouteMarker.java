package rectangledbmi.com.pittsburghrealtimetracker.selection;

import com.google.android.gms.maps.model.Marker;

import java.lang.ref.WeakReference;

/**
 * Container that contains the marker and its route number
 * Created by epicstar on 3/14/16.
 */
public class RouteMarker {
    private String routeNumber;
    private WeakReference<Marker> marker;

    public RouteMarker(String routeNumber, Marker marker) {
        this.routeNumber = routeNumber;
        this.marker = new WeakReference<>(marker);
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public Marker getMarker() {
        return marker.get();
    }
}
