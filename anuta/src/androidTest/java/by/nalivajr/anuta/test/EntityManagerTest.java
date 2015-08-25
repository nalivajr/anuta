package by.nalivajr.anuta.test;

import android.support.annotation.NonNull;
import android.test.ProviderTestCase2;
import android.util.Log;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import by.nalivajr.anuta.components.database.entitymanager.AnutaEntityManager;
import by.nalivajr.anuta.test.content.TestContract;
import by.nalivajr.anuta.test.database.TestEntityManager;
import by.nalivajr.anuta.test.database.TestProvider;
import by.nalivajr.anuta.test.models.Department;
import by.nalivajr.anuta.test.models.Employee;
import by.nalivajr.anuta.test.models.Tag;
import by.nalivajr.anuta.test.models.Technology;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class EntityManagerTest extends ProviderTestCase2<TestProvider> {

    public static final String TAG = EntityManagerTest.class.getName();

    private AnutaEntityManager entityManager;

    public EntityManagerTest() {
        super(TestProvider.class, TestContract.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        entityManager = new TestEntityManager(getMockContext());
    }

    public void testSavePlain() {
        Tag tag = new Tag();
        tag.setName("JAVA");
        entityManager.save(tag);

        tag = entityManager.find(Tag.class, tag.getName());
        Assert.assertEquals("JAVA", tag.getName());

        int found = entityManager.findAll(Tag.class).size();
        assertEquals(1, found);
        Log.i(TAG, "Found tags: " + found);
    }

    public void testSaveCollectionRecursively() {
        List<Tag> tagsList = createTags();

        entityManager.saveAll(tagsList);

        int found = entityManager.findAll(Tag.class).size();
        assertEquals(tagsList.size(), found);
        Log.i(TAG, "Found tags: " + found);
    }

    public void testSaveCascade() {
        Employee employee1 = createEmployee("Employee One", "Male", new Date(), "e1");
        Employee employee2 = createEmployee("Employee Two", "Female", new Date(), "e2");
        Employee employee3 = createEmployee("Employee Three", "Male", new Date(), "e3");
        Employee employee4 = createEmployee("Employee Four", "Female", new Date(), "e4");
        Employee employee5 = createEmployee("Employee Five", "Male", new Date(), "e5");

        Department devDepartment = new Department();
        devDepartment.setMaster(employee1);
        devDepartment.setName("Development department");
        devDepartment.setDepartmentEmployees(Arrays.asList(employee1, employee3, employee5));


        Technology webTechnology = new Technology();
        String technologyName = "Web Enterprise";
        webTechnology.setName(technologyName);
        List<Tag> technologyTags = createTags("HTML", "CSS", "JAVA", "JAVASCRIPT");
        webTechnology.setTags(technologyTags.toArray(new Tag[]{}));

        devDepartment.setTechnologies(new Technology[]{webTechnology});

        entityManager.save(employee1);
        assertNotNull(entityManager.find(Employee.class, employee1.getId()));

        assertTrue(entityManager.findAll(Department.class).size() == 0);
        assertTrue(entityManager.findAll(Technology.class).size() == 0);
        assertTrue(entityManager.findAll(Tag.class).size() == 0);

        entityManager.save(devDepartment);

        assertTrue(entityManager.findAll(Department.class).size() == 1);
        assertTrue(entityManager.findAll(Technology.class).size() == 1);
        assertTrue(entityManager.findAll(Tag.class).size() == technologyTags.size());
        assertTrue(entityManager.findAll(Employee.class).size() == devDepartment.getDepartmentEmployees().size());

        webTechnology = entityManager.find(Technology.class, "1");
        assertNotNull(webTechnology);

        webTechnology = entityManager.initialize(webTechnology);
        assertEquals(webTechnology.getName(), technologyName);
        assertTrue(webTechnology.getDepartment() != null);
        assertTrue(webTechnology.getTags().length == technologyTags.size());

        Department qaDepartment = new Department();
        qaDepartment.setMaster(employee2);
        employee2.setDepartment(qaDepartment);
        qaDepartment.setName("QA department");
        qaDepartment.setDepartmentEmployees(Arrays.asList(employee2, employee4));

        Technology qaTechnology = new Technology();
        String qaTechnologyName = "QA";
        qaTechnology.setName(qaTechnologyName);
        List<Tag> qaTechnologyTags = createTags("Selenium", "Monkey Runner");
        qaTechnology.setTags(qaTechnologyTags.toArray(new Tag[]{}));
        qaDepartment.setTechnologies(new Technology[]{qaTechnology});

        entityManager.save(employee2);

        assertTrue(entityManager.findAll(Department.class).size() == 2);
        assertTrue(entityManager.findAll(Technology.class).size() == 2);
        assertTrue(entityManager.findAll(Tag.class).size() == technologyTags.size() + qaTechnologyTags.size());
        assertTrue(entityManager.findAll(Employee.class).size() == devDepartment.getDepartmentEmployees().size() +
                qaDepartment.getDepartmentEmployees().size());
    }

    public void testUpdateCascade() {
        Employee employee1 = createEmployee("Employee One", "Male", new Date(), "e1");
        Employee employee2 = createEmployee("Employee Two", "Female", new Date(), "e2");
        Employee employee3 = createEmployee("Employee Three", "Male", new Date(), "e3");
        Employee employee4 = createEmployee("Employee Four", "Female", new Date(), "e4");
        Employee employee5 = createEmployee("Employee Five", "Male", new Date(), "e5");

        Department devDepartment = new Department();
        devDepartment.setMaster(employee1);
        devDepartment.setName("Development department");
        devDepartment.setDepartmentEmployees(Arrays.asList(employee1, employee3, employee5));

        Technology webTechnology = new Technology();
        String technologyName = "Web Enterprise";
        webTechnology.setName(technologyName);
        List<Tag> technologyTags = createTags("HTML", "CSS", "JAVA", "JAVASCRIPT");
        webTechnology.setTags(technologyTags.toArray(new Tag[]{}));

        devDepartment.setTechnologies(new Technology[]{webTechnology});

        entityManager.save(devDepartment);

        Department qaDepartment = new Department();
        qaDepartment.setMaster(employee2);
        employee2.setDepartment(qaDepartment);
        qaDepartment.setName("QA department");
        qaDepartment.setDepartmentEmployees(Arrays.asList(employee2, employee4, employee1, employee3));

        Technology qaTechnology = new Technology();
        String qaTechnologyName = "QA";
        qaTechnology.setName(qaTechnologyName);
        List<Tag> qaTechnologyTags = createTags("Selenium", "Monkey Runner");
        qaTechnology.setTags(qaTechnologyTags.toArray(new Tag[]{}));
        qaDepartment.setTechnologies(new Technology[]{qaTechnology});

        entityManager.save(employee2);

        assertTrue(entityManager.findAll(Department.class).size() == 2);
        assertTrue(entityManager.findAll(Technology.class).size() == 2);
        assertTrue(entityManager.findAll(Tag.class).size() == technologyTags.size() + qaTechnologyTags.size());
        assertTrue(entityManager.findAll(Employee.class).size() == 5);

        devDepartment.setTechnologies(null);
        devDepartment.setDepartmentEmployees(null);

        entityManager.update(devDepartment);
        devDepartment = entityManager.initialize(entityManager.find(Department.class, "1"));
        assertNotNull(devDepartment);
        assertEquals(devDepartment.getName(), "Development department");
        assertTrue(devDepartment.getTechnologies() == null || devDepartment.getTechnologies().length == 0);
        assertTrue(devDepartment.getDepartmentEmployees() == null || devDepartment.getDepartmentEmployees().size() == 0);

        assertTrue(entityManager.findAll(Department.class).size() == 2);
        assertTrue(entityManager.findAll(Technology.class).size() == 2);
        assertTrue(entityManager.findAll(Tag.class).size() == technologyTags.size() + qaTechnologyTags.size());
        assertTrue(entityManager.findAll(Employee.class).size() == 5);
    }

    @NonNull
    private Employee createEmployee(String name, String gender, Date birthDate, String uid) {
        Employee employee1 = new Employee();
        employee1.setName(name);
        employee1.setGender(gender);
        employee1.setBirthDate(birthDate);
        employee1.setUserId(uid);
        return employee1;
    }

    @NonNull
    private List<Tag> createTags(String ... names) {
        List<Tag> tags = new LinkedList<Tag>();
        for (String name : names) {
            tags.add(createTag(name));
        }
        return tags;
    }

    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return tag;
    }
}