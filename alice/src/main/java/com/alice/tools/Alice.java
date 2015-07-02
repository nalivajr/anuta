package com.alice.tools;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alice.annonatations.db.Column;
import com.alice.annonatations.db.Entity;
import com.alice.annonatations.db.Id;
import com.alice.annonatations.ui.AutoActivity;
import com.alice.annonatations.ui.AutoFragment;
import com.alice.annonatations.ui.AutoView;
import com.alice.annonatations.ui.InnerView;
import com.alice.components.database.models.Identifiable;
import com.alice.components.database.models.Persistable;
import com.alice.exceptions.ConversionException;
import com.alice.exceptions.IncorrectMapingException;
import com.alice.exceptions.InvalidDataTypeException;
import com.alice.exceptions.NotAnnotatedActivityUsedException;
import com.alice.exceptions.NotAnnotatedEntityException;
import com.alice.exceptions.NotAnnotatedFieldException;
import com.alice.exceptions.NotAnnotatedFragmentUsedException;
import com.alice.exceptions.ObjectDeserializationException;
import com.alice.exceptions.ObjectSerializationException;
import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class Alice {

    public static abstract class ViewTools {

        public static final String TAG = String.format("%s.%s", Alice.class.getSimpleName(), ViewTools.class.getSimpleName());

        /**
         * Sets content view to activity. Finds and injects all views, annotated with {@link InnerView}
         *
         * @param activity the activity to setContent view
         * @return the view, set as a content
         * @throws NotAnnotatedActivityUsedException if activity is not annotated with {@link AutoActivity}
         */
        public static View setContentView(Activity activity) throws NotAnnotatedActivityUsedException {
            AutoActivity annotation = activity.getClass().getAnnotation(AutoActivity.class);
            if (annotation == null) {
                throw new NotAnnotatedActivityUsedException();
            }
            int id = annotation.layoutId();
            boolean recursive = annotation.recursive();
            return setContentView(activity, id, recursive);
        }

        /**
         * Creates layout for fragment. Finds and injects all views, annotated with {@link InnerView}
         *
         * @param fragment the fragment to create view
         * @param context  the context
         * @return the view, set as a root view
         */
        public static View createView(Fragment fragment, Context context) {
            AutoFragment annotation = fragment.getClass().getAnnotation(AutoFragment.class);
            if (annotation == null) {
                throw new NotAnnotatedFragmentUsedException();
            }
            int layoutId = annotation.layoutId();
            boolean recursive = annotation.recursive();
            return createView(context, fragment, layoutId, recursive);
        }

        /**
         * Creates layout for fragment. Finds and injects all views, annotated with {@link InnerView}
         *
         * @param fragment the fragment to create view
         * @param context  the context
         * @return the view, set as a root view
         */
        public static View createView(Context context, Fragment fragment, int layoutId, boolean recursive) {
            View view = LayoutInflater.from(context).inflate(layoutId, null);

            Field[] fields = fragment.getClass().getDeclaredFields();
            for (Field field : fields) {
                InnerView innerViewAnnotation = field.getAnnotation(InnerView.class);
                if (innerViewAnnotation != null) {
                    int id = innerViewAnnotation.value();
                    View v = view.findViewById(id);
                    field.setAccessible(true);
                    try {
                        field.set(fragment, v);
                    } catch (IllegalAccessException e) {
                        Log.w(TAG, "Could not initialize annotated field", e);
                    }
                    field.setAccessible(false);
                }
            }
            initView(context, view, recursive);
            return view;
        }

        /**
         * Sets content view to activity. Finds and injects all views, annotated with {@link InnerView}
         *
         * @param activity the activity to setContent view
         * @param layoutId the id of layout resource
         * @return the view, set as a content
         */
        public static View setContentView(Activity activity, int layoutId, boolean recursive) {
            View root = activity.getLayoutInflater().inflate(layoutId, null);
            activity.setContentView(root);

            Field[] fields = activity.getClass().getDeclaredFields();
            for (Field field : fields) {
                InnerView innerViewAnnotation = field.getAnnotation(InnerView.class);
                if (innerViewAnnotation != null) {
                    int id = innerViewAnnotation.value();
                    View v = root.findViewById(id);
                    field.setAccessible(true);
                    try {
                        field.set(activity, v);
                    } catch (IllegalAccessException e) {
                        Log.w(TAG, "Could not initialize annotated field", e);
                    }
                    field.setAccessible(false);
                }
            }
            initView(activity, root, recursive);
            return root;
        }

        /**
         * Finds and injects all views, annotated with {@link InnerView}
         *
         * @param view      target view
         * @param recursive if true then all sub views in hierarchy will be initialized too
         */
        public static void initView(Context context, View view, boolean recursive) {
            if (view == null) {
                return;
            }

            if (ViewGroup.class.isAssignableFrom(view.getClass())) {
                AutoView autoView = view.getClass().getAnnotation(AutoView.class);
                ViewGroup v = (ViewGroup) view;
                if (autoView != null) {
                    int layoutId = autoView.layoutId();
                    LayoutInflater.from(context).inflate(layoutId, v);
                    recursive = autoView.recursive();
                }

                for (int i = 0; i < v.getChildCount() && recursive; i++) {
                    View child = v.getChildAt(i);
                    initView(context, child, recursive);
                }
            }

            Field[] fields = view.getClass().getDeclaredFields();
            for (Field field : fields) {
                InnerView innerViewAnnotation = field.getAnnotation(InnerView.class);
                if (innerViewAnnotation != null) {
                    int id = innerViewAnnotation.value();
                    View v = view.findViewById(id);
                    field.setAccessible(true);
                    try {
                        field.set(view, v);
                    } catch (IllegalAccessException e) {
                        Log.w(TAG, "Could not initialize annotated field", e);
                    }
                    field.setAccessible(false);
                }
                if (View.class.isAssignableFrom(field.getType()) && (field.getType().getAnnotation(AutoView.class) != null)) {
                    field.setAccessible(true);
                    try {
                        View v = (View) field.get(view);
                        initView(context, v, recursive);
                    } catch (IllegalAccessException e) {
                        Log.w(TAG, "Could not get access", e);
                    }
                    field.setAccessible(false);
                }
            }
        }
    }

    public static abstract class AdapterTools {

        private static final String LOG_TAG = String.format("%s.%s", Alice.class.getSimpleName(), AdapterTools.class.getSimpleName()).toUpperCase();

        /**
         * Parses XML layouts from resources and extracts ids for all widgets for this.
         * BE AWARE to parse many layout and big layouts in main tread as it can be heavy operation. You can build the map
         * by yourself, if needed
         * @param context the context
         * @param resLayoutsIds a set of ids for layouts to be parsed
         * @return the map, which contains pairs: layout-resource-id -> list of ids of widgets from this layout
         */
        public static Map<Integer, List<Integer>> buildIdsMap(Context context , int ... resLayoutsIds) {
            Map<Integer, List<Integer>> result = new HashMap<>();
            if (resLayoutsIds == null || resLayoutsIds.length == 0) {
                return result;
            }
            for (int layoutId : resLayoutsIds) {
                List<Integer> ids = parseLayout(context, layoutId);
                result.put(layoutId, ids);
            }
            return result;
        }

        private static List<Integer> parseLayout(Context context, int layoutId) {
            List<Integer> ids = new ArrayList<>();
            XmlResourceParser parser = context.getResources().getLayout(layoutId);
            try {
                int eventType = parser.next();

                while (eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType != XmlPullParser.START_TAG) {
                        eventType = parser.next();
                        continue;
                    }
                    Log.i(LOG_TAG, "Parsing: start TAG: " + parser.getName());
                    extractId(ids, parser);
                    eventType = parser.next();
                }
            } catch (Resources.NotFoundException e) {
                Log.w(LOG_TAG, String.format("Resource layout with id: %d was not found", layoutId), e);
            } catch (XmlPullParserException e) {
                Log.w(LOG_TAG, String.format("And error occurred during parsing layout with id: %d", layoutId), e);
            } catch (IOException e) {
                Log.w(LOG_TAG, String.format("IOException occurred during parsing layout with id: %d", layoutId), e);
            }
            return ids;
        }

        private static void extractId(List<Integer> ids, XmlResourceParser parser) {
            int count = parser.getAttributeCount();
            for (int i = 0; i < count; i++) {
                String name = parser.getAttributeName(i);
                if (name == null || !name.equalsIgnoreCase("id")) {
                    continue;
                }
                String val = parser.getAttributeValue(i);
                try {
                    Integer id = Integer.parseInt(val.substring(1));
                    ids.add(id);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Could not extract id", e);
                }
                break;
            }
        }
    }

    public static abstract class DatabaseTools {

        public static final String ENTITY_NAME_COLUMN = "entityName";

        private static final String TAG = String.format("%s.%s", Alice.class.getSimpleName(), DatabaseTools.class.getSimpleName());
        private static final Gson gson = new Gson();
        /**
         * Generates tables creation script for the given list of entity classes
         * @param entityClasses classes of entities
         * @return string, representing table creation script;
         * @throws NotAnnotatedEntityException if class is not annotated with {@link Entity}
         */
        public static <T extends Identifiable> String generateRelationalTableScript(List<Class<T>> entityClasses) {
            StringBuilder builder = new StringBuilder();
            for (Class entityClass : entityClasses) {
                builder.append(generateRelationalTableScript(entityClass));
            }
            return builder.toString();
        }

        /**
         * Generates NoSQL tables creation script for the given list of entity classes
         * @param entityClasses classes of entities
         * @return string, representing table creation script;
         * @throws NotAnnotatedEntityException if class is not annotated with {@link Entity}
         */
        public static <T extends Identifiable> String generateNoSQLTableScript(List<Class<T>> entityClasses) {
            StringBuilder builder = new StringBuilder();
            for (Class entityClass : entityClasses) {
                builder.append(generateNoSQLTableScript(entityClass));
            }
            return builder.toString();
        }

        /**
         * Generates table creation script for the given entity class
         * @param cls the class of entity
         * @return string, representing table creation script;
         * @throws NotAnnotatedEntityException if class is not annotated with {@link Entity}
         */
        public static <T extends Identifiable> String generateRelationalTableScript(Class<T> cls) {
            Entity entityAnnotation = cls.getAnnotation(Entity.class);
            if (entityAnnotation == null) {
                throw new NotAnnotatedEntityException();
            }
            String tableName = entityAnnotation.tableName();
            if (tableName.isEmpty()) {
                tableName = cls.getSimpleName();
            }

            StringBuilder builder = new StringBuilder();
            builder.append(String.format("DROP TABLE %s;", tableName));
            builder.append(String.format("CREATE TABLE %s (", tableName));
            List<Field> columnFields = extractFields(cls);
            validateFields(columnFields);

            for (Field field : columnFields) {
                String columnScript = buildColumnDefinition(field);
                builder.append(columnScript);
                builder.append(',');
            }

            String primaryKeyScript = buildPrimaryKeyScript(columnFields);
            builder.append(primaryKeyScript);
            builder.append(')').append(';');
            return builder.toString();
        }

        /**
         * Generates NoSQL mode table creation script for the given entity class
         * @param cls the class of entity
         * @return string, representing table creation script;
         * @throws NotAnnotatedEntityException if class is not annotated with {@link Entity}
         */
        public static <T extends Identifiable> String generateNoSQLTableScript(Class<T> cls) {
            Entity entityAnnotation = cls.getAnnotation(Entity.class);
            if (entityAnnotation == null) {
                throw new NotAnnotatedEntityException();
            }
            String tableName = entityAnnotation.tableName();
            if (tableName.isEmpty()) {
                tableName = cls.getSimpleName();
            }

            StringBuilder builder = new StringBuilder();
            builder.append(String.format("DROP TABLE %s;", tableName));
            builder.append(String.format("CREATE TABLE %s (", tableName));
            List<Field> columnFields = extractFields(cls);
            validateFields(columnFields);

            for (Field field : columnFields) {
                boolean index = false;
                Id idAnno = field.getAnnotation(Id.class);
                Column columnAnno = field.getAnnotation(Column.class);
                index = idAnno != null || columnAnno.index();
                if (!index) {
                    continue;
                }
                String columnScript = buildColumnDefinition(field);
                builder.append(columnScript);
                builder.append(',');
            }
            // Appends definition of columns for entity name and JSON data
            builder
                    .append(ENTITY_NAME_COLUMN + " TEXT,")
                    .append(buildJsonDataColumnName(cls)).append(" TEXT,");

            String primaryKeyScript = buildPrimaryKeyScript(columnFields);
            builder.append(primaryKeyScript);
            builder.append(')').append(';');
            return builder.toString();
        }

        private static void validateFields(List<Field> columnFields) {
            Set<String> columnNames = new HashSet<>();
            for(Field field : columnFields) {
                Column columnAnno = field.getAnnotation(Column.class);
                if (columnAnno == null) {
                    continue;
                }
                String name = columnAnno.value();
                if (name.isEmpty()) {
                    name = field.getName();
                }
                if (columnNames.contains(name)) {
                    throw new IncorrectMapingException(String.format("Column names are not unique. Please check whether all columns have different name including parent objects columns if inheritance mode is enabled. Property: %s in class %s", field.getName(), field.getDeclaringClass().getName()));
                }
                columnNames.add(name);
            }
        }

        public static <T extends Identifiable> String buildJsonDataColumnName(Class<T> cls) {
            return cls.getSimpleName() + "JsonData";
        }

        /**
         * Extract all fields, which are annotated with {@link Column} and should be persisted
         */
        public static <T extends Identifiable> List<Field> extractFields(Class<T> cls) {
            return extractColumnsFields(cls, null);
        }

        @NonNull
        private static <T extends Identifiable> List<Field> extractColumnsFields(Class<T> cls, Boolean indexed) {
            Entity entityAnnotation = cls.getAnnotation(Entity.class);
            if (entityAnnotation == null) {
                throw new NotAnnotatedEntityException();
            }

            List<Field> resultList = new ArrayList<>();
            resultList.addAll(Arrays.asList(cls.getDeclaredFields()));

            Class processing = cls;
            Entity.InheritancePolicy policy = entityAnnotation.inheritColumns();
            while (entityAnnotation != null && policy != Entity.InheritancePolicy.NO) {
                processing = processing.getSuperclass();
                entityAnnotation = (Entity) processing.getAnnotation(Entity.class);
                resultList.addAll(Arrays.asList(processing.getDeclaredFields()));
                if (policy == Entity.InheritancePolicy.PARENT_ONLY_NO_ID || policy == Entity.InheritancePolicy.PARENT_ONLY_COMPOSITE_ID) {
                    break;
                }
                if (policy == Entity.InheritancePolicy.SEQUENTIAL_NO_ID || policy == Entity.InheritancePolicy.SEQUENTIAL_COMPOSITE_ID) {
                    policy = entityAnnotation.inheritColumns();
                }
            }

            for (int i = 0; i < resultList.size(); i++) {
                Field field = resultList.get(i);
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation == null || (indexed != null && columnAnnotation.index() != indexed)) {
                    resultList.remove(i--);
                    continue;
                }
                String name = columnAnnotation.value();
                if (name.isEmpty()) {
                    name = field.getName();
                }
                if (name.equals(BaseColumns._ID) && field.getDeclaringClass() != cls) {
                    resultList.remove(i--);
                    continue;
                }
                Id idColumn = field.getAnnotation(Id.class);
                if (idColumn != null && !isIdShouldBeInherited(cls, field)) {
                    resultList.remove(i--);
                }
            }
            return resultList;
        }

        private static <T extends Identifiable> boolean isIdShouldBeInherited(Class<T> cls, Field field) {
            Entity.InheritancePolicy targetEntityPolicy = cls.getAnnotation(Entity.class).inheritColumns();
            switch (targetEntityPolicy) {
                case NO:
                    return false;
                case HIERARCHY_COMPOSITE_ID:
                    return true;
                case HIERARCHY_NO_ID:
                case PARENT_ONLY_NO_ID:
                    return field.getDeclaringClass() == cls;
                case PARENT_ONLY_COMPOSITE_ID:
                    return cls.getSuperclass() == field.getDeclaringClass() || field.getDeclaringClass() == cls;
            }

            Class fieldClass = field.getDeclaringClass();
            if (fieldClass == cls) {
                return true;
            }
            Class<?> child = cls;
            while (child.getSuperclass() != fieldClass) {
                child = child.getSuperclass();
            }
            Entity.InheritancePolicy policy = child.getAnnotation(Entity.class).inheritColumns();
            switch (policy) {
                case HIERARCHY_COMPOSITE_ID:
                case PARENT_ONLY_COMPOSITE_ID:
                case SEQUENTIAL_COMPOSITE_ID:
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Generates script, which describes column in database
         */
        private static String buildColumnDefinition(Field field) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = columnAnnotation.value();
            if (columnName.isEmpty()) {
                columnName = field.getName();
            }
            String type = dispatchType(field, columnAnnotation.dataType());
            StringBuilder builder = new StringBuilder(columnName.length() + type.length() + 1);
            builder.append(columnName);
            builder.append(' ');
            builder.append(type);
            if (columnName.equals(BaseColumns._ID)) {
                builder.append(" AUTO_INCREMENT");
            }
            return builder.toString();
        }

        private static String buildPrimaryKeyScript(List<Field> columnFields) {
            List<Field> idFields = new ArrayList<>();
            boolean hasRowIdDefinition = false;
            for (Field column : columnFields) {
                Column columnAnnotation = column.getAnnotation(Column.class);
                String name = columnAnnotation.value();
                if (name.isEmpty()) {
                    name = column.getName();
                }
                if (name.equals(BaseColumns._ID) &&
                        Number.class.isAssignableFrom(column.getType()) &&
                        column.getType() != Float.class && column.getType() != Float.TYPE &&
                        column.getType() != Double.class && column.getType() != Double.TYPE) {
                    hasRowIdDefinition = true;
                }
                if (column.getAnnotation(Id.class) != null) {
                    idFields.add(column);
                }
            }
            StringBuilder builder = new StringBuilder();
            if (!hasRowIdDefinition)  {
                builder.append("_id INTEGER AUTO_INCREMENT,");
            }
            builder.append("PRIMARY KEY(");
            for (Field field : idFields) {
                Column column = field.getAnnotation(Column.class);
                String name = column.value();
                if (name.isEmpty()) {
                    name = field.getName();
                }
                builder.append(name);
                builder.append(',');
            }
            builder.setCharAt(builder.length() - 1, ')');
            return builder.toString();
        }

        private static String dispatchType(Field field, Column.DataType dataType) {
            String textType = "TEXT";
            String integerType = "INTEGER";
            String realType = "REAL";
            String blobType = "BLOB";

            switch (dataType) {
                case DATE_MILLIS:
                case ENUM_ORDINAL:
                    return integerType;
                case DATE_TIMESTAMP:
                case ENUM_STRING:
                case JSON_STRING:
                    return textType;
                case SERIALIZABLE:
                case BLOB:
                case BLOB_STRING:
                    return blobType;
                default:
                case AUTO:
                    Class cls = field.getType();
                    if (cls == Boolean.TYPE || cls == Boolean.class) {
                        return textType;
                    }
                    if (cls.isPrimitive() || Number.class.isAssignableFrom(cls)) {
                        if (cls == Float.TYPE || cls == Float.class ||
                                cls == Double.TYPE || cls == Double.class) {
                            return realType;
                        }
                        return integerType;
                    }
                    if (cls.isEnum()) {     //ENUM_STRING
                        return textType;
                    }
                    if (String.class.isAssignableFrom(cls)) {
                        return textType;
                    }
                    return blobType;
            }
        }

        /**
         * Extracts fields values and adopts them according to {@link com.alice.annonatations.db.Column.DataType} for this field
         * @param field value source field
         * @param entity source entity
         * @return converted value, corresponding to {@link com.alice.annonatations.db.Column.DataType}
         */
        public static <I, T extends Persistable<I>> Object getFieldValue(Field field, T entity) {
            Column annotation = field.getAnnotation(Column.class);
            if (annotation == null) {
                throw new NotAnnotatedFieldException(field);
            }
            Column.DataType dataType = annotation.dataType();

            Object val = null;
            field.setAccessible(true);
            try {
                val = field.get(entity);
            } catch (Throwable e) {
                Log.e(TAG, "Could not get value", e);
                return null;
            } finally {
                field.setAccessible(false);
            }
            if (val == null) {
                return null;
            }

            switch (dataType) {
                case DATE_MILLIS:
                    return ((Date) val).getTime();
                case ENUM_ORDINAL:
                    return ((Enum) val).ordinal();
                case DATE_TIMESTAMP:
                    return SimpleDateFormat.getDateTimeInstance().format(val);
                case ENUM_STRING:
                    return ((Enum) val).name();
                case JSON_STRING:
                    return gson.toJson(val);
                case SERIALIZABLE:
                    return toByteArray(dataType, field, val);
                case BLOB_STRING:
                    return ((String)val).getBytes();
                case BLOB:
                    if (val instanceof byte[]) {
                        return val;
                    }
                default:
                case AUTO:
                    Class cls = field.getType();
                    if (cls == Boolean.TYPE || cls == Boolean.class) {
                        return String.valueOf(val);
                    }
                    if (cls.isPrimitive() || Number.class.isAssignableFrom(cls)) {
                        if (cls == Character.TYPE || cls == Character.class) {
                            return (int) (Character) val;
                        }
                        return val;
                    }
                    if (cls.isEnum()) {     //ENUM_STRING
                        return ((Enum)val).name();
                    }
                    if (val instanceof String) {
                        return val;
                    }
                    if (val instanceof Serializable) {
                        return toByteArray(Column.DataType.SERIALIZABLE, field, val);
                    }
                    return val.toString();
            }
        }

        /**
         * Converts value from data type to entity's property value
         * @param field target field
         * @param dataType type for storing in DB
         * @param val the value to be converted
         * @return converted value
         */
        public static Object convert(Field field, Column.DataType dataType, Object val) {

            if (val == null) {
                return null;
            }

            switch (dataType) {
                case DATE_MILLIS:
                    return new Date((Long) val);
                case ENUM_ORDINAL:
                    int index = ((Number) val).intValue();
                    return (field.getType().getEnumConstants())[index];
                case DATE_TIMESTAMP:
                    try {
                        return SimpleDateFormat.getDateTimeInstance().parse((String) val);
                    } catch (ParseException e) {
                        Log.e(TAG, "Could not parse timestamp to date", e);
                    }
                case ENUM_STRING:
                    Class<?> type = field.getType();
                    if (!type.isEnum()) {
                        throw new InvalidDataTypeException(dataType, field);
                    }
                    Enum[] enumConstants = (Enum[]) type.getEnumConstants();
                    String name = (String) val;
                    for (Enum e : enumConstants) {
                        if (e.name().equals(name)) {
                            return e;
                        }
                    }
                    throw new ConversionException(val, type);
                case JSON_STRING:
                    return gson.fromJson((String)val, field.getType());
                case SERIALIZABLE:
                    return readObject(dataType, field, (byte[])val);
                case BLOB_STRING:
                    return new String((byte[])val);
                case BLOB:
                    if (val instanceof byte[]) {
                        return val;
                    }
                default:
                case AUTO:
                    Class cls = field.getType();
                    if (cls == Boolean.TYPE || cls == Boolean.class) {
                        return Boolean.parseBoolean((String) val);
                    }
                    if (cls.isPrimitive() || Number.class.isAssignableFrom(cls) || Character.class.isAssignableFrom(cls)) {
                        if (cls == Character.class || cls == Character.TYPE) {
                            return (char) val;
                        }
                        return cls.cast(val);
                    }
                    if (cls.isEnum()) {     //ENUM_STRING
                        return ((Enum)val).name();
                    }
                    if (val instanceof String) {
                        return val;
                    }
                    if (val instanceof byte[]) {
                        readObject(dataType, field, (byte[])val);
                    }
                    return readObject(Column.DataType.SERIALIZABLE, field, (byte[]) val);
            }
        }

        /**
         * Puts value according to its type to {@link ContentValues} object
         * @param contentValues destination
         * @param key value key
         * @param val the value to be put
         */
        public static void putValue(ContentValues contentValues, String key, Object val) {
            if (val == null) {
                contentValues.putNull(key);
            } else if (val instanceof Boolean) {
                contentValues.put(key, (Boolean)val);
            } else if (val instanceof String) {
                contentValues.put(key, (String)val);
            } else if (val instanceof byte[]) {
                contentValues.put(key, (byte)val);
            } else if (val instanceof Byte) {
                contentValues.put(key, (Byte)val);
            } else if (val instanceof Short) {
                contentValues.put(key,((Short)val));
            } else if (val instanceof Integer) {
                contentValues.put(key, (Integer)val);
            } else if (val instanceof Long) {
                contentValues.put(key, (Long)val);
            } else if (val instanceof Float) {
                contentValues.put(key, (Float)val);
            } else if (val instanceof Double) {
                contentValues.put(key, (Double)val);
            }
        }

        private static byte[] toByteArray(Column.DataType dataType, Field field, Object val) {
            if (!(val instanceof Serializable)) {
                throw new InvalidDataTypeException(dataType, field);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = null;
            try {
                objectOutputStream = new ObjectOutputStream(baos);
                objectOutputStream.writeObject(val);
                return baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                throw new ObjectSerializationException(dataType, field);
            }
        }

        private static Object readObject(Column.DataType dataType, Field field, byte[] bytes) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(byteArrayInputStream);
                return objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new ObjectDeserializationException(dataType, field);
            }
        }
    }
}