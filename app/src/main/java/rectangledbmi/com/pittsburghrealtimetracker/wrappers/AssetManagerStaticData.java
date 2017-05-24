package rectangledbmi.com.pittsburghrealtimetracker.wrappers;

import android.content.res.AssetManager;

import com.rectanglel.patstatic.model.StaticData;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wraps the {@link AssetManager} for the TrueTime API in order to get bundled data from it.
 * <p>
 * Created by epicstar on 3/11/17.
 * @author Jeremy Jao
 * @since 80
 */

public class AssetManagerStaticData implements StaticData {

    private AssetManager assetManager;
    private static final String cachePath = "cache/%s";

    public AssetManagerStaticData(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public InputStreamReader getInputStreamForFileName(String filename) throws IOException {
        String realFileName = String.format(cachePath, filename);
        return new InputStreamReader(assetManager.open(realFileName));
    }
}
