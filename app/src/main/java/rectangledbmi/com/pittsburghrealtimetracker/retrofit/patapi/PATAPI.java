package rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi;

import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.PatternResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.PredictionResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.VehicleResponse;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * This is the general api that sets the retrofit api. You must have the class
 * and the pat_api key before using the api in the gradle.properties file
 *
 * @author Jeremy Jao
 * @since 46
 */
public interface PATAPI {
    /**
     * generates a response for patters
     * @param rt - the route
     * @param api_key - the api key
     */
    @GET("getpatterns?format=json" )
    Observable<PatternResponse> getPatterns(@Query("rt") String rt, @Query("key") String api_key);

    /**
     * Generates a response to get vehicles
     * @param routes - the routes
     * @param api_key - the api key
     */
    @GET("getvehicles?format=json")
    Observable<VehicleResponse> getVehicles(@Query("rt") String routes, @Query("key") String api_key);

    /**
     * Generates a response to get the predictions using the stop id
     * @param stpid - the stop id
     * @param api_key - the api key
     */
    @GET("getpredictions?format=json")
    Observable<PredictionResponse> getStopPredictions(@Query("stpid") int stpid, @Query("key") String api_key);

    /**
     * Generates a response to get the predictions using the stop id
     * @param stpid - the stop id
     * @param rts - the routes
     * @param api_key - the api key
     */
    @GET("getpredictions?format=json")
    Observable<PredictionResponse> getStopPredictions(@Query("stpid") int stpid, @Query("rt") String rts, @Path("api_key") String api_key);

    /**
     * Generates a response to get the predictions using the bus id
     * @param vid - the bus id
     * @param api_key - the api key
     */
    @GET("getpredictions?format=json")
    Observable<PredictionResponse> getBusPrediction(@Query("vid") int vid, @Query("api_key") String api_key);


}
