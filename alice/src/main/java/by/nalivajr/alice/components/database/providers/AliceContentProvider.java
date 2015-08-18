package by.nalivajr.alice.components.database.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.nalivajr.alice.annonatations.database.Entity;
import by.nalivajr.alice.components.database.helpers.AliceDatabaseHelper;
import by.nalivajr.alice.components.database.models.Identifiable;
import by.nalivajr.alice.exceptions.NotAnnotatedEntityException;
import by.nalivajr.alice.tools.Alice;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceContentProvider extends ContentProvider {

    private static final String TAG = AliceContentProvider.class.getSimpleName();

    private AliceDatabaseHelper helper;
    protected Map<String, String> uriToTableName;
    protected Map<Integer, String> mimeCodeToMimeType;
    protected Map<String, String> tableNameToAuthority;
    protected UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        helper = createDatabaseHelper();
        List<Class<Identifiable>> entityClasses = getEntityClasses();
        helper.setEntityClasses(entityClasses);
        populateMap(entityClasses);
        return true;
    }

    /**
     * Provides list of classes which is managed by this provider
     * @return list of classes which is managed by this provider
     */
    public abstract <T> List<Class<T>> getEntityClasses();

    protected abstract AliceDatabaseHelper createDatabaseHelper();

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String type = mimeCodeToMimeType.get(uriMatcher.match(uri));
        if (type == null) {
            return null;
        }
        String tableName = uriToTableName.get(type);
        return helper.getReadableDatabase().query(false, tableName, projection, selection, selectionArgs, null, null, sortOrder, null);
    }


    @Override
    public String getType(Uri uri) {
        int code = uriMatcher.match(uri);
        return mimeCodeToMimeType.get(code);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String type = mimeCodeToMimeType.get(uriMatcher.match(uri));
        if (type == null) {
            return null;
        }
        String tableName = uriToTableName.get(type);
        long rowId = -1;

        SQLiteDatabase database = helper.getWritableDatabase();
        try {
            database.beginTransaction();
            rowId = database.insert(tableName, null, values);
            database.setTransactionSuccessful();
        } catch (Throwable e) {
            Log.e(TAG, "Could not insert data", e);
            throw new RuntimeException(e);
        } finally {
            database.endTransaction();
        }
        if (rowId == -1) {
            throw new RuntimeException("Could not save to database. Incorrect data.");
        }

        Uri newUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(tableNameToAuthority.get(tableName))
                .appendPath(tableName)
                .build();
        notifyObservers(uri);
        return ContentUris.withAppendedId(newUri, rowId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        String type = mimeCodeToMimeType.get(uriMatcher.match(uri));
        if (type == null) {
            return 0;
        }
        String tableName = uriToTableName.get(type);
        int deleted = 0;

        SQLiteDatabase database = helper.getWritableDatabase();
        try {
            database.beginTransaction();
            deleted = database.delete(tableName, selection, selectionArgs);
            database.setTransactionSuccessful();
        } catch (Throwable e) {
            Log.e(TAG, "Could not delete data", e);
            throw new RuntimeException(e);
        } finally {
            database.endTransaction();
        }
        notifyObservers(uri);
        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String type = mimeCodeToMimeType.get(uriMatcher.match(uri));
        if (type == null) {
            return 0;
        }
        String tableName = uriToTableName.get(type);
        int updated = 0;

        SQLiteDatabase database = helper.getWritableDatabase();
        try {
            database.beginTransaction();
            updated = database.update(tableName, values, selection, selectionArgs);
            database.setTransactionSuccessful();
        } catch (Throwable e) {
            Log.e(TAG, "Could not update data", e);
            throw new RuntimeException(e);
        } finally {
            database.endTransaction();
        }
        notifyObservers(uri);
        return updated;
    }

    protected <T> void populateMap(List<Class<T>> entityClasses) {

        int start = 10001;
        int step = 10000;

        uriToTableName = new HashMap<String, String>();
        mimeCodeToMimeType = new HashMap<Integer, String>();
        tableNameToAuthority = new HashMap<String, String>();
        int code = start;
        for (Class<T> cls : entityClasses) {
            Entity annotation = cls.getAnnotation(Entity.class);
            if (annotation == null) {
                throw new NotAnnotatedEntityException(cls);
            }
            String tableName = Alice.databaseTools.getEntityTableName(cls, annotation);
            String authority = annotation.authority();

            String mimeTypeOne = String.format("vnd.android.cursor.item/vnd.%s.%s", authority, tableName);
            uriMatcher.addURI(authority, tableName, code++);
            uriToTableName.put(mimeTypeOne, tableName);
            mimeCodeToMimeType.put(code, mimeTypeOne);
            Log.i(TAG, String.format("Mapping %s to MIME type %s with code %d", tableName, mimeTypeOne, code));

            String mimeTypeMany = String.format("vnd.android.cursor.dir/vnd.%s.%s", authority, tableName);
            uriMatcher.addURI(authority, tableName, code++);
            uriToTableName.put(mimeTypeMany, tableName);
            mimeCodeToMimeType.put(code, mimeTypeMany);
            Log.i(TAG, String.format("Mapping %s to MIME type %s with code %d", tableName, mimeTypeMany, code));

            tableNameToAuthority.put(tableName, authority);
            code = (code / step + 1) * step;
        }
    }

    protected void notifyObservers(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }
}
