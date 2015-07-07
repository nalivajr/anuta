package com.alice.components.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alice.annonatations.database.Column;
import com.alice.annonatations.database.Entity;
import com.alice.annonatations.database.Id;
import com.alice.components.database.models.Identifiable;
import com.alice.exceptions.NotRegisteredEntityClassUsedException;
import com.alice.tools.Alice;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class AbstractEntityManager implements AliceEntityManager {

    public static final String TAG = AbstractEntityManager.class.getSimpleName();

    private Context context;
    private HashSet<Class<?>> entitiesSet;

    public AbstractEntityManager(Context context) {
        this.context = context;
        entitiesSet = new HashSet<>(getEntityClasses());
        Alice.databaseTools.validateEntityClasses(entitiesSet);
    }

    /**
     * Converts cursor data to entities.
     * @param entityClass target entity class
     * @param cursor cursor with data
     * @param count amount of entities to read from cursor. -1 means all possible entities (all form cursor)
     * @return list of converted entities. If no entitites was read or any error occurred should return empty list
     */
    protected abstract <T> List<T> convertCursorToEntities(Class<T> entityClass, Cursor cursor, int count);

    /**
     * Returns the projection for the given entity class
     * @param entityClass target entity class
     * @return an array of Strings with column names or null if all columns are requested
     */
    protected abstract <T> String[] getProjection(Class<T> entityClass);

    /**
     * Converts entity to content values object
     * @param entity source entity to be converted
     * @return {@link ContentValues} object, representing source entity
     */
    protected abstract <T> ContentValues convertToContentValues(T entity);

    /**
     * @return list of entity classes managed by this entity manager
     */
    protected abstract List<Class<?>> getEntityClasses();

    @Override
    public <T> T find(Class<T> entityClass, String id) {
        checkClassRegistered(entityClass);

        Uri uri = getUri(entityClass);

        String idColumn = getIdColumnName(entityClass);

        Cursor cursor = getContext().getContentResolver().query(uri, getProjectionWithRowId(entityClass), idColumn + "=?", new String[]{id}, null);
        List<T> entities = convertCursorToEntities(entityClass, cursor, 1);
        if (entities == null) {
            return null;
        }
        return entities.isEmpty() ? null : entities.get(0);
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        checkClassRegistered(entityClass);

        Uri uri = getUri(entityClass);

        Cursor cursor = getContext().getContentResolver().query(uri, getProjectionWithRowId(entityClass), null, null, null);
        List<T> entities = convertCursorToEntities(entityClass, cursor, -1);
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities;
    }

    @Override
    public <T> T save(T entity) {
        if (entity == null) {
            throw new RuntimeException("Attempt to save null entity");
        }
        checkClassRegistered(entity.getClass());

        ContentValues contentValues = convertToContentValues(entity);

        Uri newUri = getContext().getContentResolver().insert(getUri(entity.getClass()), contentValues);
        if (newUri != null) {
            long rowId = ContentUris.parseId(newUri);
            setEntityRowId(entity, rowId);
        }
        return entity;
    }

    @Override
    public <T> T update(T entity) {
        if (entity == null) {
            throw new RuntimeException("Attempt to update null entity");
        }
        if (Alice.databaseTools.getRowId(entity) == null) {
            return save(entity);
        }
        checkClassRegistered(entity.getClass());

        ContentValues contentValues = convertToContentValues(entity);

        getContext().getContentResolver().update(getUri(entity.getClass()), contentValues,
                getIdColumnName(entity.getClass()) + "=?", new String[]{getEntityId(entity)});
        return entity;
    }

    @Override
    public <T> boolean delete(Class<T> entityClass, String id) {
        checkClassRegistered(entityClass);
        if (id == null) {
            Log.w(TAG, "Attempt to delete entity with null id");
            return false;
        }
        Uri uri = getUri(entityClass);
        String idColumn = getIdColumnName(entityClass);
        int res = context.getContentResolver().delete(uri, idColumn + "=?", new String[] { id } );
        return res != 0;
    }

    @Override
    public <T> boolean delete(T entity) {
        if (entity == null) {
            Log.w(TAG, "Attempt to delete null entity");
            return false;
        }
        checkClassRegistered(entity.getClass());
        String strId = getEntityId(entity);
        return delete(entity.getClass(), strId);
    }

    @Nullable
    private <T> String getEntityId(T entity) {
        Object id = null;
        if (entity instanceof Identifiable) {
            id = ((Identifiable) entity).getId();
        } else {
            Field idFiled = Alice.reflectionTools.getFieldsAnnotatedWith(entity.getClass(), Id.class).get(0);
            id = Alice.reflectionTools.getValue(idFiled, entity);
        }
        String strId = null;
        if (id != null) {
            strId = id.toString();
        }
        return strId;
    }

    protected void checkClassRegistered(Class<?> entityClass) {
        if (!entitiesSet.contains(entityClass)) {
            throw new NotRegisteredEntityClassUsedException(entityClass, this.getClass());
        }
    }

    protected <T> void setEntityRowId(T entity, long rowId) {
        Alice.databaseTools.setRowId(entity, rowId);
    }

    protected <T> Uri getUri(Class<T> entityClass) {
        checkClassRegistered(entityClass);
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnnotation.tableName();
        if (tableName == null) {
            tableName = entityClass.getSimpleName();
        }
        String authority = entityAnnotation.authority();

        try {
            return new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(authority)
                    .appendPath(tableName)
                    .build();
        } catch (Throwable e) {
            Log.e(TAG, String.format("Uri for entity %s is incorrect", entityClass.getSimpleName()), e);
            throw e;
        }
    }

    private <T> String getIdColumnName(Class<T> entityClass) {
        List<Field> fields = Alice.reflectionTools.getFieldsAnnotatedWith(entityClass, Id.class);
        return getFieldKey(fields.get(0));
    }

    private <T> String[] getProjectionWithRowId(Class<T> entityClass) {
        String[] projection = getProjection(entityClass);
        for (String column : projection) {
            if (column.equals(BaseColumns._ID)) {
                return projection;
            }
        }
        String[] projectionExt = new String[projection.length + 1];
        projectionExt[0] = BaseColumns._ID;
        System.arraycopy(projection, 0, projectionExt, 1, projection.length);
        return projectionExt;
    }

    protected String getFieldKey(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        Id idAnnotation = field.getAnnotation(Id.class);
        if (columnAnnotation == null && idAnnotation != null) {
            return field.getName();
        }
        String key = columnAnnotation.value();
        if (key.isEmpty()) {
            key = field.getName();
        }
        return key;
    }

    protected  <T> T createEntity(Class<T> entityClass) {
        return Alice.reflectionTools.createEntity(entityClass);
    }

    protected Context getContext() {
        return context;
    }
}
