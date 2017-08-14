package com.rectanglel.patstatic.patterns

import com.rectanglel.patstatic.mock.PatApiMock
import com.rectanglel.patstatic.model.RetrofitPatApi
import com.rectanglel.patstatic.model.StaticData
import com.rectanglel.patstatic.wrappers.WifiChecker
import org.junit.Before
import org.mockito.Mockito
import java.io.File

/**
 * Created by epicstar on 8/13/17.
 */
class PatternDataManagerTestKt { // TODO: rename this to PatternDataManagerTest
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
                staticData
        ))
    }


}