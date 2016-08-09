package rectangledbmi.com.pittsburghrealtimetracker.selection;

import android.support.annotation.NonNull;

import java.util.Set;

import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import timber.log.Timber;

/**
 * An object that contains the selected route and/or all selected routes.
 *
 * Primarily used in the selection subject
 *
 * @author Jeremy Jao
 */
public class RouteSelection {

    public static RouteSelection create(Route route) {
        return new RouteSelection(route);
    }

    public static RouteSelection create(Set<String> selectedRoutes) {
        return new RouteSelection(selectedRoutes);
    }

    public static RouteSelection create(Route route, Set<String> selectedRoutes) {
        return new RouteSelection(route, selectedRoutes);
    }

    private Route toggledRoute;

    private Set<String> selectedRoutes;

    private RouteSelection(@NonNull Route route) {
        toggledRoute = route;
        Timber.d("Only initiating route for selection: %s", route.getRoute());
    }

    private RouteSelection(@NonNull Set<String> selectedRoutes) {
        this.selectedRoutes = selectedRoutes;
        Timber.d("Only initiating route for selection: %s", selectedRoutes.toString());
    }

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
