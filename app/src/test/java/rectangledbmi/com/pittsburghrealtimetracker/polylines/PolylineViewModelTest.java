package rectangledbmi.com.pittsburghrealtimetracker.polylines;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import rectangledbmi.com.pittsburghrealtimetracker.mock.PatApiMock;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.PATAPI;
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection;
import rx.Subscription;
import rx.observers.TestSubscriber;
import rx.subjects.BehaviorSubject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static rectangledbmi.com.pittsburghrealtimetracker.TestHelperMethods.noErrorsAndNotCompleted;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.PatApiMock.getPatApiMock;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.StubRouteSelection.getRouteSelection;

/**
 * <p></p>
 * <p>Created by epicstar on 7/18/16.</p>
 * @author Jeremy Jao and Michael Antonacci
 */
public class PolylineViewModelTest {
    private File dir;
    private Subscription polylinePresenterSubscription;
    private TestSubscriber<PatternSelection> patternSelectionTestSubscriber;
    private PATAPI patapiMock;
    @Before
    public void setUp() {
        patapiMock = getPatApiMock();
        dir = new File("testDirectory");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        PolylineViewModel polylineViewModel = new PolylineViewModel(
                patapiMock,
                PatApiMock.stubApiKey,
                subject.asObservable(),
                dir
        );
        patternSelectionTestSubscriber = new TestSubscriber<>();
        polylinePresenterSubscription = polylineViewModel
                .getPolylineObservable()
                .subscribe(patternSelectionTestSubscriber);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() {
        deleteFiles(dir);
        polylinePresenterSubscription.unsubscribe();
    }

    private void deleteFiles(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteFiles(f);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    /**
     * Tests the
     */
    @Test
    public void testGetPolylineObservable() {
        subject.onNext(getRouteSelection());
        verify(patapiMock).getPatterns(PatApiMock.testRoute1, PatApiMock.stubApiKey);
        noErrorsAndNotCompleted(patternSelectionTestSubscriber);

        subject.onNext(getRouteSelection());
        noErrorsAndNotCompleted(patternSelectionTestSubscriber);

        for (PatternSelection patternSelection : patternSelectionTestSubscriber.getOnNextEvents()) {
            assertEquals(PatApiMock.getResponse().getPatternResponse().getPtr(), patternSelection.getPatterns());
            assertEquals(true, patternSelection.isSelected());
        }

    }


    private static BehaviorSubject<RouteSelection> subject = BehaviorSubject.create();

}