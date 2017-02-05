package com.rectanglel.patstatic

import org.junit.*
import java.io.File

/**
 * General unit test classes for
 * Created by epicstar on 1/15/17.
 */
class ShapeFileJsonConverterTest {
    //region static setup
    companion object {
        val outFolder = File("testOut")
        val testURL = "https://data.wprdc.org/dataset/6be8185a-7fc3-48c7-9b8d-716602ea03f0/resource/018d8051-1bec-4210-bc04-c9fcc5d9da94/download/paacroutes1611.zip"

        fun deleteFiles(file: File) {
            if (file.isDirectory) {
                for (f in file.listFiles()) {
                    deleteFiles(f)
                }
            }
            file.delete()
        }

        @BeforeClass @JvmStatic
        fun setUp() {
            outFolder.mkdirs()
        }

        @AfterClass @JvmStatic
        fun tearDown() {
            deleteFiles(outFolder)
            Assert.assertFalse(outFolder.exists())
        }
    }
    //endregion

    @Test
    fun testConvert() {
        val shpFolder = downloadAndUnzipFile()
        ShapeFileJsonConverter.convert(shpFolder)
    }

    fun downloadAndUnzipFile() : File {
        val downloadedFile = downloadFile()
        return unzipFile(downloadedFile)
    }

    fun downloadFile() : File {
        val outFile = File(outFolder, "routes.zip")
        val downloadedFile = ShapeFileJsonConverter.downloadFile(testURL, outFile.path)
        Assert.assertTrue(outFile.exists())
        Assert.assertEquals(outFile, downloadedFile)
        return downloadedFile
    }

    fun unzipFile(inputFile: File) : File {
        val outFile = File(outFolder, "routes")
        val unzippedFile = ShapeFileJsonConverter.unzip(inputFile, outFile.path)
        Assert.assertEquals(7, outFile.listFiles().size)
        Assert.assertEquals(outFile, unzippedFile)
        return unzippedFile
    }






}
