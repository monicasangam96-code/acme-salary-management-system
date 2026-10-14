package com.acme.salarymgmt.service.unit;

import com.acme.salarymgmt.dto.CurrencyItem;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private com.acme.salarymgmt.service.AnalyticsService analyticsService;

    // --- getSummary ---
    @Nested
    @DisplayName("getSummary")
    class GetSummaryTests {

        @Test
        @DisplayName("should return aggregates when data exists")
        void shouldReturnAggregates() {
            var mockResult = new com.acme.salarymgmt.dto.response.AnalyticsSummaryResponse(
                    new BigDecimal("5000000"), new BigDecimal("85000"), new BigDecimal("82000"),
                    new BigDecimal("45000"), new BigDecimal("150000"), 42L);
            when(salaryRepository.computeSummary(null, null, null)).thenReturn(List.of(mockResult));

            var result = analyticsService.getSummary(null, null, null);

            assertNotNull(result);
            assertEquals(new BigDecimal("5000000"), result.getTotalPayroll());
            assertEquals(new BigDecimal("85000"), result.getAverageSalary());
            assertEquals(42L, result.getEmployeeCount());
        }

        @Test
        @DisplayName("should filter by country")
        void shouldFilterByCountry() {
            var mockResult = new com.acme.salarymgmt.dto.response.AnalyticsSummaryResponse(
                    new BigDecimal("2000000"), new BigDecimal("95000"), new BigDecimal("93000"),
                    new BigDecimal("70000"), new BigDecimal("140000"), 15L);
            when(salaryRepository.computeSummary(null, "US", null)).thenReturn(List.of(mockResult));

            var result = analyticsService.getSummary("US", null, null);

            assertEquals(15L, result.getEmployeeCount());
        }

        @Test
        @DisplayName("should filter by department")
        void shouldFilterByDepartment() {
            when(salaryRepository.computeSummary(null, null, "Engineering")).thenReturn(List.of(
                    new com.acme.salarymgmt.dto.response.AnalyticsSummaryResponse(
                            new BigDecimal("3000000"), new BigDecimal("110000"), new BigDecimal("108000"),
                            new BigDecimal("85000"), new BigDecimal("150000"), 25L)));

            var result = analyticsService.getSummary(null, "Engineering", null);

            assertEquals(25L, result.getEmployeeCount());
        }

        @Test
        @DisplayName("should return zeros when no data matches")
        void shouldReturnZerosWhenEmpty() {
            when(salaryRepository.computeSummary(any(), any(), any()))
                    .thenReturn(List.of());

            var result = analyticsService.getSummary("US", "Engineering", null);

            assertEquals(BigDecimal.ZERO, result.getTotalPayroll());
            assertEquals(BigDecimal.ZERO, result.getAverageSalary());
            assertEquals(0L, result.getEmployeeCount());
        }

        @Test
        @DisplayName("should filter by currency code")
        void shouldFilterByCurrency() {
            when(salaryRepository.computeSummary("USD", null, null)).thenReturn(List.of());

            analyticsService.getSummary(null, null, "USD");

            verify(salaryRepository).computeSummary("USD", null, null);
        }
    }

    // --- getByCountry ---
    @Nested
    @DisplayName("getByCountry")
    class GetByCountryTests {

        @Test
        @DisplayName("should return country breakdown list")
        void shouldReturnBreakdown() {
            var us = new com.acme.salarymgmt.dto.response.CountryBreakdownResponse("US", 35L, new BigDecimal("2000000"));
            when(salaryRepository.getAnalyticsByCountry(null)).thenReturn(List.of(us));

            var result = analyticsService.getByCountry(null);

            assertEquals(1, result.size());
            assertEquals("US", result.get(0).getCountry());
        }
    }

    // --- getByDepartment ---
    @Nested
    @DisplayName("getByDepartment")
    class GetByDepartmentTests {

        @Test
        @DisplayName("should return department breakdown list")
        void shouldReturnBreakdown() {
            var eng = new com.acme.salarymgmt.dto.response.DepartmentBreakdownResponse("Engineering", 40L, new BigDecimal("3000000"));
            when(salaryRepository.getAnalyticsByDepartment(null, null)).thenReturn(List.of(eng));

            var result = analyticsService.getByDepartment(null, null);

            assertEquals(1, result.size());
        }
    }

    // --- getByJobLevel ---
    @Nested
    @DisplayName("getByJobLevel")
    class GetByJobLevelTests {

        @Test
        @DisplayName("should return job level breakdown list")
        void shouldReturnBreakdown() {
            var l2 = new com.acme.salarymgmt.dto.response.JobLevelBreakdownResponse(
                    "L2", 12L, new BigDecimal("85000"), new BigDecimal("70000"), new BigDecimal("100000"));
            when(salaryRepository.getAnalyticsByJobLevel(null, null, null)).thenReturn(List.of(l2));

            var result = analyticsService.getByJobLevel(null, null, null);

            assertEquals(1, result.size());
        }
    }

    // --- getCurrencyBreakdown ---
    @Nested
    @DisplayName("getCurrencyBreakdown")
    class GetCurrencyBreakdownTests {

        @Test
        @DisplayName("should return active currencies with codes and symbols")
        void shouldReturnCurrencies() {
            var usd = new com.acme.salarymgmt.model.Currency("USD", "US Dollar", "$", "US Dollar", true);
            var eur = new com.acme.salarymgmt.model.Currency("EUR", "Euro", "€", "Euro", true);
            when(currencyRepository.findAllByIsActiveTrue()).thenReturn(List.of(usd, eur));

            var result = analyticsService.getCurrencyBreakdown();

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(c -> "USD".equals(c.getCode()) && "$".equals(c.getSymbol())));
            assertTrue(result.stream().anyMatch(c -> "EUR".equals(c.getCode()) && "€".equals(c.getSymbol())));
        }
    }
}
