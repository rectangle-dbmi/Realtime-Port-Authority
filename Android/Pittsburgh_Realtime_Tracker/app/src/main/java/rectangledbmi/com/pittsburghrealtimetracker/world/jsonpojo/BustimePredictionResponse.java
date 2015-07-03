
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

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

}