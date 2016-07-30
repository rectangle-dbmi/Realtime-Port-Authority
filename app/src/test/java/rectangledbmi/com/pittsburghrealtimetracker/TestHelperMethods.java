package rectangledbmi.com.pittsburghrealtimetracker;

import rx.observers.TestSubscriber;

import static org.junit.Assert.assertNotNull;

/**
 * <p>Static helper methods for unit testing.</p>
 * <p>Created by epicstar on 7/30/16.</p>
 * @author Jeremy Jao
 */
public class TestHelperMethods {
    /**
     *
     * @param testSubscriber a test subscriber
     * @param <N> any Object inside a TestSubscriber
     */
    public static <N> void noErrorsAndNotCompleted(TestSubscriber<N> testSubscriber) {
        assertNotNull(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotCompleted();
    }
}
