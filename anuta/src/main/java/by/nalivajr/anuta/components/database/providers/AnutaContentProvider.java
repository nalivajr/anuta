package by.nalivajr.anuta.components.database.providers;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.components.database.helpers.AnutaDatabaseHelper;
import by.nalivajr.anuta.components.database.models.Identifiable;
import by.nalivajr.anuta.exceptions.InvalidMimeCodeException;
import by.nalivajr.anuta.exceptions.NotAnnotatedEntityException;
import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AnutaContentProvider extends ContentProvider {

    private static final String TAG = AnutaContentProvider.class.getSimpleName();

    private static final String PATH_SUFFIX_SINGLE = "/#";
    private static final String PATH_SUFFIX_MANY = "/*";

    private AnutaDatabaseHelper helper;
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

    protected abstract AnutaDatabaseHelper createDatabaseHelper();

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

    protected void notifyObservers(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }

    protected <T> void populateMap(List<Class<T>> entityClasses) {

        int start = 10001;
        int step = 10000;

        uriToTableName = new HashMap<String, String>();
        mimeCodeToMimeType = new HashMap<Integer, String>();
        tableNameToAuthority = new HashMap<String, String>();
        Set<String> tables = new HashSet<String>();
        Map<String, String> relationTables = new HashMap<String, String>();

        int code = start;
        for (Class<T> cls : entityClasses) {
            Entity annotation = cls.getAnnotation(Entity.class);
            if (annotation == null) {
                throw new NotAnnotatedEntityException(cls);
            }
            String tableName = Anuta.databaseTools.getEntityTableName(cls, annotation);
            String authority = annotation.authority();
            String mimeTypeOne = annotation.mimeTypeOne();
            String mimeTypeMany = annotation.mimeTypeMany();
            int mimeTypeOneCode = annotation.mimeTypeOneCode();
            int mimeTypeManyCode = annotation.mimeTypeManyCode();
            code = addToMatcherAndReturnCode(step, code, tableName, authority, mimeTypeOne, mimeTypeMany, mimeTypeOneCode, mimeTypeManyCode);

            tables.contains(tableName);
            relationTables.putAll(Anuta.databaseTools.getRelatedTablesNames(cls));
        }
        for (String relationTableName : relationTables.keySet()) {
            if (tables.contains(relationTableName)) {
                continue;
            }
            String authority = relationTables.get(relationTableName);
            code = addToMatcherAndReturnCode(step, code, relationTableName, authority, "", "",  -1, -1);
        }
    }

    private int addToMatcherAndReturnCode(int step, int counter, String tableName, String authority,
                                          String mimeTypeOne, String mimeTypeMany,
                                          int mimeTypeOneCode, int mimeTypeManyCode) {
        if (mimeTypeOne == null || mimeTypeOne.isEmpty()) {
            mimeTypeOne = String.format("vnd.android.cursor.item/vnd.%s.%s", authority, tableName);
        }
        mimeTypeOneCode = getAvailableCode(counter, mimeTypeOneCode);
        uriMatcher.addURI(authority, tableName + PATH_SUFFIX_SINGLE, mimeTypeOneCode);
        uriToTableName.put(mimeTypeOne, tableName);
        mimeCodeToMimeType.put(mimeTypeOneCode, mimeTypeOne);
        Log.i(TAG, String.format("Mapping %s to MIME type %s with code %d", tableName, mimeTypeOne, mimeTypeOneCode));

        if (mimeTypeMany == null || mimeTypeMany.isEmpty()) {
            mimeTypeMany = String.format("vnd.android.cursor.dir/vnd.%s.%s", authority, tableName);
        }
        mimeTypeManyCode = getAvailableCode(counter, mimeTypeManyCode);
        uriMatcher.addURI(authority, tableName + PATH_SUFFIX_MANY, mimeTypeManyCode);
        uriToTableName.put(mimeTypeMany, tableName);
        mimeCodeToMimeType.put(mimeTypeManyCode, mimeTypeMany);
        Log.i(TAG, String.format("Mapping %s to MIME type %s with code %d", tableName, mimeTypeMany, mimeTypeManyCode));

        uriMatcher.addURI(authority, tableName, mimeTypeManyCode);
        Log.i(TAG, String.format("Mapping table %s to with code %d", tableName, mimeTypeManyCode));

        tableNameToAuthority.put(tableName, authority);
        counter = (counter / step + 1) * step;
        return counter;
    }

    private int getAvailableCode(int counter, int mimeTypeCode) {
        Set<Integer> registeredCodes = mimeCodeToMimeType.keySet();
        if (mimeTypeCode == -1) {
            mimeTypeCode = counter;
            while (registeredCodes.contains(mimeTypeCode)) {
                mimeTypeCode++;
            }
        } else {
            if (registeredCodes.contains(mimeTypeCode)) {
                throw new InvalidMimeCodeException(mimeTypeCode);
            }
        }
        return mimeTypeCode;
    }
}
