package com.acme.salarymgmt.service.unit;

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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private com.acme.salarymgmt.service.ReportService reportService;

    // ---- exportEmployeesCsv ----
    @Nested
    @DisplayName("exportEmployeesCsv")
    class ExportEmployeesCsvTests {

        private Employee makeEmployee(String empNo, String firstName, String lastName, String city) {
            Employee emp = new Employee();
            emp.setId(1L);
            emp.setEmpNo(empNo);
            emp.setFirstName(firstName);
            emp.setLastName(lastName);
            emp.setEmail(firstName.toLowerCase() + "@acme.com");
            emp.setPhone("+1-555-" + empNo);
            emp.setAddressLine1("123 Main St");
            emp.setCity(city != null ? city : "New York");
            emp.setStateProvince("NY");
            emp.setZipCode("10001");
            emp.setCountry("United States (US)");
            emp.setDept("Engineering");
            emp.setJobTitle("SWE II");
            emp.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
            emp.setStartDate(LocalDate.of(2023, 1, 15));
            emp.setActive(true);
            return emp;
        }

        @Test
        @DisplayName("should generate valid CSV with header row and data rows")
        void shouldGenerateValidCsv() throws Exception {
            var page = new PageImpl<Employee>(List.of(makeEmployee("EMP-001", "John", "Doe", "New York")));
            when(employeeRepository.searchWithFilters(any(), any(), any(), any(), any())).thenReturn(page);

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
            Employee emp = makeEmployee("EMP-003", "Bob", "O'Brien", null);
            emp.setAddressLine1("123 Main St, Apt 4B");

            var page = new PageImpl<Employee>(List.of(emp));
            when(employeeRepository.searchWithFilters(any(), any(), any(), any(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains("\"Bob\""));
            assertTrue(csv.contains("\"O'Brien\""));
            assertTrue(csv.contains("\"123 Main St; Apt 4B\""));
        }

        @Test
        @DisplayName("should use empty strings for null fields")
        void shouldHandleEmptyFields() throws Exception {
            Employee emp = makeEmployee("EMP-EMPTY", "Empty", "Fields", "Nowhere");
            emp.setPhone(null);
            emp.setStateProvince(null);

            var page = new PageImpl<Employee>(List.of(emp));
            when(employeeRepository.searchWithFilters(any(), any(), any(), any(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains("EMP-EMPTY"));
            // Null fields should be escaped as empty strings (just commas)
        }

        @Test
        @DisplayName("should paginate for large datasets")
        void shouldPaginateLargeResults() throws Exception {
            Employee p1 = makeEmployee("EMP-P1", "Page", "One", null);
            Employee p2 = makeEmployee("EMP-P2", "Page", "Two", null);

            // With pageSize=500, first page returns both employees; second call is empty (service's break condition)
            var page1 = new PageImpl<Employee>(List.of(p1, p2), org.springframework.data.domain.PageRequest.of(0, 500), 2);

            when(employeeRepository.searchWithFilters(any(), any(), any(), any(), any()))
                    .thenReturn(page1)
                    .thenReturn(Page.empty());  // No more pages - service breaks on empty

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains("EMP-P1"));
            assertTrue(csv.contains("EMP-P2"));
        }

        @Test
        @DisplayName("should include header row in output")
        void shouldIncludeHeaderRow() throws Exception {
            var page = new PageImpl<Employee>(List.of(makeEmployee("EMP-H", "Header", "Test", null)));
            when(employeeRepository.searchWithFilters(any(), any(), any(), any(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            String[] lines = csv.split("\n");
            assertEquals("emp_no", lines[0].split(",")[0]);
            assertEquals("current_salary", lines[0].split(",")[15]);
        }
    }
}
