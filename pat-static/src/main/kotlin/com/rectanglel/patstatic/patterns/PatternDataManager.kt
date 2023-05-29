package com.rectanglel.patstatic.patterns

import com.google.gson.reflect.TypeToken
import com.rectanglel.patstatic.model.AbstractDataManager
import com.rectanglel.patstatic.model.RetrofitPatApi
import com.rectanglel.patstatic.model.StaticData
import com.rectanglel.patstatic.patterns.response.PatternResponse
import com.rectanglel.patstatic.patterns.response.Ptr
import com.rectanglel.patstatic.routes.BusRoute
import com.rectanglel.patstatic.wrappers.WifiChecker
import io.reactivex.Flowable
import io.reactivex.exceptions.Exceptions
import java.io.File
import java.io.IOException

private val dataType = object : TypeToken<List<Ptr>>() {}.type!!
private const val maxFileAge = 24 * 60 * 1000 * 1000 // 24 hours

/**
 * Create a data manager for pattern selections that will handle:
 *
 * 1. getting data from:
 *     1. disk
 *     2. [RetrofitPatApi] and saving that data to disk
 * 2. cache clear logic
 *
 * Created by epicstar on 8/19/17.
 * @author Jeremy Jao
 */
class PatternDataManager(dataDirectory: File,
                         private val patApiClient: RetrofitPatApi,
                         staticData: StaticData,
                         private val wifiChecker: WifiChecker)
    : AbstractDataManager<List<Ptr>>(
        dataDirectory,
        staticData,
        dataType,
        "lineinfo") {

    fun getPatterns(route: BusRoute): Flowable<List<Ptr>> {
        val polylineFile = getPatternsFile(route.number)
        // 1. if doesn't exist, get from internet (merely a fallback to when a pattern isn't yet saved to disk)
        // 2. if off wifi, always get from disk
        // 3. if age is past 24 hours and there is wifi, retrieve from the internet
        // 4. otherwise,
        if (!polylineFile.exists()) {
            return getPatternsFromInternet(route)
        }
        if (!wifiChecker.isWifiOn()) {
            return getPatternsFromDisk(route.number)
        }
        val polylineAge = polylineFile.lastModified()
        val getNowDate = System.currentTimeMillis()
        return if (getNowDate - polylineAge >= maxFileAge) {
            getPatternsFromInternet(route)
        } else {
            getPatternsFromDisk(route.number)
                .concatMap { ptrList ->
                    when (ptrList?.size) {
                        0 -> getPatternsFromInternet(route)
                        else -> Flowable.just(ptrList)
                    }
                }
        }
    }

    private fun getPatternsFile(rt: String): File = File(dataDirectory, "$rt.json")

    internal fun getPatternsFromDisk(rt: String): Flowable<List<Ptr>> {
        return Flowable.just(getPatternsFile(rt))
                .map { file ->
                    try {
                        getFromDisk(file)
                    } catch (e: IOException) {
                        throw Exceptions.propagate(e)
                    }
                }
    }

    // TODO: remove this warning suppression when ViewModel has fixed
    @Suppress("RedundantVisibilityModifier")
    public fun getPatternsFromInternet(route: BusRoute): Flowable<List<Ptr>> {
        println("whats updog -> $route")
        return patApiClient.getPatterns(route.number, route.datafeed)
                .map(PatternResponse::patternResponse)
                .map { bustimePatternResponse ->
                    try {
                        println("oh hello!")
                        val patterns = bustimePatternResponse.ptr // TODO: FIX
                        val patternsFile = getPatternsFile(route.number)
                        saveAsJson(patterns, patternsFile)
                        patterns
                    } catch (e: IOException) {
                        println("OH NOOO")
                        throw Exceptions.propagate(e)
                    }
                }
                .onErrorResumeNext { _: Throwable -> getPatternsFromDisk(route.number) }
//                .onErrorResumeNext(t -> return getPatternsFromDisk(rt))
    }


}