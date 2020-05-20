package rectangledbmi.com.pittsburghrealtimetracker.selection

import timber.log.Timber

/**
 * An object that contains the selected route and/or all selected routes.
 *
 * Primarily used in the selection subject
 *
 * @author Jeremy Jao
 */
class RouteSelection private constructor(val toggledRoute: Route, val selectedRoutes: Set<String?>?) {

    init {
        Timber.d("getting both routes: %s, %s", toggledRoute.route, selectedRoutes.toString())
    }

    companion object {

        @JvmStatic
        fun create(route: Route, selectedRoutes: Set<String?>?): RouteSelection {
            return RouteSelection(route, selectedRoutes)
        }
    }
}
