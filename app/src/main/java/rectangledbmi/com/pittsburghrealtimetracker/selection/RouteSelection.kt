package rectangledbmi.com.pittsburghrealtimetracker.selection

import timber.log.Timber

/**
 * An object that contains the selected route and/or all selected routes.
 *
 * Primarily used in the selection subject
 *
 * @author Jeremy Jao
 */
data class RouteSelection(val toggledRoute: Route, val selectedRoutes: Set<String?>?) {

    init {
        Timber.d("getting routes: %s, %s", toggledRoute.transitRoute.number, selectedRoutes.toString())
    }
}
