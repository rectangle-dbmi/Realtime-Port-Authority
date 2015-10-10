
package rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo;

import com.google.gson.annotations.Expose;

import java.util.Date;

import javax.annotation.Generated;

/**
 * Prediction POJO
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
public class Prd {

    @Expose
    private Date tmstmp;
    @Expose
    private String typ;
    @Expose
    private String stpnm;
    @Expose
    private String stpid;
    @Expose
    private String vid;
    @Expose
    private int dstp;
    @Expose
    private String rt;
    @Expose
    private String rtdir;
    @Expose
    private String des;
    @Expose
    private Date prdtm;
    @Expose
    private String tablockid;
    @Expose
    private String tatripid;
    @Expose
    private boolean dly;
    @Expose
    private String prdctdn;
    @Expose
    private String zone;

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
    public void setTmstmp(Date tmstmp) {
        this.tmstmp = tmstmp;
    }

    /**
     *
     * @return
     *     The typ
     */
    public String getTyp() {
        return typ;
    }

    /**
     *
     * @param typ
     *     The typ
     */
    public void setTyp(String typ) {
        this.typ = typ;
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
     *     The stpid
     */
    public String getStpid() {
        return stpid;
    }

    /**
     *
     * @param stpid
     *     The stpid
     */
    public void setStpid(String stpid) {
        this.stpid = stpid;
    }

    /**
     *
     * @return
     *     The vid
     */
    public String getVid() {
        return vid;
    }

    /**
     *
     * @param vid
     *     The vid
     */
    public void setVid(String vid) {
        this.vid = vid;
    }

    /**
     *
     * @return
     *     The dstp
     */
    public int getDstp() {
        return dstp;
    }

    /**
     *
     * @param dstp
     *     The dstp
     */
    public void setDstp(int dstp) {
        this.dstp = dstp;
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
     *     The prdtm
     */
    public Date getPrdtm() {
        return prdtm;
    }

    /**
     *
     * @param prdtm
     *     The prdtm
     */
    public void setPrdtm(Date prdtm) {
        this.prdtm = prdtm;
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
     *     The tatripid
     */
    public String getTatripid() {
        return tatripid;
    }

    /**
     *
     * @param tatripid
     *     The tatripid
     */
    public void setTatripid(String tatripid) {
        this.tatripid = tatripid;
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
     *     The prdctdn
     */
    public String getPrdctdn() {
        return prdctdn;
    }

    /**
     *
     * @param prdctdn
     *     The prdctdn
     */
    public void setPrdctdn(String prdctdn) {
        this.prdctdn = prdctdn;
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

}