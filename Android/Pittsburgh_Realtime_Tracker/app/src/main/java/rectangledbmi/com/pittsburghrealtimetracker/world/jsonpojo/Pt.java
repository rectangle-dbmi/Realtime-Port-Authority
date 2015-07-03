
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class Pt {

    @Expose
    private int seq;
    @Expose
    private double lat;
    @Expose
    private double lon;
    @Expose
    private char typ;
    @Expose
    private int stpid;
    @Expose
    private String stpnm;
    @Expose
    private double pdist;
    @Expose
    private String msg;

    /**
     *
     * @return
     *     The seq
     */
    public int getSeq() {
        return seq;
    }

    /**
     *
     * @param seq
     *     The seq
     */
    public void setSeq(int seq) {
        this.seq = seq;
    }

    /**
     *
     * @return
     *     The lat
     */
    public double getLat() {
        return lat;
    }

    /**
     *
     * @param lat
     *     The lat
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     *
     * @return
     *     The lon
     */
    public double getLon() {
        return lon;
    }

    /**
     *
     * @param lon
     *     The lon
     */
    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     *
     * @return
     *     The typ
     */
    public char getTyp() {
        return typ;
    }

    /**
     *
     * @param typ
     *     The typ
     */
    public void setTyp(String typ) {
        this.typ = typ.charAt(0);
    }

    /**
     *
     * @return
     *     The stpid
     */
    public int getStpid() {
        return stpid;
    }

    /**
     *
     * @param stpid
     *     The stpid
     */
    public void setStpid(String stpid) {
        this.stpid = Integer.parseInt(stpid);
    }

    /**
     *
     * @return
     *     The stpnm
     */
    public String getStpnm() {
        return stpnm;
    }

    /**
     *
     * @param stpnm
     *     The stpnm
     */
    public void setStpnm(String stpnm) {
        this.stpnm = stpnm;
    }

    /**
     *
     * @return
     *     The pdist
     */
    public double getPdist() {
        return pdist;
    }

    /**
     *
     * @param pdist
     *     The pdist
     */
    public void setPdist(double pdist) {
        this.pdist = pdist;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}