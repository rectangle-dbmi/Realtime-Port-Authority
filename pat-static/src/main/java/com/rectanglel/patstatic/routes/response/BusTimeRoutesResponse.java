package com.rectanglel.patstatic.routes.response;

import com.google.gson.annotations.Expose;
import com.rectanglel.patstatic.routes.BusRoute;

import java.util.List;

/**
 * Created by epicstar on 3/5/17.
 */

public class BusTimeRoutesResponse {

    @Expose
    private List<BusRoute> routes;

    public List<BusRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(List<BusRoute> routes) {
        this.routes = routes;
    }

}
