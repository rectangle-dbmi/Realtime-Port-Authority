package rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering

import com.rectanglel.patstatic.patterns.response.Pt

/**
 *
 * Rendering info for getStopRenderRequests
 *
 * Created by epicstar on 9/20/16.
 * @since 78
 * @author Jeremy Jao
 * @author Michael Antonacci
 */
data class StopRenderRequest (val stopPt: Pt, val isVisible: Boolean)
