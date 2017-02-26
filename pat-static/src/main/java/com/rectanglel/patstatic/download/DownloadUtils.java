package com.rectanglel.patstatic.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class that downloads and unzips files
 * Created by epicstar on 2/26/17.
 * @author Jeremy Jao
 */

public class DownloadUtils {
    /**
     * Downloads a file and saves it to the [outputPath]
     *
     * @param urlString the URL as a string
     * @param outputPath output path as a string
     * @return the file downloaded
     */
    public static File download(String urlString, String outputPath) throws IOException {
        URL url = new URL(urlString);
        InputStream urlIn = url.openStream();
        ReadableByteChannel readableByteChannel = Channels.newChannel(urlIn);
        FileOutputStream fos = new FileOutputStream(outputPath);
        fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        readableByteChannel.close();
        urlIn.close();
        fos.close();

        return new File(outputPath);
    }

    /**
     * Unzips a zip file from
     * @param zippedFile
     *   the file to unzip
     * @param outputPath
     *   the path to unzip to
     * @return the [outputPath] as a file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File unzip(File zippedFile, String outputPath) throws IOException {
        byte[] buffer = new byte[1024];
        File outputFile = new File(outputPath);
        outputFile.mkdirs();
        ZipInputStream zin = new ZipInputStream(new FileInputStream(zippedFile));
        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            File zippedEntry = new File(outputFile, entry.getName());
            new File(zippedEntry.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(zippedEntry);
            int length = zin.read(buffer);
            while (length > 0) {
                fos.write(buffer, 0, length);
                length = zin.read(buffer);
            }
            fos.close();
            entry = zin.getNextEntry();
        }
        zin.closeEntry();
        zin.close();
        return outputFile;
    }
}
