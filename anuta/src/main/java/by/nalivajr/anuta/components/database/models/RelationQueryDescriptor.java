package by.nalivajr.anuta.components.database.models;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.components.database.query.AnutaQuery;
import by.nalivajr.anuta.components.database.query.AnutaQueryBuilder;
import by.nalivajr.anuta.components.database.query.BaseAnutaQueryBuilder;
import by.nalivajr.anuta.tools.Anuta;

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

    public AnutaQuery<T> buildQuery(ContentResolver resolver, Cursor parentEntity) {
        AnutaQueryBuilder<T> queryBuilder = new BaseAnutaQueryBuilder<T>(relatedEntity);

        int specifiedParams = 0;
        for (String param : params) {
            String[] values = extractArgs(resolver, parentEntity);
            if (values.length == 0) {   //if args are empty
                continue;
            }
            queryBuilder.or(
                    queryBuilder.in(param, values)
            );
            specifiedParams++;
        }
        return specifiedParams > 0 ? queryBuilder.build() : null;
    }

    private String[] extractArgs(ContentResolver resolver, Cursor rootEntityCursor) {
        List<String> args = new LinkedList<String>();
        if (relationDescriptor.getRelationType() != RelationType.MANY_TO_MANY) {
            args.add(rootEntityCursor.getString(rootEntityCursor.getColumnIndex(relationDescriptor.getRelationColumnName())));
        } else {
            String authority = relatedEntity.getAnnotation(Entity.class).authority();
            Uri uri = Anuta.databaseTools.buildUriForTableName(relationDescriptor.getRelationTable(), authority);
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
        if (cursor != null && cursor.moveToFirst()) {
            do {
                args.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
    }
}
