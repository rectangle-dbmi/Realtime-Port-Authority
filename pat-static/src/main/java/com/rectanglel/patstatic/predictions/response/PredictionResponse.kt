package com.rectanglel.patstatic.predictions.response

import javax.annotation.Generated
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Stop predictions Starting POJO
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
data class PredictionResponse (

    @SerializedName("bustime-response")
    @Expose
    val bustimeResponse: BustimePredictionResponse? = null

)