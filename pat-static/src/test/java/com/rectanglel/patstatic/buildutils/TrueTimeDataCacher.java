package com.rectanglel.patstatic.buildutils;

import com.rectanglel.patstatic.model.PatApiService;
import com.rectanglel.patstatic.model.PatApiServiceImpl;
import com.rectanglel.patstatic.routes.BusRoute;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Cache all routes to a folder
 * <p>
 * One thing I need to watch out for is that the
 * Created by epicstar on 3/5/17.
 * @author Jeremy Jao
 */

public class TrueTimeDataCacher {

    private PatApiService patApiService;
    private File cacheDirectory;

    public TrueTimeDataCacher(String baseUrl, String apiKey, File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        patApiService = new PatApiServiceImpl(baseUrl, apiKey, this.cacheDirectory, new StubStaticData(), new StubWifiDataChecker());
    }

    /**
     * Cache routes if not present.
     */
    public void cacheAllRoutes() {
        patApiService.getRoutes()
                .toObservable()
                .flatMap(routes ->
                        io.reactivex.Observable.fromIterable(routes)
                                .skipWhile(route -> {
                                    String routeNumber = route.getRouteNumber();
                                    return new File(cacheDirectory, String.format("lineinfo/%s.json", routeNumber)).exists();
                                })
                                .zipWith(io.reactivex.Observable.interval(0, 500, TimeUnit.MILLISECONDS), (stuff, aLong) -> stuff)
                )
                .buffer(8)
                .zipWith(io.reactivex.Observable.interval(0, 5, TimeUnit.SECONDS), (stuff, aLong) -> stuff)
                .flatMapIterable(routes1 -> routes1)
                .map(BusRoute::getRouteNumber)
                .flatMap(routeNumber -> patApiService
                        .getPatterns(routeNumber)
                        .onErrorResumeNext(io.reactivex.Observable.empty())
                        .doOnNext((pattern) -> System.out.println(String.format("Saving route number: %s", routeNumber)))
                );
    }

    public static void main(String[] args) {
        String baseUrl = args[0];
        String patApiKey = args[1];
        File cacheDirectory = new File(args[2]);
        TrueTimeDataCacher cacher = new TrueTimeDataCacher(baseUrl, patApiKey, cacheDirectory);
        cacher.cacheAllRoutes();
    }
}
