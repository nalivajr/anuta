package by.nalivajr.anuta.components.database.query;

import android.content.ContentValues;
import android.net.Uri;

import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class AnutaQueryWrapper<T> implements AnutaQueryWithUri<T> {

    private final AnutaQuery<T> baseQuery;
    private Uri uri = null;

    public AnutaQueryWrapper(AnutaQuery<T> baseQuery) {
        this.baseQuery = baseQuery;
    }

    public AnutaQuery<T> getBaseQuery() {
        return baseQuery;
    }

    @Override
    public Uri getUri() {
        if (uri == null) {
            Entity entity = baseQuery.getTargetClass().getAnnotation(Entity.class);
            String tableName = entity.tableName();
            String authority = entity.authority();
            uri = Anuta.databaseTools.buildUriForTableName(tableName, authority);
        }
        return uri;
    }

    @Override
    public String getSelection() {
        return baseQuery.getSelection();
    }

    @Override
    public String[] getSelectionArgs() {
        return baseQuery.getSelectionArgs();
    }

    @Override
    public Class<T> getTargetClass() {
        return baseQuery.getTargetClass();
    }

    @Override
    public QueryType getType() {
        return baseQuery.getType();
    }

    @Override
    public ContentValues getContentValues() {
        return baseQuery.getContentValues();
    }

    @Override
    public String getLimit() {
        return baseQuery.getLimit();
    }
}
