package world;

public class Bus {

    private String vid;
    private String lat;
    private String lon;
    private String msg;
    private String tmstmp;
    private String hdg;
    private String pid;
    private String rt;
    private String des;
    private String pdist;
    private String dly;
    private String spd;
    private String tablockid;
    private String tatripid;

    public float getLat() {
        return Float.parseFloat(lat);
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getRt() { return rt; }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public float getLon() {
        return Float.parseFloat(lon);
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public int getVid() {
        return Integer.parseInt(vid);
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public int getSpd() { return Integer.parseInt(spd); }

    public void setSpd(String spd) {
        this.spd = spd;
    }

    public int getHdg() { return Integer.parseInt(hdg); }

    public void setHdg(String hdg) {
        this.hdg = hdg;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setTmStmp(String tmStmp) {
        this.tmstmp = tmStmp;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setPdist(String pdist) {
        this.pdist = pdist;
    }

    public void setDly(String dly) {
        this.dly = dly;
    }

    public void setTablockid(String tablockid) {
        this.tablockid = tablockid;
    }

    public void setTatripid(String tatripid) {
        this.tatripid = tatripid;
    }

    @Override
    public String toString() {
        return vid + "\t" + lat + "\t" + lon + "\t" + tmstmp + "\t" + hdg
                + "\t" + pid + "\t" + rt + "\t" + des + "\t" + pdist + "\t"
                + spd + "\t" + tablockid + "\t" + tatripid;
    }
}
