package com.rectanglel.patstatic

import com.rectanglel.patstatic.routes.*
import com.vividsolutions.jts.geom.MultiLineString
import org.apache.commons.io.FileUtils
import org.geotools.data.DataStoreFinder
import org.opengis.filter.Filter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import java.util.zip.ZipInputStream

/**
 *
 * Created by epicstar on 1/15/17.
 */
object ShapeFileJsonConverter {


    fun convert(shpFileString: String) : File? {
        return convert(File(shpFileString))
    }

    fun convert(folder: File): File? {
        val map = HashMap<String, Any>()

        val shpFile = folder.listFiles({ file, string -> ".shp" == string.substring(string.length - 4, string.length) })
                .first()
        map.put("url", shpFile.toURI().toURL())

        val dataStore = DataStoreFinder.getDataStore(map)
        Filter.INCLUDE
        val collection = dataStore.getFeatureSource(dataStore.typeNames[0])
        collection.features.features().use { // special kotlin try-with-resources... amazing!
            var numberOfRoutes = 0
            while (it.hasNext()) {
                val feature = it.next()
                numberOfRoutes += 1
                feature.value
                val weeklyRiders = feature.getAverageWeeklyRiders()
                val routeNumber = feature.getRouteNumber()
                val routeName = feature.getRouteName()
                val routeType = feature.getRouteType()
                val hasWeekendService = feature.hasWeekendService()
                val geometry = feature.defaultGeometry as MultiLineString
                var i = 0
                for (coordinate in geometry.coordinates) {
                    i += 1
                }
                println("Number of points for $routeNumber: $i")
            }
            println()
            println("Number of routes: $numberOfRoutes")
        }
        return null
    }

    /**
     * Downloads a file from the internet.
     * @param urlString
     *   the URL of the file
     * @param outPath
     *   the output file
     * @param timeoutMs
     *    the timeout... default is 10 seconds or 10000 ms
     */
    fun downloadFile(urlString: String, outPath: String, timeoutMs: Int = 10 * 1000): File {
        val url = URL(urlString)
        val outFile = File(outPath)
        FileUtils.copyURLToFile(url, outFile, timeoutMs, timeoutMs)
        return outFile
    }

    /**
     * Unzips a zip file from
     * @param inputFile
     *   the file to unzip
     * @param outPath
     *   the path to unzip to
     * @return the [outPath] as a file
     */
    fun unzip(inputFile: File, outPath: String) : File {
        val buffer = ByteArray(1024)
        val outFile = File(outPath)
        outFile.mkdirs()
        val zin = ZipInputStream(FileInputStream(inputFile))
        var entry = zin.nextEntry

        while (entry != null) {
            val newFile = File(outFile, entry.name)
            File(newFile.parent).mkdirs()

            val fos = FileOutputStream(newFile)
            var len = zin.read(buffer)
            while (len > 0) {
                fos.write(buffer, 0, len)
                len = zin.read(buffer)
            }
            fos.close()
            entry = zin.nextEntry
        }
        zin.closeEntry()
        zin.close()
        return outFile
    }
}
