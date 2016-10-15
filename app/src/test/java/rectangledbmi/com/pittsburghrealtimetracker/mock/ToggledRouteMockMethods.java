package rectangledbmi.com.pittsburghrealtimetracker.mock;

import rectangledbmi.com.pittsburghrealtimetracker.selection.Route;

/**
 * <p>Stub route selection for unit testing.</p>
 * <p>Created by epicstar on 7/18/16.</p>
 * @since 78
 * @author Jeremy Jao
 */
public class ToggledRouteMockMethods {
    /**
     * Gets a stub route selection. The route number is from {@link PatApiMock#testRoute1} and
     * it is a selected route.
     *
     * @return a stub route selection
     */
    public static Route getSelectedRouteSelection() {
        return new Route(PatApiMock.testRoute1, "", 1, 1, true);
    }

    /**
     * Gets a stub route selection. The route number is from {@link PatApiMock#testRoute1} and
     * it is an unselected route.
     *
     * @return a stub route selection
     */
    public static Route getUnselectedRouteSelection() {
        return new Route(PatApiMock.testRoute1, "", 1, 1, false);
    }

}