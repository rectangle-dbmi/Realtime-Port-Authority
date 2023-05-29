package com.rectanglel.patstatic.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.*
import java.lang.reflect.Type
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Abstract class that has logic that helps with deciding to download from disk or from internet
 *
 *
 * Created by epicstar on 3/12/17.
 * @author Jeremy Jao
 */

abstract class AbstractDataManager<out T>(dataDirectory: File,
                                          private val staticData: StaticData,
                                          private val serializedType: Type,
                                          private val cacheFolderName: String) {

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
    protected fun saveAsJson(obj: Any?, file: File) {
        if (obj == null) {
            return
        }
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
    protected fun getFromDisk(file: File): T {
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
