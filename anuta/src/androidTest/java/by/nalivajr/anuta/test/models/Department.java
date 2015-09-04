package by.nalivajr.anuta.test.models;

import android.provider.BaseColumns;

import java.util.Arrays;
import java.util.List;

import by.nalivajr.anuta.annonatations.database.CascadeType;
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

    @OneToMany(cascadeType = CascadeType.ALL)
    private List<Technology> technologies;

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
        if (technologies == null) {
            return null;
        }
        return technologies.toArray(new Technology[technologies.size()]);
    }

    public void setTechnologies(Technology[] technologies) {
        this.technologies = null;
        if (technologies != null) {
            this.technologies = Arrays.asList(technologies);
        }
    }

    public List<Employee> getDepartmentEmployees() {
        return departmentEmployees;
    }

    public void setDepartmentEmployees(List<Employee> departmentEmployees) {
        this.departmentEmployees = departmentEmployees;
    }
}
