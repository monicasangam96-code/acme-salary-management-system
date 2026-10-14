package com.acme.salarymgmt.dto.response;

import java.math.BigDecimal;

public class JobLevelBreakdownResponse {
    private String jobLevel;
    private Long count;
    private BigDecimal avgSalary;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;

    public JobLevelBreakdownResponse(String jobLevel, Long count, BigDecimal avgSalary,
                                     BigDecimal minSalary, BigDecimal maxSalary) {
        this.jobLevel = jobLevel;
        this.count = count;
        this.avgSalary = avgSalary;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
    }

    // Constructor overload for AVG returning Double from Hibernate
    public JobLevelBreakdownResponse(String jobLevel, Long count, Double avgSalary,
                                     BigDecimal minSalary, BigDecimal maxSalary) {
        this.jobLevel = jobLevel;
        this.count = count;
        this.avgSalary = avgSalary != null ? BigDecimal.valueOf(avgSalary) : BigDecimal.ZERO;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
    }

    public String getJobLevel() { return jobLevel; }
    public Long getCount() { return count; }
    public BigDecimal getAvgSalary() { return avgSalary; }
    public BigDecimal getMinSalary() { return minSalary; }
    public BigDecimal getMaxSalary() { return maxSalary; }
}
