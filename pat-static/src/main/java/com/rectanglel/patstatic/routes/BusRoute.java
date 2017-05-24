package com.rectanglel.patstatic.routes;

import com.google.gson.annotations.Expose;

/**
 * TrueTime representation of bus routes
 * <p>
 * Created by epicstar on 3/4/17.
 * @author Jeremy Jao
 */
public class BusRoute {
    @Expose
    private String rt;

    @Expose
    private String rtnm;

    @Expose
    private String rtclr;

    @Expose
    private String rtdd;

    public String getRouteNumber() {
        return rt;
    }

    public String getRouteName() {
        return rtnm;
    }

    public String getRouteColor() {
        return rtclr;
    }

    public String getRouteDd() {
        return rtdd;
    }
}
