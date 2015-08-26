package by.nalivajr.anuta.components.database.stub;

import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import by.nalivajr.anuta.components.database.query.AnutaQuery;
import by.nalivajr.anuta.tools.Anuta;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public abstract class RelatedEntitiesProxyFactory {

    private static final String TAG = RelatedEntitiesProxyFactory.class.getName();

    public static <T> Collection<T> getNotInitializedCollection(Class<? extends Collection<T>> type, AnutaQuery<T> query) {
        if (type.isArray()) {
            return null;
        }
        if (Set.class.isAssignableFrom(type)) {
            return new LazyInitializationSet<T>(type, query);
        }
        return new LazyInitializationList<T>(type, query);
    }


    public static Object getCorrectTypeObject(Class type, Collection related) {
        if (related == null) {
            return related;
        }
        if (type.isArray()) {
            Object[] array = (Object[]) Array.newInstance(type.getComponentType(), related.size());
            return related.toArray(array);
        }
        return getCorrectTypeCollection(type, related);
    }


    public static Collection getCorrectTypeCollection(Class type, Collection related) {
        if (related == null) {
            return related;
        }
        if (related instanceof AbstractLazyInitializationCollection) {
            return related;
        }
        Collection result = null;
        if (Set.class.isAssignableFrom(type) && type.isInterface()) {
            result = createSetInstance(type);
        } else if (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) {
            result = createListInstance(type);
        } else {
            throw new RuntimeException("Could not load entities to unknown collection " + type.getName());
        }
        result.addAll(related);
        return result;
    }

    private static Set createSetInstance(Class type) {
        if (type.isInterface()) {
            return createHashSet();
        }
        try {
            return (Set) Anuta.reflectionTools.createEntity(type);
        } catch (Throwable e) {
            Log.e(TAG, "Could create Set of type: " + type + "Using HashSet instead");
            return createHashSet();
        }
    }

    private static List createListInstance(Class type) {
        if (type.isInterface()) {
            return createArrayLIst();
        }
        try {
            return (List) Anuta.reflectionTools.createEntity(type);
        } catch (Throwable e) {
            Log.e(TAG, "Could create List of type: " + type + "Using ArrayList instead");
            return createArrayLIst();
        }
    }

    @NonNull
    private static HashSet createHashSet() {
        return new HashSet();
    }

    @NonNull
    private static ArrayList createArrayLIst() {
        return new ArrayList();
    }
}
