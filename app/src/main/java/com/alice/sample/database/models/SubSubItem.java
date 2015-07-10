package com.alice.sample.database.models;

import com.alice.annonatations.database.Column;
import com.alice.annonatations.database.Entity;
import com.alice.annonatations.database.Id;
import com.alice.annonatations.database.ParentEntity;
import com.alice.components.database.models.Identifiable;
import com.alice.sample.database.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(name = Contract.SubSubItem.ENTITY_NAME,
        tableName = Contract.SubSubItem.TABLE_NAME,
        authority = Contract.AUTHORITY,
        inheritColumns = Entity.InheritancePolicy.HIERARCHY)
public class SubSubItem extends SubItem implements Identifiable<String> {

    @Column("_id")
    private Long rowId;

    @Id
    @Column("subSubItemId")
    private String id;

    @Column
    private String subSubItemData;

    @Column
    private boolean boolVal = true;

    @Column
    private boolean bigBoolVal = false;

    @Column
    private byte byteVal = 1;

    @Column
    private Byte bigByteVal = 2;

    @Column
    private short shortVal = 11;

    @Column
    private Short bigShortVal = 12;

    @Column
    private char charVal = 101;

    @Column
    private Character bigCharVal = 102;

    @Column
    private Integer intVal = 1001;

    @Column
    private Integer bigIntegerVal = 1002;

    @Column
    private long longVal = 10001;

    @Column
    private Long bigLongVal = 10002L;

    @Column
    private float floatVal = 100001;

    @Column
    private Float bigFloatVal = 100002f;

    @Column
    private double doubleVal = 1000001;

    @Column
    private Double bigDoubleVal = 1000002d;

    @Column
    private Date longDate = new Date();

    @Column(dataType = Column.DataType.DATE_TIMESTAMP)
    private Date timestampVal = new Date();

    @Column
    private byte[] bytesVal = new byte[] {1, 0, 1, 0};

    @Column
    private List<String> serialized = new ArrayList<>(Arrays.asList("Hello"));

    @Column(dataType = Column.DataType.JSON_STRING)
    private String byteStr = "Bytes string";

    @Column
    private TestEnum testEnum = TestEnum.ENUM_VAL;

    @Column(dataType = Column.DataType.ENUM_ORDINAL)
    private TestEnum testEnum2 = TestEnum.ENUM_VAL;

    @Column(dataType = Column.DataType.JSON_STRING)
    private TestPojo testPojo = new TestPojo();

    @ParentEntity
    private Item parent;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Long getRowId() {
        return rowId;
    }

    @Override
    public void setRowId(Long id) {
        this.rowId = id;
    }

    @Override
    public String getIdColumnName() {
        return "subSubItemId";
    }

    public String getSubSubItemData() {
        return subSubItemData;
    }

    public void setSubSubItemData(String subSubItemData) {
        this.subSubItemData = subSubItemData;
    }

    public Date getLongDate() {
        return longDate;
    }

    public void setLongDate(Date longDate) {
        this.longDate = longDate;
    }

    public static class TestPojo {
        private String name = "pojoname";

        private byte[] bytes = {1, 2, 3, 4, 5};
    }

    public static enum TestEnum {
        ENUM_VAL
    }
}
