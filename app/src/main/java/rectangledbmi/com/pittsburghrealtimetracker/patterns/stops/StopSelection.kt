package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops

import com.rectanglel.patstatic.patterns.response.Pt

/**
 *
 * Immediate selection info for getStopRenderRequests.
 *
 * Created by epicstar on 9/20/16.
 * @author Jeremy Jao
 */
@Suppress("MemberVisibilityCanBePrivate")
class StopSelection private constructor(val stopPts: Collection<Pt>?, val routeNumber: String?, val isSelected: Boolean) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StopSelection) return false

        val that = other as StopSelection?

        if (isSelected != that!!.isSelected) return false
        if (if (stopPts != null) stopPts != that.stopPts else that.stopPts != null)
            return false
        return if (routeNumber != null) routeNumber == that.routeNumber else that.routeNumber == null

    }

    override fun hashCode(): Int {
        var result = stopPts?.hashCode() ?: 0
        result = 31 * result + (routeNumber?.hashCode() ?: 0)
        result = 31 * result + if (isSelected) 1 else 0
        return result
    }

    companion object {

        fun create(pts: Collection<Pt>,
                   routeNumber: String,
                   isSelected: Boolean): StopSelection {
            return StopSelection(pts, routeNumber, isSelected)
        }
    }
}
