package com.alice.components.database;

import android.content.ContentValues;
import android.util.Log;

import com.alice.annonatations.db.Column;
import com.alice.components.database.models.Persistable;
import com.alice.components.database.providers.AliceContentProvider;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractEntityManager implements AliceEntityManager {

    public static final String TAG = AbstractEntityManager.class.getSimpleName();

    public AbstractEntityManager(AliceContentProvider contentProvider) {

    }

    protected String getFieldKey(Field field) {
        Column annotation = field.getAnnotation(Column.class);
        String key = annotation.value();
        if (key == null) {
            key = field.getName();
        }
        return key;
    }


    protected <I, T extends Persistable<I>> Object getFieldValue(Field field, T entity) {
        Column annotation = field.getAnnotation(Column.class);
        Column.DataType dataType = annotation.dataType();


        Object val = null;
        field.setAccessible(true);
        try {
            val = field.get(entity);
        } catch (Throwable e) {
            Log.e(TAG, "Could not get value", e);
            return null;
        }
        field.setAccessible(false);
        if (val == null) {
            return null;
        }

        switch (dataType) {
            case DATE_MILLIS:
                return ((Date) val).getTime();
            case ENUM_ORDINAL:
                return ((Enum)val).ordinal();
            case DATE_TIMESTAMP:
                return new Timestamp(((Date) val).getTime());
            case ENUM_STRING:
                return ((Enum)val).name();
            case JSON_STRING:
                return "";
            case TO_STRING_RESULT:
                return val.toString();
            case BLOB_STRING:
                return ((String)val).getBytes();
            case BLOB:
                if (val instanceof byte[]) {
                    return val;
                }
            default:
            case AUTO:
                Class cls = field.getType();
                if (cls == Boolean.TYPE || cls == Boolean.class) {
                    return val;
                }
                if (cls.isPrimitive() || Number.class.isAssignableFrom(cls)) {
                    if (cls == Character.TYPE || cls == Character.class) {
                        return (int) (Character) val;
                    }
                    return val;
                }
                if (cls.isEnum()) {     //ENUM_STRING
                    return ((Enum)val).name();
                }
                if (val instanceof String) {
                    return val;
                }
                return val.toString();
        }
    }

    protected void putValue(ContentValues contentValues, String key, Object val) {
        if (val == null) {
            contentValues.putNull(key);
        } else if (val instanceof Boolean) {
            contentValues.put(key, (Boolean)val);
        } else if (val instanceof String) {
            contentValues.put(key, (String)val);
        } else if (val instanceof byte[]) {
            contentValues.put(key, (byte)val);
        } else if (val instanceof Byte) {
            contentValues.put(key, (Byte)val);
        } else if (val instanceof Short) {
            contentValues.put(key,((Short)val));
        } else if (val instanceof Integer) {
            contentValues.put(key, (Integer)val);
        } else if (val instanceof Long) {
            contentValues.put(key, (Long)val);
        } else if (val instanceof Float) {
            contentValues.put(key, (Float)val);
        } else if (val instanceof Double) {
            contentValues.put(key, (Double)val);
        }
    }
}
