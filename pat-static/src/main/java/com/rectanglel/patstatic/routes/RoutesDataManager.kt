package com.rectanglel.patstatic.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.rectanglel.patstatic.model.RetrofitPatApi
import com.rectanglel.patstatic.model.StaticData
import com.rectanglel.patstatic.routes.response.BusRouteResponse
import com.rectanglel.patstatic.routes.response.BusTimeRoutesResponse

import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.locks.ReentrantReadWriteLock

import io.reactivex.Single
import io.reactivex.exceptions.Exceptions

/**
 * A data mananger for getting a full list of routes that will handle:
 *
 *  * getting data from disk
 *  * or....getting data from retrofit and saving it disk
 *  * cache clear logic(not done yet)
 *
 *
 *
 * Created by epicstar on 3/5/17.
 * @author Jeremy Jao
 */
class RoutesDataManager(dataDirectory: File, private val patApiClient: RetrofitPatApi, private val staticData: StaticData) {

    private val routesDirectory: File

    private val rwl: ReentrantReadWriteLock

    private val routesFile: File
        get() = File(routesDirectory, routeFileName)

    val routes: Single<List<BusRoute>>
        get() {
            val routesFile = routesFile
            return if (routesFile.exists()) {
                routesFromDisk
            } else {
                routesFromInternet
            }
        }

    internal val routesFromDisk: Single<List<BusRoute>>
        get() = Single.just(GsonBuilder().create())
                .map { gson ->
                    rwl.readLock().lock()
                    try {
                        return@map gson.fromJson<List<BusRoute>>(JsonReader(FileReader(routesFile)), serializationType)
                    } catch (e: FileNotFoundException) {
                        throw Exceptions.propagate(e)
                    } finally {
                        rwl.readLock().unlock()
                    }
                }

    private val routesFromInternet: Single<List<BusRoute>>
        get() = patApiClient.routes
                .map<BusTimeRoutesResponse>(BusRouteResponse::busTimeRoutesResponse)
                .map { busTimeRoutesResponse ->
                    val busRoutes = busTimeRoutesResponse.routes
                    rwl.writeLock().lock()
                    try {
                        val writer = JsonWriter(FileWriter(routesFile))
                        val gson = GsonBuilder().create()
                        gson.toJson(busRoutes, serializationType, writer)
                        writer.flush()
                        writer.close()
                    } catch (e: IOException) {
                        Exceptions.propagate(e)
                    } finally {
                        rwl.writeLock().unlock()
                    }
                    busRoutes
                }

    init {
        this.routesDirectory = File(dataDirectory, routesLocation)

        routesDirectory.mkdirs()
        rwl = ReentrantReadWriteLock()
    }

    companion object {

        private const val routesLocation = "/routeinfo"
        private const val routeFileName = "routes.json"
        private val serializationType = object : TypeToken<List<BusRoute>>() {

        }.type
    }


}
