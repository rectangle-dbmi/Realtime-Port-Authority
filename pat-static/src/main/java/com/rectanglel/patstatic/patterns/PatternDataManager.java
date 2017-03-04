package com.rectanglel.patstatic.patterns;

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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.rectanglel.patstatic.patterns.response.Ptr;
import com.rectanglel.patstatic.model.RetrofitPatApi;
import com.rectanglel.patstatic.patterns.response.PatternResponse;
import rx.Observable;
import rx.exceptions.Exceptions;

/**
 * <p>Create a data manager for patternSelections that will handle:</p>
 * <ul>
 *     <li>getting data from disk</li>
 *     <li>or... getting from retrofit and saving that data to disk</li>
 *     <li>cache clear logic (not done yet)</li>
 * </ul>
 * <p>Created by epicstar on 9/18/16.</p>
 * @author Jeremy Jao
 * @since 78
 */
// TODO: fix the last thing in the list
public class PatternDataManager {

    private final static String patternsLocation = "/lineinfo";
    private final File patternsDirectory;
    private final RetrofitPatApi patApiClient;
    private final static Type serializationType = new TypeToken<List<Ptr>>() {}.getType();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public PatternDataManager(File dataDirectory, RetrofitPatApi patApiClient) {
        patternsDirectory = new File(dataDirectory, patternsLocation);
        //noinspection ResultOfMethodCallIgnored
        patternsDirectory.mkdirs();
        this.patApiClient = patApiClient;
    }

    public Observable<List<Ptr>> getPatterns(String rt) {
        File polylineFile = getPatternsFile(rt);
        if (polylineFile.exists() && polylineFile.canWrite()) {
            return getPatternsFromDisk(rt);
        } else {
            return getPatternsFromInternet(rt);
        }
    }

    // region helper methods

    /**
     * Gets the Polyline information from the disk as an observable
     *
     * @param rt the route that has the polyline info
     * @return an Observable for the Polyline data transfer object
     */
    Observable<List<Ptr>> getPatternsFromDisk(String rt) {
        return Observable.just(new GsonBuilder().create())
                .map(gson -> {
                    try {
                        rwl.readLock().lock();
                        try {
                            //noinspection UnnecessaryLocalVariable -> Android Studio thinks this is an error
                            List<Ptr> ptrs = gson.fromJson(new JsonReader(new FileReader(getPatternsFile(rt))), serializationType);
                            return ptrs;
                        } finally {
                            rwl.readLock().unlock();
                        }
                    } catch (FileNotFoundException e) {
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
    Observable<List<Ptr>> getPatternsFromInternet(String rt) {
        return patApiClient.getPatterns(rt)
                .map(PatternResponse::getPatternResponse)
                .map(bustimePatternResponse -> {
                    List<Ptr> patterns = bustimePatternResponse.getPtr();
                    try {
                        rwl.writeLock().lock();
                        try {
                            JsonWriter writer = new JsonWriter(new FileWriter(getPatternsFile(rt)));
                            Gson gson = new GsonBuilder().create();
                            gson.toJson(
                                    patterns,
                                    serializationType,
                                    writer
                            );
                            // need to flush and close otherwise the file is incomplete and bugs
                            writer.flush();
                            writer.close();
                        } finally {
                            rwl.writeLock().unlock();
                        }
                        return patterns;
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }

                });
    }

    private File getPatternsFile(String rt) {
        return new File(patternsDirectory,
                String.format("%s.json", rt));
    }
    // endregion
}
