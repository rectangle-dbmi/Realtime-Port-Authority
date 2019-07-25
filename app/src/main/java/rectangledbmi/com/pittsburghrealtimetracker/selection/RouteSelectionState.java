package rectangledbmi.com.pittsburghrealtimetracker.selection;

import java.util.Set;

import timber.log.Timber;

/**
 * An object that contains the selected route and/or all selected items.
 *
 * Primarily used in the selection subject
 *
 * @author Jeremy Jao
 */
public class RouteSelectionState {

    public static RouteSelectionState create(Route route, Set<String> selectedRoutes) {
        return new RouteSelectionState(route, selectedRoutes);
    }

    private Route toggledRoute;

    private Set<String> selectedRoutes;

    public RouteSelectionState(Route route, Set<String> selectedRoutes) {
        toggledRoute = route;
        this.selectedRoutes = selectedRoutes;
        Timber.d("getting both items: %s, %s", route.getRoute(), selectedRoutes.toString());
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
