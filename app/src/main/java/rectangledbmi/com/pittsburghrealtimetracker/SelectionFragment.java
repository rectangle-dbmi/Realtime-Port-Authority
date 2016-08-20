package rectangledbmi.com.pittsburghrealtimetracker;

/**
 * Fragment that houses the selection.
 *
 * Created by epicstar on 3/15/16.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

import java.io.File;
import java.util.Set;

import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.PATAPI;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rx.Observable;

public abstract class SelectionFragment extends Fragment implements NavigationDrawerFragment.BusListCallbacks, ClearSelection {

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
         * @return The PAT API instantiated in {@link SelectTransit#onCreate(Bundle)}
         */
        PATAPI getPatApiClient();

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


        // TODO: add documentation
        Observable<Set<String>> getSelectedRoutesObservable();

        Observable<Route> getToggledRouteObservable();

        // TODO: change bad name... add documentation
        void onBadName();
    }
}
