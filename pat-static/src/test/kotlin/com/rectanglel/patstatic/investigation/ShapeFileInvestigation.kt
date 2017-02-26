package com.rectanglel.patstatic.investigation

import org.geotools.data.shapefile.ShapefileDataStore
import org.geotools.data.shapefile.ShapefileDataStoreFactory
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.net.URL

/**
 * Investigating stuff
 *
 * Created by epicstar on 12/11/16.
 * @author Jeremy Jao
 */
class ShapeFileInvestigation {

    val urlString: String = "https://data.wprdc.org/dataset/6be8185a-7fc3-48c7-9b8d-716602ea03f0/" +
            "resource/018d8051-1bec-4210-bc04-c9fcc5d9da94/download/paacroutes1611.zip"

    @Test
    fun testBlah() {
//        System.out.println("hi")
        Assert.assertTrue(ShapefileDataStoreFactory().canProcess(File("testFiles/paacroutes1661.zip").toURI().toURL()))
        val shapeFile = ShapefileDataStore(File("testFiles/paacroutes1661.zip").toURI().toURL())
        System.out.println(shapeFile.toString())
    }
}