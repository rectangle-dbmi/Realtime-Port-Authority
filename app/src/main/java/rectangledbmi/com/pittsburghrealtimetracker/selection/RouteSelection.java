package rectangledbmi.com.pittsburghrealtimetracker.selection;

import java.util.Set;

import timber.log.Timber;

/**
 * An object that contains the selected route and/or all selected routes.
 *
 * Primarily used in the selection subject
 *
 * @author Jeremy Jao
 */
public class RouteSelection {

    public static RouteSelection create(Route route, Set<String> selectedRoutes) {
        return new RouteSelection(route, selectedRoutes);
    }

    private Route toggledRoute;

    private Set<String> selectedRoutes;

    private RouteSelection(Route route, Set<String> selectedRoutes) {
        toggledRoute = route;
        this.selectedRoutes = selectedRoutes;
        Timber.d("getting both routes: %s, %s", route.getRoute(), selectedRoutes.toString());
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
