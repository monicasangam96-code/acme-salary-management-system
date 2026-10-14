package com.acme.salarymgmt.integration;

import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CSV export with real MySQL data.
 * Verifies: correct row count, header format, filtered export, special character handling.
 */
@SpringBootTest
@Testcontainers
class ReportExportIntegrationTest {

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
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    // ---- Header Row ----
    @Test
    @DisplayName("should generate CSV with correct header row")
    void shouldGenerateCorrectHeader() throws Exception {
        saveSampleEmployees(3);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportEmployeesCsv(output, null, null, null, null, false);

        String csv = output.toString(StandardCharsets.UTF_8);
        String[] headers = csv.split("\n")[0].split(",");
        assertEquals("emp_no", headers[0]);
        assertEquals("first_name", headers[1]);
        assertEquals("last_name", headers[2]);
        assertEquals("email", headers[3]);
        assertEquals("current_salary", headers[15]);
        assertEquals("currency", headers[16]);
        assertEquals("effective_date", headers[17].trim());
    }

    // ---- Row Count ----
    @Test
    @DisplayName("should generate one data row per active employee (header excluded from count)")
    void shouldHaveCorrectRowCount() throws Exception {
        saveSampleEmployees(5);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportEmployeesCsv(output, null, null, null, null, false);

        String csv = output.toString(StandardCharsets.UTF_8);
        String[] lines = csv.split("\n");

        // First line is header, remaining are data rows
        assertEquals(6, lines.length, "1 header + 5 data rows");
    }

    // ---- Filtered Export ----
    @Test
    @DisplayName("should filter CSV export by country")
    void shouldFilterByCountry() throws Exception {
        saveEmployeesByCountry();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportEmployeesCsv(output, null, "United States (US)", null, null, false);

        String csv = output.toString(StandardCharsets.UTF_8);
        assertFalse(csv.contains("EMP-UK"));
        assertFalse(csv.contains("EMP-DE"));
        assertTrue(csv.contains("EMP-US"));
    }

    @Test
    @DisplayName("should filter CSV export by department")
    void shouldFilterByDepartment() throws Exception {
        saveEmployeesByCountry();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportEmployeesCsv(output, null, null, "Engineering", null, false);

        String csv = output.toString(StandardCharsets.UTF_8);
        List<String> lines = csv.lines().toList();
        assertTrue(lines.size() > 1); // header + at least one Engineering employee

        for (int i = 1; i < lines.size(); i++) {
            // Column index 10 is dept
            String[] fields = lines.get(i).split(",");
            assertEquals("Engineering", fields[10]);
        }
    }

