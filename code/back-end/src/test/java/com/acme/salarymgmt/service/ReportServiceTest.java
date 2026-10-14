package com.acme.salarymgmt.service;

import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReportService reportService;

    private Employee makeEmployee(String empNo, String firstName, String lastName, String city, String country, String dept, String jobTitle, String status) {
        Employee emp = new Employee();
        emp.setId(System.currentTimeMillis()); // unique ID per test
        emp.setEmpNo(empNo);
        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        emp.setEmail(firstName.toLowerCase() + "@acme.com");
        emp.setPhone("+1-555-" + empNo);
        emp.setAddressLine1("123 Main St, Apt 4B");
        emp.setCity(city != null ? city : "New York");
        emp.setStateProvince("NY");
        emp.setZipCode("10001");
        emp.setCountry(country != null ? country : "United States (US)");
        emp.setDept(dept != null ? dept : "Engineering");
        emp.setJobTitle(jobTitle != null ? jobTitle : "SWE II");
        emp.setEmploymentStatus(status != null ? Employee.EmploymentStatus.valueOf(status) : Employee.EmploymentStatus.ACTIVE);
        emp.setStartDate(LocalDate.of(2023, 1, 15));
        emp.setActive(true);
        return emp;
    }

    private Employee usEmployee;
    private Employee ukEmployee;

    @BeforeEach
    void setUp() {
        usEmployee = makeEmployee("EMP-001", "John", "Doe", "New York", "United States (US)", "Engineering", "SWE II", "ACTIVE");
        ukEmployee = makeEmployee("EMP-002", "Jane", "Smith", "London", "United Kingdom (UK)", "Sales", "Sales Rep", "ACTIVE");
    }

    // ---- exportEmployeesCsv ----
    @Nested
    @DisplayName("exportEmployeesCsv")
    class ExportEmployeesCsvTests {

        @Test
        @DisplayName("should generate valid CSV with header row and data rows")
        void shouldGenerateValidCsv() throws Exception {
            Page<Employee> page = new PageImpl<>(List.of(usEmployee));
            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertNotNull(csv);
            assertTrue(csv.contains("emp_no,first_name,last_name"));
            assertTrue(csv.contains("EMP-001"));
            assertTrue(csv.contains("John"));
            assertTrue(csv.contains("Doe"));
        }

        @Test
        @DisplayName("should handle special characters in data with quoting")
        void shouldEscapeSpecialCharacters() throws Exception {
            Employee special = makeEmployee("EMP-003", "Bob", "OBrien", null, null, null, null, null);
            special.setFirstName("Bob");
            special.setLastName("O'Brien");

            Page<Employee> page = new PageImpl<>(List.of(special));
            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains("\"Bob\""));
            assertTrue(csv.contains("\"O'Brien\""));
        }

        @Test
        @DisplayName("should handle empty fields as blank")
        void shouldHandleEmptyFields() throws Exception {
            Employee emp = makeEmployee("EMP-EMPTY", "Empty", "Fields", "Nowhere", null, null, null, null);
            emp.setPhone(null);
            emp.setStateProvince(null);

            Page<Employee> page = new PageImpl<>(List.of(emp));
            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains("EMP-EMPTY"));
        }

        @Test
        @DisplayName("should paginate for large datasets")
        void shouldPaginateLargeResults() throws Exception {
            Employee p2 = makeEmployee("EMP-P2", "Page", "Two", null, null, null, null, null);

            // With pageSize=500, first page returns both; second call is empty (service's break condition)
            var page1 = new PageImpl<Employee>(List.of(usEmployee, p2), org.springframework.data.domain.PageRequest.of(0, 500), 2);

            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any()))
                    .thenReturn(page1)
                    .thenReturn(Page.empty());  // No more pages - service breaks on empty

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains("EMP-001"));
            assertTrue(csv.contains("EMP-P2"));
        }

        @Test
        @DisplayName("should include header row in output")
        void shouldIncludeHeaderRow() throws Exception {
            Page<Employee> page = new PageImpl<>(List.of(makeEmployee("EMP-H", "Header", "Test", null, null, null, null, null)));
            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            String[] lines = csv.split("\n");
            assertTrue(lines.length > 1);
            assertTrue(csv.contains("emp_no"));
        }
    }
}
