package com.rectanglel.patstatic.errors.response

import com.google.gson.annotations.Expose
import javax.annotation.processing.Generated

/**
 * Retrofit POJO for Errors
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
class Error {
    @Expose
    var msg: String? = null

    @Expose
    var rt: String? = null
        private set

    @Expose
    var stpid = 0
        private set

    @Expose
    var vid = 0
        private set

    fun setRt() {
        rt = rt
    }

    fun setStpid(stpid: String) {
        this.stpid = stpid.toInt()
    }

    fun setVid(vid: String) {
        this.vid = vid.toInt()
    }

}