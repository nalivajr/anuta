package by.nalivajr.anuta.test.models;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.test.content.TestContract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface BadEntities {

    @Entity(authority = TestContract.AUTHORITY)
    public static class ManyIdsEntity {

        @Id
        @Column
        private Long id;

        @Id
        @Column
        private String id2;
    }

    @Entity(authority = TestContract.AUTHORITY)
    public static class NoIdsEntity {

        @Column
        private Long id;

        @Column
        private String id2;

        @Id
        private String idNotColumn;
    }

    @Entity(authority = TestContract.AUTHORITY)
    public static class InvalidRowIdTypeEntity {

        @Id
        @Column
        private int _id;

        @Column
        private Long id;

        @Column
        private String id2;
    }

    public static class NotAnnotatedEnity {

        @Id
        @Column
        private Long _id;

        @Column
        private Long id;

        @Column
        private String id2;
    }

    @Entity(authority = TestContract.AUTHORITY)
    static class ParentEntity {

        @Column
        private Long _id;

        @Id
        @Column
        private Long id;

        @Column
        private String id2;
    }

    @Entity(authority = TestContract.AUTHORITY, inheritColumns = Entity.InheritancePolicy.PARENT_ONLY)
    public static class DuplicateNamesEntity extends ParentEntity {

        @Column
        private Long _id;

        @Id
        @Column
        private Long id;

        @Column
        private String id2Child;
    }

}
