package rectangledbmi.com.pittsburghrealtimetracker.patterns;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.mock.PatApiMock;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.response.Ptr;
import rectangledbmi.com.pittsburghrealtimetracker.model.RetrofitPatApi;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static rectangledbmi.com.pittsburghrealtimetracker.TestHelperMethods.deleteFiles;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.PatApiMock.getPatApiMock;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.PatApiMock.getPatterns;

/**
 * <p>Tests the {@link PatternDataManager} class</p>
 * <p>Created by epicstar on 9/18/16.</p>
 */
public class PatternDataManagerTest {

    private File dir;
    private PatternDataManager patternDataManager;
    private RetrofitPatApi patapi;
    private long currentTime;

    @Before
    public void setUp() {
        dir = new File("testDirectory");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        patapi = getPatApiMock();
        patternDataManager = spy(new PatternDataManager(
                dir,
                patapi));
        currentTime = System.currentTimeMillis();
    }

    @After
    public void tearDown() {
        deleteFiles(dir);
    }

    /**
     * Testing the pattern cache update method
     */
    @Test
    public void testNoPatternCache() {
        File patternDir = ((File)Whitebox.getInternalState(patternDataManager,"patternsDirectory"));
        assertEquals(0, patternDir.listFiles().length);

        long current = patternDataManager.updatePatternCache(currentTime, (long)-1);
        assertEquals(currentTime, current);
    }

    /**
     * Testing the pattern cache update method when cache is stale
     */
    @Test
    public void testStalePatternCache() {
        //put data in cache
        patternDataManager.getPatterns(PatApiMock.testRoute1).toBlocking().first();

        File patternDir = ((File)Whitebox.getInternalState(patternDataManager,"patternsDirectory"));
        assertEquals(1, patternDir.listFiles().length);

        // set up times for testing
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        cal.add(Calendar.HOUR,-50);
        long recent = cal.getTimeInMillis();

        long current = patternDataManager.updatePatternCache(currentTime, recent);
        assertEquals(0, patternDir.listFiles().length);
        assertEquals(currentTime, current);
    }

    /**
     * Testing the pattern cache update method when cache is stale
     */
    @Test
    public void testNewPatternCache() {
        //put data in cache
        patternDataManager.getPatterns(PatApiMock.testRoute1).toBlocking().first();

        File patternDir = ((File)Whitebox.getInternalState(patternDataManager,"patternsDirectory"));
        assertEquals(patternDir.listFiles().length, 1);

        // set up times for testing
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        cal.add(Calendar.HOUR,-10);
        long recent = cal.getTimeInMillis();

        long current = patternDataManager.updatePatternCache(currentTime, recent);
        assertEquals(patternDir.listFiles().length, 1);
        assertEquals(recent, current);
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
        verify(patapi, times(1)).getPatterns(PatApiMock.testRoute1);
        verify(patternDataManager, times(1)).getPatternsFromInternet(PatApiMock.testRoute1);
        verify(patternDataManager, times(1)).getPatternsFromDisk(PatApiMock.testRoute1);
        assertEquals(1, ts1.getOnNextEvents().size());
        assertEquals(ts1.getOnNextEvents().size(), ts2.getOnNextEvents().size());
        assertEquals(getPatterns(), ts1.getOnNextEvents().get(0));
        assertEquals(getPatterns(), ts2.getOnNextEvents().get(0));
    }


}
