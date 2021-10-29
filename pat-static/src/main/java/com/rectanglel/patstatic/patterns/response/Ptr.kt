package com.rectanglel.patstatic.patterns.response

import java.util.ArrayList
import javax.annotation.processing.Generated
import com.google.gson.annotations.Expose

/**
 * Whole pattern Retrofit POJO that contains all its points
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
data class Ptr (

    @Expose
    val pid: Int = 0,
    @Expose
    val ln: Double = 0.toDouble(),
    @Expose
    private val rtdir: String? = null,
    @Expose
    private val pt: List<Pt>? = ArrayList(),
    @Expose
    val msg: String? = null

    ){


    fun getPt(): List<Pt>? {
        if (rtdir != null && pt != null) {
            for (p in pt) {
                p.rtdir = rtdir
            }
        }
        return pt
    }
}