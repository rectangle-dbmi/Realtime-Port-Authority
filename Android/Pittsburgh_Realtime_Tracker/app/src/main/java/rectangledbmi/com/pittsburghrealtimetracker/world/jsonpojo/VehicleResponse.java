
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * First retrofit object to get vehicles
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class VehicleResponse {

    @SerializedName("bustime-response")
    @Expose
    private BustimeVehicleResponse bustimeResponse;

    /**
     *
     * @return
     *     The bustimeResponse
     */
    public BustimeVehicleResponse getBustimeResponse() {
        return bustimeResponse;
    }

    /**
     *
     * @param bustimeResponse
     *     The bustime-response
     */
    public void setBustimeResponse(BustimeVehicleResponse bustimeResponse) {
        this.bustimeResponse = bustimeResponse;
    }

}