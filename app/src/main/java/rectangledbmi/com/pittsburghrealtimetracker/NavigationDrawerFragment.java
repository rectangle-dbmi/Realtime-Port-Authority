package rectangledbmi.com.pittsburghrealtimetracker;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import rectangledbmi.com.pittsburghrealtimetracker.handlers.Constants;
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
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
     * pointer to the current selected bus for the current callbacks instance (Activity)
     *
     * @since 43
     */
    private BusListCallbacks busCallbacks;

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

    private CompositeSubscription recyclerviewSubscriptions;
    private PublishSubject<RouteSelection> routeSelectionPublishSubject;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        Constants.DEFAULT_DATE_PARSE_FORMAT.setTimeZone(TimeZone.getTimeZone("EST"));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
        }
        RouteSelection routeSelection = new RouteSelection(null, null);
        routeSelectionPublishSubject = PublishSubject.create();
        // Select either the default item (0) or the last selected item.
//        selectItem(mCurrentSelectedPosition);

    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.navigation_drawer_layout, container, false);
        recyclerviewSubscriptions = new CompositeSubscription();
        busListAdapter = new BusRouteAdapter();
        RecyclerView busListRecyclerView = (RecyclerView) v.findViewById(R.id.bus_list_recyclerview);
        busListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        busListAdapter.setHasStableIds(true);
        busListRecyclerView.setHasFixedSize(true);
        busListRecyclerView.setAdapter(busListAdapter);
        return v;

    }

    @Override
    public void onDestroyView() {
        recyclerviewSubscriptions.unsubscribe();
        super.onDestroyView();
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
        // set up the drawer's list view with items and click listener

//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setHomeButtonEnabled(true);


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
                ((SelectTransit)getActivity()).restoreBuses();
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

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            busCallbacks = (BusListCallbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    public boolean closeDrawer() {
        if(isDrawerOpen()) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
            return true;
        }
        return false;
    }

    public boolean openDrawer() {
        if(!isDrawerOpen()) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
            return true;
        }
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        busCallbacks = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.action_clear) {
            clearMapAndSelection();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void clearMapAndSelection() {
        Timber.d("cleared_everything");
        SelectTransit activity = (SelectTransit)getActivity();
        if(activity!= null) {
            activity.clearMap();
        }
        clearSelection();

    }

    /**
     * If the Clear Buses button is clicked, clears the selection and the selected buses
     */
    protected void clearSelection() {
        File lineInfo = new File(getActivity().getFilesDir(), "/lineinfo");
        Timber.d("cleared files: %s", lineInfo.getAbsolutePath());
        if(lineInfo.exists()) {
            File[] files = lineInfo.listFiles();
            if(files != null) {
                for(File file : files)
                    file.delete();
            }
        }
        busListAdapter.clearSelection();
        Toast.makeText(getActivity(), getString(R.string.cleared), Toast.LENGTH_SHORT).show();

    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((SelectTransit)getActivity()).getSupportActionBar();
    }

    /**
     * Place to save preferences....
     */
    private void savePreferences() {
        if(getActivity() != null && busListAdapter != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor spe = sp.edit();
            spe.putStringSet(BUS_SELECT_STATE, busListAdapter.getSelectedRoutes());
//        sp.edit().putInt(BUSLIST_SIZE, getResources().getStringArray(R.array.buses).length).apply();
            spe.apply();
        }

    }

    @Override
    public void onPause() {
        savePreferences();
        super.onPause();
    }
    /**
     * Attempt to save the list view...
     */
    @Override
    public void onStop() {
//        Timber.d("saving_sbchecked", mDrawerListView.getCheckedItemPositions().toString());
        super.onStop();


    }

    /**
     * @since 43
     * @param rt - the route number
     * @return the route information by rt
     */
    public Route getSelectedRoute(String rt) {
        if(busListAdapter != null)
            return busListAdapter.getRouteMap().get(rt);
        return null;
    }

    public int getAmountSelected() {
        return busListAdapter.getAmountSelected();
    }

    public Set<String> getSelectedRoutes() {
        if(busListAdapter != null)
            return busListAdapter.getSelectedRoutes();
        return null;
    }

    /**
     * This takes the bus route information to the main activity {@link SelectTransit}.
     *
     * @since 43
     * @author Jeremy Jao
     */
    public interface BusListCallbacks {

        /**
         * This bus route has been selected
         *
         * @param route the bus route selected
         */
        void onSelectBusRoute(Route route);

        void clearSelection();

        /**
         * Do when the bus route has been deselected
         *
         * @param route the bus route deselected
         */
        void onDeselectBusRoute(Route route);
        //TODO: Fix this to use observables or designate callback function for selection or deselection
    }

    private class BusRouteAdapter extends RecyclerView.Adapter<BusRouteAdapter.BusRouteHolder> {

        /**
         * State of selected routes
         * <p/>
         * public because we want to clear this list...
         */
        private Set<String> selectedRoutes;

        /**
         * Routes dataset
         */
        private Route[] routes;

        /**
         * Map of routes by hashmap
         */
        private HashMap<String, Route> routeHashMap;

        public BusRouteAdapter() {
            super();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

            selectedRoutes = new HashSet<>(sp.getStringSet(BUS_SELECT_STATE,
                    Collections.synchronizedSet(
                            new HashSet<>(getResources().getInteger(R.integer.max_checked)))));
            createRoutes();
        }

        /**
         * Creates an array of routes for the recycler view and a reverse mapping
         *
         * TODO: This is rather slow and takes up some time according to the Android Studio debugger
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

            for(int i=0;i<numbers.length;++i) {
                currentRoute = new Route(numbers[i], descriptions[i], colors[i], i, selectedRoutes.contains(numbers[i]));
                routes[i] = currentRoute;
                routeHashMap.put(currentRoute.getRoute(), currentRoute);

            }
            return routes;
        }

        /**
         *
         * @return a set of currently selected routes from the recycler view
         */
        public Set<String> getSelectedRoutes() {
            return selectedRoutes;
        }

        /**
         *
         * @param position The route on the recycler view to peek at
         * @return the route at the certain position of the recycler view
         */
        public Route getRouteAtPosition(int position) {
            if(position >= 0 && position < routes.length) {
                return routes[position];
            }
            return null;
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
            for(String s : selectedRoutes) {
                routeHashMap.get(s).setSelected(false);
            }
            selectedRoutes.clear();
            notifyDataSetChanged();
        }

        public int getAmountSelected() {
            return selectedRoutes.size();
        }

        public long getItemId(int position) {
            return routes[position].getRoute().hashCode();
        }

        public class BusRouteHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

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
                boolean selected = mRoute.toggleSelection();
                if (selected) {
                    selectedRoutes.add(mRoute.getRoute());
                } else {
                    selectedRoutes.remove(mRoute.getRoute());
                }
                routeSelectionPublishSubject.onNext(new RouteSelection(mRoute, selectedRoutes));
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

    public Observable<RouteSelection> getListSelectionSubject() {
        return routeSelectionPublishSubject.share();
    }

}
