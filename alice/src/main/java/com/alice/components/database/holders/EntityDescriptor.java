package com.alice.components.database.holders;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class EntityDescriptor {

    private String entityName;
    private String tableName;

    private List<Field> fields;
    private Map<Field, String> fieldsToKeyMap;


}
