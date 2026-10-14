package com.acme.salarymgmt.dto.response;

import java.math.BigDecimal;

public class AnalyticsSummaryResponse {
    private BigDecimal totalPayroll;
    private BigDecimal averageSalary;
    private BigDecimal medianSalary;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private Long employeeCount;

    public AnalyticsSummaryResponse(BigDecimal totalPayroll, BigDecimal averageSalary,
                                    BigDecimal medianSalary, BigDecimal minSalary,
                                    BigDecimal maxSalary, Long employeeCount) {
        this.totalPayroll = totalPayroll;
        this.averageSalary = averageSalary;
        this.medianSalary = medianSalary;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.employeeCount = employeeCount;
    }

    // Overload for when AVG returns Double from Hibernate/JPQL
    public AnalyticsSummaryResponse(BigDecimal totalPayroll, Double averageSalary,
                                    Double medianSalary, BigDecimal minSalary,
                                    BigDecimal maxSalary, Long employeeCount) {
        this.totalPayroll = totalPayroll;
        this.averageSalary = averageSalary != null ? BigDecimal.valueOf(averageSalary) : BigDecimal.ZERO;
        this.medianSalary = medianSalary != null ? BigDecimal.valueOf(medianSalary) : BigDecimal.ZERO;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.employeeCount = employeeCount;
    }

    public BigDecimal getTotalPayroll() { return totalPayroll; }
    public BigDecimal getAverageSalary() { return averageSalary; }
    public BigDecimal getMedianSalary() { return medianSalary; }
    public BigDecimal getMinSalary() { return minSalary; }
    public BigDecimal getMaxSalary() { return maxSalary; }
    public Long getEmployeeCount() { return employeeCount; }
}
