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
data class StopRenderState constructor(val stopPt: Pt, val routeCount: Int)
