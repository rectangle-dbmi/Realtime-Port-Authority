package rectangledbmi.com.pittsburghrealtimetracker.world;

import com.google.android.gms.maps.model.LatLng;

/**
 * This is the object to wrap the minimal bus stop stuff...
 *
 * Created by epicstar on 12/7/14.
 */
public class LineInfo {
    private int stpid;
    private String stpnm;
    private String rtdir;
    private LatLng latLng;

    private boolean isBusStop;

    /**
     * Primary constructor that sets the stpid, stpnm, lat, and lon as strings
     * @param stpid the stop id
     * @param stpnm the stop name
     * @param rtdir the route direction
     * @param lat the latitude of the stop
     * @param lon the longitude of the stop
     */
    public LineInfo(String stpid, String stpnm, String rtdir, double lat, double lon) {
        setStpid(stpid);
        setStpnm(stpnm);
        setRtdir(rtdir);
        setLatLng(lat, lon);
        isBusStop = true;
    }

    public LineInfo(LatLng latLng) {
        this.latLng = latLng;
        isBusStop = false;
    }

    public LineInfo(double lat, double lon) {
        setLatLng(lat, lon);
        isBusStop = false;
    }

    public void setStpid(String stpid) {
        setStpid(Integer.parseInt(stpid));
    }

    public void setStpid(int stpid) {
        this.stpid = stpid;
    }

    public void setStpnm(String stpnm) {
        this.stpnm = stpnm;
    }

    public void setRtdir(String rtdir) {
        this.rtdir = rtdir;
    }

    public void setLatLng(String lat, String lon) {
        setLatLng(Double.parseDouble(lat), Double.parseDouble(lon));
    }

    public void setLatLng(double lat, double lon) {
        latLng = new LatLng(lat, lon);
    }

    public int getStpid() {
        return stpid;
    }

    public String getStpnm() {
        return stpnm;
    }

    public String getRtdir() {
        return rtdir;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public boolean isBusStop() {
        return isBusStop;
    }



}
