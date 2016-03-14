package rectangledbmi.com.pittsburghrealtimetracker.selection;

import java.util.Set;

import rectangledbmi.com.pittsburghrealtimetracker.world.Route;

/**
 * An object that contains the selected route and/or all selected routes.
 *
 * Primarily used in the selection subject
 *
 * @author Jeremy Jao
 */
public class RouteSelection {

    private Route toggledRoute;

    private Set<String> selectedRoutes;

    public RouteSelection(Route route) {
        toggledRoute = route;
    }

    public RouteSelection(Set<String> selectedRoutes) {
        this.selectedRoutes = selectedRoutes;
    }

    public RouteSelection(Route route, Set<String> selectedRoutes) {
        toggledRoute = route;
        this.selectedRoutes = selectedRoutes;
    }

    /**
     *
     * @return the toggled route
     */
    public Route getToggledRoute() {
        return toggledRoute;
    }

    /**
     *
     * @return the selected route
     */
    public Set<String> getSelectedRoutes() {
        return selectedRoutes;
    }
}
