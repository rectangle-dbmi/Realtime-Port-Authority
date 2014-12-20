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

    /**
     * Routes by string followed by a reference to the stop container
     */
    private ConcurrentHashMap<String, LinkedList<TransitStopContainer>> routeStops;

    /**
     * Stops by integer followed by their stop container
     */
    private ConcurrentHashMap<Integer, TransitStopContainer> stops;
    private boolean isVisible;


    public TransitStop() {
        routeStops = new ConcurrentHashMap<>(10);
        stops = new ConcurrentHashMap<>(300);

    }

    /**
     * Adds a route to an already present stop. Zoom level is necessary to specify whether or not they
     * should be visible.
     * @param stopId stop id
     * @param route route by string
     * @param zoom zoom level
     * @param visibleZoomLevel zoom level threshold
     * @return whether or not the stop was added to the route
     */
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

    /**
     * Adds a marker to the stop container if one isn't already present by its stop id. This will then the route to the marker
     * @param marker The marker of the stop from google maps
     * @param stopId stop id
     * @param route route by string
     * @param zoom zoom level
     * @param visibleZoomLevel zoom level threshold
     * @return whether or not the route was added to the stop
     */
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

    /**
     * Used when we select a route that was originally called on but is currently unselected.
     * @param route route by string
     * @param zoom zoom level
     * @param zoomVisibility zoom level threshold
     * @return whether or not this this call was successful
     */
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

    /**
     * Removes stops that previously had a stop assigned to them but not anymore.
     * @param route the route that was previously selected and is now selected
     * @return whether or not this call was successful
     */
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

    /**
     * Gets the container by stop id
     * @param stopId the stop id
     * @return the container that contains the stop info
     */
    private TransitStopContainer getStop(int stopId) {
        return stops.get(stopId);
    }

    /**
     * Checks whether or not to show the stops depending on the zoom visibility.
     * @param zoom zoom level
     * @param visibleZoomLevel zoom level threshold
     */
    public void checkAllVisibility(float zoom, float visibleZoomLevel) {
//        System.out.println(stops);

        if(stops != null) {
            for (TransitStopContainer stop : stops.values()) {
                stop.setVisibility(zoom, visibleZoomLevel);
            }
        }
    }

    /**
     *
     * @return all stop ids via a set
     */
    public Set<Integer> getStopIds() {
        return stops.keySet();
    }

    /**
     * Container class that contains the bus stop's markers and routes assigned to them
     */
    public class TransitStopContainer {

        /**
         * The marker from google maps
         */
        private Marker marker;

        /**
         * All routes assigned to the stop that are currently selected
         */
        private HashSet<String> selectedBuses;

        /**
         * All routes assigned to the stop that are currently unselected
         */
        private HashSet<String> unSelectedBuses;
//        private boolean isVisible;

        /**
         * Initial constructor
         */
        public TransitStopContainer() {
            marker = null;
            selectedBuses = new HashSet<>(10);
            unSelectedBuses = new HashSet<>(10);
        }

        /**
         * Adds a route to an already present stop if it has a marker
         * @param route route by string
         * @param zoom zoom level
         * @param visibleZoomLevel zoom level threshold
         * @return whether or not this call was successful
         */
        public boolean addMarkerToRoute(String route, float zoom, float visibleZoomLevel) {
            if(marker != null) {
                unSelectedBuses.remove(route);
                if(selectedBuses.add(route)) {
//                    System.out.println("setting visibility");
                    setVisible(zoom, visibleZoomLevel);

                }
//                System.out.println(selectedBuses);
                return true;
            }
            return false;
        }

        /**
         * Adds a marker if there is none to the container then adds the route
         * @param marker the marker that could be added
         * @param route route by string
         * @param zoom zoom level
         * @param visibleZoomLevel zoom level threshold
         * @return whether or not this call was successful
         */
        public boolean addMarker(Marker marker, String route, float zoom, float visibleZoomLevel) {
            if(!addMarkerToRoute(route, zoom, visibleZoomLevel)) {
                this.marker = marker;
                return addMarkerToRoute(route, zoom, visibleZoomLevel);
            }
            return false;
        }

        /**
         * Checks whether or not the stop should be invisible after the route is unselected.
         * If other routes are present with this stop, then it stays visible
         * @param route the route that is considered to be removed.
         * @return whether or not this call was successful
         */
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


        /**
         * This sets the stop as invisible unconditionally
         */
        public void setInvisible() {
            if(hasMarker()) {
                marker.setVisible(false);
            }
        }

        /**
         * The switch to say whether or not the stop is visible or invisible
         * @param zoom the current zoom level
         * @param visibleZoomLevel the zoom level threshold
         */
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

        /**
         * This sets the marker as visible assuming that the zoom level is higher than the threshold
         * and if any route is selected for the stop
         * @param zoom the current zoom level
         * @param visibleZoomLevel the zoom level threshold
         * @return whether or not the marker should be visible
         */
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

        /**
         *
         * @return whether or not the container has a marker
         */
        public boolean hasMarker() {
            return marker != null;
        }

        @Override
        public int hashCode() {
            return marker != null ? marker.hashCode() : 0;
        }
    }
}
