package rectangledbmi.com.pittsburghrealtimetracker.polylines;

import android.annotation.SuppressLint;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rectangledbmi.com.pittsburghrealtimetracker.model.PatApiService;
import rectangledbmi.com.pittsburghrealtimetracker.stops.StopRenderInfo;
import rectangledbmi.com.pittsburghrealtimetracker.stops.StopSelection;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Pt;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Ptr;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;

/**
 * <p>Contains the logic of the {@link PolylineView} for pre-processing the patternSelections.</p>
 * <p>Created by epicstar on 7/18/16.</p>
 *
 * @author Jeremy Jao and Michael Antonacci
 * @since 77
 */
public class PolylineViewModel {

    private final Observable<PatternSelection> patternSelectionModelObservable;
    private final Observable<Float> zoomObservable;

    private final float zoomThreshold = 15.0f;


    /**
     * Dependencies for the View Model.
     *
     * @param service             the service that retrieves data from the Port Authority API
     * @param selectionObservable the observable for bus route selection
     */
    public PolylineViewModel(PatApiService service,
                             Observable<Route> selectionObservable,
                             Observable<Float> zoomObservable) {
        patternSelectionModelObservable = createPatternSelection(service, selectionObservable);
        this.zoomObservable = zoomObservable;
    }

    private Observable<PatternSelection> createPatternSelection(PatApiService service, Observable<Route> selectionObservable) {
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
     * <p>Gets an observable so the {@link PolylineView} can listen to actions to create, show,
     * or make a {@link com.google.android.gms.maps.model.Polyline} disappear.</p>
     * <p>{@link PatternSelection#getPatterns()} will be `null` if {@link PatternSelection#isSelected()}
     * is `false`. Otherwise, it will be not be `null` unless the polyline data was unable to </p>
     *
     * @return an observable for patternSelections
     */
    public Observable<PatternSelection> patternSelections() {
        return patternSelectionModelObservable
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

    public Observable<StopRenderInfo> stopRenderInfos() {
        Observable<List<StopRenderInfo>> stopRenderList = patternSelectionModelObservable
                .concatMap(patternSelection -> Observable.from(patternSelection.getPatterns())
                        .flatMapIterable(Ptr::getPt)
                        .filter(pt -> 'S' == pt.getTyp())
                        .toList()
                        .map(pts -> StopSelection.create(pts,
                                patternSelection.getRouteNumber(),
                                patternSelection.isSelected()))
                ).map(new Func1<StopSelection, List<StopRenderInfo>>() {

                    @SuppressLint("UseSparseArrays")
                    private HashMap<Integer, Set<String>> stopRouteAssociation = new HashMap<>();
                    private List<StopRenderInfo> stopRenderInfos = new ArrayList<>();

                    @Override
                    public List<StopRenderInfo> call(StopSelection stopSelectionModel) {
                        Timber.d("changing stop render info because of list click");
                        boolean isSelected = stopSelectionModel.isSelected();
                        String routeNumber = stopSelectionModel.getRouteNumber();
                        for (Pt stopInfo : stopSelectionModel.getStopInfoCollection()) {
                            Set<String> routesForStop = stopRouteAssociation.get(stopInfo.getStpid());
                            if (routesForStop == null) {
                                routesForStop = new HashSet<>();
                                stopRouteAssociation.put(stopInfo.getStpid(), routesForStop);
                            }
                            if (isSelected) {
                                routesForStop.add(routeNumber);
                                if (routesForStop.size() == 1) {
                                    stopRenderInfos.add(StopRenderInfo.create(stopInfo, true));
                                }
                            } else {
                                routesForStop.remove(routeNumber);
                                if (routesForStop.isEmpty()) {
                                    stopRenderInfos.add(StopRenderInfo.create(stopInfo, false));
                                }
                            }
                        }
                        return stopRenderInfos;
                    }
                });

        return stopRenderList.join(zoomObservable,
                (stopRenderInfos -> Observable.never()),
                (zoomLevel) -> Observable.never(),
                new Func2<List<StopRenderInfo>, Float, Collection<StopRenderInfo>>() {
                    private List<StopRenderInfo>  oldRenderInfo = new ArrayList<>();
                    private Float oldZoomInfo = 18f;

                    @Override
                    public Collection<StopRenderInfo> call(List<StopRenderInfo> stopRenderInfos, Float aFloat) {
                        Timber.d("determining stop render state");
                        List<StopRenderInfo> toRender = new ArrayList<>();
                        // if zoom changes from invisible to visible threshold, show all visible stopRenderInfos
                        // if zoom changes from visible to invisible threshold, unshow all stopRenderInfos
                        // if stop render object changes...
                        // * get the differences
                        // * if zoom is in visible threshold
                        //   * unshow all invisible stopRenderInfos
                        //   * show all visible stopRenderInfos
                        // * if zoom is in invisible threshold
                        //   * unshow all visible stopRenderInfos
                        if (!isZoomVisible(oldZoomInfo) && isZoomVisible(aFloat)) {
                            for (StopRenderInfo info : stopRenderInfos) {
                                if (info.isVisible()) {
                                    toRender.add(info);
                                }

                            }
                        } else if (isZoomVisible(oldZoomInfo) && !isZoomVisible(aFloat)) {
                            for (StopRenderInfo info : stopRenderInfos) {
                                if (info.isVisible()) {
                                    toRender.add(StopRenderInfo.create(info.getStopInfo(), false));
                                }
                            }
                        } else {
                            for (StopRenderInfo info : stopRenderInfos) {
                                if (!info.isVisible() || (info.isVisible() && isZoomVisible(aFloat))) {
                                    toRender.add(info);
                                }
                            }
                        }
                        oldZoomInfo = aFloat;
                        oldRenderInfo = stopRenderInfos;
                        return toRender;
                    }

                    private boolean isZoomVisible(Float zoomLevel) {
                        return zoomLevel >= zoomThreshold;
                    }

                    private Set<StopRenderInfo> getDifferences(List<StopRenderInfo> stopRenderInfos) {
                        Set<StopRenderInfo> differences = new HashSet<>(oldRenderInfo);
                        differences.removeAll(stopRenderInfos);
                        return differences;

                    }
                })
                .onBackpressureBuffer()
                .concatMap(Observable::from);
    }
}
