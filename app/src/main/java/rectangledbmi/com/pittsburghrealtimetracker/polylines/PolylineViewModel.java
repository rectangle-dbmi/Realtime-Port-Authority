package rectangledbmi.com.pittsburghrealtimetracker.polylines;

import com.google.android.gms.maps.model.LatLng;

import rectangledbmi.com.pittsburghrealtimetracker.model.PatApiService;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rx.Observable;
import timber.log.Timber;

/**
 * <p>Contains the logic of the {@link PolylineView} for pre-processing the polylines.</p>
 * <p>Created by epicstar on 7/18/16.</p>
 * @since 77
 * @author Jeremy Jao and Michael Antonacci
 */
public class PolylineViewModel {

    private final PatApiService service;

    /**
     *  This is the observable that submits events to this class.
     */
    private final Observable<Route> selectionObservable;

    /**
     * Dependencies for the View Model.
     * @param service the service that retrieves data from the Port Authority API
     * @param selectionObservable the observable for bus route selection
     */
    public PolylineViewModel(PatApiService service,
                             Observable<Route> selectionObservable) {
        this.service = service;
        this.selectionObservable = selectionObservable;

    }

    /**
     * <p>Gets an observable so the {@link PolylineView} can listen to actions to create, show,
     * or make a {@link com.google.android.gms.maps.model.Polyline} disappear.</p>
     * <p>{@link PatternSelectionModel#getPatterns()} will be `null` if {@link PatternSelectionModel#isSelected()}
     * is `false`. Otherwise, it will be not be `null` unless the polyline data was unable to </p>
     * @return an observable for polylines
     */
    public Observable<PatternSelectionModel> getPolylineObservable() {
        if (selectionObservable == null) {
            Timber.i("Selection observable is null. Returning null.");
            return null;
        }
        return selectionObservable
                .flatMap(route -> {
                    Timber.d("Getting polylines");
                    if (!route.isSelected()) {
                        return Observable.just(new PatternSelectionModel(false, route.getRoute()));
                    }
                    return service.getPatterns(route.getRoute())
                            .map(patterns ->
                                    new PatternSelectionModel(
                                            patterns,
                                            route.isSelected(),
                                            route.getRoute(),
                                            route.getRouteColor()));

                }).flatMap(patternSelection -> {
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
}
