package com.rectanglel.patstatic.routes.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by epicstar on 3/5/17.
 */

class BusRouteResponse {
    @SerializedName("bustime-response")
    @Expose
    var busTimeRoutesResponse: BusTimeRoutesResponse? = null

}
