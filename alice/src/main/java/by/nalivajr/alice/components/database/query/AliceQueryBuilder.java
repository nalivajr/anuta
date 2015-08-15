package by.nalivajr.alice.components.database.query;

import android.content.ContentValues;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface AliceQueryBuilder<T> {

    public Restriction equal(String columnName, String val);

    public Restriction notEqual(String columnName, String val);

    public Restriction less(String columnName, String val);

    public Restriction le(String columnName, String val);

    public Restriction greater(String columnName, String val) ;

    public Restriction ge(String columnName, String val);

    public Restriction in(String columnName, String[] val);

    public Restriction notIn(String columnName, String[] val);

    public Restriction between(String columnName, String from, String to);

    public AliceQueryBuilder<T> and(Restriction restriction);

    public AliceQueryBuilder<T> or(Restriction restriction);

    public AliceQueryBuilder<T> limit(int offset, int size);

    public AliceQuery<T> buildDelete();

    public AliceQuery<T> buildSelect();

    public AliceQuery<T> buildInsert(ContentValues values);

    public AliceQuery<T> buildUpdate(ContentValues values);

    public AliceQuery<T> build();

    public AliceQuery<T> buildFindAllQuery();
}
