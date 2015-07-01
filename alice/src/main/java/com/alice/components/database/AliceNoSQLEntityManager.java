package com.alice.components.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.alice.annonatations.db.Entity;
import com.alice.components.database.models.Persistable;
import com.alice.components.database.providers.AliceNoSQLProvider;
import com.alice.exceptions.NotAnnotatedEntityException;
import com.alice.tools.Alice;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AliceNoSQLEntityManager extends AbstractEntityManager {

    public static final String TAG = AliceNoSQLEntityManager.class.getSimpleName();

    private AliceNoSQLProvider provider;

    public AliceNoSQLEntityManager(AliceNoSQLProvider contentProvider) {
        super(contentProvider);
        this.provider = contentProvider;
    }

    @Override
    public <I, T extends Persistable<I>> T find(Class<T> entityClass, I id) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException();
        }
        String columnName = Alice.DatabaseTools.buildJsonDataColumnName(entityClass);
        Cursor cursor = provider.query(null, new String[]{columnName}, "id=?", new String[]{id.toString()}, null);
        if (cursor == null) {
            return null;
        }
        String json = cursor.getString(cursor.getColumnIndex(columnName));

        return null;
    }

    @Override
    public <I, T extends Persistable<I>> T update(T entity) {
        return null;
    }

    @Override
    public <I, T extends Persistable<I>> List<T> findAll(Class<T> entityClass) {
        return null;
    }

    @Override
    public <I, T extends Persistable<I>> T save(T entity) {
        Class<? extends Persistable> entityClass = entity.getClass();
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException();
        }

        List<Field> fields = Alice.DatabaseTools.extractIndexedFields(entityAnnotation, entityClass);
        ContentValues contentValues = new ContentValues();
        for (Field field : fields) {
            field.setAccessible(true);
            String key = getFieldKey(field);
            Object val = getFieldValue(field, entity);
            putValue(contentValues, key, val);
            field.setAccessible(false);
        }

        provider.insert(null, contentValues);
        return entity;
    }

    @Override
    public <I, T extends Persistable<I>> boolean delete(Class<T> entityClass, I id) {
        return false;
    }

    @Override
    public <I, T extends Persistable<I>> boolean delete(T entity) {
        return false;
    }
}
