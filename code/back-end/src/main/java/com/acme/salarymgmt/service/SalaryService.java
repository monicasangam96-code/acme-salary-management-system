package com.acme.salarymgmt.service;

import com.acme.salarymgmt.model.Currency;
import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.model.Salary;
import com.acme.salarymgmt.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrencyRepository currencyRepository;
    private final Validator validator;
    private final Auditor auditor;

    public SalaryService(SalaryRepository salaryRepository,
                         EmployeeRepository employeeRepository,
                         CurrencyRepository currencyRepository,
                         Validator validator,
                         Auditor auditor) {
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
        this.currencyRepository = currencyRepository;
        this.validator = validator;
        this.auditor = auditor;
    }

    @Transactional(readOnly = true)
    public List<SalarySummaryResponse> getSalaryHistory(Long employeeId) {
        var emp = employeeRepository.findActiveById(employeeId)
                .orElseThrow(() -> new com.acme.salarymgmt.exception.ResourceNotFoundException("Employee", "id", employeeId));

        var records = salaryRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
        return records.stream().map(r -> {
            var resp = new SalarySummaryResponse();
            resp.setId(r.getId());
            resp.setSalaryAmount(r.getSalaryAmount());
            resp.setCurrencyCode(r.getCurrencyCode());
            resp.setEffectiveDate(r.getEffectiveDate());
            resp.setStatus(r.getStatus().toString());
            resp.setPreviousSalaryAmount(r.getPreviousSalaryAmount());
            resp.setNotes(r.getNotes());
            resp.setCreatedAt(r.getCreatedAt());
            return resp;
        }).toList();
    }

    @Transactional(readOnly = true)
    public SalarySummaryResponse getSalaryById(Long id) {
        var salary = salaryRepository.findById(id)
                .orElseThrow(() -> new com.acme.salarymgmt.exception.ResourceNotFoundException("Salary", "id", id));

        var resp = new SalarySummaryResponse();
        resp.setId(salary.getId());
        resp.setEmployeeId(salary.getEmployeeId());
        resp.setSalaryAmount(salary.getSalaryAmount());
        resp.setCurrencyCode(salary.getCurrencyCode());
        resp.setEffectiveDate(salary.getEffectiveDate());
        resp.setStatus(salary.getStatus().toString());
        resp.setPreviousSalaryAmount(salary.getPreviousSalaryAmount());
        resp.setNotes(salary.getNotes());
        return resp;
    }

    @Transactional
    public SalarySummaryResponse updateSalary(SalaryUpdateRequest request) {
        // Validate employee exists
        var emp = employeeRepository.findActiveById(request.employeeId())
                .orElseThrow(() -> new com.acme.salarymgmt.exception.ResourceNotFoundException("Employee", "id", request.employeeId()));

        // Get valid currencies
        var currencies = currencyRepository.findAllByIsActiveTrue();
        var validCodes = currencies.stream().map(Currency::getCode).toList();
        validator.validateCurrencyCode(request.currencyCode(), validCodes);
        validator.validateSalary(request.salaryAmount(), "salary_amount");

        // Set previous CURRENT salary to SUPERSEDED
        var currentSalary = salaryRepository.findCurrentByEmployeeId(emp.getId());
        if (currentSalary.isPresent()) {
            var old = currentSalary.get();
            old.setStatus(Salary.SalaryStatus.SUPERSEDED);
            salaryRepository.save(old);
            auditor.logUpdate(
                    emp.getId(), "salaries", old.getId(),
                    "status", "CURRENT", "SUPERSEDED", 1L);
        }

        // Create new salary record
        var salary = new Salary();
        salary.setEmployeeId(emp.getId());
        salary.setSalaryAmount(request.salaryAmount());
        salary.setCurrencyCode(request.currencyCode());
        salary.setEffectiveDate(request.effectiveDate());
        salary.setStatus(Salary.SalaryStatus.CURRENT);
        salary.setPreviousSalaryAmount(currentSalary.map(Salary::getSalaryAmount).orElse(null));
        salary.setNotes(request.notes());
        salary.setUpdatedBy(1L);

        salary = salaryRepository.save(salary);

        auditor.logUpdate(
                emp.getId(), "salaries", salary.getId(),
                "salary_amount",
                salary.getPreviousSalaryAmount() != null ? salary.getPreviousSalaryAmount().toPlainString() : "0.00",
                salary.getSalaryAmount().toPlainString(), 1L);

        var resp = new SalarySummaryResponse();
        resp.setId(salary.getId());
        resp.setEmployeeId(salary.getEmployeeId());
        resp.setSalaryAmount(salary.getSalaryAmount());
        resp.setCurrencyCode(salary.getCurrencyCode());
        resp.setEffectiveDate(salary.getEffectiveDate());
        resp.setStatus(salary.getStatus().toString());
        resp.setPreviousSalaryAmount(salary.getPreviousSalaryAmount());
        resp.setNotes(salary.getNotes());
        return resp;
    }

    public record SalaryUpdateRequest(Long employeeId, BigDecimal salaryAmount,
                                       String currencyCode, java.time.LocalDate effectiveDate, String notes) {}

    public static class SalarySummaryResponse {
        private Long id;
        private Long employeeId;
        private BigDecimal salaryAmount;
        private String currencyCode;
        private java.time.LocalDate effectiveDate;
        private String status;
        private BigDecimal previousSalaryAmount;
        private String notes;
        private java.time.LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public BigDecimal getSalaryAmount() { return salaryAmount; }
        public void setSalaryAmount(BigDecimal salaryAmount) { this.salaryAmount = salaryAmount; }
        public String getCurrencyCode() { return currencyCode; }
        public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
        public java.time.LocalDate getEffectiveDate() { return effectiveDate; }
        public void setEffectiveDate(java.time.LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getPreviousSalaryAmount() { return previousSalaryAmount; }
        public void setPreviousSalaryAmount(BigDecimal previousSalaryAmount) { this.previousSalaryAmount = previousSalaryAmount; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
