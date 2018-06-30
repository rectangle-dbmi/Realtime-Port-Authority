package rectangledbmi.com.pittsburghrealtimetracker.ui.selection;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.leakcanary.RefWatcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import rectangledbmi.com.pittsburghrealtimetracker.PATTrackApplication;
import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection;
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route;
import timber.log.Timber;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * Saved instance of the buses that are selected
     */
    private final static String BUS_SELECT_STATE = "busesSelected";

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private BusRouteAdapter busListAdapter;

    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private PublishSubject<RouteSelection> routeSelectionPublishSubject;

    /**
     * State of selected routes
     * <p>
     * public because we want to clear this list...
     */
    private Set<String> selectedRoutes;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
        }
        selectedRoutes = Collections.synchronizedSet(new HashSet<>(sp.getStringSet(BUS_SELECT_STATE,
                Collections.synchronizedSet(
                        new HashSet<>(getResources().getInteger(R.integer.max_checked))))));
        busListAdapter = new BusRouteAdapter();
        routeSelectionPublishSubject = PublishSubject.create();
    }

    public void reselectRoutes() {
        setSelection(selectedRoutes, true);
    }

    private void setSelection(Set<String> selectedRoutes, boolean isSelected) {
        String[] routesArray = selectedRoutes.toArray(new String[selectedRoutes.size()]);
        if (routeSelectionPublishSubject == null || busListAdapter == null) {
            Timber.w("Cannot multiselect things... Will not work");
            return;
        }
        for (String routeNumber : routesArray) {
            Route route = busListAdapter.getRouteByNumber(routeNumber);
            if (route.isSelected() != isSelected) {
                toggleRoute(route, isSelected);
            }
        }
        busListAdapter.notifyDataSetChanged();
        Timber.d("Finished multiselecting routes");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.navigation_drawer_layout, container, false);
        RecyclerView busListRecyclerView = (RecyclerView) v.findViewById(R.id.bus_list_recyclerview);
        busListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        busListAdapter.setHasStableIds(true);
        busListRecyclerView.setHasFixedSize(true);
        busListRecyclerView.setAdapter(busListAdapter);
        return v;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (getActivity() != null) {
            RefWatcher refWatcher = PATTrackApplication.getRefWatcher(getActivity());
            refWatcher.watch(this);
        }

        super.onDestroy();
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);


        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(mDrawerToggle::syncState);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mDrawerToggle;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public boolean closeDrawer() {
        if (isDrawerOpen()) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
            return true;
        }
        return false;
    }

    public boolean openDrawer() {
        if (!isDrawerOpen()) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
            return true;
        }
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * If the Clear Buses button is clicked, clears the selection and the selected buses
     */
    public void clearSelection() {

        Set<String> selectedRoutes = new HashSet<>(getSelectedRoutes());
        setSelection(selectedRoutes, false);
        busListAdapter.clearSelection();
        Toast.makeText(getActivity(), getString(R.string.cleared), Toast.LENGTH_SHORT).show();
    }

    /**
     * Place to save preferences....
     */
    private void savePreferences() {
        if (getActivity() != null && busListAdapter != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor spe = sp.edit();
            spe.putStringSet(BUS_SELECT_STATE, busListAdapter.getSelectedRoutes());
            spe.apply();
        }

    }

    @Override
    public void onPause() {
        savePreferences();
        super.onPause();
    }

    /**
     * @param rt - the route number
     * @return the route information by rt
     * @since 43
     */
    public Route getSelectedRoute(String rt) {
        if (busListAdapter != null)
            return busListAdapter.getRouteMap().get(rt);
        return null;
    }

    public Set<String> getSelectedRoutes() {
        if (busListAdapter != null)
            return busListAdapter.getSelectedRoutes();
        return null;
    }

    private void toggleRoute(Route route) {
        toggleRoute(route, route.toggleSelection());
    }

    private void toggleRoute(Route route, boolean isSelected) {
        route.setSelected(isSelected);
        if (isSelected) {
            selectedRoutes.add(route.getRoute());
        } else {
            selectedRoutes.remove(route.getRoute());
        }
        routeSelectionPublishSubject.onNext(RouteSelection.create(new Route(route), selectedRoutes));
    }

    private class BusRouteAdapter extends RecyclerView.Adapter<BusRouteAdapter.BusRouteHolder> {

        /**
         * Routes dataset
         */
        private Route[] routes;

        /**
         * Map of routes by hashmap
         */
        private HashMap<String, Route> routeHashMap;

        BusRouteAdapter() {
            super();
            createRoutes();
        }

        /**
         * Creates an array of routes for the recycler view and a reverse mapping
         * <p>
         * TODO: This is rather slow and takes up some time according to the Android Studio debugger
         *
         * @return the routes to be made for the recycler view
         */
        private Route[] createRoutes() {
            String[] numbers, descriptions, colors;
            numbers = getResources().getStringArray(R.array.buses);
            descriptions = getResources().getStringArray(R.array.bus_description);
            colors = getResources().getStringArray(R.array.buscolors);
            routes = new Route[numbers.length];
            routeHashMap = new HashMap<>(numbers.length);
            Route currentRoute;

            for (int i = 0; i < numbers.length; ++i) {
                currentRoute = new Route(numbers[i], descriptions[i], colors[i], i, false);
                routes[i] = currentRoute;
                routeHashMap.put(currentRoute.getRoute(), currentRoute);
                if (currentRoute.isSelected()) {
                    Timber.w("Should not be true: %s", currentRoute.getRoute());
                }
            }
            return routes;
        }

        public Route getRouteByNumber(String routeNumber) {
            if (routeHashMap == null) {
                Timber.i("Route hashmap not yet created...");
            }
            return routeHashMap.get(routeNumber);
        }

        /**
         * @return a set of currently selected routes from the recycler view
         */
        public Set<String> getSelectedRoutes() {
            return selectedRoutes;
        }

        public HashMap<String, Route> getRouteMap() {
            return routeHashMap;
        }

        @Override
        public BusRouteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bus_route_recycler_item, parent, false);

            return new BusRouteHolder(row);
        }

        @Override
        public void onBindViewHolder(BusRouteHolder holder, int position) {
            holder.bindBusRoute(routes[position]);
        }

        @Override
        public int getItemCount() {
            return routes.length;
        }

        public void clearSelection() {
            for (String s : selectedRoutes) {
                routeHashMap.get(s).setSelected(false);
            }
            selectedRoutes.clear();
            notifyDataSetChanged();
        }

        public long getItemId(int position) {
            return routes[position].getRoute().hashCode();
        }

        public class BusRouteHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private Route mRoute;

            private TextView routeIcon;

            private TextView routeDescription;

            private View itemView;

            public BusRouteHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                itemView.setOnClickListener(this);
                routeDescription = (TextView) itemView.findViewById(R.id.bus_route_text);
                routeIcon = (TextView) itemView.findViewById(R.id.bus_route_icon);
            }

            public void onClick(View v) {
                if (mRoute == null) return;
                if (!mRoute.isSelected() &&
                        selectedRoutes.size() == getResources().getInteger(R.integer.max_checked)) {
                    Toast.makeText(getActivity(), getString(R.string.max_selected_routes), Toast.LENGTH_SHORT).show();
                    return;
                }
                toggleRoute(mRoute);
                notifyItemChanged(getAdapterPosition());
            }

            private void generateIcon() {
                routeIcon.setBackgroundColor(mRoute.getRouteColor());
                routeIcon.setText(mRoute.getRoute());
                routeIcon.setTextColor(isLight(mRoute.getRouteColor())
                        ? Color.BLACK
                        : Color.WHITE);

            }

            public void bindBusRoute(Route busRoute) {
                mRoute = busRoute;
                routeDescription.setText(busRoute.getRouteInfo());
                generateIcon();
                itemView.setActivated(mRoute.isSelected());
            }

            /**
             * Decides whether or not the color (background color) is light or not.
             * <p>
             * Formula was taken from here:
             * http://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
             *
             * @param color the background color being fed
             * @return whether or not the background color is light or not (.345 is the current threshold)
             * @since 47
             */
            private boolean isLight(int color) {
                return 1.0 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 < .5;
            }
        }

    }

    public Flowable<Set<String>> getSelectedRoutesObservable() {
        if (routeSelectionPublishSubject == null) {
            return null;
        }
        return routeSelectionPublishSubject
                .toFlowable(BackpressureStrategy.BUFFER)
                .map(RouteSelection::getSelectedRoutes);
    }

    public Flowable<Route> getToggledRouteObservable() {
        if (routeSelectionPublishSubject == null) {
            return null;
        }
        return routeSelectionPublishSubject
                .toFlowable(BackpressureStrategy.BUFFER)
                .map(RouteSelection::getToggledRoute);
    }

}
