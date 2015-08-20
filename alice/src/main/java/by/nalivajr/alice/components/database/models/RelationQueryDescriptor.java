package by.nalivajr.alice.components.database.models;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import by.nalivajr.alice.annonatations.database.Entity;
import by.nalivajr.alice.components.database.query.AliceQuery;
import by.nalivajr.alice.components.database.query.AliceQueryBuilder;
import by.nalivajr.alice.components.database.query.BaseAliceQueryBuilder;
import by.nalivajr.alice.tools.Alice;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class RelationQueryDescriptor<T> {
    private Class<T> relatedEntity;
    private Set<String> params = new HashSet<String>();
    private RelationDescriptor relationDescriptor;

    public RelationQueryDescriptor(Class<T> relatedEntity, RelationDescriptor descriptor) {
        this.relatedEntity = relatedEntity;
        this.relationDescriptor = descriptor;
        Class<?> relationHoldingEntity = descriptor.getRelationHoldingEntity();
        String columnName;
        if (relationHoldingEntity == relatedEntity) {   //if related entity stores foreign key
            columnName = descriptor.getJoinReferencedRelationColumnName();
        } else {
            columnName = descriptor.getRelationReferencedColumnName();
        }
        if (descriptor.getRelationType() == RelationType.MANY_TO_MANY) {
            columnName = descriptor.getRelationReferencedColumnName();
        }
        addParam(columnName);
    }

    private void addParam(String columnName) {
        if (params.contains(columnName)) {
            return;
        }
        params.add(columnName);
    }

    public AliceQuery<T> buildQuery(ContentResolver resolver, Cursor parentEntity) {
        AliceQueryBuilder<T> queryBuilder = new BaseAliceQueryBuilder<T>(relatedEntity);

        for (String param : params) {
            String[] values = extractArgs(resolver, parentEntity);
            if (values.length == 0) {   //if args are empty
                continue;
            }
            queryBuilder.or(
                    queryBuilder.in(param, values)
            );
        }
        return queryBuilder.build();
    }

    private String[] extractArgs(ContentResolver resolver, Cursor rootEntityCursor) {
        List<String> args = new LinkedList<String>();
        if (relationDescriptor.getRelationType() != RelationType.MANY_TO_MANY) {
            args.add(rootEntityCursor.getString(rootEntityCursor.getColumnIndex(relationDescriptor.getRelationColumnName())));
        } else {
            String authority = relatedEntity.getAnnotation(Entity.class).authority();
            Uri uri = Alice.databaseTools.buildUriForTableName(relationDescriptor.getRelationTable(), authority);
            Cursor cursor = resolver.query(uri,
                    new String[]{relationDescriptor.getJoinReferencedRelationColumnName()},    //relatedEntityRef
                    relationDescriptor.getJoinRelationColumnName() + " = ?",                    //this entityRef
                    new String[]{rootEntityCursor.getString(rootEntityCursor.getColumnIndex(relationDescriptor.getRelationColumnName()))},
                    null);
            extarctFromCursor(args, cursor);
        }
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg == null) {
                args.remove(i--);
            }
        }
        return args.toArray(new String[args.size()]);
    }

    private void extarctFromCursor(List<String> args, Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) {
            args.add("-1"); //to avoid no-arg issue
        } else {
            do {
                args.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
    }
}
