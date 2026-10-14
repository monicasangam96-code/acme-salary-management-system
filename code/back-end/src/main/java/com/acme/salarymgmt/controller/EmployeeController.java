package com.acme.salarymgmt.controller;

import com.acme.salarymgmt.dto.*;
import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.service.AuditLogService;
import com.acme.salarymgmt.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuditLogService auditLogService;

    public EmployeeController(EmployeeService employeeService, AuditLogService auditLogService) {
        this.employeeService = employeeService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<SliceResponse<EmployeeSummaryResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Employee.EmploymentStatus empStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                empStatus = Employee.EmploymentStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // ignore invalid status filter
            }
        }

        var page = employeeService.listActive(search, country, dept, empStatus, pageable);
        return ResponseEntity.ok(SliceResponse.from(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDetailResponse> getById(@PathVariable Long id) {
        var response = employeeService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_MANAGER')")
    public ResponseEntity<EmployeeSummaryResponse> create(@Valid @RequestBody CreateEmployeeRequest request,
                                                           BindingResult result) {
        if (result.hasErrors()) {
            var errors = result.getFieldErrors().stream()
                    .map(fe -> new ErrorResponse.FieldErrorDetail(fe.getField(), fe.getDefaultMessage()))
                    .toList();
            throw new com.acme.salarymgmt.exception.ValidationException("Validation failed", errors.stream().map(e -> e.getField() + ": " + e.getMessage()).toList());
        }

        var created = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_MANAGER')")
    public ResponseEntity<EmployeeDetailResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody CreateEmployeeRequest request,
                                                          BindingResult result) {
        if (result.hasErrors()) {
            throw new com.acme.salarymgmt.exception.ValidationException("Validation failed", List.of());
        }
        var updated = employeeService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('HR_MANAGER')")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id,
                                              @RequestParam String status) {
        employeeService.toggleStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_MANAGER')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{employeeId}/salary-history")
    public ResponseEntity<List<com.acme.salarymgmt.service.SalaryService.SalarySummaryResponse>> salaryHistory(
            @PathVariable Long employeeId, com.acme.salarymgmt.service.SalaryService salaryService) {
        var history = salaryService.getSalaryHistory(employeeId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{employeeId}/payroll")
    public ResponseEntity<List<Object>> payrollRecords(@PathVariable Long employeeId) {
        // Placeholder: would query PayrollRecordRepository
        return ResponseEntity.ok(List.of());
    }
}
