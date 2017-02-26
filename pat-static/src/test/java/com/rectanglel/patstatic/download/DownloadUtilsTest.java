package com.rectanglel.patstatic.download;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Unit tests to test {@link DownloadUtils}
 * <p>
 * Created by epicstar on 2/26/17.
 * @author Jeremy Jao
 */

public class DownloadUtilsTest {
    private static final String outStringFolder = "testOut";
    private static final String testURL =
            "https://data.wprdc.org/dataset/" +
                    "6be8185a-7fc3-48c7-9b8d-716602ea03f0/" +
                    "resource/018d8051-1bec-4210-bc04-c9fcc5d9da94/download";

    private static File getOutputFolder() {
        return new File(outStringFolder);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeClass
    public static void setUpOutputFiles() {
        getOutputFolder().mkdirs();
    }

    @AfterClass
    public static void deleteOutputFiles() {
        deleteFiles(getOutputFolder());
    }

    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    private static void deleteFiles(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteFiles(f);
            }
        }
        file.delete();
    }

    private final static String routesZip = "routes.zip";
    private final static String routesOutFolderString = "routes";

    private static final String testZipFileString = "testFiles/paacroutes1611.zip";
    private static final String existingUnzipFolder = "existingRoutes";

    private static void assertZipFolder(File zippedFile, File expectedUnzipFolder, int numberOfEntries) throws IOException {
        File unzippedFolder = DownloadUtils.unzip(zippedFile, expectedUnzipFolder.getPath());
        Assert.assertNotNull(unzippedFolder);
        Assert.assertTrue(unzippedFolder.exists());
        //noinspection ConstantConditions
        Assert.assertEquals(numberOfEntries, unzippedFolder.listFiles().length);
        Assert.assertEquals(expectedUnzipFolder, unzippedFolder);
    }

    @Test
    public void testUnzipExistingFolder() throws IOException {
        File zippedFile = new File(testZipFileString);
        File expectedUnzipFolder = new File(getOutputFolder(), existingUnzipFolder);
        assertZipFolder(zippedFile, expectedUnzipFolder, 7);
    }

    private File testDownload() throws IOException {
        File outputFile = new File(getOutputFolder(), routesZip);
        File downloadedFile = DownloadUtils.download(testURL, outputFile.getPath());
        Assert.assertTrue("downloaded zip file should exist", downloadedFile.exists());
        Assert.assertTrue("downloaded zip file should be > 0", downloadedFile.length() > 0);
        Assert.assertEquals("downloaded zip file should be the same as the expected outputFile", outputFile, downloadedFile);
        return downloadedFile;
    }

    @Test
    public void testDownloadAndUnzip() throws IOException {
        File zippedFile = testDownload();
        File expectedUnzipFolder = new File(getOutputFolder(), routesOutFolderString);
        assertZipFolder(zippedFile, expectedUnzipFolder, 7);
    }

}
