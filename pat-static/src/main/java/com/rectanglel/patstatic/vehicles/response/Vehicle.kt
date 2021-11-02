package com.rectanglel.patstatic.vehicles.response

import com.google.gson.annotations.Expose
import com.rectanglel.patstatic.predictions.PredictionsType
import java.util.*
import javax.annotation.processing.Generated

/**
 * Vehicle (bus) Retrofit POJO
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
data class Vehicle(
        @Expose
        val vid: Int = 0,
        @Expose
        val tmstmp: Date? = null,
        @Expose
        val lat: Double = 0.toDouble(),
        @Expose
        val lon: Double = 0.toDouble(),
        @Expose
        val hdg: Int = 0,
        @Expose
        val pid: Int = 0,
        @Expose
        val rt: String? = null,
        @Expose
        val des: String? = null,
        @Expose
        val pdist: Int = 0,
        @Expose
        var isDly: Boolean = false,
        @Expose
        val spd: Int = 0,
        @Expose
        val tatripid: Int = 0,
        @Expose
        val tablockid: String? = null,
        @Expose
        val zone: String? = null,
        @Expose
        val msg: String? = null) : PredictionsType {

    override val id: Int
            get() = vid

        override val title: String
                get() = "$rt ($vid) $des${if (isDly) " - Delayed" else ""}"
}
