package by.nalivajr.alice.components.database.query;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public final class BaseAliceQueryBuilder<T> implements AliceQueryBuilder<T> {

    private static final String AND = "AND";
    private static final String OR = "OR";
    private static final String BETWEEN = "BETWEEN";
    private static final String IN = "IN";
    private static final String NOT_IN = "NOT IN";
    private static final String PARAM = "?";
    private static final String PATTERN = "%s %s %s";

    private static final String EQUAL = "=";
    private static final String NOT_EQUAL = "!=";
    private static final String LESS = "<";
    private static final String LESS_OR_EQUAL = "<=";
    private static final String GREATER = ">";
    private static final String GREATER_OR_EQUAL = ">=";

    private static final String LIMIT = "LIMIT";
    private static final String OFFSET = "OFFSET";

    private Class<T> cls;
    private StringBuilder builder = new StringBuilder();
    private String limitation = "";
    private List<String> args = new LinkedList<String>();

    public BaseAliceQueryBuilder(Class<T> cls) {
        this.cls = cls;
    }

    public Restriction equal(String columnName, String val) {
        return new BaseRestriction(format(columnName, EQUAL), new String[]{val});
    }

    public Restriction notEqual(String columnName, String val) {
        return new BaseRestriction(format(columnName, NOT_EQUAL), new String[]{val});
    }

    public Restriction less(String columnName, String val) {
        return new BaseRestriction(format(columnName, LESS), new String[]{val});
    }

    public Restriction le(String columnName, String val) {
        return new BaseRestriction(format(columnName, LESS_OR_EQUAL), new String[]{val});
    }

    public Restriction greater(String columnName, String val) {
        return new BaseRestriction(format(columnName, GREATER), new String[]{val});
    }

    public Restriction ge(String columnName, String val) {
        return new BaseRestriction(format(columnName, GREATER_OR_EQUAL), new String[]{val});
    }

    public Restriction in(String columnName, String[] val) {
        if (val == null || val.length == 0) {
            throw new RuntimeException("Method parameter val should not be null or empty");
        }
        StringBuilder builder = new StringBuilder((val.length + 1) * 2);
        builder.append('(');
        for (int i = 0; i < val.length; i++) {
            builder.append(PARAM).append(',');
        }
        builder.setCharAt(builder.length() - 1, ')');
        return new BaseRestriction(format(columnName, IN, builder.toString()), val);
    }

    public Restriction notIn(String columnName, String[] val) {
        if (val == null || val.length == 0) {
            throw new RuntimeException("Method parameter val should not be null or empty");
        }
        StringBuilder builder = new StringBuilder((val.length + 1) * 2);
        builder.append('(');
        for (int i = 0; i < val.length; i++) {
            builder.append(PARAM).append(',');
        }
        builder.setCharAt(builder.length() - 1, ')');
        return new BaseRestriction(format(columnName, NOT_IN, builder.toString()), val);
    }

    public Restriction between(String columnName, String from, String to) {
        if (columnName == null || columnName.isEmpty()) {
            throw new RuntimeException("Method parameter columnName should not be null or empty");
        }
        String param = String.format(PATTERN, PARAM, AND, PARAM);
        return new BaseRestriction(format(columnName, BETWEEN, param), new String[] {from, to});
    }

    private String format(String columnName, String operation) {
        return format(columnName, operation, PARAM);
    }

    private String format(String columnName, String operation, String param) {
        return String.format(PATTERN, columnName, operation, param);
    }

    public BaseAliceQueryBuilder<T> and(Restriction restriction) {
        if (builder.length() != 0) {
            builder.append(AND);
        }
        builder
                .append(' ')
                .append(restriction.getProperty())
                .append(' ');
        args.addAll(Arrays.asList(restriction.getArgs()));
        return this;
    }

    public BaseAliceQueryBuilder<T> or(Restriction restriction) {
        if (builder.length() != 0) {
            builder.append(OR);
        }
        builder
                .append(' ')
                .append(restriction.getProperty())
                .append(' ');
        args.addAll(Arrays.asList(restriction.getArgs()));
        return this;
    }

    @Override
    public AliceQueryBuilder<T> limit(int offset, int size) {
        StringBuilder builder = new StringBuilder();
        if (size > 0) {
            builder.append(LIMIT).append(' ').append(size);
            if (offset > 0) {
                builder.append(' ').append(OFFSET).append(' ').append(offset);
            }
        }
        limitation = builder.toString();
        return this;
    }

    public AliceQuery<T> build() {
        if (builder.length() == 0) {
            return buildFindAllQuery();
        }
        return buildQuery(AliceQuery.QueryType.SELECT, null);
    }

    public AliceQuery<T> buildFindAllQuery() {
        return buildQuery(AliceQuery.QueryType.SELECT, null);
    }

    @Override
    public AliceQuery<T> buildDelete() {
        return buildQuery(AliceQuery.QueryType.DELETE, null);
    }

    @Override
    public AliceQuery<T> buildSelect() {
        return buildQuery(AliceQuery.QueryType.SELECT, null);
    }

    @Override
    public AliceQuery<T> buildInsert(ContentValues values) {
        return buildQuery(AliceQuery.QueryType.INSERT, values);
    }

    @Override
    public AliceQuery<T> buildUpdate(ContentValues values) {
        return buildQuery(AliceQuery.QueryType.UPDATE, values);
    }

    @NonNull
    protected AliceQuery<T> buildQuery(AliceQuery.QueryType type, ContentValues contentValues) {
        return new AliceSimpleQuery<T>(builder.toString().trim(), args.toArray(new String[args.size()]), cls, type, contentValues, limitation);
    }
}
