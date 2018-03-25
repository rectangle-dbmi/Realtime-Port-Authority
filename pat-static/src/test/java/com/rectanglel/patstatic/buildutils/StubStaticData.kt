package com.rectanglel.patstatic.buildutils

import com.rectanglel.patstatic.model.StaticData
import java.io.InputStreamReader

/**
 * Stub input stream to satisfy requirements on kotlin for nullness/
 * Created by epicstar on 11/11/17.
 */
class StubStaticData : StaticData {
    override fun getInputStreamForFileName(filename: String): InputStreamReader {
        val inputStream = "stub".byteInputStream()
        return InputStreamReader(inputStream)
    }

}