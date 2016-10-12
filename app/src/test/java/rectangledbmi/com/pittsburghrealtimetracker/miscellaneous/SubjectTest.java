package rectangledbmi.com.pittsburghrealtimetracker.miscellaneous;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;

/**
 * <p>Testing how publish subjects work.</p>
 * <p>Created by epicstar on 10/11/16.</p>
 * @author Jeremy Jao
 */

public class SubjectTest {

    private PublishSubject<Integer> testSubject;
    private final static int maxEmissions = 1000;

    @Before
    public void setUp() {
        testSubject = PublishSubject.create();
    }

    /**
     * Test to confirm that subjects can work in a for-loop. Would go against a hypothesis on why issue #290 is failing.
     * @see <a href="https://github.com/rectangle-dbmi/Realtime-Port-Authority/issues/290">Issue #290</a>
     */
    @Test
    public void testToggle() {
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        testSubject.subscribe(testSubscriber);
        for (int i=0;i<maxEmissions;++i) {
            testSubject.onNext(i);
        }
        List<Integer> events = testSubscriber.getOnNextEvents();
        assertEquals(maxEmissions, events.size());
        for (int i=0;i<maxEmissions;++i) {
            Integer event = events.get(i);
            assertEquals(i, event.intValue());
        }
    }
}
