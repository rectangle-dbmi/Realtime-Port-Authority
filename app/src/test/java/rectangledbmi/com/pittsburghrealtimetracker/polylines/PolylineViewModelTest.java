package rectangledbmi.com.pittsburghrealtimetracker.polylines;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.mock.PatApiMock;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.PATAPI;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rx.Subscription;
import rx.observers.TestSubscriber;
import rx.subjects.BehaviorSubject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static rectangledbmi.com.pittsburghrealtimetracker.TestHelperMethods.noErrorsAndNotCompleted;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.PatApiMock.getPatApiMock;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.ToggledRouteMockMethods.getSelectedRouteSelection;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.ToggledRouteMockMethods.getUnselectedRouteSelection;

/**
 * <p>Unit tests around the {@link PolylineViewModel}</p>
 * <p>Created by epicstar on 7/18/16.</p>
 * @since 76
 * @author Jeremy Jao and Michael Antonacci
 */
public class PolylineViewModelTest {
    private File dir;
    private Subscription polylinePresenterSubscription;
    private TestSubscriber<PatternSelection> patternSelectionTestSubscriber;
    private PATAPI patapiMock;
    private static BehaviorSubject<Route> subject = BehaviorSubject.create();

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

    private static void deleteFiles(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteFiles(f);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    /**
     * Tests the observables of the polyline of two "click events":
     * <li>
     *     <ul>The retrofit call has been made</ul>
     *     <ul>Then makes sure that the next call gets from disk and ensures that what is deserialized correctly</ul>
     *     <ul>The unselection event of the same route works as expected</ul>
     * </li>
     */
    @Test
    public void testGetPolylineObservable() {
        Route firstRouteSelection = getSelectedRouteSelection();
        subject.onNext(firstRouteSelection);
        verify(patapiMock).getPatterns(PatApiMock.testRoute1, PatApiMock.stubApiKey);
        noErrorsAndNotCompleted(patternSelectionTestSubscriber);

        subject.onNext(firstRouteSelection);
        noErrorsAndNotCompleted(patternSelectionTestSubscriber);

        for (PatternSelection patternSelection : patternSelectionTestSubscriber.getOnNextEvents()) {
            assertEquals(PatApiMock.getResponse().getPatternResponse().getPtr(), patternSelection.getPatterns());
            assertEquals(firstRouteSelection.isSelected(), patternSelection.isSelected());
            assertEquals(firstRouteSelection.getRoute(), patternSelection.getRouteNumber());
        }

        Route unselectedSelection = getUnselectedRouteSelection();
        subject.onNext(unselectedSelection);
        noErrorsAndNotCompleted(patternSelectionTestSubscriber);
        List<PatternSelection> onNextEvents = patternSelectionTestSubscriber.getOnNextEvents();
        PatternSelection unselectedOnNextEvent = onNextEvents.get(onNextEvents.size() - 1);
        assertNull(unselectedOnNextEvent.getPatterns());
        assertFalse(unselectedOnNextEvent.isSelected());
        assertEquals(unselectedSelection.getRoute(), unselectedOnNextEvent.getRouteNumber());
    }

}