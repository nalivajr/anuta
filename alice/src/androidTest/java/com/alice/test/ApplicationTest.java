package com.alice.test;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.alice.components.database.models.Item;
import com.alice.tools.Alice;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testScriptCreation() {
        String sql = Alice.DatabaseTools.generateRelationalTableScript(Item.class);
        System.out.println("[DB]" + sql);
    }
}