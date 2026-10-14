package com.acme.salarymgmt.integration;

import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.model.Salary;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for analytics JPQL queries with real MySQL data.
 * Verifies: computeSummary aggregates, country/dept/job-level breakdowns, currency filtering.
 */
@SpringBootTest
@Testcontainers
class AnalyticsIntegrationTest {

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

    @BeforeEach
    void setUp() {
        salaryRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    // ---- computeSummary ----
    @Test
    @DisplayName("should return correct aggregates for all current salaries")
    void shouldReturnCorrectAggregates() {
        saveSampleDataForAnalytics();

        List<SalaryRepository.AnalyticsSummaryResponse> results = salaryRepository.computeSummary(null, null, null);

        assertEquals(1, results.size());
        var summary = results.get(0);
        assertNotNull(summary.getTotalPayroll());
        assertTrue(summary.getTotalPayroll().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(summary.getEmployeeCount());
        assertTrue(summary.getEmployeeCount() > 0);
    }

    @Test
    @DisplayName("should return zero totals when no employees exist")
    void shouldReturnZeroForEmptyDataSet() {
        List<SalaryRepository.AnalyticsSummaryResponse> results = salaryRepository.computeSummary(null, null, null);

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("should filter summary by country")
    void shouldFilterSummaryByCountry() {
        saveSampleDataForAnalytics();

        List<SalaryRepository.AnalyticsSummaryResponse> results = salaryRepository.computeSummary(null, "United States (US)", null);

        assertEquals(1, results.size());
        assertTrue(results.get(0).getTotalPayroll().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("should filter summary by department")
    void shouldFilterSummaryByDepartment() {
        saveSampleDataForAnalytics();

        List<SalaryRepository.AnalyticsSummaryResponse> results = salaryRepository.computeSummary(null, null, "Engineering");

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("should filter summary by currency code")
    void shouldFilterSummaryByCurrency() {
        saveSampleDataForAnalytics();

        List<SalaryRepository.AnalyticsSummaryResponse> usResults = salaryRepository.computeSummary("USD", null, null);
        List<SalaryRepository.AnalyticsSummaryResponse> eurResults = salaryRepository.computeSummary("EUR", null, null);

        assertEquals(1, usResults.size());
        assertEquals(1, eurResults.size());
    }

    // ---- getAnalyticsByCountry ----
    @Test
    @DisplayName("should return country breakdown ordered by total payroll desc")
    void shouldReturnCountryBreakdown() {
        saveSampleDataForAnalytics();

        List<SalaryRepository.CountryBreakdownResponse> results = salaryRepository.getAnalyticsByCountry(null);

        assertTrue(results.size() >= 2, "Should have at least 2 countries");
        for (int i = 0; i < results.size() - 1; i++) {
            BigDecimal currentTotal = results.get(i).getTotalPayroll();
            BigDecimal nextTotal = results.get(i + 1).getTotalPayroll();
            assertTrue(currentTotal.compareTo(nextTotal) >= 0, "Should be in descending order");
        }
    }

    // ---- getAnalyticsByDepartment ----
    @Test
    @DisplayName("should return department breakdown ordered by total payroll desc")
    void shouldReturnDepartmentBreakdown() {
        saveSampleDataForAnalytics();

        List<SalaryRepository.DepartmentBreakdownResponse> results = salaryRepository.getAnalyticsByDepartment(null, null);

        assertTrue(results.size() >= 2, "Should have at least 2 departments");
        for (int i = 0; i < results.size() - 1; i++) {
            BigDecimal currentTotal = results.get(i).getTotalPayroll();
            BigDecimal nextTotal = results.get(i + 1).getTotalPayroll();
            assertTrue(currentTotal.compareTo(nextTotal) >= 0, "Should be in descending order");
        }
    }

    // ---- getAnalyticsByJobLevel ----
    @Test
    @DisplayName("should return job level breakdown with avg salary")
    void shouldReturnJobLevelBreakdown() {
        saveSampleDataForAnalytics();

        List<SalaryRepository.JobLevelBreakdownResponse> results = salaryRepository.getAnalyticsByJobLevel(null, null, null);

        assertTrue(results.size() >= 1, "Should have at least 1 job level");
        for (var result : results) {
            assertNotNull(result.getAvgSalary());
            assertTrue(result.getAvgSalary().compareTo(BigDecimal.ZERO) > 0);
            assertNotNull(result.getMaxSalary());
            assertNotNull(result.getMinSalary());
        }
    }

    // ---- Mixed Currencies ----
    @Test
    @DisplayName("should not mix currencies in totals — each currency has its own total")
    void shouldNotMixCurrencies() {
        Employee empUs = createEmployee("EMP-USD-MIX", "United States (US)", "Engineering");
        Employee empEu = createEmployee("EMP-EUR-MIX", "Germany (DE)", "Engineering");

        Salary usSalary = new Salary();
        usSalary.setEmployeeId(empUs.getId());
        usSalary.setSalaryAmount(new BigDecimal("150000"));
        usSalary.setCurrencyCode("USD");
        usSalary.setStatus(Salary.SalaryStatus.CURRENT);
        usSalary.setEffectiveDate(LocalDate.of(2025, 1, 1));
        salaryRepository.save(usSalary);

        Salary euSalary = new Salary();
        euSalary.setEmployeeId(empEu.getId());
        euSalary.setSalaryAmount(new BigDecimal("90000"));
        euSalary.setCurrencyCode("EUR");
        euSalary.setStatus(Salary.SalaryStatus.CURRENT);
        euSalary.setEffectiveDate(LocalDate.of(2025, 1, 1));
        salaryRepository.save(euSalary);

        var usResults = salaryRepository.computeSummary("USD", null, null);
        var eurResults = salaryRepository.computeSummary("EUR", null, null);

        assertEquals(new BigDecimal("150000"), usResults.get(0).getTotalPayroll());
        assertEquals(new BigDecimal("90000"), eurResults.get(0).getTotalPayroll());
    }

    // ---- Helper ----
    private void saveSampleDataForAnalytics() {
        String[] countries = {"United States (US)", "Germany (DE)", "United Kingdom (UK)"};
        String[] depts = {"Engineering", "Sales", "Marketing"};
        String[] currencies = {"USD", "EUR", "GBP"};
        BigDecimal[] salaries = {new BigDecimal("120000"), new BigDecimal("85000"), new BigDecimal("75000")};

        for (int i = 0; i < 3; i++) {
            Employee emp = createEmployee("EMP-AN-" + i, countries[i], depts[i]);
            Salary salary = new Salary();
            salary.setEmployeeId(emp.getId());
            salary.setSalaryAmount(salaries[i]);
            salary.setCurrencyCode(currencies[i]);
            salary.setStatus(Salary.SalaryStatus.CURRENT);
            salary.setEffectiveDate(LocalDate.of(2025, 1, 1));
            salaryRepository.save(salary);
        }

        // Add an extra US/Sales employee to test aggregation
        Employee usSales = createEmployee("EMP-AN-SALES", "United States (US)", "Sales");
        Salary salesSalary = new Salary();
        salesSalary.setEmployeeId(usSales.getId());
        salesSalary.setSalaryAmount(new BigDecimal("95000"));
        salesSalary.setCurrencyCode("USD");
        salesSalary.setStatus(Salary.SalaryStatus.CURRENT);
        salesSalary.setEffectiveDate(LocalDate.of(2025, 1, 1));
        salaryRepository.save(salesSalary);
    }

    private Employee createEmployee(String empNo, String country, String dept) {
        Employee emp = new Employee();
        emp.setEmpNo(empNo);
        emp.setFirstName("Analytics");
        emp.setLastName("TestUser");
        emp.setEmail(empNo.toLowerCase() + "@acme.com");
        emp.setCity("Test City");
        emp.setCountry(country);
        emp.setDept(dept);
        emp.setJobTitle("Employee");
        emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        emp.setStartDate(LocalDate.of(2025, 1, 1));
        emp.setActive(true);
        return employeeRepository.save(emp);
    }
}
