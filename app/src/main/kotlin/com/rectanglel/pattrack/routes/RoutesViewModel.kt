package com.rectanglel.pattrack.routes

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import com.rectanglel.patstatic.model.PatApiService
import com.rectanglel.patstatic.routes.BusRoute
import io.reactivex.Flowable
import io.reactivex.Observable
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route
import timber.log.Timber

/**
 * Gets items and
 */
public class RoutesViewModel(private val selectionStateRepository: RouteSelectionStateRepository,
                             patApiService: PatApiService) : ViewModel() {

    companion object {
        const val MAX_NUMBER_OF_SELECTED_ROUTES : Int = 10
    }

    val routes : Flowable<List<Route>> = patApiService.routes
            .toFlowable()
            .flatMapIterable { busRouteList -> busRouteList }
            .map { busRoute: BusRoute ->  Route(busRoute.routeNumber, busRoute.routeName, busRoute.routeColor, 0, selectionStateRepository.isSelected(busRoute.routeNumber)) }
            .toList()
            .toFlowable()
            .share()

    val routesLiveData : LiveData<List<Route>> = LiveDataReactiveStreams.fromPublisher {
        patApiService.routes
                .toFlowable()
                .flatMapIterable { busRouteList -> busRouteList }
                .map { busRoute: BusRoute ->  Route(busRoute.routeNumber, busRoute.routeName, busRoute.routeColor, 0, selectionStateRepository.isSelected(busRoute.routeNumber)) }
                .toList()
                .toFlowable()
    }

    val routesHashmap = routes
            .flatMapIterable { routeList -> routeList }
            .toMap { route -> route.route }

    /**
     * selected routes
     */
    fun toggleRoute2(route : Route) {

    }

    // TODO: rewrite the ViewModel to get toggled state by LiveData
    fun toggleRoute(route : Route) : Route {
        if (numberOfSelectedRoutes == MAX_NUMBER_OF_SELECTED_ROUTES) {
            Timber.d("Returning same route because # of selected routes is > 10")
            return route
        }
        val isSelectedWhenToggled = selectionStateRepository.toggleSelection(route.route)
        return Route(route.route, route.routeInfo, route.routeColor, 0, isSelectedWhenToggled)
    }

    val numberOfSelectedRoutes = selectionStateRepository.selectedRoutes.size

    val currentSelectedRoutes = selectionStateRepository.selectedRoutes
}