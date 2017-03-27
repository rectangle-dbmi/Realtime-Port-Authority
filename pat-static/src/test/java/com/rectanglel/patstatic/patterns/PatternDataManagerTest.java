package com.rectanglel.patstatic.patterns;

import com.rectanglel.patstatic.TestHelperMethods;
import com.rectanglel.patstatic.mock.PatApiMock;
import com.rectanglel.patstatic.model.RetrofitPatApi;
import com.rectanglel.patstatic.model.SourceOfTruth;
import com.rectanglel.patstatic.patterns.response.Ptr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

/**
 * <p>Tests the {@link PatternDataManager} class</p>
 * <p>Created by epicstar on 9/18/16.</p>
 */
public class PatternDataManagerTest {

    private File dir;
    private PatternDataManager patternDataManager;
    private RetrofitPatApi patapi;
    private SourceOfTruth sourceOfTruth;

    @Before
    public void setUp() {
        dir = new File("testDirectory");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        patapi = PatApiMock.getPatApiMock();
        sourceOfTruth = Mockito.mock(SourceOfTruth.class);
        patternDataManager = Mockito.spy(new PatternDataManager(
                dir,
                patapi,
                sourceOfTruth)
        );
    }

    @After
    public void tearDown() {
        TestHelperMethods.deleteFiles(dir);
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
    public void testGetPatterns() {
        TestSubscriber<List<Ptr>> ts1 = new TestSubscriber<>();
        TestSubscriber<List<Ptr>> ts2 = new TestSubscriber<>();
        patternDataManager.getPatterns(PatApiMock.testRoute1).subscribe(ts1);
        patternDataManager.getPatterns(PatApiMock.testRoute1).subscribe(ts2);

        Mockito.verify(patapi, Mockito.times(1)).getPatterns(PatApiMock.testRoute1);
        Mockito.verify(patternDataManager, Mockito.times(1)).getPatternsFromInternet(PatApiMock.testRoute1);
        Mockito.verify(patternDataManager, Mockito.times(1)).getPatternsFromDisk(PatApiMock.testRoute1);
        assertEquals(1, ts1.getOnNextEvents().size());
        assertEquals(ts1.getOnNextEvents().size(), ts2.getOnNextEvents().size());
        assertEquals(PatApiMock.getPatterns(), ts1.getOnNextEvents().get(0));
        assertEquals(PatApiMock.getPatterns(), ts2.getOnNextEvents().get(0));
    }

    @Test
    public void testGetPatternsFromDisk() throws IOException {
        TestSubscriber<List<Ptr>> ts1 = new TestSubscriber<>();
        TestSubscriber<List<Ptr>> ts2 = new TestSubscriber<>();
        String filename = String.format(Locale.US, "lineinfo/%s.json", PatApiMock.testRoute1);
        File file = new File("testFiles", filename);
        Mockito.when(sourceOfTruth.getInputStreamForFileName(filename))
                .thenReturn(new InputStreamReader(new FileInputStream(file)));

        patternDataManager.getPatternsFromDisk(PatApiMock.testRoute1).subscribe(ts1);
        patternDataManager.getPatternsFromDisk(PatApiMock.testRoute1).subscribe(ts2);
        Mockito.verify(sourceOfTruth, Mockito.times(1)).getInputStreamForFileName(filename);
        assertEquals(1, ts1.getOnNextEvents().size());
        assertEquals(ts1.getOnNextEvents().size(), ts2.getOnNextEvents().size());
        assertEquals(ts1.getOnNextEvents().get(0), ts2.getOnNextEvents().get(0));
    }

    @Test
    public void testGetPatternsFromDisk_Error() throws IOException {
        TestSubscriber<List<Ptr>> ts1 = new TestSubscriber<>();
        String filename = String.format(Locale.US, "lineinfo/%s.json", "71C");
        IOException ex = new IOException("error");
        Mockito.when(sourceOfTruth.getInputStreamForFileName(filename))
                .thenThrow(ex);

        patternDataManager.getPatternsFromDisk("71C").subscribe(ts1);
        assertEquals(0, ts1.getOnNextEvents().size());
        assertEquals(1, ts1.getOnErrorEvents().size());
        //noinspection ThrowableResultOfMethodCallIgnored
        assertEquals(ex, ts1.getOnErrorEvents().get(0).getCause());
    }

}
