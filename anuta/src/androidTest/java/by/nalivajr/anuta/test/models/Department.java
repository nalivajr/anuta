package by.nalivajr.anuta.test.models;

import android.provider.BaseColumns;

import java.util.List;

import by.nalivajr.anuta.annonatations.database.Column;
import by.nalivajr.anuta.annonatations.database.Entity;
import by.nalivajr.anuta.annonatations.database.Id;
import by.nalivajr.anuta.annonatations.database.OneToMany;
import by.nalivajr.anuta.annonatations.database.RelatedEntity;
import by.nalivajr.anuta.test.content.TestContract;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@Entity(authority = TestContract.AUTHORITY, tableName = "ITDepartment")
public class Department {

    @Id
    @Column(BaseColumns._ID)
    private Long id;

    @Column
    private String name;

    @RelatedEntity(relationColumnName = "masterId", dependentEntityClass = Department.class)
    private Employee master;

    @OneToMany(relationReferencedColumnName = "departmentId")
    private List<Employee> departmentEmployees;

    @OneToMany
    private Technology[] technologies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Employee getMaster() {
        return master;
    }

    public void setMaster(Employee master) {
        this.master = master;
    }

    public Technology[] getTechnologies() {
        return technologies;
    }

    public void setTechnologies(Technology[] technologies) {
        this.technologies = technologies;
    }

    public List<Employee> getDepartmentEmployees() {
        return departmentEmployees;
    }

    public void setDepartmentEmployees(List<Employee> departmentEmployees) {
        this.departmentEmployees = departmentEmployees;
    }
}
