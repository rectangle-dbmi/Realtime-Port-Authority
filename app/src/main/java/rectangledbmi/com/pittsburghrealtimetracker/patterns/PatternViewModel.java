package rectangledbmi.com.pittsburghrealtimetracker.patterns;

import android.annotation.SuppressLint;

import com.google.android.gms.maps.model.LatLng;
import com.rectanglel.patstatic.model.PatApiService;
import com.rectanglel.patstatic.patterns.polylines.PolylineView;
import com.rectanglel.patstatic.patterns.response.Pt;
import com.rectanglel.patstatic.patterns.response.Ptr;
import com.rectanglel.patstatic.patterns.stops.StopView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Flowable;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.StopRenderState;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.StopSelection;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.EitherStopState;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.FullStopSelectionState;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.either.MapState;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering.StopRenderRequest;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.rendering.StopRequestAccumulator;
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route;
import timber.log.Timber;

/**
 * <p>Contains the logic of the {@link PolylineView} for pre-processing the patternSelections.</p>
 * <p>Created by epicstar on 7/18/16.</p>
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 * @since 78
 */
@SuppressWarnings("Convert2streamapi")
public class PatternViewModel {

    private final Flowable<PatternSelection> patternSelections;

    private final float zoomThreshold = 15.0f;

    /**
     * Dependencies for the View Model.
     *
     * @param service             the service that retrieves data from the Port Authority API
     * @param selectionFlowable the flowable for bus route selection
     */
    public PatternViewModel(PatApiService service,
                            Flowable<Route> selectionFlowable) {
        patternSelections = createPatternSelection(service, selectionFlowable);
    }

    private static Flowable<PatternSelection> createPatternSelection(PatApiService service,
                                                                       Flowable<Route> selectionFlowable) {
        if (selectionFlowable == null) {
            Timber.i("Selection flowable is null. Returning null.");
            return null;
        }
        return selectionFlowable
                .flatMap(route -> {
                    Timber.d("Getting patternSelections: %s", route.getRoute());
                    return service.getPatterns(route.getRoute())
                            .map(patterns ->
                                    new PatternSelection(
                                            patterns,
                                            route.isSelected(),
                                            route.getRoute(),
                                            route.getRouteColor()));

                })
                .retry()
                .share();
    }

    /**
     * <p>Gets a flowable so the {@link PolylineView} can listen to actions to create, show,
     * or make a {@link com.google.android.gms.maps.model.Polyline} disappear.</p>
     * <p>{@link PatternSelection#getPatterns()} will be `null` if {@link PatternSelection#isSelected()}
     * is `false`. Otherwise, it will be not be `null` unless the polyline data was unable to </p>
     *
     * @return a flowable for patternSelections
     */
    public Flowable<PatternSelection> getPatternSelections() {
        return patternSelections
            .flatMap(patternSelection -> {
                if (patternSelection.getPatterns() == null) {
                    return Flowable.just(patternSelection);
                }
                return Flowable
                    .fromIterable(patternSelection.getPatterns())
                    .flatMapSingle(patterns -> Flowable
                        .fromIterable(patterns.getPt())
                        .map(pt -> new LatLng(pt.getLat(), pt.getLon()))
                        .toList()
                    )
                    .toList()
                    .map(latLngs -> {
                        patternSelection.setLatLngs(latLngs);
                        return patternSelection;
                    })
                    .toFlowable();
            });
    }

    /**
     * <p>Gets a flowable so the {@link StopView} only needs
     *    to implement an {@link io.reactivex.Observer} can listen to changes to the state of visible stops.</p>
     * <p>Internally, this will store state changes to map camera and selection changes such that
     *    the only items being emitted will be whether or not a stop will show or not.</p>
     * @param zoomFlowable the zoom flowable
     * @return a flowable that emits any change to change in stop visibility
     */
    public Flowable<StopRenderRequest> getStopRenderRequests(Flowable<Float> zoomFlowable) {
        // NOTE: Will have to make our own StopViewModel and move this there when
        // we start creating our Trip Planner.
        if (zoomFlowable == null) {
            return null;
        }

        Flowable<EitherStopState<?>> stopSelectionState = getStopSelectionState();
        Flowable<EitherStopState<?>> mapState = getZoomState(zoomFlowable);
        return stopSelectionState.mergeWith(mapState)
                .scan(StopRequestAccumulator.create(null, null, Collections.emptyList()), // initial val
                        ((stopRequestAccumulator, eitherStopState) -> { // scan lambda
                            if (eitherStopState instanceof FullStopSelectionState) {
                                return changeStopSelectionState(
                                        stopRequestAccumulator,
                                        (FullStopSelectionState) eitherStopState
                                );
                            } else if (eitherStopState instanceof MapState) {
                                return changeMapState(
                                        stopRequestAccumulator,
                                        (MapState) eitherStopState
                                );
                            }
                            Timber.w("Resetting the stop state accumulator");
                            return StopRequestAccumulator.create(null, null, Collections.emptyList());
                        }))
        .flatMapIterable(StopRequestAccumulator::getStopsToChange);
    }

