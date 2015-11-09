
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Expose
    private List<Error> error = new ArrayList<Error>();

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

    /**
     *
     * @param error
     *     The list of errors
     */
    public void setError(List<Error> error) {
        this.error = error;
        Log.d("vehicle_error", "set errors....");

    }

    /**
     * Processes the errors into a hashmap since like messages can be transient
     * @return the hashmap of processed errors
     * @since 55
     */
    private HashMap<String, ArrayList<String>> processErrors() {
        HashMap<String, ArrayList<String>> processedErrors =
                new HashMap<String, ArrayList<String>>(error.size());
        for(Error err : error) {
            ArrayList<String> listOfParams = processedErrors.get(err.getMsg());
            if(listOfParams == null) {
                listOfParams = new ArrayList<String>();
                processedErrors.put(err.getMsg(), listOfParams);
            }
            listOfParams.add(err.getRt());
        }
        return processedErrors;
    }

    /**
     * Gets the processed errors into a HashMap.
     * @return a hashmap of messages as keys and the parameters for the messages as the value.
     * @since 55
     */
    public HashMap<String, ArrayList<String>> getProcessedErrors() {
        Log.d("vehicle_error", getError().toString());
        return processErrors();
    }

    /**
     *
     * @return
     *     the raw error messages
     */
    public List<Error> getError() {
        return error;
    }



}