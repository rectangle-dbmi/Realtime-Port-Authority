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
data class StopSelection(val stopPts: Collection<Pt>, val routeNumber: String, val isSelected: Boolean)
