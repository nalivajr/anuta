package com.alice.components.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.alice.annonatations.db.Column;
import com.alice.annonatations.db.Entity;
import com.alice.components.database.models.Persistable;
import com.alice.exceptions.EntityInstantiationException;
import com.alice.exceptions.NotAnnotatedEntityException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractEntityManager implements AliceEntityManager {

    public static final String TAG = AbstractEntityManager.class.getSimpleName();

    private Context context;

    public AbstractEntityManager(Context context) {
        this.context = context;
    }

    /**
     * Converts cursor data to entities.
     * @param entityClass target entity class
     * @param cursor cursor with data
     * @param count amount of entities to read from cursor. -1 means all possible entities (all form cursor)
     * @return list of converted entities. If no entitites was read or any error occurred should return empty list
     */
    protected abstract <I, T extends Persistable<I>> List<T> convertCursorToEntities(Class<T> entityClass, Cursor cursor, int count);

    /**
     * Returns the projection for the given entity class
     * @param entityClass target entity class
     * @return an array of Strings with column names or null if all columns are requested
     */
    protected abstract <I, T extends Persistable<I>> String[] getProjection(Class<T> entityClass);

    /**
     * Converts entity to content values object
     * @param entity source entity to be converted
     * @return {@link ContentValues} object, representing source entity
     */
    protected abstract  <I, T extends Persistable<I>> ContentValues convertToContentValues(T entity);

    @Override
    public <I, T extends Persistable<I>> T find(Class<T> entityClass, I id) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException();
        }

        Uri uri = getUri(entityClass);

        T entity = createEntity(entityClass);
        String idColumn = entity.getIdColumnName();

        Cursor cursor = getContext().getContentResolver().query(uri, getProjection(entityClass), idColumn + "=?", new String[]{id.toString()}, null);
        List<T> entities = convertCursorToEntities(entityClass, cursor, 1);
        if (entities == null) {
            return null;
        }
        return entities.isEmpty() ? null : entities.get(0);
    }

    @Override
    public <I, T extends Persistable<I>> List<T> findAll(Class<T> entityClass) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException();
        }
        Uri uri = getUri(entityClass);

        Cursor cursor = getContext().getContentResolver().query(uri, getProjection(entityClass), null, null, null);
        List<T> entities = convertCursorToEntities(entityClass, cursor, -1);
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities;
    }

    @Override
    public <I, T extends Persistable<I>> T save(T entity) {
        Class<? extends Persistable> entityClass = entity.getClass();
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException();
        }

        ContentValues contentValues = convertToContentValues(entity);

        Uri newUri = getContext().getContentResolver().insert(getUri(entity.getClass()), contentValues);
        if (newUri != null) {
            long rowId = ContentUris.parseId(newUri);
            entity.setRowId(rowId);
        }
        return entity;
    }

    @Override
    public <I, T extends Persistable<I>> T update(T entity) {
        if (entity == null) {
            throw new RuntimeException("Attempt to update null entity");
        }
        if (entity.getRowId() == null) {
            return save(entity);
        }
        Class<? extends Persistable> entityClass = entity.getClass();
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException();
        }

        ContentValues contentValues = convertToContentValues(entity);

        getContext().getContentResolver().update(getUri(entity.getClass()), contentValues, entity.getIdColumnName() + "=?", new String[]{entity.getId().toString()});
        return entity;
    }

    @Override
    public <I, T extends Persistable<I>> boolean delete(Class<T> entityClass, I id) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException();
        }
        Uri uri = getUri(entityClass);
        T entity = createEntity(entityClass);
        String idColumn = entity.getIdColumnName();
        int res = context.getContentResolver().delete(uri, idColumn + "=?", new String[] { id.toString() } );
        return res != 0;
    }

    @Override
    public <I, T extends Persistable<I>> boolean delete(T entity) {
        if (entity == null) {
            Log.w(TAG, "Attempt to delete null entity");
            return false;
        }
        return delete(entity.getClass(), entity.getId());
    }

    protected String getFieldKey(Field field) {
        Column annotation = field.getAnnotation(Column.class);
        String key = annotation.value();
        if (key == null) {
            key = field.getName();
        }
        return key;
    }

    protected <I, T extends Persistable<I>> Uri getUri(Class<T> entityClass) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new NotAnnotatedEntityException();
        }
        try {
            return Uri.parse(entityAnnotation.tableUri());
        } catch (Throwable e) {
            Log.e(TAG, String.format("Uri for entity %s is incorrect", entityClass.getSimpleName()), e);
            throw e;
        }
    }

    protected  <I, T extends Persistable<I>> T createEntity(Class<T> entityClass) {
        try {
            return entityClass.newInstance();
        } catch (InstantiationException e) {
            Log.e(TAG, "Instantiation exception during creating instance of " + entityClass.getName(), e);
            throw new EntityInstantiationException(entityClass, e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal access exception during creating instance of " + entityClass.getName(), e);
            throw new EntityInstantiationException(entityClass, e);
        }
    }

    protected Context getContext() {
        return context;
    }
}
