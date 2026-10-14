package com.acme.salarymgmt.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateSalaryRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Salary amount is required")
    @DecimalMin(value = "0.01", message = "Salary amount must be greater than zero")
    private BigDecimal salaryAmount;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3)
    private String currencyCode;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private String notes;

    // --- Getters and Setters ---
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public BigDecimal getSalaryAmount() { return salaryAmount; }
    public void setSalaryAmount(BigDecimal salaryAmount) { this.salaryAmount = salaryAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
