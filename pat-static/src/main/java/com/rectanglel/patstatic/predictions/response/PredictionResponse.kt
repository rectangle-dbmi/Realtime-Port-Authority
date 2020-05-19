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
class PredictionResponse {

    @SerializedName("bustime-response")
    @Expose
    var bustimeResponse: BustimePredictionResponse? = null

}