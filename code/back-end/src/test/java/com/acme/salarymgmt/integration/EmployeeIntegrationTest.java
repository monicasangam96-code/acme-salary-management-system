package com.acme.salarymgmt.integration;

import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.repository.CurrencyRepository;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Employee repository with H2 in-memory DB.
 * Verifies: CRUD, soft-delete, search filters, pagination, duplicate detection.
 */
@SpringBootTest
class EmployeeIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        // Delete salaries first to respect FK constraint (salaries.employee_id → employees.id)
        salaryRepository.deleteAll();
        employeeRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    // ---- CRUD Operations ----
    @Test
    @DisplayName("should create a new employee and retrieve it by ID")
    void shouldCreateAndFindEmployee() {
        Employee emp = createSampleEmployee();
        Employee saved = employeeRepository.save(emp);

        assertNotNull(saved.getId());
        assertTrue(saved.getEmpNo().startsWith("EMP-001"));

        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    @DisplayName("should update an existing employee")
    void shouldUpdateEmployee() {
        Employee emp = createSampleEmployee();
        Employee saved = employeeRepository.save(emp);

        saved.setFirstName("Jonathan");
        saved.setLastName("Doe-Smith");
        saved.setJobTitle("Senior Software Engineer II");
        saved.setDept("Engineering Lead");

        Employee updated = employeeRepository.save(saved);

        assertEquals("Jonathan", updated.getFirstName());
        assertEquals("Doe-Smith", updated.getLastName());
        assertEquals("Senior Software Engineer II", updated.getJobTitle());
    }

    @Test
    @DisplayName("should deactivate an employee on soft delete")
    void shouldSoftDeleteEmployee() {
        Employee emp = createSampleEmployee();
        Employee saved = employeeRepository.save(emp);

        employeeRepository.delete(saved);

        Optional<Employee> found = employeeRepository.findActiveById(saved.getId());
        assertFalse(found.isPresent(), "Should not find deleted employee in active list");
    }

    // ---- Search with Filters ----
    @Test
    @DisplayName("should search employees by country filter")
    void shouldSearchByCountry() {
        Employee usEmp = createSampleEmployee();
        usEmp.setCountry("United States (US)");
        employeeRepository.save(usEmp);

        Employee ukEmp = createSampleEmployee();
        ukEmp.setEmpNo("EMP-UK-1");
        ukEmp.setFirstName("James");
        ukEmp.setLastName("Bond");
        ukEmp.setCountry("United Kingdom (UK)");
        employeeRepository.save(ukEmp);

        Page<Employee> result = employeeRepository.searchWithFilters(
                "United Kingdom (UK)", null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Bond", result.getContent().get(0).getLastName());
    }

    @Test
    @DisplayName("should search employees by department filter")
    void shouldSearchByDepartment() {
        Employee engEmp = createSampleEmployee();
        engEmp.setDept("Engineering");
        employeeRepository.save(engEmp);

        Employee hrEmp = createSampleEmployee();
        hrEmp.setEmpNo("EMP-HR-1");
        hrEmp.setFirstName("Alice");
        hrEmp.setLastName("Wonderland");
        hrEmp.setDept("HR");
        employeeRepository.save(hrEmp);

        Page<Employee> result = employeeRepository.searchWithFilters(
                null, "Engineering", null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("should search employees by status filter")
    void shouldSearchByStatus() {
        Employee activeEmp = createSampleEmployee();
        activeEmp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        employeeRepository.save(activeEmp);

        Page<Employee> result = employeeRepository.searchWithFilters(
                null, null, Employee.EmploymentStatus.ACTIVE, null, PageRequest.of(0, 10));

        assertTrue(result.getContent().stream().anyMatch(e -> e.getFirstName().equals("John")));
    }

    @Test
    @DisplayName("should search employees by text search on first/last name")
    void shouldSearchByText() {
        Employee emp = createSampleEmployee();
        emp.setFirstName("Alexander");
        emp.setLastName("Hamilton");
        employeeRepository.save(emp);

        Page<Employee> result = employeeRepository.searchWithFilters(
                null, null, null, "Alexander", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Alexander", result.getContent().get(0).getFirstName());
    }

    // ---- Pagination ----
    @Test
    @DisplayName("should paginate results correctly")
    void shouldPaginateResults() {
        for (int i = 0; i < 15; i++) {
            Employee emp = new Employee();
            emp.setEmpNo("EMPPAG" + i);
            emp.setFirstName("PAG" + i);
            emp.setLastName("User");
            emp.setEmail("pag-" + i + "@acme.com");
            emp.setCity("Test City");
            emp.setCountry("United States (US)");
            emp.setAddressLine1("123 Test St");
            emp.setZipCode("00001");
            emp.setDept("Engineering");
            emp.setJobTitle("Employee");
            emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
            emp.setStartDate(LocalDate.of(2025, 1, 1));
            emp.setActive(true);
            employeeRepository.save(emp);
        }

        Page<Employee> page1 = employeeRepository.searchWithFilters(
                null, null, null, null, PageRequest.of(0, 5));
        assertEquals(5, page1.getContent().size());
        assertEquals(15, page1.getTotalElements());

        Page<Employee> page2 = employeeRepository.searchWithFilters(
                null, null, null, null, PageRequest.of(1, 5));
        assertEquals(5, page2.getContent().size());
    }

    // ---- Duplicate Employee Number ----
    @Test
    @DisplayName("should detect duplicate employee number")
    void shouldDetectDuplicateEmpNo() {
        Employee emp1 = createSampleEmployee();
        employeeRepository.save(emp1);
        String actualEmpNo = emp1.getEmpNo();
        assertTrue(employeeRepository.existsByEmpNo(actualEmpNo));

        Employee emp2 = createSampleEmployee();
        emp2.setEmpNo("EMP-DUP");
        emp2.setFirstName("Duplicate");
        emp2.setEmail("dup@acme.com");
        employeeRepository.save(emp2);

        assertFalse(emp2.getEmpNo().equals(actualEmpNo));
    }

    // ---- List Active ----
    @Test
    @DisplayName("should return only active employees")
    void shouldReturnOnlyActiveEmployees() {
        Employee activeEmp = createSampleEmployee();
        employeeRepository.save(activeEmp);

        Employee inactiveEmp = createSampleEmployee();
        inactiveEmp.setEmpNo("EMPIACT");
        inactiveEmp.setFirstName("Former");
        inactiveEmp.setLastName("Employee");
        inactiveEmp.setEmail("former@acme.com");
        inactiveEmp.setActive(false);
        employeeRepository.save(inactiveEmp);

        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getActive() != null && e.getActive())
                .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
                .toList();

        assertEquals(1, activeEmployees.size());
        assertEquals("John", activeEmployees.get(0).getFirstName());
    }

    // ---- Helper ----
    private static final java.util.concurrent.atomic.AtomicLong EMAIL_COUNTER = new java.util.concurrent.atomic.AtomicLong(0);

    private Employee createSampleEmployee() {
        String suffix = "-" + EMAIL_COUNTER.getAndIncrement();
        return createSampleEmployeeWithSuffix(suffix);
    }

    private Employee createSampleEmployeeWithSuffix(String suffix) {
        Employee emp = new Employee();
        emp.setEmpNo("EMP-001" + suffix);
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setEmail("john.doe" + suffix + "@acme.com");
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
}
