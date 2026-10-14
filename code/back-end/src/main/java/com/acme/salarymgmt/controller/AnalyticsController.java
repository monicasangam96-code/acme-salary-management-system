package com.acme.salarymgmt.controller;

import com.acme.salarymgmt.dto.AnalyticsSummaryResponse;
import com.acme.salarymgmt.dto.CurrencyItem;
import com.acme.salarymgmt.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> summary(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String currency) {
        return ResponseEntity.ok(analyticsService.getSummary(country, dept, currency));
    }

    @GetMapping("/by-country")
    public ResponseEntity<List<com.acme.salarymgmt.repository.SalaryRepository.CountryBreakdownResponse>> byCountry(
            @RequestParam(required = false) String currency) {
        return ResponseEntity.ok(analyticsService.getByCountry(currency));
    }

    @GetMapping("/by-department")
    public ResponseEntity<List<com.acme.salarymgmt.repository.SalaryRepository.DepartmentBreakdownResponse>> byDepartment(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String currency) {
        return ResponseEntity.ok(analyticsService.getByDepartment(country, currency));
    }

    @GetMapping("/by-job-level")
    public ResponseEntity<List<com.acme.salarymgmt.repository.SalaryRepository.JobLevelBreakdownResponse>> byJobLevel(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String currency) {
        return ResponseEntity.ok(analyticsService.getByJobLevel(country, dept, currency));
    }

    @GetMapping("/by-currency")
    public ResponseEntity<List<CurrencyItem>> byCurrency() {
        return ResponseEntity.ok(analyticsService.getCurrencyBreakdown());
    }
}
