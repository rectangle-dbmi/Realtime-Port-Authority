package com.rectanglel.patstatic.patterns.response

import java.util.ArrayList
import javax.annotation.Generated
import com.google.gson.annotations.Expose

/**
 * Bustime pattern for patterns
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
class BustimePatternResponse {

    @Expose
    var ptr: List<Ptr> = ArrayList()

    @Expose
    var error: List<Error> = ArrayList()

}