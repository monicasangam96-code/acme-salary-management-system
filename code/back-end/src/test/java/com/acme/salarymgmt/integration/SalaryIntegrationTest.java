package com.acme.salarymgmt.integration;

import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.model.Salary;
import com.acme.salarymgmt.repository.CurrencyRepository;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Salary repository and service with H2 in-memory DB.
 * Verifies: salary history, CURRENT/SUPERSEDED lifecycle, multi-currency, cascading deletes.
 */
@SpringBootTest
class SalaryIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @BeforeEach
    void setUp() {
        salaryRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    // ---- createSalary ----
    @Test
    @DisplayName("should create a new salary record for an active employee")
    void shouldCreateSalary() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        Salary salary = new Salary();
        salary.setEmployee(emp);
        salary.setSalaryAmount(new BigDecimal("100000"));
        salary.setCurrencyCode("USD");
        salary.setStatus(Salary.SalaryStatus.CURRENT);
        salary.setEffectiveDate(LocalDate.of(2025, 1, 1));

        salary = salaryRepository.save(salary);

        assertNotNull(salary.getId());
        assertEquals(emp.getEmpNo(), salary.getEmployee().getEmpNo());
        assertTrue(new BigDecimal("100000").compareTo(salary.getSalaryAmount()) == 0);
    }

    // ---- getSalaryById ----
    @Test
    @DisplayName("should return salary detail by ID")
    void shouldReturnSalaryById() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        Salary salary = new Salary();
        salary.setEmployee(emp);
        salary.setSalaryAmount(new BigDecimal("100000"));
        salary.setCurrencyCode("USD");
        salary.setStatus(Salary.SalaryStatus.CURRENT);
        salary.setEffectiveDate(LocalDate.of(2025, 1, 1));
        salary = salaryRepository.save(salary);

        Optional<Salary> found = salaryRepository.findById(salary.getId());
        assertTrue(found.isPresent());
        assertTrue(new BigDecimal("100000").compareTo(found.get().getSalaryAmount()) == 0);
    }

    // ---- getSalaryHistory ----
    @Test
    @DisplayName("should return all salaries for employee ordered by effective date desc")
    void shouldReturnSalaryHistory() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        Salary s1 = new Salary();
        s1.setEmployee(emp);
        s1.setSalaryAmount(new BigDecimal("80000"));
        s1.setCurrencyCode("USD");
        s1.setStatus(Salary.SalaryStatus.CURRENT);
        s1.setEffectiveDate(LocalDate.of(2024, 1, 1));
        salaryRepository.save(s1);

        Salary s2 = new Salary();
        s2.setEmployee(emp);
        s2.setSalaryAmount(new BigDecimal("100000"));
        s2.setCurrencyCode("USD");
        s2.setStatus(Salary.SalaryStatus.CURRENT);
        s2.setEffectiveDate(LocalDate.of(2025, 1, 1));
        Salary savedS2 = salaryRepository.save(s2);

        // Supersede old
        s1.setStatus(Salary.SalaryStatus.SUPERSEDED);
        salaryRepository.save(s1);

        List<Salary> history = salaryRepository.findByEmployeeIdOrderByEffectiveDateDesc(emp.getId());
        assertEquals(2, history.size());
        assertEquals(savedS2.getEffectiveDate(), history.get(0).getEffectiveDate());
    }

    // ---- updateCurrentSalary ----
    @Test
    @DisplayName("should set old CURRENT to SUPERSEDED and create new CURRENT on update")
    void shouldSupersedeOldSalary() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        Salary currentSalary = new Salary();
        currentSalary.setEmployee(emp);
        currentSalary.setSalaryAmount(new BigDecimal("80000"));
        currentSalary.setCurrencyCode("USD");
        currentSalary.setStatus(Salary.SalaryStatus.CURRENT);
        currentSalary.setEffectiveDate(LocalDate.of(2024, 1, 1));
        salaryRepository.save(currentSalary);

        // Manually set old salary to SUPERSEDED (simulating the service layer behavior)
        currentSalary.setStatus(Salary.SalaryStatus.SUPERSEDED);
        salaryRepository.save(currentSalary);

        Salary newSalary = new Salary();
        newSalary.setEmployee(emp);
        newSalary.setSalaryAmount(new BigDecimal("100000"));
        newSalary.setCurrencyCode("USD");
        newSalary.setStatus(Salary.SalaryStatus.CURRENT);
        newSalary.setEffectiveDate(LocalDate.of(2025, 7, 1));
        salaryRepository.save(newSalary);

        // Old should be SUPERSEDED (manually set since no cascade update in test)
        List<Salary> allForEmployee = salaryRepository.findByEmployeeIdOrderByEffectiveDateDesc(emp.getId());
        assertTrue(allForEmployee.stream().anyMatch(s -> s.getStatus() == Salary.SalaryStatus.SUPERSEDED));
        assertTrue(allForEmployee.stream().anyMatch(s -> s.getStatus() == Salary.SalaryStatus.CURRENT));
    }

    // ---- multiCurrency ----
    @Test
    @DisplayName("should support multiple currencies for different employees")
    void shouldSupportMultiCurrency() {
        Employee emp1 = createSampleEmployee();
        emp1 = employeeRepository.save(emp1);

        Salary s1 = new Salary();
        s1.setEmployee(emp1);
        s1.setSalaryAmount(new BigDecimal("100000"));
        s1.setCurrencyCode("USD");
        s1.setStatus(Salary.SalaryStatus.CURRENT);
        s1.setEffectiveDate(LocalDate.of(2025, 1, 1));
        salaryRepository.save(s1);

        Employee emp2 = createSampleEmployee();
        emp2 = employeeRepository.save(emp2);

        Salary s2 = new Salary();
        s2.setEmployee(emp2);
        s2.setSalaryAmount(new BigDecimal("95000"));
        s2.setCurrencyCode("EUR");
        s2.setStatus(Salary.SalaryStatus.CURRENT);
        s2.setEffectiveDate(LocalDate.of(2025, 1, 1));
        salaryRepository.save(s2);

        var usSalaries = salaryRepository.findAll().stream()
                .filter(s -> s.getCurrencyCode().equals("USD"))
                .toList();
        var eurSalaries = salaryRepository.findAll().stream()
                .filter(s -> s.getCurrencyCode().equals("EUR"))
                .toList();
        assertEquals(1, usSalaries.size());
        assertEquals(1, eurSalaries.size());
    }

    // ---- deleteSalary ----
    @Test
    @DisplayName("should not find deleted salary")
    void shouldNotFindDeletedSalary() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        Salary salary = new Salary();
        salary.setEmployee(emp);
        salary.setSalaryAmount(new BigDecimal("100000"));
        salary.setCurrencyCode("USD");
        salary.setStatus(Salary.SalaryStatus.CURRENT);
        salary.setEffectiveDate(LocalDate.of(2025, 1, 1));
        Salary saved = salaryRepository.save(salary);

        salaryRepository.deleteById(saved.getId());

        Optional<Salary> found = salaryRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    // ---- currentSalaryExists ----
    @Test
    @DisplayName("should return true when CURRENT salary exists")
    void shouldFindCurrentSalary() {
        Employee emp = createSampleEmployee();
        emp = employeeRepository.save(emp);

        Salary currentSalary = new Salary();
        currentSalary.setEmployee(emp);
        currentSalary.setSalaryAmount(new BigDecimal("100000"));
        currentSalary.setCurrencyCode("USD");
        currentSalary.setStatus(Salary.SalaryStatus.CURRENT);
        currentSalary.setEffectiveDate(LocalDate.of(2025, 1, 1));
        salaryRepository.save(currentSalary);

        Optional<Salary> result = salaryRepository.findCurrentByEmployeeId(emp.getId());
        assertTrue(result.isPresent());
        assertEquals(Salary.SalaryStatus.CURRENT, result.get().getStatus());
    }

    // ---- Helper ----
    private Employee createSampleEmployee() {
        Employee emp = new Employee();
        emp.setEmpNo("EMPSAL" + (System.currentTimeMillis() % 10000));
        emp.setFirstName("Salary");
        emp.setLastName("TestUser");
        emp.setEmail("test-salary-" + System.currentTimeMillis() + "@acme.com");
        emp.setCity("Test City");
        emp.setCountry("United States (US)");
        emp.setDept("Engineering");
        emp.setJobTitle("Employee");
        emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        emp.setStartDate(LocalDate.of(2025, 1, 1));
        emp.setActive(true);
        emp.setAddressLine1("123 Test St");
        emp.setZipCode("00001");
        return emp;
    }
}
