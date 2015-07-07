
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Stop predictions Starting POJO
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class PredictionResponse {

    @SerializedName("bustime-response")
    @Expose
    private BustimePredictionResponse bustimeResponse;

    /**
     *
     * @return
     *     The bustimeResponse
     */
    public BustimePredictionResponse getBustimeResponse() {
        return bustimeResponse;
    }

    /**
     *
     * @param bustimeResponse
     *     The bustime-response
     */
    public void setBustimeResponse(BustimePredictionResponse bustimeResponse) {
        this.bustimeResponse = bustimeResponse;
    }

}