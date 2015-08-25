package by.nalivajr.anuta.test.models;

import java.util.Date;
import java.util.Set;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.annonatations.database.ManyToMany;
import by.nalivajr.anuta.annonatations.database.RelatedEntity;
import by.nalivajr.anuta.components.database.models.Persistable;
import by.nalivajr.anuta.test.content.TestContract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(authority = TestContract.AUTHORITY)
public class Employee implements Persistable<String> {

    @Column
    private Long _id;

    @Id
    @Column
    private String userId;

    @Column
    private String name;

    @Column
    private String gender;

    @Column
    private Date birthDate;

    @RelatedEntity(relationColumnName = "departmentId", dependentEntityClass = Employee.class)
    private Department department;

    @ManyToMany
    private Set<Tag> tags;

    @Override
    public Long getRowId() {
        return _id;
    }

    @Override
    public void setRowId(Long rowId) {
        _id = rowId;
    }

    @Override
    public String getIdColumnName() {
        return "userId";
    }

    @Override
    public String getId() {
        return userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
