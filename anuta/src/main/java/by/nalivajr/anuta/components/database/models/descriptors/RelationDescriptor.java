package by.nalivajr.anuta.components.database.models.descriptors;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import by.nalivajr.anuta.annonatations.database.CascadeType;
import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.FetchType;
import by.nalivajr.anuta.annonatations.database.ManyToMany;
import by.nalivajr.anuta.annonatations.database.OneToMany;
import by.nalivajr.anuta.annonatations.database.RelatedEntity;
import by.nalivajr.anuta.components.database.models.enums.RelationType;
import by.nalivajr.anuta.components.database.models.enums.SqliteDataType;
import by.nalivajr.anuta.exceptions.DifferentDataTypesInRelationMappingException;
import by.nalivajr.anuta.exceptions.NotEntityClassUsedInRelation;
import by.nalivajr.anuta.tools.Anuta;

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
    private FetchType fetchType;
    private boolean cascadeSave = false;
    private boolean cascadeUpdate = false;
    private boolean cascadeDelete = false;

    public RelationDescriptor(Class<?> entityClass, Field field) {
        RelatedEntity otoAnno = field.getAnnotation(RelatedEntity.class);
        if (otoAnno != null) {
            buildOnRelatedEntity(otoAnno, field, entityClass);
            fetchType = otoAnno.fetchType();
            initCascade(otoAnno.cascadeType());
            return;
        }
        OneToMany otmAnno = field.getAnnotation(OneToMany.class);
        if (otmAnno != null) {
            buildOnOneToMany(otmAnno, field, entityClass);
            fetchType = otmAnno.fetchType();
            initCascade(otmAnno.cascadeType());
            return;
        }
        ManyToMany mtmAnno = field.getAnnotation(ManyToMany.class);
        if (mtmAnno != null) {
            buildOnManyToMany(mtmAnno, field, entityClass);
            fetchType = mtmAnno.fetchType();
            initCascade(mtmAnno.cascadeType());
        }
    }

    private void initCascade(CascadeType[] types) {
        Set<CascadeType> cascadeTypes = new HashSet<CascadeType>(Arrays.asList(types));
        cascadeSave = cascadeTypes.contains(CascadeType.ALL) || cascadeTypes.contains(CascadeType.INSERT);
        cascadeUpdate = cascadeTypes.contains(CascadeType.ALL) || cascadeTypes.contains(CascadeType.UPDATE);
        cascadeDelete = cascadeTypes.contains(CascadeType.ALL) || cascadeTypes.contains(CascadeType.DELETE);
    }

    private void initRelationData(String relationColumnName, String relationReferencedColumnName, Class<?> entityClass, Class<?> relatedEntity) {

        Field relationField;
        Field relationReferencedField;

        if(relationType != RelationType.MANY_TO_MANY) {
            relationTable = Anuta.databaseTools.getEntityTableName(relationHoldingEntity);
        }
        if (relationColumnName.isEmpty()) {
            relationField = Anuta.databaseTools.getIdField(entityClass);
            Column column = relationField.getAnnotation(Column.class);
            this.relationColumnName = column.value();
            if (this.relationColumnName.isEmpty()) {
                this.relationColumnName = relationField.getName();
            }
            this.relationColumnType = Anuta.databaseTools.dispatchType(relationField);
        } else {
            this.relationColumnName = relationColumnName;
            relationField = Anuta.databaseTools.getFieldForColumnName(relationColumnName, entityClass);
            this.relationColumnType = relationField == null ?
                    this.relationColumnType : Anuta.databaseTools.dispatchType(relationField);
        }
        if (relationReferencedColumnName.isEmpty()) {
            relationReferencedField = Anuta.databaseTools.getIdField(relatedEntity);
            Column column = relationReferencedField.getAnnotation(Column.class);
            this.relationReferencedColumnName = column.value();
            if (this.relationReferencedColumnName.isEmpty()) {
                this.relationReferencedColumnName = relationReferencedField.getName();
            }
            this.relationReferencedColumnType = Anuta.databaseTools.dispatchType(relationReferencedField);
        } else {
            this.relationReferencedColumnName = relationReferencedColumnName;
            relationReferencedField = Anuta.databaseTools.getFieldForColumnName(relationReferencedColumnName, relatedEntity);
            this.relationReferencedColumnType = relationReferencedField == null ?
                    this.relationReferencedColumnType : Anuta.databaseTools.dispatchType(relationReferencedField);
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
            this.joinRelationColumnName = Anuta.databaseTools.getEntityTableName(relatedEntity) + this.relationReferencedColumnName;
        } else if (relationHoldingEntity == entityClass && !relationColumnName.isEmpty()) {
            this.joinRelationColumnName = this.relationColumnName;
        } else if (relationHoldingEntity == relatedEntity && relationReferencedColumnName.isEmpty()) {
            this.joinReferencedRelationColumnName = Anuta.databaseTools.getEntityTableName(entityClass) + this.relationColumnName;
        } else if (relationHoldingEntity == relatedEntity && !relationReferencedColumnName.isEmpty()) {
            this.joinReferencedRelationColumnName = this.relationReferencedColumnName;
        }
    }

    private void buildOnRelatedEntity(RelatedEntity anno, Field field, Class<?> entityClass) {
        Class<?> relatedEntity = field.getType();
        if (!Anuta.reflectionTools.isEntityClass(relatedEntity)) {
            throw new NotEntityClassUsedInRelation(relatedEntity, field);
        }

        Class<?> relationClass = anno.dependentEntityClass();
        if (!Anuta.reflectionTools.isEntityClass(relationClass)) {
            throw new RuntimeException(String.format("Relation class %s is not an entity", relationClass));
        }

        relationHoldingEntity = relationClass;
        relationType = RelationType.RELATED_ENTITY;
        initRelationData(anno.relationColumnName(), anno.relationReferencedColumnName(), entityClass, relatedEntity);
        if (relationHoldingEntity == entityClass) {
            relationColumnName = joinRelationColumnName;
        }
    }

    private void buildOnOneToMany(OneToMany anno, Field field, Class<?> entityClass) {
        Class<?> relatedEntity = Anuta.databaseTools.getRelatedGenericClass(field);
        if (!Anuta.reflectionTools.isEntityClass(relatedEntity)) {
            throw new NotEntityClassUsedInRelation(relatedEntity, field);
        }
        relationHoldingEntity = relatedEntity;
        relationType = RelationType.ONE_TO_MANY;
        initRelationData(anno.relationColumnName(), anno.relationReferencedColumnName(), entityClass, relatedEntity);
    }

    private void buildOnManyToMany(ManyToMany anno, Field field, Class<?> entityClass) {

        Class<?> relatedEntity = Anuta.databaseTools.getRelatedGenericClass(field);
        if (!Anuta.reflectionTools.isEntityClass(relatedEntity)) {
            throw new NotEntityClassUsedInRelation(relatedEntity, field);
        }

        relationType = RelationType.MANY_TO_MANY;
        String relationColumnName = anno.relationColumnName();
        String relationReferencedColumnName = anno.relationReferencedColumnName();

        initRelationData(relationColumnName, relationReferencedColumnName, entityClass, relatedEntity);
        relationTable = anno.relationTableName();
        if (relationTable.isEmpty()) {
            relationTable = Anuta.databaseTools.buildRelationTableName(entityClass, relatedEntity);
        }
        joinRelationColumnName = anno.joinTableRelationColumnName();
        joinReferencedRelationColumnName = anno.joinTableRelationReferencedColumnName();
        if (joinRelationColumnName.equals(joinReferencedRelationColumnName)) {
            joinRelationColumnName =  Anuta.databaseTools.getEntityTableName(entityClass).toLowerCase() + relationColumnName;
            joinReferencedRelationColumnName =  Anuta.databaseTools.getEntityTableName(relatedEntity).toLowerCase() + relationReferencedColumnName;
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

    public FetchType getFetchType() {
        return fetchType;
    }

    public boolean isCascadeSave() {
        return cascadeSave;
    }

    public boolean isCascadeUpdate() {
        return cascadeUpdate;
    }

    public boolean isCascadeDelete() {
        return cascadeDelete;
    }
}
