package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.CreateEmployeeRequest;
import com.acme.salarymgmt.dto.EmployeeDetailResponse;
import com.acme.salarymgmt.dto.EmployeeSummaryResponse;
import com.acme.salarymgmt.exception.ValidationException;
import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.model.Salary;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private Validator validator;

    @Mock
    private Auditor auditor;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee sampleEmployee;
    private CreateEmployeeRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee();
        sampleEmployee.setId(1L);
        sampleEmployee.setEmpNo("EMP-001");
        sampleEmployee.setFirstName("John");
        sampleEmployee.setLastName("Doe");
        sampleEmployee.setEmail("john.doe@acme.com");
        sampleEmployee.setPhone("+1-555-1234");
        sampleEmployee.setCity("New York");
        sampleEmployee.setCountry("United States (US)");
        sampleEmployee.setDept("Engineering");
        sampleEmployee.setJobTitle("Software Engineer II");
        sampleEmployee.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        sampleEmployee.setStartDate(LocalDate.of(2023, 1, 15));
        sampleEmployee.setActive(true);

        createRequest = new CreateEmployeeRequest() {
            @Override public String getEmpNo() { return "EMP-999"; }
            @Override public String getFirstName() { return "Jane"; }
            @Override public String getLastName() { return "Smith"; }
            @Override public String getEmail() { return "jane.smith@acme.com"; }
            @Override public String getPhone() { return "+44-20-1234"; }
            @Override public String getAddressLine1() { return "10 Downing St"; }
            @Override public String getCity() { return "London"; }
            @Override public String getStateProvince() { return "England"; }
            @Override public String getZipCode() { return "SW1A 2AA"; }
            @Override public String getCountry() { return "United Kingdom (UK)"; }
            @Override public String getDept() { return "Engineering"; }
            @Override public String getJobTitle() { return "Senior Engineer"; }
            @Override public String getEmploymentStatus() { return "ACTIVE"; }
            @Override public LocalDate getStartDate() { return LocalDate.of(2024, 6, 1); }
            @Override public LocalDate getEndDate() { return null; }
        };
    }

    // ---- listActive ----
    @Nested
    @DisplayName("listActive")
    class ListActiveTests {

        @Test
        @DisplayName("should return paginated active employees with current salary")
        void listActive_shouldIncludeSalary() {
            Salary currentSalary = new Salary();
            currentSalary.setSalaryAmount(new BigDecimal("120000"));
            currentSalary.setCurrencyCode("USD");

            Employee empForPage = new Employee();
            empForPage.setId(1L);
            empForPage.setEmpNo("EMP-001");
            empForPage.setFirstName("John");
            empForPage.setLastName("Doe");
            empForPage.setCity("New York");
            empForPage.setCountry("United States (US)");
            empForPage.setDept("Engineering");
            empForPage.setJobTitle("Software Engineer II");
            empForPage.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);

            Page<Employee> page = new PageImpl<>(List.of(empForPage));
            when(employeeRepository.searchWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);
            when(salaryRepository.findCurrentByEmployeeId(1L)).thenReturn(Optional.of(currentSalary));

            Page<EmployeeSummaryResponse> result = employeeService.listActive(null, null, null, null, Pageable.ofSize(25));

            assertEquals(1, result.getTotalElements());
            assertEquals(new BigDecimal("120000"), result.getContent().get(0).getCurrentSalaryAmount());
            verify(salaryRepository).findCurrentByEmployeeId(1L);
        }

        @Test
        @DisplayName("should filter by country and department")
        void listActive_shouldFilterByCountryAndDept() {
            when(employeeRepository.searchWithFilters(eq("UK"), eq("Engineering"), any(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            employeeService.listActive(null, "UK", "Engineering", null, Pageable.ofSize(10));

            verify(employeeRepository).searchWithFilters(eq("UK"), eq("Engineering"), isNull(), isNull(), any(Pageable.class));
        }
    }

    // ---- getById ----
    @Nested
    @DisplayName("getById")
    class GetByIdTests {

        @Test
        @DisplayName("should return full detail including current salary")
        void getById_shouldReturnDetailWithSalary() {
            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            Salary salary = new Salary();
            salary.setSalaryAmount(new BigDecimal("95000"));
            salary.setCurrencyCode("GBP");
            when(salaryRepository.findCurrentByEmployeeId(1L)).thenReturn(Optional.of(salary));

            EmployeeDetailResponse detail = employeeService.getById(1L);

            assertNotNull(detail);
            assertEquals("John", detail.getFirstName());
            assertEquals("95000", detail.getCurrentSalaryAmount().toPlainString());
            assertEquals("GBP", detail.getCurrentCurrencyCode());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for inactive employee")
        void getById_shouldThrowForInactive() {
            when(employeeRepository.findActiveById(99L)).thenReturn(Optional.empty());

            assertThrows(com.acme.salarymgmt.exception.ResourceNotFoundException.class, () -> employeeService.getById(99L));
        }
    }

    // ---- create ----
    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create employee with audit log")
        void create_shouldPersistAndAudit() {
            doNothing().when(validator).validateRequired(anyString(), eq("emp_no"));
            doNothing().when(validator).validateEmail(anyString());
            when(employeeRepository.existsByEmpNo("EMP-999")).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
                Employee e = inv.getArgument(0);
                e.setId(2L);
                return e;
            });

            EmployeeSummaryResponse result = employeeService.create(createRequest);

            assertNotNull(result);
            assertEquals("EMP-999", result.getEmpNo());
            verify(auditor).logInsert(eq(2L), eq("employees"), anyLong(), eq("all_fields"), anyString(), anyLong());
        }

        @Test
        @DisplayName("should reject duplicate employee number")
        void create_shouldRejectDuplicateEmpNo() {
            doNothing().when(validator).validateRequired(anyString(), eq("emp_no"));
            when(employeeRepository.existsByEmpNo("EMP-999")).thenReturn(true);

            assertThrows(ValidationException.class, () -> employeeService.create(createRequest));
        }

        @Test
        @DisplayName("should reject blank required fields")
        void create_shouldRejectBlankRequiredFields() {
            CreateEmployeeRequest badRequest = new CreateEmployeeRequest() {
                @Override public String getEmpNo() { return ""; }
                @Override public String getFirstName() { return "Jane"; }
                @Override public String getLastName() { return "Smith"; }
                @Override public String getEmail() { return "jane@acme.com"; }
                @Override public String getPhone() { return null; }
                @Override public String getAddressLine1() { return "10 St"; }
                @Override public String getCity() { return "London"; }
                @Override public String getStateProvince() { return null; }
                @Override public String getZipCode() { return "SW1A"; }
                @Override public String getCountry() { return "UK"; }
                @Override public String getDept() { return "HR"; }
                @Override public String getJobTitle() { return "Analyst"; }
                @Override public String getEmploymentStatus() { return "ACTIVE"; }
                @Override public LocalDate getStartDate() { return LocalDate.of(2024, 1, 1); }
                @Override public LocalDate getEndDate() { return null; }
            };

            doThrow(new ValidationException("Missing required field", List.of("emp_no: cannot be blank or empty")))
                    .when(validator).validateRequired(eq(""), eq("emp_no"));

            assertThrows(ValidationException.class, () -> employeeService.create(badRequest));
        }

        @Test
        @DisplayName("should reject invalid email")
        void create_shouldRejectInvalidEmail() {
            CreateEmployeeRequest badEmail = new CreateEmployeeRequest() {
                @Override public String getEmpNo() { return "EMP-100"; }
                @Override public String getFirstName() { return "John"; }
                @Override public String getLastName() { return "Doe"; }
                @Override public String getEmail() { return "not-an-email"; }
                @Override public String getPhone() { return null; }
                @Override public String getAddressLine1() { return "1 St"; }
                @Override public String getCity() { return "NYC"; }
                @Override public String getStateProvince() { return null; }
                @Override public String getZipCode() { return "10001"; }
                @Override public String getCountry() { return "US"; }
                @Override public String getDept() { return "Sales"; }
                @Override public String getJobTitle() { return "Rep"; }
                @Override public String getEmploymentStatus() { return "ACTIVE"; }
                @Override public LocalDate getStartDate() { return LocalDate.of(2024, 1, 1); }
                @Override public LocalDate getEndDate() { return null; }
            };

            doThrow(new ValidationException("Invalid email"))
                    .when(validator)
                    .validateEmail(eq("not-an-email"));

            assertThrows(ValidationException.class, () -> employeeService.create(badEmail));
        }
    }

    // ---- update ----
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("should update employee and log field changes")
        void update_shouldUpdateAndLog() {
            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateEmployeeRequest updateReq = new CreateEmployeeRequest() {
                @Override public String getEmpNo() { return sampleEmployee.getEmpNo(); }
                @Override public String getFirstName() { return "Jonathan"; }
                @Override public String getLastName() { return "Doe"; }
                @Override public String getEmail() { return "jonathan.doe@acme.com"; }
                @Override public String getPhone() { return sampleEmployee.getPhone(); }
                @Override public String getAddressLine1() { return sampleEmployee.getAddressLine1() != null ? sampleEmployee.getAddressLine1() : ""; }
                @Override public String getCity() { return "Boston"; }
                @Override public String getStateProvince() { return sampleEmployee.getStateProvince() != null ? sampleEmployee.getStateProvince() : ""; }
                @Override public String getZipCode() { return sampleEmployee.getZipCode() != null ? sampleEmployee.getZipCode() : ""; }
                @Override public String getCountry() { return "United States (US)"; }
                @Override public String getDept() { return "Sales"; }
                @Override public String getJobTitle() { return "Engineer III"; }
                @Override public String getEmploymentStatus() { return sampleEmployee.getEmploymentStatus().toString(); }
                @Override public LocalDate getStartDate() { return sampleEmployee.getStartDate() != null ? sampleEmployee.getStartDate() : LocalDate.now(); }
                @Override public LocalDate getEndDate() { return null; }
            };

            EmployeeDetailResponse result = employeeService.update(1L, updateReq);

            assertNotNull(result);
            assertEquals("Jonathan", result.getFirstName());
            verify(auditor, atLeastOnce()).logUpdate(eq(1L), eq("employees"), anyLong(), anyString(), anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("should not log audit when nothing changed")
        void update_shouldNotAuditWhenUnchanged() {
            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateEmployeeRequest noChange = new CreateEmployeeRequest() {
                @Override public String getEmpNo() { return sampleEmployee.getEmpNo(); }
                @Override public String getFirstName() { return "John"; }
                @Override public String getLastName() { return "Doe"; }
                @Override public String getEmail() { return "john.doe@acme.com"; }
                @Override public String getPhone() { return sampleEmployee.getPhone(); }
                @Override public String getAddressLine1() { return sampleEmployee.getAddressLine1() != null ? sampleEmployee.getAddressLine1() : ""; }
                @Override public String getCity() { return sampleEmployee.getCity() != null ? sampleEmployee.getCity() : ""; }
                @Override public String getStateProvince() { return sampleEmployee.getStateProvince() != null ? sampleEmployee.getStateProvince() : ""; }
                @Override public String getZipCode() { return sampleEmployee.getZipCode(); }
                @Override public String getCountry() { return sampleEmployee.getCountry(); }
                @Override public String getDept() { return sampleEmployee.getDept(); }
                @Override public String getJobTitle() { return sampleEmployee.getJobTitle(); }
                @Override public String getEmploymentStatus() { return "ACTIVE"; }
                @Override public LocalDate getStartDate() { return sampleEmployee.getStartDate(); }
                @Override public LocalDate getEndDate() { return null; }
            };

            employeeService.update(1L, noChange);
            verify(auditor, never()).logUpdate(anyLong(), eq("employees"), anyLong(), anyString(), anyString(), anyString(), anyLong());
        }
    }

    // ---- toggleStatus ----
    @Nested
    @DisplayName("toggleStatus")
    class ToggleStatusTests {

        @Test
        @DisplayName("should terminate employee and set end date")
        void toggleStatus_shouldSetTerminated() {
            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            employeeService.toggleStatus(1L, "TERMINATED");

            assertEquals(Employee.EmploymentStatus.TERMINATED, sampleEmployee.getEmploymentStatus());
            assertNotNull(sampleEmployee.getEndDate());
            verify(auditor).logUpdate(eq(1L), eq("employees"), anyLong(), eq("status"), eq("ACTIVE"), eq("TERMINATED"), anyLong());
        }
    }

    // ---- softDelete ----
    @Nested
    @DisplayName("softDelete")
    class SoftDeleteTests {

        @Test
        @DisplayName("should deactivate employee and log delete")
        void softDelete_shouldSetInactive() {
            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            employeeService.softDelete(1L);

            assertFalse(sampleEmployee.getActive());
            assertEquals(Employee.EmploymentStatus.TERMINATED, sampleEmployee.getEmploymentStatus());
            verify(auditor).logDelete(eq(1L), eq("employees"), anyLong(), anyLong());
        }
    }

    // ---- getSalaryHistory ----
    @Nested
    @DisplayName("getSalaryHistory")
    class GetSalaryHistoryTests {

        @Test
        @DisplayName("should return salary records ordered by date desc")
        void getSalaryHistory_shouldReturnDescending() {
            Salary s1 = new Salary(); s1.setEffectiveDate(LocalDate.of(2024, 6, 1));
            Salary s2 = new Salary(); s2.setEffectiveDate(LocalDate.of(2023, 1, 1));
            when(salaryRepository.findByEmployeeIdOrderByEffectiveDateDesc(1L)).thenReturn(List.of(s1, s2));

            var result = employeeService.getSalaryHistory(1L);

            assertEquals(2, result.size());
            assertTrue(result.get(0).getEffectiveDate().isAfter(result.get(1).getEffectiveDate()));
        }
    }
}
