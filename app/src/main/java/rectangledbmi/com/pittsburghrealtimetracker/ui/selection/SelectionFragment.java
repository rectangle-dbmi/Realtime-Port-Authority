package rectangledbmi.com.pittsburghrealtimetracker.ui.selection;

/**
 * Fragment that houses the selection.
 *
 * Created by epicstar on 3/15/16.
 */

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

import java.io.File;
import java.util.Set;

import com.rectanglel.patstatic.model.PatApiService;

import io.reactivex.Flowable;
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route;

public abstract class SelectionFragment extends Fragment implements ClearSelection {

    /**
     * Interface that interacts with the list of buses in {@link NavigationDrawerFragment}
     * @author Jeremy Jao
     */
    public interface BusSelectionInteraction {

        /**
         * @param routeNumber the route number
         * @return the selected route from the {@link NavigationDrawerFragment}
         */
        Route getSelectedRoute(String routeNumber);

        /**
         * @return the selected routes from the {@link NavigationDrawerFragment}
         */
        Set<String> getSelectedRoutes();

        /**
         *
         * @return the service class that gets Port Authority objects
         * @since 78
         */
        PatApiService getPatApiService();

        /**
         * Shows a toast message
         *
         * @param message the message
         * @param length  the length of the message
         */
        void showToast(String message, int length);

        void showOkDialog(String message, DialogInterface.OnClickListener okListener);

        void makeSnackbar(@NonNull String message, int length, @NonNull String action, @NonNull View.OnClickListener listener);

        /**
         * Opens the permissions page
         */
        void openPermissionsPage();

        /**
         * Gets Android's default data directory... Eventually will want the API that accepts an external
         * SD card as this
         * @return the Android Data Directory
         */
        File getDatadirectory();


        /**
         *
         * @return the current snapshot of currently selected items
         */
        Flowable<Set<String>> getSelectedRoutesObservable();

        /**
         *
         * @return the currently toggled route
         */
        Flowable<Route> getToggledRouteObservable();

        /**
         * Calls on the {@link NavigationDrawerFragment} from another componnt to restore selection
         */
        void restoreSelection();
    }
}
