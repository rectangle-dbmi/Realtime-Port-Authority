
package rectangledbmi.com.pittsburghrealtimetracker.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

/**
 * Retrofit POJO for Errors
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class Error {

    @Expose
    private String msg;
    @Expose
    private String rt;
    @Expose
    private int stpid;
    @Expose
    private int vid;

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

    public String getRt() {
        return rt;
    }

    public void setRt() {
        this.rt = rt;
    }

    /**
     *
     * @return
     *     the stpid
     */
    public int getStpid() {
        return stpid;
    }

    /**
     *
     * @param stpid
     *     the stpid
     */
    public void setStpid(String stpid) {
        this.stpid = Integer.parseInt(stpid);
    }

    public void setVid(String vid) {
        this.vid = Integer.parseInt(vid);
    }

    public int getVid() {
        return vid;
    }

}