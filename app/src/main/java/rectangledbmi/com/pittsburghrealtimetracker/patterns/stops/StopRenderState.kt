package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops

import com.rectanglel.patstatic.patterns.response.Pt

/**
 *
 * Rendering info for holding selection states for stops
 *
 * Created by epicstar on 9/20/16.
 * @since 78
 * @author Jeremy Jao
 * @author Michael Antonacci
 */
class StopRenderState private constructor(val stopPt: Pt?, private val routeCount: Int) {

    fun routeCount(): Int {
        return routeCount
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StopRenderState) return false

        val that = other as StopRenderState?


        if (routeCount != that!!.routeCount) return false
        return if (stopPt != null) stopPt == that.stopPt else that.stopPt == null

    }

    override fun hashCode(): Int {
        var result = stopPt?.hashCode() ?: 0
        result = 31 * result + routeCount
        return result
    }

    companion object {

        @JvmStatic
        fun create(stopInfo: Pt, routeCount: Int): StopRenderState {
            return StopRenderState(stopInfo, routeCount)
        }
    }
}