    /**
     * <p>Handles full state selection changes such that a diff of the previous
     *    and new selection is created so that stops will appear/disappear based on selection.</p>
     * @param previousAccumulator the previous accumulator
     * @param fullStopSelectionState the new selection state
     * @return a DTO for storing the most recent
     */
    private static StopRequestAccumulator changeStopSelectionState(
            StopRequestAccumulator previousAccumulator,
            FullStopSelectionState fullStopSelectionState
    ) {
        List<StopRenderRequest> stopsToChange = new ArrayList<>();
        if (previousAccumulator.getMapState() == null || !previousAccumulator.getMapState().value()) {
            Timber.d("Not changing stop selection state");
            return StopRequestAccumulator.create(
                    fullStopSelectionState,
                    previousAccumulator.getMapState(),
                    stopsToChange);
        }
        Timber.d("Changing stop selection state");
        Collection<StopRenderState> diff = getDiffList(
                previousAccumulator.getFullStopSelectionState().value().values(),
                fullStopSelectionState.value().values()
        );
        for (StopRenderState stopRenderState : diff) {
            if (stopRenderState.routeCount() <= 0) {
                if (stopRenderState.routeCount() < 0) {
                    Timber.v("Bug in app: Stop routeCount should never be < 0 for %d: %d", stopRenderState.getStopPt().getStpid(), stopRenderState.routeCount());
                }
                stopsToChange.add(StopRenderRequest.create(stopRenderState.getStopPt(), false));
            } else if (previousAccumulator.getMapState().value()) {
                stopsToChange.add(StopRenderRequest.create(stopRenderState.getStopPt(), true));
            }
        }
        return StopRequestAccumulator.create(
                fullStopSelectionState,
                previousAccumulator.getMapState(),
                stopsToChange);
    }

    /**
     * <p>Handles map state changes such that this will give a list of stops to appear and disappear
     *    after changing the map's camera comparing the:</p>
     * <ul>
     *     <li>most recent full selection state</li>
     *     <li>previous map state (won't emit anything map state stays the same)</li>
     *     <li>new map state</li>
     * </ul>
     * @param previousAccumulator the previous accumulator
     * @param mapState the new map state
     * @return a DTO that stores the new map state, current selection state, and new stop visibility states
     */
    private static StopRequestAccumulator changeMapState(
            StopRequestAccumulator previousAccumulator,
            MapState mapState
    ) {
        List<StopRenderRequest> stopsToChange = new ArrayList<>();
        if (previousAccumulator.getFullStopSelectionState() == null ||
                        previousAccumulator.getFullStopSelectionState().value() == null ||
                        (previousAccumulator.getMapState() != null && (previousAccumulator.getMapState().value() == mapState.value()))
        ) {
            Timber.v("Not changing stop map state");
            return StopRequestAccumulator.create(
                    previousAccumulator.getFullStopSelectionState(),
                    mapState,
                    stopsToChange
            );
        }
        Timber.v("Changing stop map state");
        for (StopRenderState stopRenderState : previousAccumulator.getFullStopSelectionState().value().values()) {
            if (stopRenderState.routeCount() > 0) {
                stopsToChange.add(StopRenderRequest.create(stopRenderState.getStopPt(), mapState.value()));
            }
        }
        return StopRequestAccumulator.create(
                previousAccumulator.getFullStopSelectionState(),
                mapState,
                stopsToChange
        );
    }

    private static <T> List<T> getDiffList(Collection<T> oldList, Collection<T> newList) {
        Set<T> set1 = new HashSet<>(oldList);
        Set<T> set2 = new HashSet<>(newList);
        set2.removeAll(set1);
        return new ArrayList<>(set2);
    }

    /**
     * Simplified zoom state
     * @param zoomFlowable the original map state flowable
     * @return the zoom flowable
     */
    private Flowable<EitherStopState<?>> getZoomState(Flowable<Float> zoomFlowable) {
        return zoomFlowable
                .map(zoomLevel -> zoomLevel >= zoomThreshold)
                .map(MapState::create);
    }

    /**
     * Creates a {@link Flowable} of a selection
     * @return the stop selection state
     */
    @SuppressLint("UseSparseArrays")
    private Flowable<EitherStopState<?>> getStopSelectionState() {
        return patternSelections
                .flatMap(patternSelection -> Flowable.fromIterable(patternSelection.getPatterns())
                        .flatMapIterable(Ptr::getPt)
                        .filter(pt -> 'S' == pt.getTyp())
                        .toList()
                        .map(pts -> StopSelection.create(pts,
                                patternSelection.getRouteNumber(),
                                patternSelection.isSelected()))
                        .toFlowable()
                ).scan(FullStopSelectionState.create(Collections.emptyMap()), (accumulator, routeStops) -> {
                    Map<Integer, StopRenderState> current = new HashMap<>((Map<Integer, StopRenderState>)(accumulator.value()));
                    for (Pt pt : routeStops.getStopPts()) {
                        Integer stpid = pt.getStpid();
                        if (!current.containsKey(stpid)) {
                            current.put(stpid,
                                    StopRenderState.create(pt, 1));
                        } else {
                            int refcount = current.get(stpid).routeCount();
                            if (routeStops.isSelected()) {
                                current.put(stpid,
                                        StopRenderState.create(pt, ++refcount));
                            }
                            else{
                                current.put(stpid,
                                        StopRenderState.create(pt, --refcount));
                            }
                        }
                    }
                    return FullStopSelectionState.create(current);
                });
    }
}
