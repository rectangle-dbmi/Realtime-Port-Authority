
package com.rectanglel.patstatic.patterns.response;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

/**
 * Whole pattern Retrofit POJO that contains all its points
 *
 * @author Jeremy Jao
 * @since 46
 */
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
        setPtrs();
        return rtdir;
    }

    /**
     *
     * @param rtdir
     *     The rtdir
     */
    public void setRtdir(String rtdir) {
        this.rtdir = rtdir;
        setPtrs();
    }

    /**
     *
     * @return
     *     The pt
     */
    public List<Pt> getPt() {
        setPtrs();
        return pt;
    }

    /**
     *
     * @param pt
     *     The pt
     */
    public void setPt(List<Pt> pt) {
        this.pt = pt;
        setPtrs();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ptr)) return false;

        Ptr ptr = (Ptr) o;

        if (pid != ptr.pid) return false;
        if (Double.compare(ptr.ln, ln) != 0) return false;
        if (rtdir != null ? !rtdir.equals(ptr.rtdir) : ptr.rtdir != null) return false;
        if (pt != null ? !pt.equals(ptr.pt) : ptr.pt != null) return false;
        if (msg != null ? !msg.equals(ptr.msg) : ptr.msg != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = pid;
        temp = Double.doubleToLongBits(ln);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (rtdir != null ? rtdir.hashCode() : 0);
        result = 31 * result + (pt != null ? pt.hashCode() : 0);
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        return result;
    }

    private void setPtrs() {
        if (rtdir != null && pt != null) {
            for (Pt p : pt) {
                p.setRtdir(rtdir);
            }
        }
    }
}