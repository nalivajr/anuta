package by.nalivajr.alice.components.database.query;

import android.content.ContentValues;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class AliceSimpleQuery<T> implements AliceQuery<T> {

    private final String selection;
    private final String[] args;
    private final Class<T> cls;
    private final QueryType type;
    private final ContentValues contentValues;
    private final String limit;

    public AliceSimpleQuery(String selection, String[] args, Class<T> cls, QueryType type, ContentValues values, String limit) {
        this.selection = selection;
        this.args = args;
        this.cls = cls;
        this.type = type;
        this.contentValues = values;
        this.limit = limit;
    }

    @Override
    public String getSelection() {
        return selection;
    }

    @Override
    public String[] getSelectionArgs() {
        return args;
    }

    @Override
    public Class<T> getTargetClass() {
        return cls;
    }

    @Override
    public QueryType getType() {
        return type;
    }

    @Override
    public ContentValues getContentValues() {
        return contentValues;
    }

    @Override
    public String getLimit() {
        return limit;
    }
}
