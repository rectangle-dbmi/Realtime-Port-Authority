package com.rectanglel.patstatic.predictions.response

import java.util.ArrayList
import javax.annotation.Generated
import com.google.gson.annotations.Expose

import com.rectanglel.patstatic.errors.response.Error

/**
 * Prediction list container POJO
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
data class BustimePredictionResponse (

    @Expose
    var prd: List<Prd> = ArrayList(),

    @Expose
    var error: List<Error> = ArrayList()

)