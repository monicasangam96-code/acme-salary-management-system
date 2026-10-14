package com.acme.salarymgmt.dto;

import java.math.BigDecimal;

public class EmployeeSummaryResponse {

    private Long id;
    private String empNo;
    private String firstName;
    private String lastName;
    private String country;
    private String dept;
    private String jobTitle;
    private String employmentStatus;
    private BigDecimal currentSalaryAmount;
    private String currentCurrencyCode;

    public EmployeeSummaryResponse(Long id, String empNo, String firstName, String lastName,
                                    String country, String dept, String jobTitle,
                                    String employmentStatus, BigDecimal salaryAmt, String currency) {
        this.id = id;
        this.empNo = empNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.dept = dept;
        this.jobTitle = jobTitle;
        this.employmentStatus = employmentStatus;
        this.currentSalaryAmount = salaryAmt;
        this.currentCurrencyCode = currency;
    }

    public Long getId() { return id; }
    public String getEmpNo() { return empNo; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getCountry() { return country; }
    public String getDept() { return dept; }
    public String getJobTitle() { return jobTitle; }
    public String getEmploymentStatus() { return employmentStatus; }
    public BigDecimal getCurrentSalaryAmount() { return currentSalaryAmount; }
    public String getCurrentCurrencyCode() { return currentCurrencyCode; }
}
