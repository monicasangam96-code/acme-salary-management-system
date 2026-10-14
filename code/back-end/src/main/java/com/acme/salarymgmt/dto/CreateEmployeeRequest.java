package com.acme.salarymgmt.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CreateEmployeeRequest {

    @NotBlank(message = "Employee number is required")
    @Size(min = 1, max = 10)
    private String empNo;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Size(max = 30)
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String addressLine1;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String stateProvince;

    @NotBlank(message = "ZIP code is required")
    @Size(max = 20)
    private String zipCode;

    @NotBlank(message = "Country is required")
    @Size(max = 80)
    private String country;

    @NotBlank(message = "Department is required")
    @Size(max = 80)
    private String dept;

    @NotBlank(message = "Job title is required")
    @Size(max = 120)
    private String jobTitle;

    @NotNull(message = "Employment status is required")
    private String employmentStatus;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    // --- Getters and Setters ---
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
}
