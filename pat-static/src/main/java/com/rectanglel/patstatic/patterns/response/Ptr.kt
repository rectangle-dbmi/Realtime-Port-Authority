package com.rectanglel.patstatic.patterns.response

import java.util.ArrayList
import javax.annotation.Generated
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

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
    val rtdir: String,
    @SerializedName("pt")
    val pt: List<Pt>? = ArrayList(),
    @Expose
    val msg: String? = null

    ){


//    val pt: List<Pt>?
//        get() {
//            if (rtdir != null && points != null) {
//                for (p in points) {
//                    p.rtdir = rtdir
//                }
//            }
//            return points
//        }
}