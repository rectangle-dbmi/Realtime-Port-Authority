
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

/**
 * bus time response for the the vehicles
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class BustimeVehicleResponse {

    @Expose
    private List<Vehicle> vehicle = new ArrayList<Vehicle>();

    /**
     *
     * @return
     *     The vehicle
     */
    public List<Vehicle> getVehicle() {
        return vehicle;
    }

    /**
     *
     * @param vehicle
     *     The vehicle
     */
    public void setVehicle(List<Vehicle> vehicle) {
        this.vehicle = vehicle;
    }

}