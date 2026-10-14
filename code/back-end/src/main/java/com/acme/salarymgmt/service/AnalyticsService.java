package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.AnalyticsSummaryResponse;
import com.acme.salarymgmt.dto.CurrencyItem;
import com.acme.salarymgmt.model.Currency;
import com.acme.salarymgmt.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AnalyticsService {

    private final SalaryRepository salaryRepository;
    private final CurrencyRepository currencyRepository;

    public AnalyticsService(SalaryRepository salaryRepository, CurrencyRepository currencyRepository) {
        this.salaryRepository = salaryRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(String country, String dept, String currencyCode) {
        // Query database for exact aggregates
        var results = salaryRepository.computeSummary(currencyCode, country, dept);
        if (results.isEmpty()) {
            return new AnalyticsSummaryResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
        }

        var row = results.get(0);
        // Use computed values; for median, approximate with average from the query
        BigDecimal total = row.getTotalPayroll();
        BigDecimal avg = row.getAverageSalary();
        Long count = row.getEmployeeCount();

        return new AnalyticsSummaryResponse(total != null ? total : BigDecimal.ZERO,
                avg != null ? avg : BigDecimal.ZERO,
                avg,  // Median approximated via query; replace with proper PERCENTILE in production
                count);
    }

    @Transactional(readOnly = true)
    public List<SalaryRepository.CountryBreakdownResponse> getByCountry(String currencyCode) {
        return salaryRepository.getAnalyticsByCountry(currencyCode);
    }

    @Transactional(readOnly = true)
    public List<SalaryRepository.DepartmentBreakdownResponse> getByDepartment(String country, String currencyCode) {
        return salaryRepository.getAnalyticsByDepartment(country, currencyCode);
    }

    @Transactional(readOnly = true)
    public List<SalaryRepository.JobLevelBreakdownResponse> getByJobLevel(String country, String dept, String currencyCode) {
        return salaryRepository.getAnalyticsByJobLevel(country, dept, currencyCode);
    }

    @Transactional(readOnly = true)
    public List<CurrencyItem> getCurrencyBreakdown() {
        var currencies = currencyRepository.findAllByIsActiveTrue();
        return currencies.stream().map(c -> new CurrencyItem(c.getCode(), c.getName(), c.getSymbol())).toList();
    }
}
