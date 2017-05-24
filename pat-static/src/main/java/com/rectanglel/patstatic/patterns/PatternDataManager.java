package com.rectanglel.patstatic.patterns;

import com.google.gson.reflect.TypeToken;
import com.rectanglel.patstatic.model.AbstractDataManager;
import com.rectanglel.patstatic.model.RetrofitPatApi;
import com.rectanglel.patstatic.model.StaticData;
import com.rectanglel.patstatic.patterns.response.PatternResponse;
import com.rectanglel.patstatic.patterns.response.Ptr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.exceptions.Exceptions;

/**
 * /**
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
public class PatternDataManager extends AbstractDataManager<List<Ptr>> {

    private RetrofitPatApi patApiClient;

    public PatternDataManager(File dataDirectory, RetrofitPatApi patApiClient, StaticData staticData) {
        super(dataDirectory, staticData, new TypeToken<List<Ptr>>() {}.getType());
        this.patApiClient = patApiClient;
    }

    @Override
    protected String getCacheFolderName() {
        return "lineinfo";
    }

    private File getPatternsFile(String rt) {
        return new File(getDataDirectory(),
                String.format("%s.json", rt));
    }

    public Observable<List<Ptr>> getPatterns(String rt) {
        File polylineFile = getPatternsFile(rt);
        if (polylineFile.exists()) {
            return getPatternsFromDisk(rt);
        } else {
            return getPatternsFromInternet(rt);
        }
    }

    Observable<List<Ptr>> getPatternsFromDisk(String rt) {
        return Observable.just(getPatternsFile(rt))
                .map(file -> {
                    try {
                        return getFromDisk(file);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    Observable<List<Ptr>> getPatternsFromInternet(String rt) {
        return patApiClient.getPatterns(rt)
                .map(PatternResponse::getPatternResponse)
                .map(bustimePatternResponse -> {
                    try {
                        List<Ptr> patterns = bustimePatternResponse.getPtr();
                        File patternsFile = getPatternsFile(rt);
                        saveAsJson(patterns, patternsFile);
                        return patterns;
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }).onErrorResumeNext(throwable -> getPatternsFromDisk(rt));
    }
}
