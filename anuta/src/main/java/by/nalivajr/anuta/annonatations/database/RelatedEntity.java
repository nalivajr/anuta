package by.nalivajr.anuta.annonatations.database;

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

    /**
     * Specifies the strategy of data loading. If lazy that related entity won't be loaded
     */
    public FetchType fetchType() default FetchType.LAZY;

    /**
     * Specifies the strategy of data persisting. By default {@link CascadeType#INSERT} and
     * {@link CascadeType#UPDATE} are enabled
     */
    public CascadeType[] cascadeType() default {CascadeType.INSERT, CascadeType.UPDATE};

}
