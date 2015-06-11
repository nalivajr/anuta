package com.nalivajr.android.asbs;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.mock.MockContext;
import android.widget.Toast;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
        setContext(new MockContext());
    }

    public void testToast() {
        Toast.makeText(getContext(), "Test", Toast.LENGTH_SHORT).show();
    }

}