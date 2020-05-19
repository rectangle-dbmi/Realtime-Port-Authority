package com.rectanglel.patstatic.vehicles.response

import com.google.gson.annotations.Expose
import com.rectanglel.patstatic.predictions.PredictionsType

import java.text.ParseException
import java.util.Date

import javax.annotation.Generated

/**
 * Vehicle (bus) Retrofit POJO
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
class Vehicle : PredictionsType {

    @Expose
    var vid: Int = 0
        private set
    @Expose
    var tmstmp: Date? = null
    @Expose
    var lat: Double = 0.toDouble()
        private set
    @Expose
    var lon: Double = 0.toDouble()
        private set
    @Expose
    var hdg: Int = 0
        private set
    @Expose
    var pid: Int = 0
    @Expose
    var rt: String? = null
    @Expose
    var des: String? = null
    @Expose
    var pdist: Int = 0
    @Expose
    var isDly: Boolean = false
    @Expose
    var spd: Int = 0
    @Expose
    var tatripid: Int = 0
        private set
    @Expose
    var tablockid: String? = null
    @Expose
    var zone: String? = null
    @Expose
    var msg: String? = null

    override val id: Int
        get() = vid

    override val title: String
        get() {
            val st = StringBuilder()
            st.append(String.format("%s (%d) %s", rt, vid, des))
            if (isDly) {
                st.append(" - Delayed")
            }
            return st.toString()
        }

    fun setVid(vid: String) {
        this.vid = Integer.parseInt(vid)
    }

    fun setLat(lat: String) {
        this.lat = java.lang.Double.parseDouble(lat)
    }

    fun setLon(lon: String) {
        this.lon = java.lang.Double.parseDouble(lon)
    }

    fun setHdg(hdg: String) {
        this.hdg = Integer.parseInt(hdg)
    }

    fun setTatripid(tatripid: String) {
        this.tatripid = Integer.parseInt(tatripid)
    }
}