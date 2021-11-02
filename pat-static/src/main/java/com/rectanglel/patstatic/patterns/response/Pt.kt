package com.rectanglel.patstatic.patterns.response

import javax.annotation.processing.Generated
import com.google.gson.annotations.Expose
import com.rectanglel.patstatic.predictions.PredictionsType

/**
 * object for each point in a pattern
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
data class Pt(@Expose val seq: Int,
              @Expose val lat: Double = 0.toDouble(),
              @Expose val lon: Double = 0.toDouble(),
              @Expose val typ: Char = ' ',
              @Expose val stpid: Int = 0,
              @Expose val stpnm: String? = null,
              @Expose val pdist: Double = 0.toDouble(),
              @Expose val msg: String? = null,
              @Expose var rtdir: String? = null) : PredictionsType {

    override val id: Int
        get() = if (typ == 'S') {
            stpid
        } else -1

    override val title: String
        get() = String.format("(%d) %s - %s", stpid, stpnm, rtdir)
}
