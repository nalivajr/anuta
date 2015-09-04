package by.nalivajr.anuta.components.database.query;

import android.net.Uri;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface AnutaQueryWithUri<T> extends AnutaQuery<T> {

    /**
     * @return the uri of table which will be queried
     */
    public Uri getUri();
}
