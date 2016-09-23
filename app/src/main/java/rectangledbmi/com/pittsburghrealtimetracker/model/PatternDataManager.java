package rectangledbmi.com.pittsburghrealtimetracker.model;

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
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.PatternResponse;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Ptr;
import rx.Observable;
import rx.exceptions.Exceptions;
import timber.log.Timber;

/**
 * <p>Create a data manager for patternSelections that will handle:</p>
 * <ul>
 *     <li>getting data from disk</li>
 *     <li>or... getting from retrofit and saving that data to disk</li>
 *     <li>cache clear logic (not done yet)</li>
 * </ul>
 * <p>Created by epicstar on 9/18/16.</p>
 * @author Jeremy Jao
 * @since 77
 */
// TODO: fix the last thing in the list
public class PatternDataManager {

    private final static String polylineLocation = "/lineinfo";
    private final File polylineDirectory;
    private final PATAPI patApiClient;
    private final static Type serializationType = new TypeToken<List<Ptr>>() {}.getType();

    public PatternDataManager(File dataDirectory, PATAPI patApiClient) {
        polylineDirectory = new File(dataDirectory, polylineLocation);
        //noinspection ResultOfMethodCallIgnored
        polylineDirectory.mkdirs();
        this.patApiClient = patApiClient;
    }

    public Observable<List<Ptr>> getPatterns(String rt) {
        Timber.d("Getting patternSelections");
        File polylineFile = getPolylineFile(rt);
        if (polylineFile.exists() && polylineFile.canWrite()) {
            return getPolylineFromDisk(rt);
        } else {
            return getPolylineFromInternet(rt);
        }
    }

    // region helper methods

    /**
     * Gets the Polyline information from the disk as an observable
     *
     * @param rt the route that has the polyline info
     * @return an Observable for the Polyline data transfer object
     */
    Observable<List<Ptr>> getPolylineFromDisk(String rt) {
        return Observable.just(new GsonBuilder().create())
                .map(gson -> {
                    try {
                        Timber.d("Getting patternSelections from disk");
                        //noinspection UnnecessaryLocalVariable -> Android Studio thinks this is an error
                        List<Ptr> ptrs = gson.fromJson(new JsonReader(new FileReader(getPolylineFile(rt))), serializationType);
                        return ptrs;
                    } catch (FileNotFoundException e) {
                        Timber.e(e, "File does not exist when it should.");
                        throw Exceptions.propagate(e);
                    }
                });
    }


    /**
     * Gets the polyline info from the internet and stores this meta-data to disk
     *
     * @param rt the route number of the polyline info
     * @return an Observable for the polyline data transfer object
     */
    Observable<List<Ptr>> getPolylineFromInternet(String rt) {
        return patApiClient.getPatterns(rt)
                .map(PatternResponse::getPatternResponse)
                .map(bustimePatternResponse -> {
                    Timber.d("Getting patternSelections from internet");
                    List<Ptr> patterns = bustimePatternResponse.getPtr();
                    try {
                        JsonWriter writer = new JsonWriter(new FileWriter(getPolylineFile(rt)));
                        Gson gson = new GsonBuilder().create();
                        gson.toJson(
                                patterns,
                                serializationType,
                                writer
                        );
                        // need to flush and close otherwise the file is incomplete and bugs
                        writer.flush();
                        writer.close();
                        return patterns;
                    } catch (IOException e) {
                        Timber.e(e, "Could not retrieve polyline from internet when it should have.");
                        throw Exceptions.propagate(e);
                    }

                });
    }

    private File getPolylineFile(String rt) {
        return new File(polylineDirectory,
                String.format("%s.json", rt));
    }
    // endregion
}
