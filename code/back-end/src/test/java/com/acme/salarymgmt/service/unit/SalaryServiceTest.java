package com.acme.salarymgmt.service.unit;

import com.acme.salarymgmt.exception.ResourceNotFoundException;
import com.acme.salarymgmt.exception.ValidationException;
import com.acme.salarymgmt.model.Currency;
import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.model.Salary;
import com.acme.salarymgmt.repository.CurrencyRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private com.acme.salarymgmt.service.Validator validator;

    @Mock
    private com.acme.salarymgmt.service.Auditor auditor;

    @InjectMocks
    private com.acme.salarymgmt.service.SalaryService salaryService;

    private Employee sampleEmployee;
    private Salary existingSalary;
    private List<Currency> activeCurrencies;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee();
        sampleEmployee.setId(1L);
        sampleEmployee.setEmpNo("EMP-001");
        sampleEmployee.setFirstName("John");
        sampleEmployee.setLastName("Doe");
        sampleEmployee.setActive(true);

        existingSalary = new Salary();
        existingSalary.setId(10L);
        existingSalary.setEmployeeId(1L);
        existingSalary.setSalaryAmount(new BigDecimal("95000"));
        existingSalary.setCurrencyCode("USD");
        existingSalary.setStatus(Salary.SalaryStatus.CURRENT);
        existingSalary.setPreviousSalaryAmount(new BigDecimal("85000")); // not null for service toString

        activeCurrencies = List.of(
                new Currency("USD", "US Dollar", "$", "US Dollar", true),
                new Currency("EUR", "Euro", "€", "Euro", true),
                new Currency("GBP", "British Pound Sterling", "£", "UK Pound", true)
        );
    }

    // ---- getSalaryHistory ----
    @Nested
    @DisplayName("getSalaryHistory")
    class GetSalaryHistoryTests {

        @Test
        @DisplayName("should return salary history for employee")
        void shouldReturnHistory() {
            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            Salary s = new Salary(); s.setSalaryAmount(new BigDecimal("95000"));
            s.setCurrencyCode("USD"); s.setEffectiveDate(LocalDate.of(2024, 1, 1));
            s.setStatus(Salary.SalaryStatus.CURRENT);
            when(salaryRepository.findByEmployeeIdOrderByEffectiveDateDesc(1L)).thenReturn(List.of(s));

            var result = salaryService.getSalaryHistory(1L);

            assertEquals(1, result.size());
            assertEquals(new BigDecimal("95000"), result.get(0).getSalaryAmount());
        }

        @Test
        @DisplayName("should throw when employee not found")
        void shouldThrowWhenEmployeeNotFound() {
            when(employeeRepository.findActiveById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> salaryService.getSalaryHistory(99L));
        }
    }

    // ---- getSalaryById ----
    @Nested
    @DisplayName("getSalaryById")
    class GetSalaryByIdTests {

        @Test
        @DisplayName("should return salary detail by ID")
        void shouldReturnById() {
            Salary salary = new Salary();
            salary.setId(10L); salary.setEmployeeId(1L);
            salary.setSalaryAmount(new BigDecimal("95000")); salary.setCurrencyCode("USD");
            salary.setStatus(Salary.SalaryStatus.CURRENT);
            when(salaryRepository.findById(10L)).thenReturn(Optional.of(salary));

            var result = salaryService.getSalaryById(10L);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals(new BigDecimal("95000"), result.getSalaryAmount());
        }

        @Test
        @DisplayName("should throw when salary not found")
        void shouldThrowWhenNotFound() {
            when(salaryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> salaryService.getSalaryById(99L));
        }
    }

    // ---- updateSalary ----
    @Nested
    @DisplayName("updateSalary")
    class UpdateSalaryTests {

        private com.acme.salarymgmt.service.SalaryService.SalaryUpdateRequest makeValidRequest() {
            return new com.acme.salarymgmt.service.SalaryService.SalaryUpdateRequest(1L, new BigDecimal("120000"), "USD", LocalDate.of(2025, 7, 1), "Annual review");
        }

        @Test
        @DisplayName("should create new CURRENT and supersede old when updating salary")
        void shouldSupersedeOldAndCreateNew() {
            var request = makeValidRequest();
            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(currencyRepository.findAllByIsActiveTrue()).thenReturn(activeCurrencies);
            when(salaryRepository.findCurrentByEmployeeId(1L)).thenReturn(Optional.of(existingSalary));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = salaryService.updateSalary(request);

            assertNotNull(result);
            assertEquals(new BigDecimal("120000"), result.getSalaryAmount());
            assertEquals("CURRENT", result.getStatus());
            assertEquals(existingSalary.getSalaryAmount(), result.getPreviousSalaryAmount());
        }

        @Test
        @DisplayName("should handle first salary entry (no previous CURRENT)")
        void shouldHandleFirstSalary() {
            var request = new com.acme.salarymgmt.service.SalaryService.SalaryUpdateRequest(1L, new BigDecimal("85000"), "GBP", LocalDate.of(2025, 1, 1), "Starting salary");

            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(currencyRepository.findAllByIsActiveTrue()).thenReturn(activeCurrencies);
            when(salaryRepository.findCurrentByEmployeeId(1L)).thenReturn(Optional.empty());
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> {
                Salary s = inv.getArgument(0);
                s.setId(20L);
                return s;
            });

            var result = salaryService.updateSalary(request);

            assertNotNull(result);
            assertNull(result.getPreviousSalaryAmount());
        }

        @Test
        @DisplayName("should reject invalid currency code")
        void shouldRejectInvalidCurrency() {
            var request = new com.acme.salarymgmt.service.SalaryService.SalaryUpdateRequest(1L, new BigDecimal("100000"), "XYZ", LocalDate.of(2025, 1, 1), null);

            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            // Force validation failure by making validateCurrencyCode throw
            doThrow(new ValidationException("Invalid currency code: XYZ"))
                    .when(validator)
                    .validateCurrencyCode(eq("XYZ"), any());

            assertThrows(ValidationException.class, () -> salaryService.updateSalary(request));
        }

        @Test
        @DisplayName("should reject zero or negative salary")
        void shouldRejectInvalidSalary() {
            var request = new com.acme.salarymgmt.service.SalaryService.SalaryUpdateRequest(1L, BigDecimal.ZERO, "USD", LocalDate.of(2025, 1, 1), null);

            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            doThrow(new ValidationException("Invalid salary amount")).when(validator).validateSalary(BigDecimal.ZERO, "salary_amount");

            assertThrows(ValidationException.class, () -> salaryService.updateSalary(request));
        }

        @Test
        @DisplayName("should reject inactive employee")
        void shouldRejectInactiveEmployee() {
            var request = new com.acme.salarymgmt.service.SalaryService.SalaryUpdateRequest(99L, new BigDecimal("100000"), "USD", LocalDate.of(2025, 1, 1), null);

            when(employeeRepository.findActiveById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> salaryService.updateSalary(request));
        }

        @Test
        @DisplayName("should set currency from request")
        void shouldSetCurrencyFromRequest() {
            var request = new com.acme.salarymgmt.service.SalaryService.SalaryUpdateRequest(1L, new BigDecimal("110000"), "EUR", LocalDate.of(2025, 7, 1), "Promotion");

            when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(currencyRepository.findAllByIsActiveTrue()).thenReturn(activeCurrencies);
            when(salaryRepository.findCurrentByEmployeeId(1L)).thenReturn(Optional.empty());
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> {
                Salary s = inv.getArgument(0);
                s.setId(21L);
                return s;
            });

            var result = salaryService.updateSalary(request);

            assertEquals("EUR", result.getCurrencyCode());
        }
    }
}
