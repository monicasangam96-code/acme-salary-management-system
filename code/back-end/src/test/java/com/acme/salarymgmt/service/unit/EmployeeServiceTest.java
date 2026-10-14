package com.acme.salarymgmt.service.unit;

import com.acme.salarymgmt.dto.CreateEmployeeRequest;
import com.acme.salarymgmt.dto.EmployeeDetailResponse;
import com.acme.salarymgmt.dto.EmployeeSummaryResponse;
import com.acme.salarymgmt.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private com.acme.salarymgmt.service.Validator validator;

    @Mock
    private com.acme.salarymgmt.service.Auditor auditor;

    @InjectMocks
    private com.acme.salarymgmt.service.EmployeeService employeeService;

    private Employee sampleEmployee;

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
        sampleEmployee.setUpdatedAt(java.time.LocalDateTime.now());
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

            // Mock repository — returns Employee entities, not DTOs
            var empPage = new PageImpl<Employee>(List.of(sampleEmployee));
            when(employeeRepository.searchWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(empPage);
            when(salaryRepository.findCurrentByEmployeeId(1L)).thenReturn(Optional.of(currentSalary));

            Page<EmployeeSummaryResponse> result = employeeService.listActive(null, null, null, null, Pageable.ofSize(25));

            assertEquals(1, result.getTotalElements());
            assertNotNull(result.getContent().get(0).getCurrentSalaryAmount());
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
            assertEquals(new BigDecimal("95000"), detail.getCurrentSalaryAmount());
            assertEquals("GBP", detail.getCurrentCurrencyCode());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for inactive employee")
        void getById_shouldThrowForInactive() {
            when(employeeRepository.findActiveById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(99L));
        }
    }

    // ---- create ----
    @Nested
    @DisplayName("create")
    class CreateTests {

        private CreateEmployeeRequest makeValidRequest() {
            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setEmpNo("EMP-999");
            req.setFirstName("Jane");
            req.setLastName("Smith");
            req.setEmail("jane.smith@acme.com");
            req.setPhone("+44-20-1234");
            req.setAddressLine1("10 Downing St");
            req.setCity("London");
            req.setStateProvince("England");
            req.setZipCode("SW1A 2AA");
            req.setCountry("United Kingdom (UK)");
            req.setDept("Engineering");
            req.setJobTitle("Senior Engineer");
            req.setEmploymentStatus("ACTIVE");
            req.setStartDate(LocalDate.of(2024, 6, 1));
            req.setEndDate(null);
            return req;
        }

        @Test
        @DisplayName("should create employee with audit log")
        void create_shouldPersistAndAudit() {
            CreateEmployeeRequest req = makeValidRequest();
            when(employeeRepository.existsByEmpNo("EMP-999")).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
                Employee e = inv.getArgument(0);
                e.setId(2L);
                return e;
            });

            EmployeeSummaryResponse result = employeeService.create(req);

            assertNotNull(result);
            assertEquals("EMP-999", result.getEmpNo());
            verify(auditor).logInsert(eq(2L), eq("employees"), anyLong(), eq("all_fields"), anyString(), anyLong());
        }

        @Test
        @DisplayName("should reject duplicate employee number")
        void create_shouldRejectDuplicateEmpNo() {
            CreateEmployeeRequest req = makeValidRequest();
            when(employeeRepository.existsByEmpNo("EMP-999")).thenReturn(true);

            assertThrows(ValidationException.class, () -> employeeService.create(req));
        }

        @Test
        @DisplayName("should reject invalid email")
        void create_shouldRejectInvalidEmail() {
            CreateEmployeeRequest req = makeValidRequest();
            req.setEmail("not-an-email");
            doThrow(new ValidationException("Invalid email")).when(validator).validateEmail("not-an-email");

            assertThrows(ValidationException.class, () -> employeeService.create(req));
        }
    }

    // ---- update ----
    @Nested
    @DisplayName("update")
    class UpdateTests {

        private CreateEmployeeRequest makeUpdatedRequest() {
            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setEmpNo(sampleEmployee.getEmpNo());
            req.setFirstName("Jonathan");
            req.setLastName("Doe");
            req.setEmail("jonathan.doe@acme.com");
            req.setPhone(sampleEmployee.getPhone());
            req.setAddressLine1(sampleEmployee.getAddressLine1() != null ? sampleEmployee.getAddressLine1() : "");
            req.setCity("Boston");
            req.setStateProvince(sampleEmployee.getStateProvince() != null ? sampleEmployee.getStateProvince() : "");
            req.setZipCode(sampleEmployee.getZipCode());
            req.setCountry("United States (US)");
            req.setDept("Sales");  // Changed from Engineering
            req.setJobTitle("Engineer III");
            req.setEmploymentStatus("ACTIVE");
            req.setStartDate(sampleEmployee.getStartDate());
            req.setEndDate(null);
            return req;
        }

        @Test
        @DisplayName("should update employee and log field changes")
        void update_shouldUpdateAndLog() {
            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            EmployeeDetailResponse result = employeeService.update(1L, makeUpdatedRequest());

            assertNotNull(result);
            assertEquals("Jonathan", result.getFirstName());
            verify(auditor, atLeastOnce()).logUpdate(eq(1L), eq("employees"), anyLong(), anyString(), anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("should not log audit when nothing changed")
        void update_shouldNotAuditWhenUnchanged() {
            CreateEmployeeRequest noChange = makeUpdatedRequest();
            noChange.setFirstName("John");
            noChange.setDept("Engineering");
            sampleEmployee.setAddressLine1("");
            sampleEmployee.setStateProvince(null);

            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            employeeService.update(1L, noChange);

            verify(auditor, never()).logUpdate(eq(1L), eq("employees"), anyLong(), anyString(), anyString(), anyString(), anyLong());
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
