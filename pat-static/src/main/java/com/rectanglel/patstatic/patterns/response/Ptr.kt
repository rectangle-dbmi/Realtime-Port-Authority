package com.rectanglel.patstatic.patterns.response

import java.util.ArrayList
import javax.annotation.Generated
import com.google.gson.annotations.Expose

/**
 * Whole pattern Retrofit POJO that contains all its points
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
class Ptr {

    @Expose
    var pid: Int = 0
    @Expose
    var ln: Double = 0.toDouble()
    @Expose
    private var rtdir: String? = null
    @Expose
    private var pt: List<Pt>? = ArrayList()
    @Expose
    var msg: String? = null

    fun getRtdir(): String? {
        setPtrs()
        return rtdir
    }

    fun setRtdir(rtdir: String) {
        this.rtdir = rtdir
        setPtrs()
    }

    fun getPt(): List<Pt>? {
        setPtrs()
        return pt
    }

    fun setPt(pt: List<Pt>) {
        this.pt = pt
        setPtrs()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ptr) return false

        val ptr = other as Ptr?

        if (pid != ptr!!.pid) return false
        if (ptr.ln.compareTo(ln) != 0) return false
        if (if (rtdir != null) rtdir != ptr.rtdir else ptr.rtdir != null) return false
        if (if (pt != null) pt != ptr.pt else ptr.pt != null) return false
        return if (if (msg != null) msg != ptr.msg else ptr.msg != null) false else true

    }

    override fun hashCode(): Int {
        var result: Int = pid
        val temp: Long = java.lang.Double.doubleToLongBits(ln)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        result = 31 * result + if (rtdir != null) rtdir!!.hashCode() else 0
        result = 31 * result + if (pt != null) pt!!.hashCode() else 0
        result = 31 * result + if (msg != null) msg!!.hashCode() else 0
        return result
    }

    private fun setPtrs() {
        if (rtdir != null && pt != null) {
            for (p in pt!!) {
                p.rtdir = rtdir
            }
        }
    }
}