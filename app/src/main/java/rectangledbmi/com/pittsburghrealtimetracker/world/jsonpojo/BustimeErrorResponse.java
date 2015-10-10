
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

/**
 * Retrofit POJO for errors that contains a list of errors
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class BustimeErrorResponse {

    @Expose
    private List<Error> error = new ArrayList<Error>();

    /**
     *
     * @return
     *     The error
     */
    public List<Error> getError() {
        return error;
    }

    /**
     *
     * @param error
     *     The error
     */
    public void setError(List<Error> error) {
        this.error = error;
    }

}