package by.nalivajr.alice.annonatations.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used to specify one-to-one or many to one relations between entities
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RelatedEntity {

    /**
     * Specifies the column, which is used in relation. By default, the the property, annotated with {@link Id} in related entity will be used
     * @return the name of column in relation.
     */
    public String relationReferencedColumnName() default "";

    /**
     * Specifies the column, which is used in relation. By default, the the property, annotated with {@link Id} in this entity will be used
     * @return the name of column in relation.
     */
    public String relationColumnName() default "";

    /**
     * Specifies the class of entity, which has foreign key column
     */
    public Class<?> dependentEntityClass();
}
