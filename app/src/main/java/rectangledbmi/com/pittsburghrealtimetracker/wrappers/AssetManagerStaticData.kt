package rectangledbmi.com.pittsburghrealtimetracker.wrappers

import android.content.res.AssetManager

import com.rectanglel.patstatic.model.StaticData

import java.io.IOException
import java.io.InputStreamReader

/**
 * Wraps the [AssetManager] for the TrueTime API in order to get bundled data from it.
 *
 *
 * Created by epicstar on 3/11/17.
 * @author Jeremy Jao
 * @since 80
 */

class AssetManagerStaticData(private val assetManager: AssetManager) : StaticData {

    @Throws(IOException::class)
    override fun getInputStreamForFileName(filename: String): InputStreamReader {
        val realFileName = String.format(cachePath, filename)
        return InputStreamReader(assetManager.open(realFileName))
    }

    companion object {
        private val cachePath = "cache/%s"
    }
}
