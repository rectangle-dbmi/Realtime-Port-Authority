
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import com.google.gson.annotations.Expose;

import java.text.ParseException;
import java.util.Date;

import javax.annotation.Generated;

import rectangledbmi.com.pittsburghrealtimetracker.predictions.PredictionsType;

/**
 * Vehicle (bus) Retrofit POJO
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class Vehicle implements PredictionsType {

    @Expose
    private int vid;
    @Expose
    private Date tmstmp;
    @Expose
    private double lat;
    @Expose
    private double lon;
    @Expose
    private int hdg;
    @Expose
    private int pid;
    @Expose
    private String rt;
    @Expose
    private String des;
    @Expose
    private int pdist;
    @Expose
    private boolean dly;
    @Expose
    private int spd;
    @Expose
    private int tatripid;
    @Expose
    private String tablockid;
    @Expose
    private String zone;
    @Expose
    private String msg;

    /**
     *
     * @return
     *     The vid
     */
    public int getVid() {
        return vid;
    }

    /**
     *
     * @param vid
     *     The vid
     */
    public void setVid(String vid) {
        this.vid = Integer.parseInt(vid);
    }

    /**
     *
     * @return
     *     The tmstmp
     */
    public Date getTmstmp() {
        return tmstmp;
    }

    /**
     *
     * @param tmstmp
     *     The tmstmp
     */
    public void setTmstmp(Date tmstmp) throws ParseException {
        this.tmstmp = tmstmp;
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
    public void setLat(String lat) {
        this.lat = Double.parseDouble(lat);
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
    public void setLon(String lon) {
        this.lon = Double.parseDouble(lon);
    }

    /**
     *
     * @return
     *     The hdg
     */
    public int getHdg() {
        return hdg;
    }

    /**
     *
     * @param hdg
     *     The hdg
     */
    public void setHdg(String hdg) {
        this.hdg = Integer.parseInt(hdg);
    }

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
     *     The rt
     */
    public String getRt() {
        return rt;
    }

    /**
     *
     * @param rt
     *     The rt
     */
    public void setRt(String rt) {
        this.rt = rt;
    }

    /**
     *
     * @return
     *     The des
     */
    public String getDes() {
        return des;
    }

    /**
     *
     * @param des
     *     The des
     */
    public void setDes(String des) {
        this.des = des;
    }

    /**
     *
     * @return
     *     The pdist
     */
    public int getPdist() {
        return pdist;
    }

    /**
     *
     * @param pdist
     *     The pdist
     */
    public void setPdist(int pdist) {
        this.pdist = pdist;
    }

    /**
     *
     * @return
     *     The dly
     */
    public boolean isDly() {
        return dly;
    }

    /**
     *
     * @param dly
     *     The dly
     */
    public void setDly(boolean dly) {
        this.dly = dly;
    }

    /**
     *
     * @return
     *     The spd
     */
    public int getSpd() {
        return spd;
    }

    /**
     *
     * @param spd
     *     The spd
     */
    public void setSpd(int spd) {
        this.spd = spd;
    }

    /**
     *
     * @return
     *     The tatripid
     */
    public int getTatripid() {
        return tatripid;
    }

    /**
     *
     * @param tatripid
     *     The tatripid
     */
    public void setTatripid(String tatripid) {
        this.tatripid = Integer.parseInt(tatripid);
    }

    /**
     *
     * @return
     *     The tablockid
     */
    public String getTablockid() {
        return tablockid;
    }

    /**
     *
     * @param tablockid
     *     The tablockid
     */
    public void setTablockid(String tablockid) {
        this.tablockid = tablockid;
    }

    /**
     *
     * @return
     *     The zone
     */
    public String getZone() {
        return zone;
    }

    /**
     *
     * @param zone
     *     The zone
     */
    public void setZone(String zone) {
        this.zone = zone;
    }

    /**
     *
     * @return
     *     the message
     */
    public String getMsg() {
        return msg;
    }

    /**
     *
     * @param msg
     *     the message
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public int getId() {
        return getVid();
    }

    @Override
    public String getTitle() {
        StringBuilder st = new StringBuilder();
        st.append(String.format("%s (%d) %s", getRt(), getVid(), getDes()));
        if (isDly()) {
            st.append(" - Delayed");
        }
        return st.toString();
    }
}