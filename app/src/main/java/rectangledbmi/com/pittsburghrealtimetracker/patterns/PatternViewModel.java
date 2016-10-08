package rectangledbmi.com.pittsburghrealtimetracker.patterns;

import android.annotation.SuppressLint;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

import rectangledbmi.com.pittsburghrealtimetracker.model.PatApiService;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.polylines.PatternSelection;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.StopRenderRequest;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.stops.StopSelection;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Pt;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Ptr;
import rx.Observable;
import timber.log.Timber;

/**
 * <p>Contains the logic of the {@link rectangledbmi.com.pittsburghrealtimetracker.patterns.polylines.PolylineView} for pre-processing the patternSelections.</p>
 * <p>Created by epicstar on 7/18/16.</p>
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 * @since 77
 */
public class PatternViewModel {

    private final Observable<PatternSelection> patternSelections;
    private final Observable<Float> currentZoom;

    private final float zoomThreshold = 15.0f;


    /**
     * Dependencies for the View Model.
     *
     * @param service             the service that retrieves data from the Port Authority API
     * @param selectionObservable the observable for bus route selection
     */
    public PatternViewModel(PatApiService service,
                            Observable<Route> selectionObservable,
                            Observable<Float> currentZoom) {
        patternSelections = createPatternSelection(service, selectionObservable);
        this.currentZoom = currentZoom;
    }

    private Observable<PatternSelection> createPatternSelection(PatApiService service,
                                                                Observable<Route> selectionObservable) {
        if (selectionObservable == null) {
            Timber.i("Selection observable is null. Returning null.");
            return null;
        }
        return selectionObservable
                .flatMap(route -> {
                    Timber.d("Getting patternSelections");
                    return service.getPatterns(route.getRoute())
                            .map(patterns ->
                                    new PatternSelection(
                                            patterns,
                                            route.isSelected(),
                                            route.getRoute(),
                                            route.getRouteColor()));

                }).share();
    }

    /**
     * <p>Gets an observable so the {@link rectangledbmi.com.pittsburghrealtimetracker.patterns.polylines.PolylineView} can listen to actions to create, show,
     * or make a {@link com.google.android.gms.maps.model.Polyline} disappear.</p>
     * <p>{@link PatternSelection#getPatterns()} will be `null` if {@link PatternSelection#isSelected()}
     * is `false`. Otherwise, it will be not be `null` unless the polyline data was unable to </p>
     *
     * @return an observable for patternSelections
     */
    public Observable<PatternSelection> getPatternSelections() {
        return patternSelections
                .flatMap(patternSelection -> {
                    if (patternSelection.getPatterns() == null) {
                        return Observable.just(patternSelection);
                    }
                    return Observable
                            .from(patternSelection.getPatterns())
                            .flatMap(patterns -> Observable
                                    .from(patterns.getPt())
                                    .map(pt -> new LatLng(pt.getLat(), pt.getLon()))
                                    .toList()
                            ).toList()
                            .map(latLngs -> {
                                patternSelection.setLatLngs(latLngs);
                                return patternSelection;
                            });
                });
    }

    @SuppressLint("UseSparseArrays")
    public Observable<StopRenderRequest> getStopRenderRequests() {
        return patternSelections
                .flatMap(patternSelection -> Observable.from(patternSelection.getPatterns())
                        .flatMapIterable(Ptr::getPt)
                        .filter(pt -> 'S' == pt.getTyp())
                        .toList()
                        .map(pts -> StopSelection.create(pts,
                                patternSelection.getRouteNumber(),
                                patternSelection.isSelected()))
                ).scan(new HashMap<Integer, StopRenderRequest>(200), (current, routeStops) -> {
                    for (Pt pt : routeStops.getStopPts()) {
                        Integer stpid = pt.getStpid();
                        if (!current.containsKey(stpid)) {
                            current.put(stpid,
                                        StopRenderRequest.create(pt, 1));
                        } else {
                            int refcount = current.get(stpid).routeCount();
                            if (routeStops.isSelected()) {
                                current.put(stpid,
                                        StopRenderRequest.create(pt, ++refcount));
                            }
                            else{
                                current.put(stpid,
                                        StopRenderRequest.create(pt, --refcount));
                            }
                        }
                    }
                    return current;
                })
                .flatMapIterable(HashMap::values)
                .onBackpressureBuffer();
    }
}
