package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import java.util.Set;

import rectangledbmi.com.pittsburghrealtimetracker.world.Route;

/**
 * This is an interface for common methods to react to the
 * {@link rectangledbmi.com.pittsburghrealtimetracker.NavigationDrawerFragment}
 *
 * @author Jeremy Jao
 * @since 64
 */
public interface RouteSelectedInterface {

    /**
     * The route is selected. React in a way that the route is selected from the selection
     * @param route the route that was selected
     */
    void routeIsSelected(Route route);

    /**
     * The route is unselected. React in a way that the route is unselected from the selection
     * @param route the route that was unselected
     */
    void routeIsUnselected(Route route);

    /**
     * Gets the routes as a {@link Set} of {@link String}
     * @return the routes selected from the drawer
     */
    Set<String> getSelectedRoutes();


}
