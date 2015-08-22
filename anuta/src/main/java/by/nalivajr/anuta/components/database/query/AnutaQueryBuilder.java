package by.nalivajr.anuta.components.database.query;

import android.content.ContentValues;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface AnutaQueryBuilder<T> {

    public Restriction equal(String columnName, String val);

    public Restriction notEqual(String columnName, String val);

    public Restriction less(String columnName, String val);

    public Restriction le(String columnName, String val);

    public Restriction greater(String columnName, String val) ;

    public Restriction ge(String columnName, String val);

    public Restriction in(String columnName, String[] val);

    public Restriction notIn(String columnName, String[] val);

    public Restriction between(String columnName, String from, String to);

    public AnutaQueryBuilder<T> and(Restriction restriction);

    public AnutaQueryBuilder<T> or(Restriction restriction);

    public AnutaQueryBuilder<T> limit(int offset, int size);

    public AnutaQuery<T> buildDelete();

    public AnutaQuery<T> buildSelect();

    public AnutaQuery<T> buildInsert(ContentValues values);

    public AnutaQuery<T> buildUpdate(ContentValues values);

    public AnutaQuery<T> build();

    public AnutaQuery<T> buildFindAllQuery();
}
