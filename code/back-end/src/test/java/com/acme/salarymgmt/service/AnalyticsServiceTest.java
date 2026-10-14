package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.CurrencyItem;
import com.acme.salarymgmt.model.Currency;
import com.acme.salarymgmt.dto.response.AnalyticsSummaryResponse;
import com.acme.salarymgmt.dto.response.CountryBreakdownResponse;
import com.acme.salarymgmt.dto.response.DepartmentBreakdownResponse;
import com.acme.salarymgmt.dto.response.JobLevelBreakdownResponse;
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

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalyticsServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private List<AnalyticsSummaryResponse> mockResults;
    private List<Currency> activeCurrencies;

    @BeforeEach
    void setUp() {
        mockResults = List.of(new AnalyticsSummaryResponse(
                new BigDecimal("5000000"), new BigDecimal("85000"),
                new BigDecimal("75000"), new BigDecimal("40000"),
                new BigDecimal("120000"), 42L
        ));

        activeCurrencies = List.of(
                new Currency("USD", "US Dollar", "$", "US Dollar", true),
                new Currency("EUR", "Euro", "€", "Euro", true),
                new Currency("GBP", "British Pound Sterling", "£", "UK Pound", true)
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

            var result = analyticsService.getSummary(null, null, null);

            assertNotNull(result);
            assertEquals(new BigDecimal("5000000"), result.getTotalPayroll());
            assertEquals(42L, result.getEmployeeCount());
        }

        @Test
        @DisplayName("should filter summary by country")
        void shouldFilterByCountry() {
            when(salaryRepository.computeSummary(eq(null), eq("US"), eq(null))).thenReturn(List.of(
                    new AnalyticsSummaryResponse(
                            new BigDecimal("2000000"), new BigDecimal("95000"),
                            new BigDecimal("80000"), new BigDecimal("50000"),
                            new BigDecimal("150000"), 15L
                    )
            ));

            // getSummary("US", null, null) -> computeSummary(currencyCode=null, country="US", dept=null)
            var result = analyticsService.getSummary("US", null, null);

            assertEquals(15L, result.getEmployeeCount());
        }

        @Test
        @DisplayName("should filter summary by department")
        void shouldFilterByDepartment() {
            // getSummary(null, "Engineering", null) -> computeSummary(currencyCode=null, country=null, dept="Engineering")
            when(salaryRepository.computeSummary(eq(null), eq(null), eq("Engineering"))).thenReturn(List.of(
                    new AnalyticsSummaryResponse(
                            new BigDecimal("3000000"), new BigDecimal("110000"),
                            new BigDecimal("95000"), new BigDecimal("70000"),
                            new BigDecimal("200000"), 25L
                    )
            ));

            var result = analyticsService.getSummary(null, "Engineering", null);

            assertEquals(25L, result.getEmployeeCount());
        }

        @Test
        @DisplayName("should return zeros when no data matches")
        void shouldReturnZerosWhenEmpty() {
            when(salaryRepository.computeSummary(any(), any(), any())).thenReturn(List.of());

            var result = analyticsService.getSummary("US", "Engineering", null);

            assertEquals(BigDecimal.ZERO, result.getTotalPayroll());
            assertEquals(BigDecimal.ZERO, result.getAverageSalary());
            assertEquals(0L, result.getEmployeeCount());
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
            var us = new CountryBreakdownResponse("US", 35L, new BigDecimal("2000000"));

            when(salaryRepository.getAnalyticsByCountry(null)).thenReturn(List.of(us));

            var result = analyticsService.getByCountry(null);

            assertEquals(1, result.size());
            assertEquals("US", result.get(0).getCountry());
        }
    }

    // ---- getByDepartment ----
    @Nested
    @DisplayName("getByDepartment")
    class GetByDepartmentTests {

        @Test
        @DisplayName("should return department breakdown list")
        void shouldReturnBreakdown() {
            var eng = new DepartmentBreakdownResponse("Engineering", 40L, new BigDecimal("3000000"));

            when(salaryRepository.getAnalyticsByDepartment(eq(null), eq(null))).thenReturn(List.of(eng));

            var result = analyticsService.getByDepartment(null, null);

            assertEquals(1, result.size());
            assertEquals("Engineering", result.get(0).getDepartment());
        }
    }

    // ---- getByJobLevel ----
    @Nested
    @DisplayName("getByJobLevel")
    class GetByJobLevelTests {

        @Test
        @DisplayName("should return job level breakdown list")
        void shouldReturnBreakdown() {
            var l2 = new JobLevelBreakdownResponse("L2", 12L, new BigDecimal("85000"), new BigDecimal("60000"), new BigDecimal("120000"));

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
            assertTrue(result.stream().anyMatch(c -> "USD".equals(c.getCode()) && "$".equals(c.getSymbol())));
            assertTrue(result.stream().anyMatch(c -> "EUR".equals(c.getCode()) && "€".equals(c.getSymbol())));
        }
    }
}
