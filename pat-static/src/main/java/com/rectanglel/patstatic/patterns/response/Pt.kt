package com.rectanglel.patstatic.patterns.response

import javax.annotation.Generated
import com.google.gson.annotations.Expose
import com.rectanglel.patstatic.predictions.PredictionsType

/**
 * object for each point in a pattern
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
class Pt : PredictionsType {

    @Expose
    var seq: Int = 0
    @Expose
    var lat: Double = 0.toDouble()
    @Expose
    var lon: Double = 0.toDouble()
    @Expose
    var typ: Char = ' '
        private set
    @Expose
    var stpid: Int = 0
        private set
    @Expose
    var stpnm: String? = null
    @Expose
    var pdist: Double = 0.toDouble()
    @Expose
    var msg: String? = null
    @Expose
    var rtdir: String? = null

    override val id: Int
        get() = if (typ == 'S') {
            stpid
        } else -1

    override val title: String
        get() = String.format("(%d) %s - %s", stpid, stpnm, rtdir)

    fun setTyp(typ: String) {
        this.typ = typ[0]
    }

    fun setStpid(stpid: String) {
        this.stpid = Integer.parseInt(stpid)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Pt) return false

        val pt = other as Pt?

        if (seq != pt!!.seq) return false
        if (pt.lat.compareTo(lat) != 0) return false
        if (pt.lon.compareTo(lon) != 0) return false
        if (typ != pt.typ) return false
        if (stpid != pt.stpid) return false
        if (pt.pdist.compareTo(pdist) != 0) return false
        if (if (stpnm != null) stpnm != pt.stpnm else pt.stpnm != null) return false
        return if (if (msg != null) msg != pt.msg else pt.msg != null) false else true

    }

    override fun hashCode(): Int {
        var result: Int = seq
        var temp: Long = java.lang.Double.doubleToLongBits(lat)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        temp = java.lang.Double.doubleToLongBits(lon)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        result = 31 * result + typ.toInt()
        result = 31 * result + stpid
        result = 31 * result + if (stpnm != null) stpnm!!.hashCode() else 0
        temp = java.lang.Double.doubleToLongBits(pdist)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        result = 31 * result + if (msg != null) msg!!.hashCode() else 0
        return result
    }
}