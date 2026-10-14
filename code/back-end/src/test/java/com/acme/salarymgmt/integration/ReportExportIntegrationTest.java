package com.acme.salarymgmt.integration;

import com.acme.salarymgmt.model.Currency;
import com.acme.salarymgmt.dto.response.AnalyticsSummaryResponse;
import com.acme.salarymgmt.dto.response.CountryBreakdownResponse;
import com.acme.salarymgmt.dto.response.DepartmentBreakdownResponse;
import com.acme.salarymgmt.dto.response.JobLevelBreakdownResponse;
import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.dto.response.AnalyticsSummaryResponse;
import com.acme.salarymgmt.dto.response.CountryBreakdownResponse;
import com.acme.salarymgmt.dto.response.DepartmentBreakdownResponse;
import com.acme.salarymgmt.dto.response.JobLevelBreakdownResponse;
import com.acme.salarymgmt.model.Salary;
import com.acme.salarymgmt.dto.response.AnalyticsSummaryResponse;
import com.acme.salarymgmt.dto.response.CountryBreakdownResponse;
import com.acme.salarymgmt.dto.response.DepartmentBreakdownResponse;
import com.acme.salarymgmt.dto.response.JobLevelBreakdownResponse;
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
 * Integration tests for Currency repository and salary report queries with H2 in-memory DB.
 * Verifies: currency CRUD, active/inactive currencies, report analytics queries.
 */
