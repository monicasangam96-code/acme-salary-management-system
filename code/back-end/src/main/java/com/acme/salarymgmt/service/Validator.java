package com.acme.salarymgmt.service;

import java.math.BigDecimal;

/**
 * Interface for validation service to enable mocking without inline mock maker.
 */
public interface Validator {
    void validateEmail(String email);
    void validatePhone(String phone);
    void validateSalary(BigDecimal amount, String fieldName);
    void validateCurrencyCode(String currencyCode, java.util.List<String> validCodes);
    void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate);
    void validateHours(BigDecimal hours);
    void validatePayPeriod(java.time.LocalDate start, java.time.LocalDate end);
    void validateGrossNet(BigDecimal gross, BigDecimal net);
    void validateRequired(String value, String fieldName);
}
