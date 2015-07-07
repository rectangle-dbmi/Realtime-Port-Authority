
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

/**
 * Bustime pattern for patterns
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class BustimePatternResponse {

    @Expose
    private List<Ptr> ptr = new ArrayList<Ptr>();

    @Expose
    private List<Error> error = new ArrayList<Error>();

    /**
     *
     * @return
     *     The ptr
     */
    public List<Ptr> getPtr() {
        return ptr;
    }

    /**
     *
     * @param ptr
     *     The ptr
     */
    public void setPtr(List<Ptr> ptr) {
        this.ptr = ptr;
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