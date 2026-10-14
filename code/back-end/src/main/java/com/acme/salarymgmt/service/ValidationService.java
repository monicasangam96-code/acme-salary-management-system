package com.acme.salarymgmt.service;

import com.acme.salarymgmt.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ValidationService implements Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+._-]+@[A-Za-z0-9.-]+$");
    private static final BigDecimal MIN_SALARY = new BigDecimal("0.01");
    private static final BigDecimal MAX_SALARY = new BigDecimal("500000.00");

    public void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format", List.of("email: must be a valid email address"));
        }
    }

    public void validatePhone(String phone) {
        if (phone != null && phone.length() > 30) {
            throw new ValidationException("Phone number too long", List.of("phone: must not exceed 30 characters"));
        }
    }

    public void validateSalary(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(MIN_SALARY) <= 0 || amount.compareTo(MAX_SALARY) > 0) {
            throw new ValidationException(
                "Invalid salary for field: " + fieldName,
                List.of(fieldName + ": must be between 0.01 and 500000")
            );
        }
    }

    public void validateCurrencyCode(String currencyCode, java.util.List<String> validCodes) {
        if (currencyCode == null || !validCodes.contains(currencyCode)) {
            throw new ValidationException(
                "Invalid currency code: " + currencyCode,
                List.of("currency_code: must be one of " + validCodes)
            );
        }
    }

    public void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new ValidationException(
                "Invalid date range",
                List.of("end_date: must be on or after start_date")
            );
        }
    }

    public void validateHours(BigDecimal hours) {
        if (hours == null || hours.compareTo(java.math.BigDecimal.ZERO) <= 0
                || hours.compareTo(new BigDecimal("504")) > 0) {
            throw new ValidationException(
                "Invalid hours worked",
                List.of("hours_worked: must be between 0 and 504")
            );
        }
    }

    public void validatePayPeriod(java.time.LocalDate start, java.time.LocalDate end) {
        if (end != null && start != null && !end.isAfter(start)) {
            throw new ValidationException(
                "Invalid pay period",
                List.of("period_end: must be after period_start")
            );
        }
    }

    public void validateGrossNet(BigDecimal gross, BigDecimal net) {
        if (gross != null && net != null && net.compareTo(gross) > 0) {
            throw new ValidationException(
                "Payroll math error",
                List.of("net_pay: must not exceed gross_pay")
            );
        }
    }

    public void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                "Missing required field",
                List.of(fieldName + ": cannot be blank or empty")
            );
        }
    }
}
