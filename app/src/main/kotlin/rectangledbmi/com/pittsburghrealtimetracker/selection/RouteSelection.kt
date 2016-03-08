package rectangledbmi.com.pittsburghrealtimetracker.selection

import rectangledbmi.com.pittsburghrealtimetracker.world.Route

/**
 * This is the route selection object that shows the
 * current selected route and the all routes selected
 * @author Jeremy Jao
 */
class RouteSelection(selectedRoutes: Set<String>?, toggledRoute: Route?) {
    var selectedRoutes: Set<String>? = selectedRoutes
    var toggledRoute: Route? = toggledRoute
}
