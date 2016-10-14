
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

import rectangledbmi.com.pittsburghrealtimetracker.predictions.PredictionInfo;

/**
 * object for each point in a pattern
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class Pt implements PredictionInfo {

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
    @Expose
    private String rtdir;

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

    public String getRtdir() {
        return rtdir;
    }

    public void setRtdir(String rtdir) {
        this.rtdir = rtdir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pt)) return false;

        Pt pt = (Pt) o;

        if (seq != pt.seq) return false;
        if (Double.compare(pt.lat, lat) != 0) return false;
        if (Double.compare(pt.lon, lon) != 0) return false;
        if (typ != pt.typ) return false;
        if (stpid != pt.stpid) return false;
        if (Double.compare(pt.pdist, pdist) != 0) return false;
        if (stpnm != null ? !stpnm.equals(pt.stpnm) : pt.stpnm != null) return false;
        if (msg != null ? !msg.equals(pt.msg) : pt.msg != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = seq;
        temp = Double.doubleToLongBits(lat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lon);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) typ;
        result = 31 * result + stpid;
        result = 31 * result + (stpnm != null ? stpnm.hashCode() : 0);
        temp = Double.doubleToLongBits(pdist);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        return result;
    }

    @Override
    public int getId() {
        if (getTyp() == 'S') {
            return getStpid();
        }
        return -1;
    }

    @Override
    public String getTitle() {
        return String.format("(%d) %s - %s", getStpid(), getStpnm(), getRtdir());
    }
}