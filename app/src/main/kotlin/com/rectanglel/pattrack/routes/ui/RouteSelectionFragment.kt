package com.rectanglel.pattrack.routes.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rectanglel.pattrack.routes.RoutesViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.navigation_drawer_layout.view.*
import rectangledbmi.com.pittsburghrealtimetracker.PATTrackApplication
import rectangledbmi.com.pittsburghrealtimetracker.R
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelectionState
import rectangledbmi.com.pittsburghrealtimetracker.ui.MainActivity
import timber.log.Timber
import java.util.*

/**
 * This is the navigation drawer fragment that contains
 * the list of transit routes to select. This is instantiated by [MainActivity]
 */
class RouteSelectionFragment : Fragment() {

    private lateinit var routeSelectionViewModel: RoutesViewModel
    private val routeSelectionPublishSubject: PublishSubject<RouteSelectionState> = PublishSubject.create()

    // region terrible Android API properties that I apparently need
    private lateinit var drawerLayout : DrawerLayout // do we need this? we'll find out in the next episode of dbz
    private lateinit var fragmentContainerView : View
    public lateinit var drawerToggle: ActionBarDrawerToggle
        private set
    // endregion

    val toggledRouteObservable = routeSelectionPublishSubject
            .toFlowable(BackpressureStrategy.BUFFER)
            .map { it.toggledRoute }

    val selectedRoutesObservable = routeSelectionPublishSubject
            .toFlowable(BackpressureStrategy.BUFFER)
            .map { it.selectedRoutes }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainActivity = activity as? MainActivity
        mainActivity?.let {
            routeSelectionViewModel = ViewModelProviders
                    .of(
                            it,
                            it.routesViewModelFactory
                    ).get(RoutesViewModel::class.java)

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val v = inflater.inflate(R.layout.navigation_drawer_layout, container, false)
        v.bus_list_recyclerview?.layoutManager = LinearLayoutManager(activity)
        v.bus_list_recyclerview?.setHasFixedSize(true)

        // there's still some spaghetti code here....
        val routeSelectionClickListener = { adapter: RouteAdapter, viewHolder: RouteViewHolder, route: Route ->
            // until I refactor selelectionViewModel.toggleRoute(route) to use liveData,
            // we'll be using this stupid logic here
            val newRoute = routeSelectionViewModel.toggleRoute(route)
            if (newRoute == route) {
                Timber.i("Number of routes is equal to 10, so nothing will happen")
            } else {
                viewHolder.route = newRoute
                adapter.notifyItemChanged(viewHolder.adapterPosition)
                routeSelectionPublishSubject.onNext(RouteSelectionState(newRoute, routeSelectionViewModel.currentSelectedRoutes))
                if (routeSelectionViewModel.numberOfSelectedRoutes == 0) {
                    Timber.i("No routes selected.")
                }
            }
        }
        val adapter = RouteAdapter(routeSelectionClickListener)
        v.bus_list_recyclerview?.adapter = adapter
        val routesListLiveData = routeSelectionViewModel.routesLiveData
        routesListLiveData.observe(viewLifecycleOwner, Observer {
            adapter.items = it ?: emptyList()
        })

        return v
    }


    override fun onDestroy() {
        if (activity != null) {
            val refWatcher = PATTrackApplication.getRefWatcher(activity!!)
            refWatcher.watch(this)
        }

        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    // lol why the fk do I need this method... terrible
    public fun setUp(fragmentId: Int, drawerLayout: DrawerLayout) {
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)

        // LMAO fk everything
        this.fragmentContainerView = activity!!.findViewById(fragmentId)
        this.drawerLayout = drawerLayout
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)

        drawerToggle = object : ActionBarDrawerToggle(
                activity,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if (!isAdded) {
                    return
                }
                activity?.invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                if (!isAdded) {
                    return
                }
                activity?.invalidateOptionsMenu()
            }
        }

    }

    public fun isDrawerOpen() : Boolean = drawerLayout.isDrawerOpen(fragmentContainerView)

    public fun openDrawer() : Boolean {
        if (!isDrawerOpen()) {
            drawerLayout.openDrawer(fragmentContainerView)
            return true
        }
        return false
    }

    public fun closeDrawer() : Boolean {
        if (isDrawerOpen()) {
            drawerLayout.closeDrawer(fragmentContainerView)
            return true
        }
        return false
    }

    public fun clearSelection() {

    }
    public fun getSelectedRoute(routeNumber: String) : Route? = null

    public fun getSelectedRoutes() : Set<String>? = Collections.emptySet()

    public fun reselectRoutes() {
        // LOL
    }

    @Deprecated("Kotlin is amazing, so I don't know why I had this originally",
                ReplaceWith("drawerToggle"))
    public fun getActionBarDrawerToggle() : ActionBarDrawerToggle {
        return drawerToggle
    }

}