    @Test
    @DisplayName("should filter CSV export by status")
    void shouldFilterByStatus() throws Exception {
        saveSampleEmployees(3);

        Employee suspended = new Employee();
        suspended.setEmpNo("EMP-STATUS");
        suspended.setFirstName("Suspended");
        suspended.setLastName("User");
        suspended.setEmail("suspended@acme.com");
        suspended.setCity("Test");
        suspended.setCountry("US");
        suspended.setDept("Engineering");
        suspended.setJobTitle("Engineer");
        suspended.setEmploymentStatus(Employee.EmploymentStatus.SUSPENDED);
        suspended.setStartDate(LocalDate.of(2025, 1, 1));
        suspended.setActive(true);
        employeeRepository.save(suspended);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportEmployeesCsv(output, null, null, null, "SUSPENDED", false);

        String csv = output.toString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("EMP-STATUS"));
        assertTrue(csv.contains("SUSPENDED"));
    }

    // ---- Special Characters ----
    @Test
    @DisplayName("should escape commas in address fields")
    void shouldEscapeSpecialCharacters() throws Exception {
        Employee emp = createEmployeeWithAddress("EMP-SPECIAL", "123 Main St, Apt 4B");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportEmployeesCsv(output, null, null, null, null, false);

        String csv = output.toString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("\"123 Main St, Apt 4B\""));
    }

    @Test
    @DisplayName("should handle empty fields gracefully")
    void shouldHandleEmptyFields() throws Exception {
        Employee emp = new Employee();
        emp.setEmpNo("EMP-EMPTY");
        emp.setFirstName("Empty");
        emp.setLastName("Fields");
        emp.setEmail("empty@acme.com");
        emp.setCity("Nowhere");
        emp.setCountry("US");
        emp.setDept("Other");
        emp.setJobTitle("N/A");
        emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        emp.setStartDate(LocalDate.of(2025, 1, 1));
        emp.setActive(true);
        // phone, address, state, zip intentionally null
        employeeRepository.save(emp);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportEmployeesCsv(output, null, null, null, null, false);

        String csv = output.toString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("EMP-EMPTY"));
    }

    // ---- Large Dataset Pagination ----
    @Test
    @DisplayName("should paginate through large dataset without data loss")
    void shouldHandleLargeDataset() throws Exception {
        saveSampleEmployees(1500);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportEmployeesCsv(output, null, null, null, null, false);

        String csv = output.toString(StandardCharsets.UTF_8);
        String[] lines = csv.split("\n");

        // 1 header + 1500 data rows
        assertEquals(1501, lines.length);
    }

    // ---- Helpers ----
    private void saveSampleEmployees(int count) {
        for (int i = 1; i <= count; i++) {
            Employee emp = new Employee();
            emp.setEmpNo("EMP-" + String.format("%04d", i));
            emp.setFirstName("Sample" + i);
            emp.setLastName("Employee");
            emp.setEmail("emp" + i + "@acme.com");
            emp.setPhone("+1-555-" + String.format("%04d", i));
            emp.setAddressLine1(i % 2 == 0 ? "123 Main St, Apt " + i : "456 Oak Ave");
            emp.setCity("Test City" + (i % 10));
            emp.setStateProvince(i % 2 == 0 ? "NY" : null);
            emp.setZipCode("1000" + (i % 100));
            emp.setCountry("United States (US)");
            emp.setDept(i % 3 == 0 ? "Engineering" : "Sales");
            emp.setJobTitle("Employee " + i);
            emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
            emp.setStartDate(LocalDate.of(2025, 1, 1));
            emp.setActive(true);
            employeeRepository.save(emp);
        }
    }

    private void saveEmployeesByCountry() {
        String[][] data = {
                {"EMP-US", "United States (US)", "Engineering"},
                {"EMP-UK", "United Kingdom (UK)", "Sales"},
                {"EMP-DE", "Germany (DE)", "Marketing"}
        };
        for (String[] row : data) {
            Employee emp = new Employee();
            emp.setEmpNo(row[0]);
            emp.setFirstName("Country");
            emp.setLastName(row[0].split("-")[1]);
            emp.setEmail(row[0].toLowerCase() + "@acme.com");
            emp.setCity("Test City");
            emp.setCountry(row[1]);
            emp.setDept(row[2]);
            emp.setJobTitle("Employee");
            emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
            emp.setStartDate(LocalDate.of(2025, 1, 1));
            emp.setActive(true);
            employeeRepository.save(emp);
        }
    }

    private Employee createEmployeeWithAddress(String empNo, String address) {
        Employee emp = new Employee();
        emp.setEmpNo(empNo);
        emp.setFirstName("Special");
        emp.setLastName("Chars");
        emp.setEmail("special@acme.com");
        emp.setPhone("+1-555-9999");
        emp.setAddressLine1(address);
        emp.setCity("Test City");
        emp.setZipCode("10001");
        emp.setCountry("US");
        emp.setDept("Engineering");
        emp.setJobTitle("Engineer");
        emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        emp.setStartDate(LocalDate.of(2025, 1, 1));
        emp.setActive(true);
        return employeeRepository.save(emp);
    }
}