@SpringBootTest
class ReportExportIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        salaryRepository.deleteAll();
        employeeRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    // ---- Currency Operations ----
    @Test
    @DisplayName("should create and retrieve a new currency")
    void shouldCreateAndFindCurrency() {
        Currency currency = new Currency();
        currency.setCode("JPY");
        currency.setName("Japanese Yen");
        currency.setSymbol("¥");
        currency.setNativeName("癲ライ");
        currency.setActive(true);
        Currency saved = currencyRepository.save(currency);

        assertEquals("JPY", saved.getCode());

        Optional<Currency> found = currencyRepository.findById("JPY");
        assertTrue(found.isPresent());
        assertEquals("Japanese Yen", found.get().getName());
    }

    @Test
    @DisplayName("should find currencies by code")
    void shouldFindCurrencyByCode() {
        Currency currency = new Currency();
        currency.setCode("GBP");
        currency.setName("British Pound");
        currency.setSymbol("£");
        currency.setNativeName("Pound Sterling");
        currency.setActive(true);
        currencyRepository.save(currency);

        Optional<Currency> found = currencyRepository.findByCode("GBP");
        assertTrue(found.isPresent());
        assertEquals("British Pound", found.get().getName());
    }

    @Test
    @DisplayName("should return only active currencies")
    void shouldReturnActiveCurrencies() {
        Currency activeCurrency = new Currency();
        activeCurrency.setCode("USD");
        activeCurrency.setName("US Dollar");
        activeCurrency.setSymbol("$");
        activeCurrency.setNativeName("US Dollar");
        activeCurrency.setActive(true);
        currencyRepository.save(activeCurrency);

        Currency inactiveCurrency = new Currency();
        inactiveCurrency.setCode("IRR");
        inactiveCurrency.setName("Iranian Rial");
        inactiveCurrency.setSymbol("﷼");
        inactiveCurrency.setNativeName("ریال");
        inactiveCurrency.setActive(false);
        currencyRepository.save(inactiveCurrency);

        List<Currency> activeCurrencies = currencyRepository.findAllByIsActiveTrue();

        assertEquals(1, activeCurrencies.size());
        assertEquals("USD", activeCurrencies.get(0).getCode());
    }

    // ---- Report Analytics Queries ----
    @Test
    @DisplayName("should compute total payroll across all employees")
    void shouldComputeTotalPayroll() {
        Employee emp1 = createEmployee("EMP-RPT-1", "United States (US)", "Engineering");
        Salary s1 = createSalary(emp1, new BigDecimal("100000"), "USD");
        salaryRepository.save(s1);

        Employee emp2 = createEmployee("EMP-RPT-2", "Germany (DE)", "Engineering");
        Salary s2 = createSalary(emp2, new BigDecimal("80000"), "EUR");
        salaryRepository.save(s2);

        // Verify USD total
        List<AnalyticsSummaryResponse> usResults = salaryRepository.computeSummary("USD", null, null);
        assertEquals(1, usResults.size());
        assertTrue(new BigDecimal("100000").compareTo(usResults.get(0).getTotalPayroll()) == 0);

        // Verify EUR total
        List<AnalyticsSummaryResponse> eurResults = salaryRepository.computeSummary("EUR", null, null);
        assertEquals(1, eurResults.size());
        assertTrue(new BigDecimal("80000").compareTo(eurResults.get(0).getTotalPayroll()) == 0);
    }

    @Test
    @DisplayName("should compute department breakdown for payroll analytics")
    void shouldComputeDepartmentBreakdown() {
        Employee engEmp = createEmployee("EMP-DEPT-1", "United States (US)", "Engineering");
        salaryRepository.save(createSalary(engEmp, new BigDecimal("120000"), "USD"));

        Employee hrEmp = createEmployee("EMP-DEPT-2", "United Kingdom (UK)", "HR");
        salaryRepository.save(createSalary(hrEmp, new BigDecimal("75000"), "GBP"));

        List<DepartmentBreakdownResponse> results =
                salaryRepository.getAnalyticsByDepartment(null, null);

        assertTrue(results.size() >= 2, "Should have at least Engineering and HR departments");
        for (var result : results) {
            assertNotNull(result.getTotalPayroll());
            assertTrue(result.getTotalPayroll().compareTo(BigDecimal.ZERO) >= 0);
        }
    }

    @Test
    @DisplayName("should compute job level breakdown with average salary")
    void shouldComputeJobLevelBreakdown() {
        Employee seniorEmp = createEmployee("EMP-JL-1", "United States (US)", "Engineering");
        salaryRepository.save(createSalary(seniorEmp, new BigDecimal("150000"), "USD"));

        Employee juniorEmp = createEmployee("EMP-JL-2", "Germany (DE)", "Marketing");
        salaryRepository.save(createSalary(juniorEmp, new BigDecimal("60000"), "EUR"));

        List<JobLevelBreakdownResponse> results =
                salaryRepository.getAnalyticsByJobLevel(null, null, null);

        assertFalse(results.isEmpty());
        for (var result : results) {
            assertNotNull(result.getAvgSalary());
            assertTrue(result.getAvgSalary().compareTo(BigDecimal.ZERO) > 0);
            assertNotNull(result.getMaxSalary());
            assertNotNull(result.getMinSalary());
        }
    }

    @Test
    @DisplayName("should support filtering analytics by currency")
    void shouldFilterAnalyticsByCurrency() {
        Employee emp1 = createEmployee("EMP-FLT-U", "United States (US)", "Sales");
        salaryRepository.save(createSalary(emp1, new BigDecimal("95000"), "USD"));

        Employee emp2 = createEmployee("EMP-FLT-E", "Germany (DE)", "Sales");
        salaryRepository.save(createSalary(emp2, new BigDecimal("85000"), "EUR"));

        List<AnalyticsSummaryResponse> usResults = salaryRepository.computeSummary("USD", null, null);
        List<AnalyticsSummaryResponse> eurResults = salaryRepository.computeSummary("EUR", null, null);

        assertEquals(1, usResults.size());
        assertEquals(1, eurResults.size());
        assertTrue(new BigDecimal("95000").compareTo(usResults.get(0).getTotalPayroll()) == 0);
        assertTrue(new BigDecimal("85000").compareTo(eurResults.get(0).getTotalPayroll()) == 0);
    }

    @Test
    @DisplayName("should filter analytics by department")
    void shouldFilterAnalyticsByDepartment() {
        Employee salesEmp = createEmployee("EMP-SALES", "United States (US)", "Sales");
        salaryRepository.save(createSalary(salesEmp, new BigDecimal("80000"), "USD"));

        Employee engEmp = createEmployee("EMP-ENG", "Germany (DE)", "Engineering");
        salaryRepository.save(createSalary(engEmp, new BigDecimal("120000"), "EUR"));

        List<AnalyticsSummaryResponse> salesResults = salaryRepository.computeSummary(null, null, "Sales");
        List<AnalyticsSummaryResponse> engResults = salaryRepository.computeSummary(null, null, "Engineering");

        assertEquals(1, salesResults.size());
        assertEquals(1, engResults.size());
    }

    @Test
    @DisplayName("should filter analytics by country")
    void shouldFilterAnalyticsByCountry() {
        Employee usEmp = createEmployee("EMP-US", "United States (US)", "Engineering");
        salaryRepository.save(createSalary(usEmp, new BigDecimal("100000"), "USD"));

        Employee euEmp = createEmployee("EMP-EU", "Germany (DE)", "Engineering");
        salaryRepository.save(createSalary(euEmp, new BigDecimal("90000"), "EUR"));

        List<AnalyticsSummaryResponse> usResults = salaryRepository.computeSummary(null, "United States (US)", null);
        List<AnalyticsSummaryResponse> euResults = salaryRepository.computeSummary(null, "Germany (DE)", null);

        assertEquals(1, usResults.size());
        assertEquals(1, euResults.size());
    }

    @Test
    @DisplayName("should compute country breakdown with descending order by total payroll")
    void shouldComputeCountryBreakdownDescending() {
        Employee highEmp = createEmployee("EMP-HIGH", "United States (US)", "Engineering");
        salaryRepository.save(createSalary(highEmp, new BigDecimal("200000"), "USD"));

        Employee lowEmp = createEmployee("EMP-LOW", "Germany (DE)", "Marketing");
        salaryRepository.save(createSalary(lowEmp, new BigDecimal("50000"), "EUR"));

        List<CountryBreakdownResponse> results = salaryRepository.getAnalyticsByCountry(null);

        assertTrue(results.size() >= 2, "Should have at least 2 countries");
        for (int i = 0; i < results.size() - 1; i++) {
            BigDecimal currentTotal = results.get(i).getTotalPayroll();
            BigDecimal nextTotal = results.get(i + 1).getTotalPayroll();
            assertTrue(currentTotal.compareTo(nextTotal) >= 0, "Should be in descending order");
        }
    }

    // ---- Helper ----
    private Employee createEmployee(String empNo, String country, String dept) {
        Employee emp = new Employee();
        emp.setEmpNo(empNo);
        emp.setFirstName("Report");
        emp.setLastName("TestUser");
        emp.setEmail(empNo.toLowerCase() + "@acme.com");
        emp.setCity("Test City");
        emp.setCountry(country);
        emp.setAddressLine1("123 Test St");
        emp.setZipCode("00001");
        emp.setDept(dept);
        emp.setJobTitle("Employee");
        emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        emp.setStartDate(LocalDate.of(2025, 1, 1));
        emp.setActive(true);
        return employeeRepository.save(emp);
    }

    private Salary createSalary(Employee emp, BigDecimal amount, String currencyCode) {
        Salary salary = new Salary();
        salary.setEmployee(emp);
        salary.setSalaryAmount(amount);
        salary.setCurrencyCode(currencyCode);
        salary.setStatus(Salary.SalaryStatus.CURRENT);
        salary.setEffectiveDate(LocalDate.of(2025, 1, 1));
        return salary;
    }
}
