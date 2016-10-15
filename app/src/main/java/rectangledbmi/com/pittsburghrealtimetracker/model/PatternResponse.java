
package rectangledbmi.com.pittsburghrealtimetracker.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import rectangledbmi.com.pittsburghrealtimetracker.patterns.response.BustimePatternResponse;

/**
 * Starting response to get patterns for the buses
 *
 * @author Jeremy Jao
 * @since 46
 */
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