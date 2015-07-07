package rectangledbmi.com.pittsburghrealtimetracker.retrofit;

import rectangledbmi.com.pittsburghrealtimetracker.hidden.HiddenConstants;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.PatternResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.PredictionResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.VehicleResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * This is the general api that sets the retrofit api. You must have tte {@link HiddenConstants} class
 * and the PAT_API_KEY before using the api
 *
 * @author Jeremy Jao
 * @since 46
 */
public interface PATAPI {

    /**
     * generates a response for patters
     * @param rt - the route
     * @param responseCallback - the callback
     */
    @GET("/getpatterns?format=json&&rt={route}&key=" + HiddenConstants.PAT_API_KEY)
    void getPatterns(@Path("route") String rt, Callback<PatternResponse> responseCallback);

    /**
     * Generates a response to get vehicles
     * @param routes - the routes
     * @param responseCallback - the callback
     */
    @GET("/getvehicles?format=json&&rt={routes}&key=" + HiddenConstants.PAT_API_KEY)
    void getVehicles(@Path("routes") String routes, Callback<VehicleResponse> responseCallback);

    /**
     * Generates a response to get predictions when clicking on a bus
     * @param vid - the bus id
     * @param responseCallback - the callback
     */
    @GET("/getpredictions?format=json&vid={vid}&key=" + HiddenConstants.PAT_API_KEY)
    void getBusPredictions(@Path("vid") int vid, Callback<PredictionResponse> responseCallback);

    /**
     * Generates a response to get the predictions
     * @param stpid - the bus id
     * @param responseCallback
     */
    @GET("/getpredictions?format=json&stpid={stpid}&key=" + HiddenConstants.PAT_API_KEY)
    void getStopPredictions(@Path("stpid") int stpid, Callback<PredictionResponse> responseCallback);

    @GET("/getpredictions?format=json&stpid={stpid}&rt={rts}&key=" + HiddenConstants.PAT_API_KEY)
    void getStopPredictions(@Path("stpid") int stpid, @Path("rts") String rts, Callback<PredictionResponse> responseCallback);


}
