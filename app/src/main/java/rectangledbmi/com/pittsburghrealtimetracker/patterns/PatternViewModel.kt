package rectangledbmi.com.pittsburghrealtimetracker.patterns

import android.annotation.SuppressLint
import com.google.android.gms.maps.model.LatLng
import com.rectanglel.patstatic.model.PatApiService
import com.rectanglel.patstatic.patterns.response.Pt
import com.rectanglel.patstatic.patterns.response.Ptr
import io.reactivex.Flowable
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.StopRenderState
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.StopSelection
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.EitherStopState
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.FullStopSelectionState
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.MapState
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering.StopRenderRequest
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering.StopRequestAccumulator
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route
import timber.log.Timber
import java.util.*

/**
 *
 * Contains the logic of the [PolylineView] for pre-processing the patternSelections.
 *
 * Created by epicstar on 7/18/16.
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 * @since 78
 */
class PatternViewModel(service: PatApiService,
                       selectionFlowable: Flowable<Route>?) {
    private val patternSelections: Flowable<PatternSelection>?
    private val zoomThreshold = 15.0f

    /**
     *
     * Gets a flowable so the [PolylineView] can listen to actions to create, show,
     * or make a [com.google.android.gms.maps.model.Polyline] disappear.
     *
     * [PatternSelection.getPatterns] will be `null` if [PatternSelection.isSelected]
     * is `false`. Otherwise, it will be not be `null` unless the polyline data was unable to
     *
     * @return a flowable for patternSelections
     */
    fun getPatternSelections(): Flowable<PatternSelection>? {
        return patternSelections
                ?.flatMap { patternSelection: PatternSelection ->
                    if (patternSelection.patterns == null) {
                        return@flatMap Flowable.just(patternSelection)
                    }
                    Flowable
                            .fromIterable(patternSelection.patterns)
                            .flatMapSingle { patterns: Ptr ->
                                Flowable
                                        .fromIterable(patterns.getPt())
                                        .map { pt: Pt -> LatLng(pt.lat, pt.lon) }
                                        .toList()
                            }
                            .toList()
                            .map { latLngs: List<List<LatLng>>? ->
                                patternSelection.latLngs = latLngs
                                patternSelection
                            }
                            .toFlowable()
                }
    }

    /**
     *
     * Gets a flowable so the [StopView] only needs
     * to implement an [io.reactivex.Observer] can listen to changes to the state of visible stops.
     *
     * Internally, this will store state changes to map camera and selection changes such that
     * the only items being emitted will be whether or not a stop will show or not.
     * @param zoomFlowable the zoom flowable
     * @return a flowable that emits any change to change in stop visibility
     */
    fun getStopRenderRequests(zoomFlowable: Flowable<Float>?): Flowable<StopRenderRequest>? {
        // NOTE: Will have to make our own StopViewModel and move this there when
        // we start creating our Trip Planner.
        if (zoomFlowable == null) {
            return null
        }
        val stopSelectionState = stopSelectionState
        val mapState = getZoomState(zoomFlowable)
        return stopSelectionState?.mergeWith(mapState)
                ?.scan(StopRequestAccumulator(null, null, emptyList()))  // initial val
                label@{ stopRequestAccumulator, eitherStopState ->  // scan lambda
                    when (eitherStopState) {
                        is FullStopSelectionState -> {
                            return@label changeStopSelectionState(
                                    stopRequestAccumulator,
                                    eitherStopState
                            )
                        }
                        is MapState -> {
                            return@label changeMapState(
                                    stopRequestAccumulator,
                                    eitherStopState
                            )
                        }
                    }
                }
                ?.flatMapIterable(StopRequestAccumulator::stopsToChange)
    }

    /**
     * Simplified zoom state
     * @param zoomFlowable the original map state flowable
     * @return the zoom flowable
     */
    private fun getZoomState(zoomFlowable: Flowable<Float>): Flowable<EitherStopState>? {
        return zoomFlowable
                .map { zoomLevel: Float -> zoomLevel >= zoomThreshold }
                .map(::MapState)
    }

    /**
     * Creates a [Flowable] of a selection
     * @return the stop selection state
     */
    @Suppress("UNCHECKED_CAST")
    @get:SuppressLint("UseSparseArrays")
    private val stopSelectionState: Flowable<EitherStopState>?
        get() = patternSelections
                ?.flatMap { patternSelection: PatternSelection ->
                    Flowable.fromIterable(patternSelection.patterns)
                            .flatMapIterable { obj: Ptr -> obj.getPt() }
                            .filter { pt: Pt -> 'S' == pt.typ }
                            .toList()
                            .map { pts: List<Pt>? ->
                                StopSelection(pts.orEmpty(),
                                        patternSelection.routeNumber,
                                        patternSelection.isSelected)
                            }
                            .toFlowable()
                }?.scan(FullStopSelectionState(emptyMap()), { accumulator: EitherStopState, routeStops: StopSelection ->
                    val current: MutableMap<Int, StopRenderState> = HashMap<Int, StopRenderState>((accumulator as FullStopSelectionState).stopRenderStateMap as Map<Int, StopRenderState>?)
                    for (pt in routeStops.stopPts) {
                        val stpid = pt.stpid
                        if (!current.containsKey(stpid)) {
                            current[stpid] = StopRenderState(pt, 1)
                        } else {
                            var refcount = current[stpid]!!.routeCount
                            if (routeStops.isSelected) {
                                current[stpid] = StopRenderState(pt, ++refcount)
                            } else {
                                current[stpid] = StopRenderState(pt, --refcount)
                            }
                        }
                    }
                    FullStopSelectionState(current)
                })

    companion object {
        private fun createPatternSelection(service: PatApiService,
                                           selectionFlowable: Flowable<Route>?): Flowable<PatternSelection>? {
            if (selectionFlowable == null) {
                Timber.i("Selection flowable is null. Returning null.")
                return null
            }
            return selectionFlowable
                    .flatMap { route: Route ->
                        Timber.d("Getting patternSelections: %s", route.route)
                        return@flatMap route.route?.let { rt ->
                            service.getPatterns(rt)
                                    .map { patterns: List<Ptr?>? ->
                                        PatternSelection(
                                                patterns,
                                                route.isSelected,
                                                rt,
                                                route.routeColor)
                                    }
                        }
                    }
                    .retry()
                    .share()
        }

        /**
         *
         * Handles full state selection changes such that a diff of the previous
         * and new selection is created so that stops will appear/disappear based on selection.
         * @param previousAccumulator the previous accumulator
         * @param fullStopSelectionState the new selection state
         * @return a DTO for storing the most recent
         */
        private fun changeStopSelectionState(
                previousAccumulator: StopRequestAccumulator,
                fullStopSelectionState: FullStopSelectionState
        ): StopRequestAccumulator {
            val stopsToChange: MutableList<StopRenderRequest> = ArrayList()
            if (previousAccumulator.mapState == null || !previousAccumulator.mapState.shouldStopsBeVisible) {
                Timber.d("Not changing stop selection state")
                return StopRequestAccumulator(
                        fullStopSelectionState,
                        previousAccumulator.mapState,
                        stopsToChange)
            }
            Timber.d("Changing stop selection state")
            val diff = previousAccumulator.fullStopSelectionState?.let { previous ->
                fullStopSelectionState.stopRenderStateMap.values - previous.stopRenderStateMap.values
            }
            for (stopRenderState in diff.orEmpty()) {
                when {
                    stopRenderState.routeCount <= 0 -> {
                        if (stopRenderState.routeCount < 0) {
                            Timber.v("Bug in app: Stop routeCount should never be < 0 for %d: %d", stopRenderState.stopPt.stpid, stopRenderState.routeCount)
                        }
                        stopsToChange.add(StopRenderRequest(stopRenderState.stopPt, false))
                    }
                    previousAccumulator.mapState.shouldStopsBeVisible -> {
                        stopsToChange.add(StopRenderRequest(stopRenderState.stopPt, true))
                    }
                    else -> Unit
                }
            }
            return previousAccumulator.copy(fullStopSelectionState = fullStopSelectionState, stopsToChange = stopsToChange)
        }

        /**
         *
         * Handles map state changes such that this will give a list of stops to appear and disappear
         * after changing the map's camera comparing the:
         *
         *  * most recent full selection state
         *  * previous map state (won't emit anything map state stays the same)
         *  * new map state
         *
         * @param previousAccumulator the previous accumulator
         * @param mapState the new map state
         * @return a DTO that stores the new map state, current selection state, and new stop visibility states
         */
        private fun changeMapState(
                previousAccumulator: StopRequestAccumulator,
                mapState: MapState
        ): StopRequestAccumulator {
            val stopsToChange: MutableList<StopRenderRequest> = ArrayList()
            if (previousAccumulator.fullStopSelectionState?.stopRenderStateMap == null ||
                    previousAccumulator.mapState?.shouldStopsBeVisible == mapState.shouldStopsBeVisible) {
                Timber.v("Not changing stop map state")
                return StopRequestAccumulator(
                        previousAccumulator.fullStopSelectionState,
                        mapState,
                        stopsToChange
                )
            }
            Timber.v("Changing stop map state")
            for (stopRenderState in previousAccumulator.fullStopSelectionState.stopRenderStateMap.values) {
                if (stopRenderState.routeCount > 0) {
                    stopsToChange.add(StopRenderRequest(stopRenderState.stopPt, mapState.shouldStopsBeVisible))
                }
            }
            return previousAccumulator.copy(mapState = mapState, stopsToChange = stopsToChange)
        }
    }

    /**
     * Dependencies for the View Model.
     *
     * @param service             the service that retrieves data from the Port Authority API
     * @param selectionFlowable the flowable for bus route selection
     */
    init {
        patternSelections = createPatternSelection(service, selectionFlowable)
    }
}