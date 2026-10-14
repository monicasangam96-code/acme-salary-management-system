package com.acme.salarymgmt.controller;

import com.acme.salarymgmt.service.SalaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salaries")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @GetMapping("/summary")
    public ResponseEntity<List<SalaryService.SalarySummaryResponse>> getSummary(
            @RequestParam Long employeeId) {
        var history = salaryService.getSalaryHistory(employeeId);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_MANAGER')")
    public ResponseEntity<SalaryService.SalarySummaryResponse> create(@Valid @RequestBody SalaryService.SalaryUpdateRequest request) {
        var created = salaryService.updateSalary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaryService.SalarySummaryResponse> getById(@PathVariable Long id) {
        var response = salaryService.getSalaryById(id);
        return ResponseEntity.ok(response);
    }
}
