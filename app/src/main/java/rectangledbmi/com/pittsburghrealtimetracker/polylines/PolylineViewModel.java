package rectangledbmi.com.pittsburghrealtimetracker.polylines;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.PATAPI;
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.PatternResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Ptr;
import rx.Observable;
import rx.exceptions.Exceptions;
import timber.log.Timber;

/**
 * <p>Contains the logic of the {@link PolylineView} for pre-processing the polylines.</p>
 * <p>Created by epicstar on 7/18/16.</p>
 * @since 76
 * @author Jeremy Jao and Michael Antonacci
 */
public class PolylineViewModel {

    /**
     * This is the folder path that determines the location of the polylines
     */
    private final static String polylineLocation = "/lineinfo";

    /**
     * the serialization type to
     */
    private final static Type serializationType = new TypeToken<List<Ptr>>() {}.getType();

    /**
     * the default data directory
     */
    private File dataDirectory;

    /**
     * The service that gets information from Port Authority's API.
     */
    private final PATAPI patapi;

    /**
     * The API key of the Port Authority API
     */
    private final String patApiKey;

    /**
     *  This is the observable that submits events to this class.
     */
    private final Observable<RouteSelection> selectionObservable;

    /**
     * Dependencies for the View Model.
     * @param patapi Port Authority API
     * @param patApiKey the API Key of the Port Authority API
     * @param selectionObservable the observable for bus route selection
     * @param dataDirectory the root data directory
     */
    public PolylineViewModel(PATAPI patapi,
                             String patApiKey,
                             Observable<RouteSelection> selectionObservable,
                             File dataDirectory) {
        this.patapi = patapi;
        this.patApiKey = patApiKey;
        this.selectionObservable = selectionObservable;
        this.dataDirectory = new File(dataDirectory, polylineLocation);
        //noinspection ResultOfMethodCallIgnored
        this.dataDirectory.mkdirs();
    }

    /**
     * <p>Gets an observable so the {@link PolylineView} can listen to actions to create, show,
     * or make a {@link com.google.android.gms.maps.model.Polyline} disappear.</p>
     * <p>{@link PatternSelection#getPatterns()} will be `null` if {@link PatternSelection#isSelected()}
     * is `false`. Otherwise, it will be not be `null` unless the polyline data was unable to </p>
     * @return an observable for polylines
     */
    public Observable<PatternSelection> getPolylineObservable() {
        if (selectionObservable == null) {
            Timber.d("Selection observable is null. Returning null.");
            return null;
        }

        return selectionObservable
                .flatMap(routeSelection -> {
                    Timber.d("Getting polyline from disk");
                    Route route = routeSelection.getToggledRoute();
                    if (!route.isSelected()) {
                        return Observable.just(new PatternSelection(false, route.getRoute()));
                    }
                    File polylineFile = new File(dataDirectory,
                            String.format("%s.json", route.getRoute()));
                    if (polylineFile.exists()) {
                        return getPolylineFromDisk(polylineFile, route);
                    } else {
                        return getPolylineFromInternet(polylineFile, route);
                    }

                });

    }

    /**
     * Gets the Polyline information from the disk as an observable
     * @param polylineFile the file that has polyline info
     * @param route the route of the polyline info
     * @return an Observable for the Polyline data transfer object
     */
    private Observable<PatternSelection> getPolylineFromDisk(File polylineFile, Route route) {
        try {
            Gson gson = new GsonBuilder().create();
            PatternSelection patternFromDisk = new PatternSelection(
                    gson.fromJson(
                            new JsonReader(new FileReader(polylineFile)),
                            serializationType),
                    route.isSelected(), route.getRoute());
            return Observable.just(patternFromDisk);
        } catch (FileNotFoundException e) {
            Timber.e(e, "File does not exist when it should.");
            return Observable.error(e);
        }
    }

    /**
     * Gets the polyline info from the internet and stores this meta-data to disk
     * @param polylineFile the file to save polyline info from the internet to the disk
     * @param route the route of the polyline info
     * @return an Observable for the polyline data transfer object
     */
    private Observable<PatternSelection> getPolylineFromInternet(File polylineFile, Route route) {
        return patapi.getPatterns(route.getRoute(), patApiKey)
                .map(PatternResponse::getPatternResponse)
                .map(bustimePatternResponse -> {
                    List<Ptr> patterns = bustimePatternResponse.getPtr();
                    try {
                        JsonWriter writer = new JsonWriter(new FileWriter(polylineFile));
                        Gson gson = new GsonBuilder().create();
                        gson.toJson(
                                patterns,
                                serializationType,
                                writer
                        );
                        // need to flush and close otherwise the file is incomplete and bugs
                        writer.flush();
                        writer.close();
                        return new PatternSelection(patterns, route.isSelected(), route.getRoute());
                    } catch (IOException e) {
                        Timber.e(e, "Could not retrieve polyline from internet when it should have.");
                        throw Exceptions.propagate(e);
                    }

                });
    }
}
