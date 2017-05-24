package com.rectanglel.patstatic.model;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This should be a wrapper around an object that contains the cached (perhaps old) routes bundled
 * with the app at install-time.
 * <p>
 * Created by epicstar on 3/11/17.
 * @author Jeremy Jao
 */
public interface StaticData {
    /**
     * Gets an input stream for a certain filename
     * @param filename the name of the file
     * @return an input stream
     * @throws IOException if the file doesn't exist
     */
    InputStreamReader getInputStreamForFileName(String filename) throws IOException;
}
