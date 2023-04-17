package com.rectanglel.patstatic.routes

import com.google.gson.annotations.SerializedName

/**
 * TrueTime representation of bus routes
 *
 *
 * Created by epicstar on 3/4/17.
 * @author Jeremy Jao
 */
data class BusRoute (
    @SerializedName("rt")
    val routeNumber: String,

    @SerializedName("rtnm")
    val routeName: String,

    @SerializedName("rtclr")
    val routeColor: String,

    @SerializedName("rtdd")
    val routeDesignator: String,

    @SerializedName("rtpidatafeed")
    val routeDatafeed: String
)
