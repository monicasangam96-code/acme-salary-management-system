package com.acme.salarymgmt.dto.response;

import java.math.BigDecimal;

public class DepartmentBreakdownResponse {
    private String department;
    private Long count;
    private BigDecimal totalPayroll;

    public DepartmentBreakdownResponse(String department, Long count, BigDecimal totalPayroll) {
        this.department = department;
        this.count = count;
        this.totalPayroll = totalPayroll;
    }

    // Overload for when SUM returns Double from Hibernate type inference
    public DepartmentBreakdownResponse(String department, Long count, Double totalPayroll) {
        this.department = department;
        this.count = count;
        this.totalPayroll = totalPayroll != null ? BigDecimal.valueOf(totalPayroll) : BigDecimal.ZERO;
    }

    public String getDepartment() { return department; }
    public Long getCount() { return count; }
    public BigDecimal getTotalPayroll() { return totalPayroll; }
}
