package rectangledbmi.com.pittsburghrealtimetracker.world;

import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is supposed to be class that maps the stop markers to the route numbers. In this way, we're
 * making sure that the bus stop markers sync with the selection at constant time.
 *
 * Created by epicstar on 9/5/14.
 */
public class TransitStop {

    private ConcurrentHashMap<String, LinkedList<TransitStopContainer>> routeStops;
    private ConcurrentHashMap<Integer, TransitStopContainer> stops;
    private boolean isVisible;


    public TransitStop() {
        routeStops = new ConcurrentHashMap<>(10);
        stops = new ConcurrentHashMap<>(300);

    }

    public boolean addRouteToMarker(Integer stopId, String route, float zoom, float visibleZoomLevel) {
        TransitStopContainer stop = getStop(stopId);
        if(stop != null) {
            if(stop.addMarkerToRoute(route, zoom, visibleZoomLevel)) {
                LinkedList<TransitStopContainer> stops = routeStops.get(route);
                if(stops == null) {
                    stops = new LinkedList<>();
                }
                stops.add(stop);
                routeStops.put(route, stops);
                return true;
            }
        }

        return false;
    }

    public boolean addMarkerToRoute(Marker marker, Integer stopId, String route, float zoom, float visibleZoomLevel) {
        TransitStopContainer container = stops.get(stopId);
        if(container == null) {
            container = new TransitStopContainer();
        }

        if(!container.hasMarker()) {
            container.addMarker(marker, route, zoom, visibleZoomLevel);
            stops.put(stopId, container);

        }
        return addRouteToMarker(stopId, route, zoom, visibleZoomLevel);

    }

    public boolean updateAddRoutes(String route, float zoom, float zoomVisibility) {
        LinkedList<TransitStopContainer> stoplist = routeStops.get(route);
        if(stoplist != null) {
            for(TransitStopContainer stop : stoplist) {
                stop.addMarkerToRoute(route, zoom, zoomVisibility);
            }
            return true;
        }
        return false;
    }

    public boolean removeRoute(String route) {
        LinkedList<TransitStopContainer> stoplist = routeStops.get(route);
        if(stoplist != null) {
            for(TransitStopContainer stop : stoplist) {
                stop.removeMarker(route);
            }
            return true;
        }
        return false;
    }

    public void clearRoutes() {
        for(String route : routeStops.keySet()) {
            for(TransitStopContainer stop : stops.values())
                stop.removeMarker(route);
        }
    }

    private TransitStopContainer getStop(int stopId) {
        return stops.get(stopId);
    }

    public void checkAllVisibility(float zoom, float visibleZoomLevel) {
//        System.out.println(stops);

        if(stops != null) {
            for (TransitStopContainer stop : stops.values()) {
                stop.setVisibility(zoom, visibleZoomLevel);
            }
        }
    }

    public Set<Integer> getStopIds() {
        return stops.keySet();
    }

    public class TransitStopContainer {

        private Marker marker;
        private HashSet<String> selectedBuses;
        private HashSet<String> unSelectedBuses;
//        private boolean isVisible;

        public TransitStopContainer() {
            marker = null;
            selectedBuses = new HashSet<>(10);
            unSelectedBuses = new HashSet<>(10);
        }

        public boolean addMarkerToRoute(String route, float zoom, float visibleZoomLevel) {
            if(marker != null) {
                unSelectedBuses.remove(route);
                if(selectedBuses.add(route)) {
                    System.out.println("setting visibility");
                    setVisible(zoom, visibleZoomLevel);

                }
//                System.out.println(selectedBuses);
                return true;
            }
            return false;
        }

        public boolean addMarker(Marker marker, String route, float zoom, float visibleZoomLevel) {
            if(!addMarkerToRoute(route, zoom, visibleZoomLevel)) {
                this.marker = marker;
                return addMarkerToRoute(route, zoom, visibleZoomLevel);
            }
            return false;
        }

        public boolean removeMarker(String route) {
            if(selectedBuses.remove(route)) {
                unSelectedBuses.add(route);
                if(selectedBuses.isEmpty())
                    setInvisible();
                return true;
            }
            return false;
        }

        public boolean containsRoute(String route) {
            return selectedBuses.contains(route);
        }

        public Marker getMarker() {
            return marker;
        }



        public void setInvisible() {
            if(hasMarker()) {
                marker.setVisible(false);
            }
        }

        public void setVisibility(float zoom, float visibleZoomLevel) {
            if(!setVisible(zoom, visibleZoomLevel)) {
                setInvisible();
//                System.out.println("Invisible");
                isVisible = false;
            }
            else {
                isVisible = true;
            }

        }

        public boolean setVisible(float zoom, float visibleZoomLevel) {
            if(hasMarker()) {
//                System.out.println(zoom);
//                System.out.println(zoom >= visibleZoomLevel);
                if ((!selectedBuses.isEmpty()) && zoom >= visibleZoomLevel) {
                    marker.setVisible(true);
//                    System.out.println(marker);
//                    System.out.println("Marker should be visible " + marker.isVisible());
                    return true;
                } /*else {
//                    System.out.println(zoom > visibleZoomLevel);
                }*/
            } /*else {
//                System.out.println("setVisible: no marker" );
            }*/
            return false;
        }

        public boolean isVisible() {
            if(hasMarker())
                return marker.isVisible();
            return false;
        }

        public boolean hasMarker() {
            return marker != null;
        }

        @Override
        public int hashCode() {
            return marker != null ? marker.hashCode() : 0;
        }
    }
}
