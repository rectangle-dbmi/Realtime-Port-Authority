
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

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

    private HashMap<String, ArrayList<String>> processedErrors = new HashMap<String, ArrayList<String>>();

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
        processErrors();
    }

    private void processErrors() {
        for(Error err : error) {
            ArrayList<String> listOfParams = processedErrors.get(err.getMsg());
            if(listOfParams == null) {
                listOfParams = new ArrayList<String>();
                processedErrors.put(err.getMsg(), listOfParams);
            }
            listOfParams.add(err.getRt());

        }
    }

    public HashMap<String, ArrayList<String>> getProcessedErrors() {
        return processedErrors;
    }

    /**
     *
     * @return
     *     the error
     */
    public List<Error> getError() {
        return error;
    }



}