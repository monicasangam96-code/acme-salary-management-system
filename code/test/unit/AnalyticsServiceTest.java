package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.AnalyticsSummaryResponse;
import com.acme.salarymgmt.dto.CurrencyItem;
import com.acme.salarymgmt.model.Currency;
import com.acme.salarymgmt.repository.CurrencyRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private List<SalaryRepository.AnalyticsSummaryResponse> mockResults;
    private List<Currency> activeCurrencies;

    @BeforeEach
    void setUp() {
        mockResults = List.of(new SalaryRepository.AnalyticsSummaryResponse(
                new BigDecimal("5000000"), new BigDecimal("85000"), 42L
        ));

        activeCurrencies = List.of(
                new Currency("USD", "US Dollar", "$"),
                new Currency("EUR", "Euro", "€"),
                new Currency("GBP", "British Pound Sterling", "£")
        );
    }

    // ---- getSummary ----
    @Nested
    @DisplayName("getSummary")
    class GetSummaryTests {

        @Test
        @DisplayName("should return aggregates for all employees when no filters")
        void shouldReturnAllAggregates() {
            when(salaryRepository.computeSummary(null, null, null)).thenReturn(mockResults);

            AnalyticsSummaryResponse result = analyticsService.getSummary(null, null, null);

            assertNotNull(result);
            assertEquals(new BigDecimal("5000000"), result.totalPayroll());
            assertEquals(42L, result.employeeCount());
        }

        @Test
        @DisplayName("should filter summary by country")
        void shouldFilterByCountry() {
            when(salaryRepository.computeSummary(eq(null), eq("US"), eq(null))).thenReturn(List.of(new SalaryRepository.AnalyticsSummaryResponse(
                    new BigDecimal("2000000"), new BigDecimal("95000"), 15L
            )));

            AnalyticsSummaryResponse result = analyticsService.getSummary("US", null, null);

            assertEquals(15L, result.employeeCount());
        }

        @Test
        @DisplayName("should filter summary by department")
        void shouldFilterByDepartment() {
            when(salaryRepository.computeSummary(eq(null), eq(null), eq("Engineering"))).thenReturn(List.of(new SalaryRepository.AnalyticsSummaryResponse(
                    new BigDecimal("3000000"), new BigDecimal("110000"), 25L
            )));

            AnalyticsSummaryResponse result = analyticsService.getSummary(null, "Engineering", null);

            assertEquals(25L, result.employeeCount());
        }

        @Test
        @DisplayName("should return zeros when no data matches")
        void shouldReturnZerosWhenEmpty() {
            when(salaryRepository.computeSummary(any(), any(), any())).thenReturn(List.of());

            AnalyticsSummaryResponse result = analyticsService.getSummary("US", "Engineering", null);

            assertEquals(BigDecimal.ZERO, result.totalPayroll());
            assertEquals(BigDecimal.ZERO, result.averageSalary());
            assertEquals(0L, result.employeeCount());
        }

        @Test
        @DisplayName("should filter by currency code")
        void shouldFilterByCurrency() {
            when(salaryRepository.computeSummary(eq("USD"), any(), any())).thenReturn(mockResults);

            analyticsService.getSummary(null, null, "USD");

            verify(salaryRepository).computeSummary("USD", null, null);
        }
    }

    // ---- getByCountry ----
    @Nested
    @DisplayName("getByCountry")
    class GetByCountryTests {

        @Test
        @DisplayName("should return country breakdown list")
        void shouldReturnBreakdown() {
            var us = new SalaryRepository.CountryBreakdownResponse();
            us.setCountryName("US"); us.setTotalPayroll(new BigDecimal("2000000")); us.setEmployeeCount(35L);

            when(salaryRepository.getAnalyticsByCountry(null)).thenReturn(List.of(us));

            var result = analyticsService.getByCountry(null);

            assertEquals(1, result.size());
            assertEquals("US", result.get(0).getCountryName());
        }
    }

    // ---- getByDepartment ----
    @Nested
    @DisplayName("getByDepartment")
    class GetByDepartmentTests {

        @Test
        @DisplayName("should return department breakdown list")
        void shouldReturnBreakdown() {
            var eng = new SalaryRepository.DepartmentBreakdownResponse();
            eng.setDept("Engineering"); eng.setTotalPayroll(new BigDecimal("3000000")); eng.setEmployeeCount(40L);

            when(salaryRepository.getAnalyticsByDepartment(eq(null), eq(null))).thenReturn(List.of(eng));

            var result = analyticsService.getByDepartment(null, null);

            assertEquals(1, result.size());
            assertEquals("Engineering", result.get(0).getDept());
        }
    }

    // ---- getByJobLevel ----
    @Nested
    @DisplayName("getByJobLevel")
    class GetByJobLevelTests {

        @Test
        @DisplayName("should return job level breakdown list")
        void shouldReturnBreakdown() {
            var l2 = new SalaryRepository.JobLevelBreakdownResponse();
            l2.setJobLevel("L2"); l2.setAvgSalary(new BigDecimal("85000")); l2.setEmployeeCount(12L);

            when(salaryRepository.getAnalyticsByJobLevel(eq(null), eq(null), eq(null))).thenReturn(List.of(l2));

            var result = analyticsService.getByJobLevel(null, null, null);

            assertEquals(1, result.size());
        }
    }

    // ---- getCurrencyBreakdown ----
    @Nested
    @DisplayName("getCurrencyBreakdown")
    class GetCurrencyBreakdownTests {

        @Test
        @DisplayName("should return all active currencies with symbols")
        void shouldReturnCurrencies() {
            when(currencyRepository.findAllByIsActiveTrue()).thenReturn(activeCurrencies);

            List<CurrencyItem> result = analyticsService.getCurrencyBreakdown();

            assertEquals(3, result.size());
            assertTrue(result.stream().anyMatch(c -> "USD".equals(c.code()) && "$".equals(c.symbol())));
            assertTrue(result.stream().anyMatch(c -> "EUR".equals(c.code()) && "€".equals(c.symbol())));
        }
    }
}
