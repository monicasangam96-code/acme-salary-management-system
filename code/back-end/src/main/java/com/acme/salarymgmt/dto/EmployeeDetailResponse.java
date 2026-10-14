package com.acme.salarymgmt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeDetailResponse {

    private Long id;
    private String empNo;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String addressLine1;
    private String city;
    private String stateProvince;
    private String zipCode;
    private String country;
    private String dept;
    private String jobTitle;
    private String employmentStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;

    // Current salary (from the most recent CURRENT record)
    private BigDecimal currentSalaryAmount;
    private String currentCurrencyCode;
    private LocalDate effectiveDate;

    public EmployeeDetailResponse() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmpNo() { return empNo; }
    public void setEmpNo(String empNo) { this.empNo = empNo; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStateProvince() { return stateProvince; }
    public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getCreatedBy() { return createdBy; }
    public BigDecimal getCurrentSalaryAmount() { return currentSalaryAmount; }
    public void setCurrentSalaryAmount(BigDecimal currentSalaryAmount) { this.currentSalaryAmount = currentSalaryAmount; }
    public String getCurrentCurrencyCode() { return currentCurrencyCode; }
    public void setCurrentCurrencyCode(String currentCurrencyCode) { this.currentCurrencyCode = currentCurrencyCode; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    // Map a Salary entity into the current salary fields
    public static EmployeeDetailResponse withCurrentSalary(EmployeeDetailResponse response,
                                                            com.acme.salarymgmt.repository.SalaryRepository.AnalyticsSummaryResponse summary) {
        return response; // handled in service layer
    }
}
