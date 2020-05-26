package com.rectanglel.patstatic.predictions.response

import com.google.gson.annotations.Expose

import java.util.Date

import javax.annotation.Generated

/**
 * Prediction POJO
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
data class Prd (

    @Expose
    var tmstmp: Date? = null,
    @Expose
    var typ: String? = null,
    @Expose
    var stpnm: String? = null,
    @Expose
    var stpid: String? = null,
    @Expose
    var vid: String? = null,
    @Expose
    var dstp: Int = 0,
    @Expose
    var rt: String? = null,
    @Expose
    var rtdir: String? = null,
    @Expose
    var des: String? = null,
    @Expose
    var prdtm: Date? = null,
    @Expose
    var tablockid: String? = null,
    @Expose
    var tatripid: String? = null,
    @Expose
    var isDly: Boolean = false,
    @Expose
    var prdctdn: String? = null,
    @Expose
    var zone: String? = null

)