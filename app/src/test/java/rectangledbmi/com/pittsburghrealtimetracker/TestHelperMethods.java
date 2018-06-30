package rectangledbmi.com.pittsburghrealtimetracker;

import java.io.File;

import io.reactivex.subscribers.TestSubscriber;

import static org.junit.Assert.assertNotNull;

/**
 * <p>Static helper methods for unit testing.</p>
 * <p>Created by epicstar on 7/30/16.</p>
 * @author Jeremy Jao
 */
public class TestHelperMethods {
    /**
     * Assert that the {@link TestSubscriber} has no errors and the subscription hasn't been completed
     * @param testSubscriber a test subscriber
     * @param <N> any Object inside a TestSubscriber
     */
    public static <N> void noErrorsAndNotCompleted(TestSubscriber<N> testSubscriber) {
        assertNotNull(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotComplete();
    }

    public static void deleteFiles(File file) {
        if (file.isDirectory()) {
            //noinspection ConstantConditions
            for (File f : file.listFiles()) {
                deleteFiles(f);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
