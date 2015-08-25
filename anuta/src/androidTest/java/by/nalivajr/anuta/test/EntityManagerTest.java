package by.nalivajr.anuta.test;

import android.test.ProviderTestCase2;
import android.util.Log;

import junit.framework.Assert;

import by.nalivajr.anuta.components.database.entitymanager.AnutaEntityManager;
import by.nalivajr.anuta.test.content.TestContract;
import by.nalivajr.anuta.test.database.TestEntityManager;
import by.nalivajr.anuta.test.database.TestProvider;
import by.nalivajr.anuta.test.models.Tag;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class EntityManagerTest extends ProviderTestCase2<TestProvider> {

    public static final String TAG = EntityManagerTest.class.getName();

    private AnutaEntityManager entityManager;

    public EntityManagerTest() {
        super(TestProvider.class, TestContract.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        entityManager = new TestEntityManager(getMockContext());
    }

    public void testSavePlain() {
        Tag tag = new Tag();
        tag.setName("JAVA");
        entityManager.save(tag);

        tag = entityManager.find(Tag.class, tag.getName());
        Assert.assertEquals("JAVA", tag.getName());

        int found = entityManager.findAll(Tag.class).size();
        assertEquals(1, found);
        Log.i(TAG, "Found tags: " + found);
    }
}