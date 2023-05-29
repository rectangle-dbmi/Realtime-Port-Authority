package rectangledbmi.com.pittsburghrealtimetracker.selection

import android.graphics.Color
import com.rectanglel.patstatic.routes.BusRoute

/**
 * This is the object container that contains all information of the buses
 *
 * Created by epicstar on 9/5/14.
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 */
data class Route(
    val transitRoute: BusRoute,
    private val listPosition: Int,
    var isSelected: Boolean
) {
    /**
     * Gets the int color as a hex string from:
     * http://stackoverflow.com/questions/4506708/android-convert-color-int-to-hexa-string
     *
     * @return color as hex-string
     */
    val colorAsString: String
        get() {
            return transitRoute.color
        }

    val color: Int by lazy {
        Color.parseColor(transitRoute.color)
    }

    /**
     * Copy constructor for Route objects
     * @param route
     */
    constructor(route: Route) : this(route.transitRoute, route.listPosition, route.isSelected)

    /**
     *
     * @return true if state is changed to selected
     * @since 58
     */
    private fun selectRoute(): Boolean {
        if (!isSelected) {
            isSelected = true
            return true
        }
        return false
    }

    /**
     *
     * @return true if state is changed to deselected
     * @since 58
     */
    private fun deselectRoute(): Boolean {
        if (isSelected) {
            isSelected = false
            return true
        }
        return false
    }

    /**
     * Toggles the selection to change the state of the route's selection.
     *
     * @return true if the route becomes selected; false if it becomes unselected
     * @since 58
     */
    fun toggleSelection(): Boolean {
        return if (isSelected) {
            deselectRoute()
            false
        } else {
            selectRoute()
            true
        }
    }
}
