package com.rectanglel.patstatic.buildutils

import com.rectanglel.patstatic.model.PatApiService
import com.rectanglel.patstatic.model.PatApiServiceImpl
import com.rectanglel.patstatic.routes.BusRoute
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Cache all routes to a folder
 *
 *
 * One thing I need to watch out for is that the
 * Created by epicstar on 3/5/17.
 * @author Jeremy Jao
 */
class TrueTimeDataCacher(apiKey: String, private val cacheDirectory: File) {
    private val patApiService: PatApiService

    /**
     * Cache routes if not present.
     */
    fun cacheAllRoutes() {
        patApiService.routes
                .toObservable()
                .flatMap { routes: List<BusRoute>? ->
                    Observable.fromIterable(routes)
                            .skipWhile { route: BusRoute ->
                                val routeNumber = route.routeNumber
                                File(cacheDirectory, String.format("lineinfo/%s.json", routeNumber)).exists()
                            }
                            .zipWith(Observable.interval(0, 500, TimeUnit.MILLISECONDS), BiFunction { stuff: BusRoute?, _: Long? -> stuff!! })
                }
                .buffer(8)
                .zipWith(Observable.interval(0, 5, TimeUnit.SECONDS), BiFunction { stuff: List<BusRoute?>?, _: Long? -> stuff!! })
                .flatMapIterable { routes1: List<BusRoute?>? -> routes1 }
                .map(BusRoute::routeNumber)
                .flatMap { routeNumber: String? ->
                    patApiService
                            .getPatterns(routeNumber!!)
                            .onErrorResumeNext(Flowable.empty())
                            .toObservable()
                            .doOnNext { println(String.format("Saving route number: %s", routeNumber)) }
                }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val patApiKey = args[0]
            val cacheDirectory = File(args[1])
            val cacher = TrueTimeDataCacher(patApiKey, cacheDirectory)
            cacher.cacheAllRoutes()
        }
    }

    init {
        patApiService = PatApiServiceImpl(apiKey, cacheDirectory, StubStaticData(), StubWifiDataChecker())
    }
}