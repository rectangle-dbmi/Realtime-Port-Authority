package com.rectanglel.patstatic.routes

import com.google.gson.annotations.Expose

/**
 * TrueTime representation of bus routes
 *
 *
 * Created by epicstar on 3/4/17.
 * @author Jeremy Jao
 */
data class BusRoute (
    @Expose
    val routeNumber: String,

    @Expose
    val routeName: String,

    @Expose
    val routeColor: String,

    @Expose
    val routeDd: String
)
