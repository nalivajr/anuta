package by.nalivajr.anuta.utils;

import java.util.Collection;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class CollectionsUtil {

    /**
     * Converts collection to array of strings
     * @param collection source collection
     */
    public static String[] toStringArray(Collection collection) {
        if (collection == null) {
            return null;
        }
        if (collection.isEmpty()) {
            return new String[]{};
        }
        int index = 0;
        String[] result = new String[collection.size()];
        for (Object item : collection) {
            if (item == null) {
                result[index++] = null;
            }
            result[index++] = String.valueOf(item);
        }
        return result;
    }
}
