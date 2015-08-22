package by.nalivajr.anuta.components.database.models;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class SimpleEntityCache implements EntityCache {

    private Map<Class<?>, Map<Long, Object>> cacheMap;

    public SimpleEntityCache() {
        this.cacheMap = new LinkedHashMap<Class<?>, Map<Long, Object>>();
    }

    @Override
    public <T> Collection<T> getEntitiesOfClass(Class<T> entityClass) {
        Map<Long, Object> objectMap = cacheMap.get(entityClass);
        if (objectMap == null) {
            objectMap = new LinkedHashMap<Long, Object>();
            cacheMap.put(entityClass, objectMap);
        }
        return (Collection<T>) objectMap.values();
    }

    @Override
    public <T> T getByRowId(Class<T> entityClass, Long rowId) {
        Map<Long, Object> entityMap = cacheMap.get(entityClass);
        if (entityMap == null) {
            return null;
        }
        return (T) entityMap.get(rowId);
    }

    @Override
    public <T> void put(T entity, Long rowId) {
        if (entity == null) {
            return;
        }
        Map<Long, Object> objectMap = cacheMap.get(entity.getClass());
        if (objectMap == null) {
            objectMap = new LinkedHashMap<Long, Object>();
            cacheMap.put(entity.getClass(), objectMap);
        }
        objectMap.put(rowId, entity);
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }

    @Override
    public boolean containsEntity(Class<?> entityClass, Long rowId) {
        Map<Long, Object> entityMap = cacheMap.get(entityClass);
        return entityMap != null && entityMap.containsKey(rowId);
    }
}
