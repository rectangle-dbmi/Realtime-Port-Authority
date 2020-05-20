package rectangledbmi.com.pittsburghrealtimetracker.ui.selection

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import rectangledbmi.com.pittsburghrealtimetracker.PATTrackApplication.Companion.getRefWatcher
import rectangledbmi.com.pittsburghrealtimetracker.R
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection.Companion.create
import rectangledbmi.com.pittsburghrealtimetracker.ui.selection.NavigationDrawerFragment.BusRouteAdapter.BusRouteHolder
import timber.log.Timber
import java.util.*

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the [
 * design guidelines](https://developer.android.com/design/patterns/navigation-drawer.html#Interaction) for a complete explanation of the behaviors implemented here.
 */
class NavigationDrawerFragment : Fragment() {
    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    var actionBarDrawerToggle: ActionBarDrawerToggle? = null
        private set
    private var mDrawerLayout: DrawerLayout? = null
    private var mFragmentContainerView: View? = null
    private var busListAdapter: BusRouteAdapter? = null
    private var mFromSavedInstanceState = false
    private var mUserLearnedDrawer = false
    private var routeSelectionPublishSubject: PublishSubject<RouteSelection>? = null

    /**
     * State of selected routes
     *
     *
     * public because we want to clear this list...
     */
    internal var selectedRoutes: MutableSet<String?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false)
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true
        }
        selectedRoutes = Collections.synchronizedSet(HashSet(sp.getStringSet(BUS_SELECT_STATE,
                Collections.synchronizedSet(
                        HashSet(resources.getInteger(R.integer.max_checked))))!!))
        busListAdapter = BusRouteAdapter()
        routeSelectionPublishSubject = PublishSubject.create()
    }

    fun reselectRoutes() {
        setSelection(selectedRoutes, true)
    }

    private fun setSelection(selectedRoutes: Set<String?>?, isSelected: Boolean) {
        val routesArray = selectedRoutes!!.toTypedArray()
        if (routeSelectionPublishSubject == null || busListAdapter == null) {
            Timber.w("Cannot multiselect things... Will not work")
            return
        }
        for (routeNumber in routesArray) {
            val route = busListAdapter!!.getRouteByNumber(routeNumber)
            if (route!!.isSelected != isSelected) {
                toggleRoute(route, isSelected)
            }
        }
        busListAdapter!!.notifyDataSetChanged()
        Timber.d("Finished multiselecting routes")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val v = inflater.inflate(R.layout.navigation_drawer_layout, container, false)
        val busListRecyclerView = v.findViewById<View>(R.id.bus_list_recyclerview) as RecyclerView
        busListRecyclerView.layoutManager = LinearLayoutManager(activity)
        busListAdapter!!.setHasStableIds(true)
        busListRecyclerView.setHasFixedSize(true)
        busListRecyclerView.adapter = busListAdapter
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (activity != null) {
            val refWatcher = getRefWatcher(activity!!)
            refWatcher!!.watch(this)
        }
        super.onDestroy()
    }

    val isDrawerOpen: Boolean
        get() = mDrawerLayout != null && mDrawerLayout!!.isDrawerOpen(mFragmentContainerView!!)

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    fun setUp(fragmentId: Int, drawerLayout: DrawerLayout?) {
        mFragmentContainerView = activity!!.findViewById(fragmentId)
        mDrawerLayout = drawerLayout

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)


        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        actionBarDrawerToggle = object : ActionBarDrawerToggle(
                activity,  /* host Activity */
                mDrawerLayout,  /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close /* "close drawer" description for accessibility */
        ) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if (!isAdded) {
                    return
                }
                activity!!.invalidateOptionsMenu() // calls onPrepareOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                if (!isAdded) {
                    return
                }
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true
                    val sp = PreferenceManager
                            .getDefaultSharedPreferences(activity)
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply()
                }
                activity!!.invalidateOptionsMenu() // calls onPrepareOptionsMenu()
            }
        }

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout!!.openDrawer(mFragmentContainerView as View)
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout!!.post { (actionBarDrawerToggle as ActionBarDrawerToggle).syncState() }
        mDrawerLayout!!.addDrawerListener(actionBarDrawerToggle as ActionBarDrawerToggle)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    fun closeDrawer(): Boolean {
        if (isDrawerOpen) {
            mDrawerLayout!!.closeDrawer(mFragmentContainerView!!)
            return true
        }
        return false
    }

    fun openDrawer(): Boolean {
        if (!isDrawerOpen) {
            mDrawerLayout!!.openDrawer(mFragmentContainerView!!)
            return true
        }
        return false
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Forward the new configuration the drawer toggle component.
        actionBarDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    /**
     * If the Clear Buses button is clicked, clears the selection and the selected buses
     */
    fun clearSelection() {
        val selectedRoutes: Set<String?> = HashSet(getSelectedRoutes()!!)
        setSelection(selectedRoutes, false)
        busListAdapter!!.clearSelection()
        Toast.makeText(activity, getString(R.string.cleared), Toast.LENGTH_SHORT).show()
    }

    /**
     * Place to save preferences....
     */
    private fun savePreferences() {
        if (activity != null && busListAdapter != null) {
            val sp = PreferenceManager.getDefaultSharedPreferences(activity)
            val spe = sp.edit()
            spe.putStringSet(BUS_SELECT_STATE, busListAdapter!!.getSelectedRoutes())
            spe.apply()
        }
    }

    override fun onPause() {
        savePreferences()
        super.onPause()
    }

    /**
     * @param rt - the route number
     * @return the route information by rt
     * @since 43
     */
    fun getSelectedRoute(rt: String?): Route? {
        return if (busListAdapter != null) busListAdapter!!.routeMap!![rt] else null
    }

    private fun getSelectedRoutes(): Set<String?>? {
        return if (busListAdapter != null) busListAdapter!!.getSelectedRoutes() else null
    }

    private fun toggleRoute(route: Route?, isSelected: Boolean = route!!.toggleSelection()) {
        route!!.isSelected = isSelected
        if (isSelected) {
            selectedRoutes!!.add(route.route)
        } else {
            selectedRoutes!!.remove(route.route)
        }
        routeSelectionPublishSubject!!.onNext(create(Route(route), selectedRoutes))
    }

    private inner class BusRouteAdapter internal constructor() : RecyclerView.Adapter<BusRouteHolder>() {
        /**
         * Routes dataset
         */
        private var routes: Array<Route?>? = null

        /**
         * Map of routes by hashmap
         */
        var routeMap: HashMap<String?, Route>? = null
            private set

        /**
         * Creates an array of routes for the recycler view and a reverse mapping
         *
         *
         * TODO: This is rather slow and takes up some time according to the Android Studio debugger
         *
         * @return the routes to be made for the recycler view
         */
        private fun createRoutes(): Array<Route?> {
            val numbers: Array<String> = resources.getStringArray(R.array.buses)
            val descriptions: Array<String> = resources.getStringArray(R.array.bus_description)
            val colors: Array<String> = resources.getStringArray(R.array.buscolors)
            routes = arrayOfNulls(numbers.size)
            routeMap = HashMap(numbers.size)
            var currentRoute: Route
            for (i in numbers.indices) {
                currentRoute = Route(numbers[i], descriptions[i], colors[i], i, false)
                routes!![i] = currentRoute
                routeMap!![currentRoute.route] = currentRoute
                if (currentRoute.isSelected) {
                    Timber.w("Should not be true: %s", currentRoute.route)
                }
            }
            return routes!!
        }

        fun getRouteByNumber(routeNumber: String?): Route? {
            if (routeMap == null) {
                Timber.i("Route hashmap not yet created...")
            }
            return routeMap!![routeNumber]
        }

        /**
         * @return a set of currently selected routes from the recycler view
         */
        fun getSelectedRoutes(): Set<String?>? {
            return selectedRoutes
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusRouteHolder {
            val row = LayoutInflater.from(parent.context)
                    .inflate(R.layout.bus_route_recycler_item, parent, false)
            return BusRouteHolder(row)
        }

        override fun onBindViewHolder(holder: BusRouteHolder, position: Int) {
            holder.bindBusRoute(routes!![position])
        }

        override fun getItemCount(): Int {
            return routes!!.size
        }

        fun clearSelection() {
            for (s in selectedRoutes!!) {
                routeMap!![s]!!.isSelected = false
            }
            selectedRoutes!!.clear()
            notifyDataSetChanged()
        }

        override fun getItemId(position: Int): Long {
            return routes!![position]!!.route.hashCode().toLong()
        }

        inner class BusRouteHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            private var mRoute: Route? = null
            private val routeIcon: TextView
            private val routeDescription: TextView
            override fun onClick(v: View) {
                if (mRoute == null) return
                if (!mRoute!!.isSelected &&
                        selectedRoutes!!.size == resources.getInteger(R.integer.max_checked)) {
                    Toast.makeText(activity, getString(R.string.max_selected_routes), Toast.LENGTH_SHORT).show()
                    return
                }
                toggleRoute(mRoute)
                notifyItemChanged(adapterPosition)
            }

            private fun generateIcon() {
                routeIcon.setBackgroundColor(mRoute!!.routeColor)
                routeIcon.text = mRoute!!.route
                routeIcon.setTextColor(if (isLight(mRoute!!.routeColor)) Color.BLACK else Color.WHITE)
            }

            fun bindBusRoute(busRoute: Route?) {
                mRoute = busRoute
                routeDescription.text = busRoute!!.routeInfo
                generateIcon()
                itemView.isActivated = mRoute!!.isSelected
            }

            /**
             * Decides whether or not the color (background color) is light or not.
             *
             *
             * Formula was taken from here:
             * http://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
             *
             * @param color the background color being fed
             * @return whether or not the background color is light or not (.345 is the current threshold)
             * @since 47
             */
            private fun isLight(color: Int): Boolean {
                return 1.0 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 < .5
            }

            init {
                itemView.setOnClickListener(this)
                routeDescription = itemView.findViewById<View>(R.id.bus_route_text) as TextView
                routeIcon = itemView.findViewById<View>(R.id.bus_route_icon) as TextView
            }
        }

        init {
            createRoutes()
        }
    }

    val selectedRoutesObservable: Flowable<Set<String?>?>?
        get() = if (routeSelectionPublishSubject == null) {
            null
        } else routeSelectionPublishSubject!!
                .toFlowable(BackpressureStrategy.BUFFER)
                .map(RouteSelection::selectedRoutes)

    val toggledRouteObservable: Flowable<Route>?
        get() = if (routeSelectionPublishSubject == null) {
            null
        } else routeSelectionPublishSubject!!
                .toFlowable(BackpressureStrategy.BUFFER)
                .map(RouteSelection::toggledRoute)

    companion object {
        /**
         * Per the design guidelines, you should show the drawer on launch until the user manually
         * expands it. This shared preference tracks this.
         */
        private const val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"

        /**
         * Saved instance of the buses that are selected
         */
        private const val BUS_SELECT_STATE = "busesSelected"
    }
}