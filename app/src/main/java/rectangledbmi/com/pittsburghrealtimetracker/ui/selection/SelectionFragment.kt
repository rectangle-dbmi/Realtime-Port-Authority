package rectangledbmi.com.pittsburghrealtimetracker.ui.selection

/**
 * Fragment that houses the selection.
 *
 * Created by epicstar on 3/15/16.
 */

import android.content.DialogInterface
import androidx.fragment.app.Fragment
import android.view.View

import java.io.File

import com.rectanglel.patstatic.model.PatApiService

import io.reactivex.Flowable
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route

abstract class SelectionFragment : Fragment(), ClearSelection {

    /**
     * Interface that interacts with the list of buses in [NavigationDrawerFragment]
     * @author Jeremy Jao
     */
    interface BusSelectionInteraction {

        /**
         * @return the selected routes from the [NavigationDrawerFragment]
         */
        val selectedRoutes: Set<String?>?

        /**
         *
         * @return the service class that gets Port Authority objects
         * @since 78
         */
        val patApiService: PatApiService?

        /**
         * Gets Android's default data directory... Eventually will want the API that accepts an external
         * SD card as this
         * @return the Android Data Directory
         */
        val datadirectory: File


        /**
         *
         * @return the current snapshot of currently selected items
         */
        val selectedRoutesObservable: Flowable<Set<String?>?>?

        /**
         *
         * @return the currently toggled route
         */
        val toggledRouteObservable: Flowable<Route>?

        /**
         * @param routeNumber the route number
         * @return the selected route from the [NavigationDrawerFragment]
         */
        fun getSelectedRoute(routeNumber: String): Route?

        /**
         * Shows a toast message
         *
         * @param message the message
         * @param length  the length of the message
         */
        fun showToast(message: String, length: Int)

        fun showOkDialog(message: String, okListener: DialogInterface.OnClickListener)

        fun makeSnackbar(message: String, length: Int, action: String, listener: View.OnClickListener)

        /**
         * Opens the permissions page
         */
        fun openPermissionsPage()

        /**
         * Calls on the [NavigationDrawerFragment] from another componnt to restore selection
         */
        fun restoreSelection()
    }
}
