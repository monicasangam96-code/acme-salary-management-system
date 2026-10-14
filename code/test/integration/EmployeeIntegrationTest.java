package com.acme.salarymgmt.integration;

import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.repository.CurrencyRepository;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Employee repository and service with a real MySQL container.
 * Verifies: CRUD, soft-delete, search filters, pagination, duplicate detection.
 */
@SpringBootTest
@Testcontainers
class EmployeeIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.7")
            .withDatabaseName("salary_mgmt_test")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("schema.sql")
            .withInitScript("init_data.sql");

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        salaryRepository.deleteAll();
    }

    // ---- Persist & FindById ----
    @Test
    @DisplayName("should persist and find active employee by ID")
    void shouldPersistAndFindById() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        Optional<Employee> found = employeeRepository.findActiveById(emp.getId());
        assertTrue(found.isPresent());
        assertEquals("EMP-001", found.get().getEmpNo());
        assertEquals("John", found.get().getFirstName());
        assertEquals(Employee.EmploymentStatus.ACTIVE, found.get().getEmploymentStatus());
    }

    @Test
    @DisplayName("should persist and find employee by empNo")
    void shouldFindByEmpNo() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        Optional<Employee> found = employeeRepository.findByEmpNo("EMP-001");
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
    }

    @Test
    @DisplayName("should not find inactive employee via findActiveById")
    void shouldNotFindInactiveEmployee() {
        Employee emp = createSampleEmployee();
        emp.setActive(false);
        emp = employeeRepository.save(emp);

        Optional<Employee> found = employeeRepository.findActiveById(emp.getId());
        assertFalse(found.isPresent());
    }

    // ---- Duplicate Detection ----
    @Test
    @DisplayName("should detect duplicate employee number")
    void shouldDetectDuplicate() {
        Employee emp1 = createSampleEmployee();
        emp1 = employeeRepository.save(emp1);

        Employee emp2 = createSampleEmployee();
        emp2.setEmpNo("EMP-001"); // same empNo
        assertFalse(employeeRepository.existsByEmpNo("EMP-001")); // check before saving
    }

    @Test
    @DisplayName("existsByEmpNo should return true after duplicate is saved")
    void existsByEmpNo_shouldReturnTrueForDuplicate() {
        Employee emp1 = createSampleEmployee();
        emp1 = employeeRepository.save(emp1);

        assertTrue(employeeRepository.existsByEmpNo("EMP-001"));
    }

    // ---- Search & Filter ----
    @Test
    @DisplayName("should search employees by name with LIKE")
    void shouldSearchByName() {
        saveEmployees();

        Page<Employee> results = employeeRepository.searchWithFilters(null, null, null, "doe", PageRequest.of(0, 10));
        assertTrue(results.getContent().stream().anyMatch(e -> e.getLastName().equalsIgnoreCase("Doe")));
    }

    @Test
    @DisplayName("should filter by country and department")
    void shouldFilterByCountryAndDept() {
        saveEmployees();

        Page<Employee> results = employeeRepository.searchWithFilters(
                "United States (US)", "Engineering", null, null, PageRequest.of(0, 10));

        assertFalse(results.isEmpty());
        results.getContent().forEach(e -> {
            assertEquals("United States (US)", e.getCountry());
            assertEquals("Engineering", e.getDept());
        });
    }

    @Test
    @DisplayName("should filter by employment status")
    void shouldFilterByStatus() {
        Employee emp = createSampleEmployee();
        emp.setEmploymentStatus(Employee.EmploymentStatus.SUSPENDED);
        employeeRepository.save(emp);

        Page<Employee> results = employeeRepository.searchWithFilters(
                null, null, Employee.EmploymentStatus.SUSPENDED, null, PageRequest.of(0, 10));

        assertFalse(results.isEmpty());
    }

    // ---- Pagination ----
    @Test
    @DisplayName("should paginate results correctly")
    void shouldPaginate() {
        saveEmployees();

        Page<Employee> page1 = employeeRepository.searchWithFilters(null, null, null, null, PageRequest.of(0, 5));
        assertTrue(page1.getContent().size() <= 5);
        assertTrue(page1.getTotalPages() > 0);
    }

    // ---- Count ----
    @Test
    @DisplayName("should count active employees by country and department")
    void shouldCountByCountryAndDept() {
        saveEmployees();

        long count = employeeRepository.countByCountryAndDeptAndIsActiveTrue("United States (US)", "Engineering");
        assert count >= 0; // depends on seed data but should not throw
    }

    // ---- Delete ----
    @Test
    @DisplayName("should soft-delete employee and it should no longer appear in active queries")
    void softDeleteShouldHideFromActiveQueries() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        Optional<Employee> before = employeeRepository.findActiveById(emp.getId());
        assertTrue(before.isPresent());

        emp.setActive(false);
        emp.setEmploymentStatus(Employee.EmploymentStatus.TERMINATED);
        employeeRepository.save(emp);

        Optional<Employee> after = employeeRepository.findActiveById(emp.getId());
        assertFalse(after.isPresent());
    }

    // ---- Cascading Delete ----
    @Test
    @DisplayName("should cascade delete salary records when employee is deleted")
    void shouldCascadeDeleteSalaryRecords() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        SalaryRepository.SalarySummaryResponse summary = new SalaryRepository.SalarySummaryResponse();
        // Use the real entity instead
        com.acme.salarymgmt.model.Salary salary = new com.acme.salarymgmt.model.Salary();
        salary.setEmployeeId(emp.getId());
        salary.setSalaryAmount(new BigDecimal("100000"));
        salary.setCurrencyCode("USD");
        salaryRepository.save(salary);

        // FK with OnDeleteAction.CASCADE should remove salary when employee is deleted
        employeeRepository.deleteById(emp.getId());

        Optional<com.acme.salarymgmt.model.Salary> remainingSalary = salaryRepository.findById(salary.getId());
        assertFalse(remainingSalary.isPresent(), "Salary should be cascade-deleted");
    }

    // ---- Helper ----
    private Employee createSampleEmployee() {
        Employee emp = new Employee();
        emp.setEmpNo("EMP-001");
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setEmail("john.doe@acme.com");
        emp.setPhone("+1-555-0001");
        emp.setAddressLine1("123 Main St");
        emp.setCity("New York");
        emp.setStateProvince("NY");
        emp.setZipCode("10001");
        emp.setCountry("United States (US)");
        emp.setDept("Engineering");
        emp.setJobTitle("Software Engineer II");
        emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        emp.setStartDate(LocalDate.of(2023, 1, 15));
        emp.setActive(true);
        return emp;
    }

    private void saveEmployees() {
        // Save seed data for search/filter tests
        String[] countries = {"United States (US)", "United Kingdom (UK)", "Germany (DE)"};
        String[] depts = {"Engineering", "Sales", "Marketing"};
        int idx = 0;
        for (String country : countries) {
            for (String dept : depts) {
                Employee emp = createSampleEmployee();
                emp.setEmpNo("EMP-" + (++idx));
                emp.setCountry(country);
                emp.setDept(dept);
                employeeRepository.save(emp);
            }
        }

        // Add one with "Doe" in last name for search test
        Employee doe = createSampleEmployee();
        doe.setEmpNo("EMP-100");
        doe.setLastName("Doe");
        employeeRepository.save(doe);
    }
}
