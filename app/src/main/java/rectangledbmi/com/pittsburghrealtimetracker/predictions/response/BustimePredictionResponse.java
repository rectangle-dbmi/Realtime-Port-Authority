
package rectangledbmi.com.pittsburghrealtimetracker.predictions.response;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

import rectangledbmi.com.pittsburghrealtimetracker.model.Error;

/**
 * Prediction list container POJO
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class BustimePredictionResponse {

    @Expose
    private List<Prd> prd = new ArrayList<Prd>();

    @Expose
    private List<Error> error = new ArrayList<Error>();

    /**
     *
     * @return
     *     The prd
     */
    public List<Prd> getPrd() {
        return prd;
    }

    /**
     *
     * @param prd
     *     The prd
     */
    public void setPrd(List<Prd> prd) {
        this.prd = prd;
    }

    /**
     *
     * @param error
     *     The list of errors
     */
    public void setError(List<Error> error) {
        this.error = error;
    }

    /**
     *
     * @return
     *     the error
     */
    public List<Error> gettError() {
        return error;
    }

}