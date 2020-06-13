package com.rectanglel.patstatic.model

import com.rectanglel.patstatic.patterns.response.PatternResponse
import com.rectanglel.patstatic.predictions.response.PredictionResponse
import com.rectanglel.patstatic.routes.response.BusRouteResponse
import com.rectanglel.patstatic.vehicles.response.VehicleResponse
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * This is the general api that sets the retrofit api. You must have the class
 * and the pat_api key before using the api in the gradle.properties file
 *
 * @author Jeremy Jao
 * @since 46
 */
interface RetrofitPatApi {

    /**
     * Generates a response to get routes from the API
     * @return the list of routes available from the TrueTime API
     */
    @get:GET("getroutes?format=json")
    val routes: Single<BusRouteResponse>

    /**
     * generates a response for patters
     * @param rt - the route
     */
    @GET("getpatterns?format=json")
    fun getPatterns(@Query("rt") rt: String): Flowable<PatternResponse>

    /**
     * Generates a response to get vehicles
     * @param routes - the routes
     */
    @GET("getvehicles?format=json")
    fun getVehicles(@Query("rt") routes: String): Flowable<VehicleResponse>

    /**
     * Generates a response to get the predictions using the stop id
     * @param stpid - the stop id
     */
    @GET("getpredictions?format=json")
    fun getStopPredictions(@Query("stpid") stpid: Int): Flowable<PredictionResponse>

    /**
     * Generates a response to get the predictions using the stop id
     * @param stpid - the stop id
     * @param rts - the routes
     */
    @GET("getpredictions?format=json&top=10")
    fun getStopPredictions(@Query("stpid") stpid: Int, @Query("rt") rts: String): Single<PredictionResponse>

    /**
     * Generates a response to get the predictions using the bus id
     * @param vid - the bus id
     */
    @GET("getpredictions?format=json&top=10")
    fun getBusPredictions(@Query("vid") vid: Int): Single<PredictionResponse>


}
