package com.rectanglel.patstatic.patterns.response

import javax.annotation.Generated
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Starting response to get patterns for the buses
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
data class PatternResponse(
    @SerializedName("bustime-response")
    @Expose
    val patternResponse: BustimePatternResponse
)