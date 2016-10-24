package rectangledbmi.com.pittsburghrealtimetracker.patterns;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
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

    @Before
    public void setUp() {
        dir = new File("testDirectory");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        patapi = getPatApiMock();
        patternDataManager = spy(new PatternDataManager(
                dir,
                patapi));
    }

    @After
    public void tearDown() {
        deleteFiles(dir);
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
