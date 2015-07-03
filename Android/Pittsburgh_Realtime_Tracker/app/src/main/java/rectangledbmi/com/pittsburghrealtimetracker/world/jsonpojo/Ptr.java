
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class Ptr {

    @Expose
    private int pid;
    @Expose
    private double ln;
    @Expose
    private String rtdir;
    @Expose
    private List<Pt> pt = new ArrayList<Pt>();
    @Expose
    private String msg;

    /**
     *
     * @return
     *     The pid
     */
    public int getPid() {
        return pid;
    }

    /**
     *
     * @param pid
     *     The pid
     */
    public void setPid(int pid) {
        this.pid = pid;
    }

    /**
     *
     * @return
     *     The ln
     */
    public double getLn() {
        return ln;
    }

    /**
     *
     * @param ln
     *     The ln
     */
    public void setLn(double ln) {
        this.ln = ln;
    }

    /**
     *
     * @return
     *     The rtdir
     */
    public String getRtdir() {
        return rtdir;
    }

    /**
     *
     * @param rtdir
     *     The rtdir
     */
    public void setRtdir(String rtdir) {
        this.rtdir = rtdir;
    }

    /**
     *
     * @return
     *     The pt
     */
    public List<Pt> getPt() {
        return pt;
    }

    /**
     *
     * @param pt
     *     The pt
     */
    public void setPt(List<Pt> pt) {
        this.pt = pt;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}