
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class Error {

    @Expose
    private String msg;

    /**
     *
     * @return
     *     The msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     *
     * @param msg
     *     The msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

}