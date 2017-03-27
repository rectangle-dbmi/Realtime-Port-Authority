package rectangledbmi.com.pittsburghrealtimetracker.wrappers;

import android.content.res.AssetManager;

import com.rectanglel.patstatic.model.SourceOfTruth;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wraps the {@link AssetManager} for the TrueTime API in order to get bundled data from it.
 * <p>
 * Created by epicstar on 3/11/17.
 * @author Jeremy Jao
 * @since 80
 */

public class AssetManagerSourceOfTruth implements SourceOfTruth {

    private AssetManager assetManager;

    public AssetManagerSourceOfTruth(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public InputStreamReader getInputStreamForFileName(String filename) throws IOException {
        return new InputStreamReader(assetManager.open(filename));
    }
}
