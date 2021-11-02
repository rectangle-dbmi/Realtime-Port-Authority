package com.rectanglel.patstatic.routes.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by epicstar on 3/5/17.
 */

data class BusRouteResponse(
    @SerializedName("bustime-response")
    @Expose
    val busTimeRoutesResponse: BusTimeRoutesResponse? = null

)
