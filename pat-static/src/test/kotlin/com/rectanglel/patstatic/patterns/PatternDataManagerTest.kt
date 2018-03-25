package com.rectanglel.patstatic.patterns

import com.rectanglel.patstatic.HelloWorld
import com.rectanglel.patstatic.TestHelperMethods
import com.rectanglel.patstatic.mock.PatApiMock
import com.rectanglel.patstatic.model.RetrofitPatApi
import com.rectanglel.patstatic.model.StaticData
import com.rectanglel.patstatic.patterns.response.Ptr
import com.rectanglel.patstatic.wrappers.WifiChecker
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import rx.observers.TestSubscriber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * Tests for the [PatternDataManager] class.
 *
 * Created by epicstar on 8/13/17.
 */
class PatternDataManagerTest {
    private lateinit var dir : File
    private lateinit var patternDataManager : PatternDataManager
    private lateinit var patapi : RetrofitPatApi
    private lateinit var staticData : StaticData
    private lateinit var wifiChecker : WifiChecker

    @Before
    fun setUp() {
        dir = File ("testDirectory")
        dir.mkdirs()
        patapi = PatApiMock.getPatApiMock()
        staticData = Mockito.mock(StaticData::class.java)
        wifiChecker = Mockito.mock(WifiChecker::class.java)

        patternDataManager = Mockito.spy(PatternDataManager(
                dir,
                patapi,
                staticData,
                wifiChecker
        ))
    }

    @After
    fun tearDown() {
        TestHelperMethods.deleteFiles(dir)
    }


    /**
     * Testing on retrieving something from "71A":
     * <ul>
     *     <li>polyline gets from the mock REST call 1x</li>
     *     <li>polyline gets from disk 1x after a call to the REST API</li>
     *     <li>Ensures that the data from disk and internet are equivalent</li>
     * </ul>
     */
    @Test
    fun `testGetPatterns - get from internet then get from disk`() {
        // region setup
        val ts1 = TestSubscriber<List<Ptr>>()
        val ts2 = TestSubscriber<List<Ptr>>()
        Mockito.`when`(wifiChecker.isOnWifi()).thenReturn(true)
        patternDataManager.getPatterns(PatApiMock.testRoute1).subscribe(ts1)
        patternDataManager.getPatterns(PatApiMock.testRoute1).subscribe(ts2)
        // endregion setup

        //region verify
        Mockito.verify(patapi, Mockito.times(1)).getPatterns(PatApiMock.testRoute1)
        Mockito.verify(patternDataManager, Mockito.times(1)).getPatternsFromInternet(PatApiMock.testRoute1)
        Mockito.verify(patternDataManager, Mockito.times(1)).getPatternsFromDisk(PatApiMock.testRoute1)
        //endregion verify

        //region assert
        Assert.assertEquals(1, ts1.onNextEvents.size)
        Assert.assertEquals(ts1.onNextEvents.size, ts2.onNextEvents.size)
        Assert.assertEquals(PatApiMock.getPatterns(), ts1.onNextEvents[0])
        Assert.assertEquals(PatApiMock.getPatterns(), ts2.onNextEvents[0])
        //endregion assert
    }

    /**
     * Testing on retrieving something from "71A":
     * <ul>
     *     <li>polyline gets from the mock REST call 1x</li>
     *     <li>polyline gets from disk 1x after a call to the REST API</li>
     *     <li>Ensures that the data from disk and internet are equivalent</li>
     * </ul>
     */
    @Test
    fun `testGetPatterns - always get from disk if no wifi`() {
        // region setup
        val ts1 = TestSubscriber<List<Ptr>>()
        val ts2 = TestSubscriber<List<Ptr>>()
        Mockito.`when`(wifiChecker.isOnWifi()).thenReturn(false)
        patternDataManager.getPatterns(PatApiMock.testRoute1).subscribe(ts1)
        patternDataManager.getPatterns(PatApiMock.testRoute1).subscribe(ts2)
        // endregion setup

        //region verify
        Mockito.verify(patapi, Mockito.times(1)).getPatterns(PatApiMock.testRoute1)
        Mockito.verify(patternDataManager, Mockito.times(0)).getPatternsFromInternet(PatApiMock.testRoute1)
        Mockito.verify(patternDataManager, Mockito.times(1)).getPatternsFromDisk(PatApiMock.testRoute1)
        //endregion verify

        //region assert
        Assert.assertEquals(1, ts1.onNextEvents.size)
        Assert.assertEquals(ts1.onNextEvents.size, ts2.onNextEvents.size)
        Assert.assertEquals(PatApiMock.getPatterns(), ts1.onNextEvents[0])
        Assert.assertEquals(PatApiMock.getPatterns(), ts2.onNextEvents[0])
        //endregion assert
    }


    /**
     * Reads route json from disk using the StaticData class.
     */
    @Test
    fun `testGetPatterns - get from internet if wifi then from disk`() {
        //region setup
        val ts1 = TestSubscriber<List<Ptr>>()
        val ts2 = TestSubscriber<List<Ptr>>()

        val filename = "lineinfo/${PatApiMock.testRoute1}.json"
        val file = File("testFiles", filename)
        // jeremy.... RIP backticks for when because kotlin has
        // a keyword called when that replaces switch cases
        Mockito.`when`(wifiChecker.isOnWifi()).thenReturn(true)
        Mockito.`when`(staticData.getInputStreamForFileName(filename))
                .thenReturn(InputStreamReader(FileInputStream(file)))

        patternDataManager.getPatterns(PatApiMock.testRoute1).subscribe(ts1)
        patternDataManager.getPatterns(PatApiMock.testRoute1).subscribe(ts2)
        //endregion setup

        //region verify
        Mockito.verify(staticData, Mockito.times(1)).getInputStreamForFileName(filename)
        Mockito.verify(patternDataManager, Mockito.times(1)).getPatternsFromInternet(PatApiMock.testRoute1)
        Mockito.verify(patternDataManager, Mockito.times(1)).getPatternsFromDisk(PatApiMock.testRoute1)

        //endregion verify

        //region assert
        Assert.assertEquals(1, ts1.onNextEvents.size)
        Assert.assertEquals(ts1.onNextEvents.size, ts2.onNextEvents.size)
        Assert.assertEquals(ts1.onNextEvents[0], ts2.onNextEvents[0])
        //endregion assert
    }

    /**
     * Throws an error (via a mock object) when the data does not exist.
     */
    @Test
    fun `testGetPatterns - propagate error if there is an error`() {
        //region setup
        val nonExistentPattern = "71C"
        val ts1 = TestSubscriber<List<Ptr>>()
        val filename = "lineinfo/$nonExistentPattern.json"
        val mockedException = IOException("error")
        Mockito.`when`(staticData.getInputStreamForFileName(filename))
                .thenThrow(mockedException)
        patternDataManager.getPatternsFromDisk(nonExistentPattern).subscribe(ts1)
        //endregion setup

        //region assert
        Assert.assertEquals(0, ts1.onNextEvents.size)
        Assert.assertEquals(1, ts1.onErrorEvents.size)
        Assert.assertEquals(mockedException, ts1.onErrorEvents[0].cause)
        //endregion assert
    }

    @Test
    fun `hello world`() {
        val helloWorld = HelloWorld()
        Assert.assertEquals("Hello World", helloWorld.hello())
    }
}