package com.alice.sample.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface Contract {

    public static final int DATABASE_VERSION = 1;
    public static final String AUTHORITY = "alicesample";

    public static interface SubSubItem extends BaseColumns {

        public static final String TABLE_NAME = "SubSubItem";
        public static final String ENTITY_NAME = "SubSubItem";

        public static final String URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build()
                .toString();
        public static final String[] uris = new String[] {URI};
    }
}
