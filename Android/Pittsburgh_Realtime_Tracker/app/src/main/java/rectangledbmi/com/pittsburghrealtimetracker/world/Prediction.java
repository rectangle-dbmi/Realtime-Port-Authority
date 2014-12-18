package rectangledbmi.com.pittsburghrealtimetracker.world;

/**
 * Created by rgupta on 12/17/14.
 */
public class Prediction {

    /**
     * Timestamp for time prediction was generated.
     */
    private String tmpstmp;

    private String typ;

    /**
     * Stop name.
     */
    private String stpnm;

    /**
     * Stop ID.
     */
    private String stpid;

    /**
     * Vehicle ID.
     */
    private String vid;

    private String dstp;

    /**
     * Route name.
     */
    private String rt;

    /**
     * Route direction.
     */
    private String rtdir;

    /**
     * Destination of route.
     */
    private String des;

    /**
     * Predicted time bus will be at stop.
     */
    private String prdtm;

    /**
     * Bus delay boolean.
     */
    private String dly;

    private String tablockid;
    private String tatripid;
    private String prdctdn;

    public String getTmpstmp() {
        return tmpstmp;
    }

    public void setTmpstmp(String tmpstmp) {
        this.tmpstmp = tmpstmp;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getStpnm() {
        return stpnm;
    }

    public void setStpnm(String stpnm) {
        this.stpnm = stpnm;
    }

    public String getStpid() {
        return stpid;
    }

    public void setStpid(String stpid) {
        this.stpid = stpid;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getDstp() {
        return dstp;
    }

    public void setDstp(String dstp) {
        this.dstp = dstp;
    }

    public String getRt() {
        return rt;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public String getRtdir() {
        return rtdir;
    }

    public void setRtdir(String rtdir) {
        this.rtdir = rtdir;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getPrdtm() {
        return prdtm;
    }

    public void setPrdtm(String prdtm) {
        this.prdtm = prdtm;
    }

    public String getDly() {
        return dly;
    }

    public void setDly(String dly) {
        this.dly = dly;
    }

    public String getTablockid() {
        return tablockid;
    }

    public void setTablockid(String tablockid) {
        this.tablockid = tablockid;
    }

    public String getTatripid() {
        return tatripid;
    }

    public void setTatripid(String tatripid) {
        this.tatripid = tatripid;
    }

    public String getPrdctdn() {
        return prdctdn;
    }

    public void setPrdctdn(String prdctdn) {
        this.prdctdn = prdctdn;
    }

    public String toString() {
        return vid + "\t" + stpid + "\t" + prdtm;
    }
}
