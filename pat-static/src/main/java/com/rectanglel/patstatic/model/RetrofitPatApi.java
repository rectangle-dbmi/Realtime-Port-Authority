package com.rectanglel.patstatic.model;

import com.rectanglel.patstatic.patterns.response.PatternResponse;
import com.rectanglel.patstatic.predictions.response.PredictionResponse;
import com.rectanglel.patstatic.routes.response.BusRouteResponse;
import com.rectanglel.patstatic.vehicles.response.VehicleResponse;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;


/**
 * This is the general api that sets the retrofit api. You must have the class
 * and the pat_api key before using the api in the gradle.properties file
 *
 * @author Jeremy Jao
 * @since 46
 */
public interface RetrofitPatApi {
    /**
     * generates a response for patters
     * @param rt - the route
     */
    @GET("getpatterns?format=json" )
    Observable<PatternResponse> getPatterns(@Query("rt") String rt);

    /**
     * Generates a response to get vehicles
     * @param routes - the routes
     */
    @GET("getvehicles?format=json")
    Observable<VehicleResponse> getVehicles(@Query("rt") String routes);

    /**
     * Generates a response to get the predictions using the stop id
     * @param stpid - the stop id
     */
    @SuppressWarnings("unused")
    @GET("getpredictions?format=json")
    Observable<PredictionResponse> getStopPredictions(@Query("stpid") int stpid);

    /**
     * Generates a response to get the predictions using the stop id
     * @param stpid - the stop id
     * @param rts - the routes
     */
    @GET("getpredictions?format=json&top=10")
    Single<PredictionResponse> getStopPredictions(@Query("stpid") int stpid, @Query("rt") String rts);

    /**
     * Generates a response to get the predictions using the bus id
     * @param vid - the bus id
     */
    @GET("getpredictions?format=json&top=10")
    Single<PredictionResponse> getBusPredictions(@Query("vid") int vid);

    /**
     * Generates a response to get routes from the API
     * @return the list of routes available from the TrueTime API
     */
    @GET("getroutes?format=json")
    Single<BusRouteResponse> getRoutes();


}
