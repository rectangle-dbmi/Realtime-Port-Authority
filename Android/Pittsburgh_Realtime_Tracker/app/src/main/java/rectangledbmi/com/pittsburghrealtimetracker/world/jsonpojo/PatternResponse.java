
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class PatternResponse {

    @SerializedName("bustime-response")
    @Expose
    private BustimePatternResponse patternResponse;

    /**
     *
     * @return
     *     The patternResponse
     */
    public BustimePatternResponse getPatternResponse() {
        return patternResponse;
    }

    /**
     *
     * @param patternResponse
     *     The bustime-response
     */
    public void setPatternResponse(BustimePatternResponse patternResponse) {
        this.patternResponse = patternResponse;
    }

}