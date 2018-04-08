package com.rectanglel.patstatic.patterns

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.rectanglel.patstatic.model.RetrofitPatApi
import com.rectanglel.patstatic.model.StaticData
import com.rectanglel.patstatic.patterns.response.Ptr
import com.rectanglel.patstatic.wrappers.WifiChecker
import rx.Observable
import rx.exceptions.Exceptions
import java.io.*
import java.util.concurrent.locks.ReentrantReadWriteLock

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
class PatternDataManager(dataDirectory : File,
                                 private val patApiClient: RetrofitPatApi,
                                 val staticData: StaticData,
                                 private val wifiChecker: WifiChecker) {

    private val cacheFolderName = "lineinfo"
    private val serializedType = dataType


    fun getPatterns(rt: String) : Observable<List<Ptr>> {
        val polylineFile = getPatternsFile(rt)
        // 1. if doesn't exist, get from internet
        // 2. if off wifi, always get from disk
        // 3. if age is past 24 hours and there is wifi, retrieve from the internet
        // 4. otherwise,
        if (!polylineFile.exists()) {
            return getPatternsFromInternet(rt)
        }
        if (!wifiChecker.isOnWifi()) {
            return getPatternsFromDisk(rt)
        }
        val polylineAge = polylineFile.lastModified()
        val getNowDate = System.currentTimeMillis()
        return if (getNowDate - polylineAge >= maxFileAge) {
            getPatternsFromInternet(rt)
        } else {
            getPatternsFromInternet(rt)
        }
    }

    private fun getPatternsFile(rt: String) : File = File(dataDirectory, "$rt.json")

    internal fun getPatternsFromDisk(rt: String) : Observable<List<Ptr>> {
        return Observable.just(getPatternsFile(rt))
                .map { file ->
                    try {
                        getFromDisk(file)
                    } catch (e: IOException) {
                        throw Exceptions.propagate(e)
                    }
                }
    }

    internal fun getPatternsFromInternet(rt: String) : Observable<List<Ptr>> {
        return patApiClient.getPatterns(rt)
                .map { response -> response.patternResponse }
                .map { bustimePatternResponse ->
                    try {
                        val patterns = bustimePatternResponse.ptr
                        val patternsFile = getPatternsFile(rt)
                        saveAsJson(patterns, patternsFile)
                        patterns
                    } catch(e: IOException) {
                        throw Exceptions.propagate(e)
                    }
                }
                .onErrorResumeNext { getPatternsFromDisk(rt) }
    }

    private val rwl: ReentrantReadWriteLock = ReentrantReadWriteLock()
    protected val dataDirectory: File = File(dataDirectory, cacheFolderName)
    private val readLock: ReentrantReadWriteLock.ReadLock
        get() = rwl.readLock()

    private val writeLock: ReentrantReadWriteLock.WriteLock
        get() = rwl.writeLock()

    protected val gson: Gson
        get() = GsonBuilder().create()

    init {
        this.dataDirectory.mkdirs()
    }

    /**
     * Save the object to disk as JSON into the file
     * @param obj the object to save
     * @param file the file to save to
     * @throws IOException if the serialization fails
     */
    @Throws(IOException::class)
    protected fun saveAsJson(obj: Any, file: File) {
        writeLock.lock()
        val fileWriter = FileWriter(file)
        val writer = JsonWriter(fileWriter)
        try {
            val gson = gson
            gson.toJson(obj, serializedType, writer)
        } finally {
            writer.flush()
            fileWriter.close()
            writer.close()
            writeLock.unlock()
        }
    }

    @Throws(IOException::class)
    protected fun getFromDisk(file: File): List<Ptr> {
        // file exists... get from disk
        if (file.exists()) {
            readLock.lock()
            val fileReader = FileReader(file)
            val jsonReader = JsonReader(fileReader)
            try {
                return gson.fromJson(jsonReader, serializedType)
            } finally {
                fileReader.close()
                jsonReader.close()
                readLock.unlock()
            }
        }
        // if file doesn't exist, save bundled file from installation to disk
        val fileName = String.format("%s/%s", cacheFolderName, file.name)
        copyStreamToFile(staticData.getInputStreamForFileName(fileName), file)
        return getFromDisk(file)
    }

    @Throws(IOException::class)
    protected fun copyStreamToFile(reader: InputStreamReader, file: File) {
        writeLock.lock()


        try {
            reader.use { input ->
                val fileWriter = FileWriter(file)
                fileWriter.use {
                    input.copyTo(it)
                }
            }
        } finally {
            writeLock.unlock()
        }

    }

}