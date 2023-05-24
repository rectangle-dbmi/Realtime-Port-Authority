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
    val number: String,

    @SerializedName("rtnm")
    val name: String,

    @SerializedName("rtclr")
    val color: String,

    @SerializedName("rtdd")
    val designator: String,

    @SerializedName("rtpidatafeed")
    val datafeed: String
)
