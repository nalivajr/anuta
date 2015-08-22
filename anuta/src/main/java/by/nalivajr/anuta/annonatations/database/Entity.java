package by.nalivajr.anuta.annonatations.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    /**
     * Specifies unique name of entity. By default full class name will be used.
     * <p/>
     * Be aware to use default name as if class will be renamed or placed to another package then database information can become inconsistent
     *
     * @return the unique name of entity
     */
    String name() default "";
    /**
     * Specifies unique name of tablet for entity. By default {@link Class#getSimpleName()} will be used.
     * <p/>
     * Be aware to use default name as if class will be renamed then database information can become inconsistent
     *
     * @return the unique name of entity
     */
    String tableName() default "";

    /**
     * Used to specify if the entity should inherit columns from it's parent entity (or grandparent and so on).
     * If entity has flag set to true, but parent has flag set to false of even parent class is no an entity, then grandparent's columns won't be inherited
     * <br>
     *     BE AWARE! If parent has field, annotated with {@link Id} and target entity has any field annotated with {@link Id} then child's id will be used;
     * @return true if entity should inherit columns and false otherwise
     */
    InheritancePolicy inheritColumns() default InheritancePolicy.NO;

    /**
     * @return authority String to access provider, managing this entity
     */
    String authority();

    public static enum InheritancePolicy {
        /**
         * Do not inherit columns.
         */
        NO,

        /**
         * Inherits only first level parent columns but do not inherits parent's id
         */
        PARENT_ONLY,

        /**
         * Inherits columns from whole hierarchy of entity, but do not inherits parent's ids
         */
        HIERARCHY,

        /**
         * Inherits columns from parent class, but do not inherits parent's id. If Parent has {@link Entity#inheritColumns()}
         * value set then columns inheritance will continue with parent's policy. Parent id won't be used as id for target entity
         */
        SEQUENTIAL_NO_ID,
    }
}
