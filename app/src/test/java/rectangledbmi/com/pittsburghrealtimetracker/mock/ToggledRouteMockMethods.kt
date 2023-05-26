package rectangledbmi.com.pittsburghrealtimetracker.mock

import rectangledbmi.com.pittsburghrealtimetracker.selection.Route

object ToggledRouteMockMethods {
    /**
     * Gets a stub route selection. The route number is from [PatApiMock.testRoute1] and
     * it is a selected route.
     *
     * @return a stub route selection
     */
    @JvmStatic
    fun getSelectedRouteSelection(): Route {
        return Route(PatApiMock.testRoute1, 1, true)
    }

    /**
     * Gets a stub route selection. The route number is from [PatApiMock.testRoute1] and
     * it is an unselected route.
     *
     * @return a stub route selection
     */
    @JvmStatic
    fun getUnselectedRouteSelection(): Route {
        return Route(PatApiMock.testRoute1, 1, false)
    }
}