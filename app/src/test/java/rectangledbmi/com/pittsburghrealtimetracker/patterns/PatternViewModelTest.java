package rectangledbmi.com.pittsburghrealtimetracker.patterns;

import com.rectanglel.patstatic.model.PatApiService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subscribers.TestSubscriber;
import rectangledbmi.com.pittsburghrealtimetracker.mock.PatApiMock;
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static rectangledbmi.com.pittsburghrealtimetracker.TestHelperMethods.noErrorsAndNotCompleted;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.PatApiMock.getPatApiServiceMock;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.ToggledRouteMockMethods.getSelectedRouteSelection;
import static rectangledbmi.com.pittsburghrealtimetracker.mock.ToggledRouteMockMethods.getUnselectedRouteSelection;

/**
 * <p>Unit tests around the {@link PatternViewModel}</p>
 * <p>Created by epicstar on 7/18/16.</p>
 * @since 78
 * @author Jeremy Jao and Michael Antonacci
 */
public class PatternViewModelTest {

    private Disposable polylinePresenterSubscription;
    private TestSubscriber<PatternSelection> patternSelectionTestSubscriber;
    private PatApiService patapiMock;
    private static BehaviorSubject<Route> subject = BehaviorSubject.create();

    @Before
    public void setUp() {
        patapiMock = getPatApiServiceMock();
        //noinspection ResultOfMethodCallIgnored

        PatternViewModel patternViewModel = new PatternViewModel(
                patapiMock,
                subject.toFlowable(BackpressureStrategy.BUFFER)
        );
        patternSelectionTestSubscriber = new TestSubscriber<>();
        polylinePresenterSubscription = patternViewModel
                .getPatternSelections()
                .subscribeWith(patternSelectionTestSubscriber);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() {
        polylinePresenterSubscription.dispose();
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
        verify(patapiMock).getPatterns(PatApiMock.testRoute1);
        noErrorsAndNotCompleted(patternSelectionTestSubscriber);

        subject.onNext(firstRouteSelection);
        noErrorsAndNotCompleted(patternSelectionTestSubscriber);

        for (PatternSelection patternSelection : patternSelectionTestSubscriber.values()) {
            assertEquals(PatApiMock.getPatterns(), patternSelection.getPatterns());
            assertEquals(firstRouteSelection.isSelected(), patternSelection.isSelected());
            assertEquals(firstRouteSelection.getRoute(), patternSelection.getRouteNumber());
        }

        Route unselectedSelection = getUnselectedRouteSelection();
        subject.onNext(unselectedSelection);
        noErrorsAndNotCompleted(patternSelectionTestSubscriber);
        List<PatternSelection> onNextEvents = patternSelectionTestSubscriber.values();
        PatternSelection unselectedOnNextEvent = onNextEvents.get(onNextEvents.size() - 1);
        assertFalse(unselectedOnNextEvent.isSelected());
        assertEquals(unselectedSelection.getRoute(), unselectedOnNextEvent.getRouteNumber());
    }

}