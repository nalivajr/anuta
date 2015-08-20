package by.nalivajr.alice.components.database.models;

import java.lang.reflect.Field;

import by.nalivajr.alice.annonatations.database.Column;
import by.nalivajr.alice.annonatations.database.Id;
import by.nalivajr.alice.annonatations.database.ManyToMany;
import by.nalivajr.alice.annonatations.database.OneToMany;
import by.nalivajr.alice.annonatations.database.RelatedEntity;
import by.nalivajr.alice.exceptions.DifferentDataTypesInRelationMappingException;
import by.nalivajr.alice.exceptions.NotEntityClassUsedInRelation;
import by.nalivajr.alice.tools.Alice;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class RelationDescriptor {

    private String relationColumnName;
    private String relationReferencedColumnName;
    private String joinRelationColumnName;
    private String joinReferencedRelationColumnName;
    private String relationTable;
    private RelationType relationType;
    private SqliteDataType relationColumnType;
    private SqliteDataType relationReferencedColumnType;
    private Class<?> relatedEntity;
    private Class<?> relationHoldingEntity;

    public RelationDescriptor(Class<?> entityClass, Field field) {
        RelatedEntity otoAnno = field.getAnnotation(RelatedEntity.class);
        if (otoAnno != null) {
            buildOnRelatedEntity(otoAnno, field, entityClass);
            return;
        }
        OneToMany otmAnno = field.getAnnotation(OneToMany.class);
        if (otmAnno != null) {
            buildOnOneToMany(otmAnno, field, entityClass);
            return;
        }
        ManyToMany mtmAnno = field.getAnnotation(ManyToMany.class);
        if (mtmAnno != null) {
            buildOnManyToMany(mtmAnno, field, entityClass);
        }
    }

    private void initRelationData(String relationColumnName, String relationReferencedColumnName, Class<?> entityClass, Class<?> relatedEntity) {

        Field relationField;
        Field relationReferencedField;

        if(relationType != RelationType.MANY_TO_MANY) {
            relationTable = Alice.databaseTools.getEntityTableName(relationHoldingEntity);
        }
        if (relationColumnName.isEmpty()) {
            relationField = Alice.reflectionTools.getFieldsAnnotatedWith(entityClass, Id.class).get(0);
            Column column = relationField.getAnnotation(Column.class);
            this.relationColumnName = column.value();
            if (this.relationColumnName.isEmpty()) {
                this.relationColumnName = relationField.getName();
            }
            this.relationColumnType = Alice.databaseTools.dispatchType(relationField);
        } else {
            this.relationColumnName = relationColumnName;
            relationField = Alice.databaseTools.getFieldForColumnName(relationColumnName, entityClass);
            this.relationColumnType = relationField == null ?
                    this.relationColumnType : Alice.databaseTools.dispatchType(relationField);
        }
        if (relationReferencedColumnName.isEmpty()) {
            relationReferencedField = Alice.reflectionTools.getFieldsAnnotatedWith(relatedEntity, Id.class).get(0);
            Column column = relationReferencedField.getAnnotation(Column.class);
            this.relationReferencedColumnName = column.value();
            if (this.relationReferencedColumnName.isEmpty()) {
                this.relationReferencedColumnName = relationReferencedField.getName();
            }
            this.relationReferencedColumnType = Alice.databaseTools.dispatchType(relationReferencedField);
        } else {
            this.relationReferencedColumnName = relationReferencedColumnName;
            relationReferencedField = Alice.databaseTools.getFieldForColumnName(relationReferencedColumnName, relatedEntity);
            this.relationReferencedColumnType = relationReferencedField == null ?
                    this.relationReferencedColumnType : Alice.databaseTools.dispatchType(relationReferencedField);
        }

        if (relationColumnType == null && relationReferencedColumnType == null) {
            throw new RuntimeException("Could not detect the type of relation columns. Both columns do not exist");
        }
        // if the column should be created in related entity
        if (relationColumnType != null && relationReferencedColumnType == null) {
            relationReferencedColumnType = relationColumnType;
        }
        // if the column should be created in this entity
        if (relationColumnType == null && relationHoldingEntity == entityClass) {
            relationColumnType = relationReferencedColumnType;
        }
        if (relationColumnType != relationReferencedColumnType) {
            throw new DifferentDataTypesInRelationMappingException(entityClass, this.relationColumnName, relatedEntity, this.relationReferencedColumnName);
        }

        this.relatedEntity = relatedEntity;
        if (relationType == RelationType.MANY_TO_MANY) {
            return;
        }

        if (relationHoldingEntity == entityClass && relationColumnName.isEmpty()) {
            this.joinRelationColumnName = Alice.databaseTools.getEntityTableName(relatedEntity) + this.relationReferencedColumnName;
        } else if (relationHoldingEntity == entityClass && !relationColumnName.isEmpty()) {
            this.joinRelationColumnName = this.relationColumnName;
        } else if (relationHoldingEntity == relatedEntity && relationReferencedColumnName.isEmpty()) {
            this.joinReferencedRelationColumnName = Alice.databaseTools.getEntityTableName(entityClass) + this.relationColumnName;
        } else if (relationHoldingEntity == relatedEntity && !relationReferencedColumnName.isEmpty()) {
            this.joinReferencedRelationColumnName = this.relationReferencedColumnName;
        }
    }

    private void buildOnRelatedEntity(RelatedEntity anno, Field field, Class<?> entityClass) {
        Class<?> relatedEntity = field.getType();
        if (!Alice.reflectionTools.isEntityClass(relatedEntity)) {
            throw new NotEntityClassUsedInRelation(relatedEntity, field);
        }

        Class<?> relationClass = anno.dependentEntityClass();
        if (!Alice.reflectionTools.isEntityClass(relationClass)) {
            throw new RuntimeException(String.format("Relation class %s is not an entity", relationClass));
        }

        relationHoldingEntity = relationClass;
        relationType = RelationType.RELATED_ENTITY;
        initRelationData(anno.relationColumnName(), anno.relationReferencedColumnName(), entityClass, relatedEntity);
    }

    private void buildOnOneToMany(OneToMany anno, Field field, Class<?> entityClass) {
        Class<?> relatedEntity = Alice.databaseTools.getRelatedGenericClass(field);
        if (!Alice.reflectionTools.isEntityClass(relatedEntity)) {
            throw new NotEntityClassUsedInRelation(relatedEntity, field);
        }
        relationHoldingEntity = relatedEntity;
        relationType = RelationType.ONE_TO_MANY;
        initRelationData(anno.relationColumnName(), anno.relationReferencedColumnName(), entityClass, relatedEntity);
    }

    private void buildOnManyToMany(ManyToMany anno, Field field, Class<?> entityClass) {

        Class<?> relatedEntity = Alice.databaseTools.getRelatedGenericClass(field);
        if (!Alice.reflectionTools.isEntityClass(relatedEntity)) {
            throw new NotEntityClassUsedInRelation(relatedEntity, field);
        }

        relationType = RelationType.MANY_TO_MANY;
        String relationColumnName = anno.relationColumnName();
        String relationReferencedColumnName = anno.relationReferencedColumnName();

        initRelationData(relationColumnName, relationReferencedColumnName, entityClass, relatedEntity);
        relationTable = anno.relationTableName();
        if (relationTable.isEmpty()) {
            relationTable = Alice.databaseTools.buildRelationTableName(entityClass, relatedEntity);
        }
        joinRelationColumnName = this.relationColumnName;
        joinReferencedRelationColumnName = this.relationReferencedColumnName;
        if (joinRelationColumnName.equals(joinReferencedRelationColumnName)) {
            joinRelationColumnName =  Alice.databaseTools.getEntityTableName(entityClass).toLowerCase() + relationColumnName;
            joinReferencedRelationColumnName =  Alice.databaseTools.getEntityTableName(relatedEntity).toLowerCase() + relationReferencedColumnName;
        }
    }

    public Class<?> getRelatedEntity() {
        return relatedEntity;
    }

    public String getRelationTable() {
        return relationTable;
    }

    public String getRelationColumnName() {
        return relationColumnName;
    }

    public String getRelationReferencedColumnName() {
        return relationReferencedColumnName;
    }

    public String getJoinRelationColumnName() {
        return joinRelationColumnName;
    }

    public String getJoinReferencedRelationColumnName() {
        return joinReferencedRelationColumnName;
    }

    public SqliteDataType getRelationColumnType() {
        return relationColumnType;
    }

    public SqliteDataType getRelationReferencedColumnType() {
        return relationReferencedColumnType;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public Class<?> getRelationHoldingEntity() {
        return relationHoldingEntity;
    }
}
