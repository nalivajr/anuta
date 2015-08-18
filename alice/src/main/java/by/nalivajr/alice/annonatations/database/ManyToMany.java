package by.nalivajr.alice.annonatations.database;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used to specify many-to-many relations between entities
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToMany {

    /**
     * Specifies the column in this entity, which is used in relation. By default, the the property, annotated with {@link Id} in entity will be used
     * @return the name of column in relation.
     */
    public String relationColumnName() default "";

    /**
     * Specifies the column, which is used in relation. By default, the the property, annotated with {@link Id} in related entity will be used
     * @return the name of column in relation.
     */
    public String relationReferencedColumnName() default "";

    /**
     * Specifies the name of relation table. By default table name will be generated as concatenation of table names of each entity, separated with '_'.
     * E.g. entity1_entity2
     * @return the name of relation table
     */
    public String relationTableName() default "";
}
