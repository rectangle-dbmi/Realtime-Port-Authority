package com.rectanglel.pattrack.routes.ui

import com.rectanglel.patstatic.model.PatApiService
import com.rectanglel.patstatic.routes.BusRoute
import com.rectanglel.pattrack.routes.RouteSelectionStateRepository
import com.rectanglel.pattrack.routes.RoutesViewModel
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route
import java.util.*

class RoutesViewModelTest {

    private lateinit var routesViewModel : RoutesViewModel
    private lateinit var routeSelectionStateRepository: RouteSelectionStateRepository
    private lateinit var patApiService: PatApiService

    @Before
    fun setUp() {
        routeSelectionStateRepository = Mockito.mock(RouteSelectionStateRepository::class.java)
        patApiService = Mockito.mock(PatApiService::class.java)
        routesViewModel = RoutesViewModel(routeSelectionStateRepository, patApiService)
    }

    @Test
    fun testGetRoutes() {

//        val testRoute = Route("61B", "Murray Something", 0, 0, false)
        var testRoute = BusRoute()
//        testRoute

        val testRoutes = ArrayList<Route>()


        var routes : List<Route>? = null
//        Mockito.`when`(patApiService.routes).thenReturn(Single.just(testRoutes))
//        routesViewModel.routesLiveData.observeForever {

//        }
    }


}