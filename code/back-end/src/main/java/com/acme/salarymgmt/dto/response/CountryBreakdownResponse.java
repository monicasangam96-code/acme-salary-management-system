package com.acme.salarymgmt.dto.response;

import java.math.BigDecimal;

public class CountryBreakdownResponse {
    private String country;
    private Long count;
    private BigDecimal totalPayroll;

    public CountryBreakdownResponse(String country, Long count, BigDecimal totalPayroll) {
        this.country = country;
        this.count = count;
        this.totalPayroll = totalPayroll;
    }

    // Overload for when SUM returns Double from Hibernate type inference
    public CountryBreakdownResponse(String country, Long count, Double totalPayroll) {
        this.country = country;
        this.count = count;
        this.totalPayroll = totalPayroll != null ? BigDecimal.valueOf(totalPayroll) : BigDecimal.ZERO;
    }

    public String getCountry() { return country; }
    public Long getCount() { return count; }
    public BigDecimal getTotalPayroll() { return totalPayroll; }
}
