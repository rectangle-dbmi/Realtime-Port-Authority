package com.rectanglel.patstatic.routes.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by epicstar on 3/5/17.
 */

public class BusRouteResponse {
    @SerializedName("bustime-response")
    @Expose
    private BusTimeRoutesResponse busTimeRoutesResponse;

    public BusTimeRoutesResponse getBusTimeRoutesResponse() {
        return busTimeRoutesResponse;
    }

    public void setBusTimeRoutesResponse(BusTimeRoutesResponse busTimeRoutesResponse) {
        this.busTimeRoutesResponse = busTimeRoutesResponse;
    }

}
