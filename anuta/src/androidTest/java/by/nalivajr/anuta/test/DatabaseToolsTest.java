package by.nalivajr.anuta.test;

import android.util.Log;

import junit.framework.TestCase;

import by.nalivajr.anuta.exceptions.IncorrectMappingException;
import by.nalivajr.anuta.exceptions.InvalidEntityIdMappingException;
import by.nalivajr.anuta.exceptions.InvalidRowIdMappingException;
import by.nalivajr.anuta.exceptions.NotAnnotatedEntityException;
import by.nalivajr.anuta.test.models.BadEntities;
import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class DatabaseToolsTest extends TestCase {

    public static final String TAG = DatabaseToolsTest.class.getName();

    public void testValidator() {
        try {
            Anuta.databaseTools.validateEntityClass(BadEntities.NotAnnotatedEnity.class);
        } catch (NotAnnotatedEntityException e) {
            Log.i(TAG, "Not annotated entity test passed");
        }

        try {
            Anuta.databaseTools.validateEntityClass(BadEntities.NoIdsEntity.class);
        } catch (InvalidEntityIdMappingException e) {
            Log.i(TAG, "No ids test passed");
        }

        try {
            Anuta.databaseTools.validateEntityClass(BadEntities.ManyIdsEntity.class);
        } catch (InvalidEntityIdMappingException e) {
            Log.i(TAG, "Many ids test passed");
        }

        try {
            Anuta.databaseTools.validateEntityClass(BadEntities.InvalidRowIdTypeEntity.class);
        } catch (InvalidRowIdMappingException e) {
            Log.i(TAG, "Invalid row id type test passed");
        }

        try {
            Anuta.databaseTools.validateEntityClass(BadEntities.DuplicateNamesEntity.class);
        } catch (IncorrectMappingException e) {
            Log.i(TAG, "Duplicate column names test passed");
        }
    }
}
