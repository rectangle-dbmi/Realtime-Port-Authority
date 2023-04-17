package com.rectanglel.patstatic.patterns.response

import com.google.gson.annotations.Expose
import javax.annotation.Generated

/**
 * Bustime pattern for patterns
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
data class BustimePatternResponse(

    @Expose
    val ptr: List<Ptr>
)