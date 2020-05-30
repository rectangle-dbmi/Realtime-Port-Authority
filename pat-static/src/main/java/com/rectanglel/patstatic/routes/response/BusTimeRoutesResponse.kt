package com.rectanglel.patstatic.routes.response

import com.google.gson.annotations.Expose
import com.rectanglel.patstatic.routes.BusRoute

/**
 * Created by epicstar on 3/5/17.
 */

data class BusTimeRoutesResponse (

    @Expose
    val routes: List<BusRoute>? = null

)
