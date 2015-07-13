package com.alice.tools;

import android.util.Log;

import com.alice.exceptions.EntityInstantiationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public final class ReflectionTools {

    public static final String TAG = ReflectionTools.class.getSimpleName();

    ReflectionTools() {
    }

    /**
     * Sets field value using reflection
     * @param field target field
     * @param target object to set property's value
     * @param val value to set
     */
    public void setValue(Field field, Object target, Object val) {
        field.setAccessible(true);
        try {
            field.set(target, val);
        } catch (Throwable e) {
            Log.e(TAG, "Could not set value", e);
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(false);
        }
    }

    /**
     * Gets field value using reflection
     * @param field target field
     * @param target object to set property's value
     */
    public Object getValue(Field field, Object target) {
        field.setAccessible(true);
        try {
            return field.get(target);
        } catch (Throwable e) {
            Log.e(TAG, "Could not get value", e);
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(false);
        }
    }

    /**
     * Filters declared fields and returns list of fields, annotated with the ginev annotation
     * @param annotationClass annotation for a field
     * @return list of declared fields with the given annotation
     */
    public List<Field> getFieldsAnnotatedWith(Class<?> objetClass, Class<? extends Annotation> annotationClass) {
        Field[] all = objetClass.getDeclaredFields();
        List<Field> fields = new ArrayList<Field>(all.length);
        for (Field field : all) {
            if (field.getAnnotation(annotationClass) != null) {
                fields.add(field);
            }
        }
        return fields;
    }


    /**
     * Creates an instance of the given class
     * @param entityClass class type to be instantiated
     * @return created instance
     */
    public <T> T createEntity(Class<T> entityClass) {
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
}
