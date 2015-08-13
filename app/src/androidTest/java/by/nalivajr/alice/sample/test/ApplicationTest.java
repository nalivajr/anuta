package by.nalivajr.alice.sample.test;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.mock.MockContext;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
        setContext(new MockContext());
    }
}