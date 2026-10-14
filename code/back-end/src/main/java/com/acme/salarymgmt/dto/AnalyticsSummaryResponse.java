package com.acme.salarymgmt.dto;

import java.math.BigDecimal;

public class AnalyticsSummaryResponse {
    private BigDecimal totalPayroll;
    private BigDecimal averageSalary;
    private BigDecimal medianSalary;
    private Long employeeCount;

    public AnalyticsSummaryResponse(BigDecimal totalPayroll, BigDecimal averageSalary,
                                     BigDecimal medianSalary, Long employeeCount) {
        this.totalPayroll = totalPayroll;
        this.averageSalary = averageSalary;
        this.medianSalary = medianSalary;
        this.employeeCount = employeeCount;
    }

    public BigDecimal getTotalPayroll() { return totalPayroll; }
    public BigDecimal getAverageSalary() { return averageSalary; }
    public BigDecimal getMedianSalary() { return medianSalary; }
    public Long getEmployeeCount() { return employeeCount; }
}
