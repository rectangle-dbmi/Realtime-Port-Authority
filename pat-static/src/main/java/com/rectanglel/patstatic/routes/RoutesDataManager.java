package com.rectanglel.patstatic.routes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.rectanglel.patstatic.model.RetrofitPatApi;
import com.rectanglel.patstatic.model.StaticData;
import com.rectanglel.patstatic.routes.response.BusRouteResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.reactivex.Single;
import io.reactivex.exceptions.Exceptions;

/**
 * A data mananger for getting a full list of routes that will handle:
 * <ul>
 *     <li>getting data from disk</li>
 *     <li>or....getting data from retrofit and saving it disk</li>
 *     <li>cache clear logic(not done yet)</li>
 * </ul>
 * <p>
 * Created by epicstar on 3/5/17.
 * @author Jeremy Jao
 */
public class RoutesDataManager {

    private final static String routesLocation = "/routeinfo";
    private final static String routeFileName = "routes.json";
    private static Type serializationType = new TypeToken<List<BusRoute>>() {}.getType();

    private final File routesDirectory;
    private final RetrofitPatApi patApiClient;
    private final StaticData staticData;

    private final ReentrantReadWriteLock rwl;

    public RoutesDataManager(File dataDirectory, RetrofitPatApi patApiClient, StaticData staticData) {
        this.routesDirectory = new File(dataDirectory, routesLocation);
        //noinspection ResultOfMethodCallIgnored
        routesDirectory.mkdirs();
        this.patApiClient = patApiClient;
        this.staticData = staticData;
        rwl = new ReentrantReadWriteLock();
    }

    private File getRoutesFile() {
        return new File(routesDirectory, routeFileName);
    }

    public Single<List<BusRoute>> getRoutes() {
        File routesFile = getRoutesFile();
        if (routesFile.exists()) {
            return getRoutesFromDisk();
        } else {
            return getRoutesFromInternet();
        }
    }

    Single<List<BusRoute>> getRoutesFromDisk() {
        return Single.just(new GsonBuilder().create())
                .map(gson -> {
                    rwl.readLock().lock();
                    try {
                        return gson.fromJson(new JsonReader(new FileReader(getRoutesFile())), serializationType);
                    } catch (FileNotFoundException e) {
                        throw Exceptions.propagate(e);
                    } finally {
                        rwl.readLock().unlock();
                    }
                });
    }

    private Single<List<BusRoute>> getRoutesFromInternet() {
        return patApiClient.getRoutes()
                .map(BusRouteResponse::getBusTimeRoutesResponse)
                .map(busTimeRoutesResponse -> {
                    List<BusRoute> busRoutes = busTimeRoutesResponse.getRoutes();
                    rwl.writeLock().lock();
                    try {
                        JsonWriter writer = new JsonWriter(new FileWriter(getRoutesFile()));
                        Gson gson = new GsonBuilder().create();
                        gson.toJson(busRoutes, serializationType, writer);
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        //noinspection ThrowableResultOfMethodCallIgnored
                        Exceptions.propagate(e);
                    } finally {
                        rwl.writeLock().unlock();
                    }
                    return busRoutes;
                });
    }


}
