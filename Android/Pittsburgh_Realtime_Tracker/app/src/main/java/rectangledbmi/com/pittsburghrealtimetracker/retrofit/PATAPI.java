package rectangledbmi.com.pittsburghrealtimetracker.retrofit;

import rectangledbmi.com.pittsburghrealtimetracker.BuildConfig;
import rectangledbmi.com.pittsburghrealtimetracker.hidden.HiddenConstants;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.PatternResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.PredictionResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.VehicleResponse;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * This is the general api that sets the retrofit api. You must have tte {@link HiddenConstants} class
 * and the PAT_API_KEY before using the api
 *
 * @author Jeremy Jao
 * @since 46
 */
public interface PATAPI {
    String key = BuildConfig.PAT_API_KEY;
    /**
     * generates a response for patters
     * @param rt - the route
     * @param api_key - the api key
     */
    @GET("/getpatterns?format=json&&rt={route}&key={api_key}" )
    Observable<PatternResponse> getPatterns(@Path("route") String rt, @Path("api_key") String api_key);

    /**
     * Generates a response to get vehicles
     * @param routes - the routes
     * @param api_key - the api key
     */
    @GET("/getvehicles?format=json&&rt={routes}&key={api_key}")
    Observable<VehicleResponse> getVehicles(@Path("routes") String routes, @Path("api_key") String api_key);

    /**
     * Generates a response to get the predictions using the stop id
     * @param stpid - the stop id
     * @param api_key - the api key
     */
    @GET("/getpredictions?format=json&stpid={stpid}&key={api_key}")
    Observable<PredictionResponse> getStopPredictions(@Path("stpid") int stpid, @Path("api_key") String api_key);

    /**
     * Generates a response to get the predictions using the stop id
     * @param stpid - the stop id
     * @param rts - the routes
     * @param api_key - the api key
     */
    @GET("/getpredictions?format=json&stpid={stpid}&rt={rts}&key={api_key}")
    Observable<PredictionResponse> getStopPredictions(@Path("stpid") int stpid, @Path("rts") String rts, @Path("api_key") String api_key);

    /**
     * Generates a response to get the predictions using the bus id
     * @param vid - the bus id
     * @param api_key - the api key
     */
    @GET("/getpredictions?format=json&vid={vid}&key={api_key}")
    Observable<PredictionResponse> getBusPrediction(@Path("vid") int vid, @Path("api_key") String api_key);


}
