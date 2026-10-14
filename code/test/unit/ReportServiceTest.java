package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.EmployeeSummaryResponse;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @InjectMocks
    private ReportService reportService;

    private EmployeeSummaryResponse usEmployee;
    private EmployeeSummaryResponse ukEmployee;

    @BeforeEach
    void setUp() {
        usEmployee = new EmployeeSummaryResponse(
                1L, "EMP-001", "John", "Doe",
                "US", "Engineering", "SWE II", "ACTIVE",
                new BigDecimal("120000"), "USD");

        ukEmployee = new EmployeeSummaryResponse(
                2L, "EMP-002", "Jane", "Smith",
                "UK", "Sales", "Sales Rep", "ACTIVE",
                new BigDecimal("75000"), "GBP");
    }

    // ---- exportEmployeesCsv ----
    @Nested
    @DisplayName("exportEmployeesCsv")
    class ExportEmployeesCsvTests {

        @Test
        @DisplayName("should generate valid CSV with header row and data rows")
        void shouldGenerateValidCsv() throws Exception {
            List<EmployeeSummaryResponse> page1 = List.of(usEmployee);
            Page<EmployeeSummaryResponse> page = new PageImpl<>(page1);
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
            EmployeeSummaryResponse special = new EmployeeSummaryResponse(
                    3L, "EMP-003", "Bob \"Bomber\"", "O'Brien",
                    "US", "HR", "Analyst", "ACTIVE", null, null);
            List<EmployeeSummaryResponse> page1 = List.of(special);
            Page<EmployeeSummaryResponse> page = new PageImpl<>(page1);
            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains("\"Bob \"\"Bomber\"\"\""));
        }

        @Test
        @DisplayName("should paginate for large datasets")
        void shouldPaginateLargeResults() throws Exception {
            EmployeeSummaryResponse p2 = new EmployeeSummaryResponse(
                    4L, "EMP-004", "Page", "Two",
                    "DE", "Marketing", "MKT", "ACTIVE", null, null);

            Page<EmployeeSummaryResponse> page1 = new PageImpl<>(List.of(usEmployee), org.springframework.data.domain.PageRequest.of(0, 500), 2);
            Page<EmployeeSummaryResponse> page2 = new PageImpl<>(List.of(p2), org.springframework.data.domain.PageRequest.of(1, 500), 2);

            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any()))
                    .thenReturn(page1).thenReturn(page2);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains("EMP-001"));
            assertTrue(csv.contains("EMP-004"));
        }

        @Test
        @DisplayName("should include currency column when salary present")
        void shouldIncludeCurrencyColumn() throws Exception {
            List<EmployeeSummaryResponse> page1 = List.of(usEmployee);
            Page<EmployeeSummaryResponse> page = new PageImpl<>(page1);
            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains(",120000.00,USD,"));
        }

        @Test
        @DisplayName("should use USD as default currency when no salary")
        void shouldUseDefaultCurrency() throws Exception {
            EmployeeSummaryResponse noSalary = new EmployeeSummaryResponse(
                    5L, "EMP-005", "New", "Hire",
                    "JP", "Engineering", "SWE I", "PENDING", null, null);
            List<EmployeeSummaryResponse> page1 = List.of(noSalary);
            Page<EmployeeSummaryResponse> page = new PageImpl<>(page1);
            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any())).thenReturn(page);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reportService.exportEmployeesCsv(output, null, null, null, null, false);

            String csv = output.toString("UTF-8");
            assertTrue(csv.contains(",USD,"));
        }
    }
}
