package com.rectanglel.patstatic.predictions.response

import com.google.gson.annotations.Expose

import java.util.Date

import javax.annotation.processing.Generated

/**
 * Prediction POJO
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
data class Prd (

    @Expose
    val tmstmp: Date? = null,
    @Expose
    val typ: String? = null,
    @Expose
    val stpnm: String? = null,
    @Expose
    val stpid: String? = null,
    @Expose
    val vid: String? = null,
    @Expose
    val dstp: Int = 0,
    @Expose
    val rt: String? = null,
    @Expose
    val rtdir: String? = null,
    @Expose
    val des: String? = null,
    @Expose
    val prdtm: Date? = null,
    @Expose
    val tablockid: String? = null,
    @Expose
    val tatripid: String? = null,
    @Expose
    val isDly: Boolean = false,
    @Expose
    val prdctdn: String? = null,
    @Expose
    val zone: String? = null

)