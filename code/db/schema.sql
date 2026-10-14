-- =============================================================================
-- ACME Salary Management System — MySQL Schema
-- Version: 1.0
-- Database: salary_mgmt (InnoDB, utf8mb4)
-- Generated from architecture spec dated October 2026
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------------
-- 1. CURRENCIES — Master reference data for ISO 4217 currency codes
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS currencies;
CREATE TABLE currencies (
    code          VARCHAR(3)  NOT NULL COMMENT 'ISO 4217 currency code (USD, EUR, …)',
    name          VARCHAR(50) NOT NULL COMMENT 'English display name',
    symbol        VARCHAR(5)  NOT NULL COMMENT 'Currency symbol ($, €, £, ¥ …)',
    native_name   VARCHAR(80) DEFAULT NULL COMMENT 'Native-language display name',
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 2. EXCHANGE RATES — Reserved for future FX conversions (one row per date)
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS exchange_rates;
CREATE TABLE exchange_rates (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    from_code       VARCHAR(3)  NOT NULL COMMENT 'Source currency',
    to_code         VARCHAR(3)  NOT NULL COMMENT 'Target currency',
    rate            DECIMAL(12,8) NOT NULL CHECK (rate > 0)     COMMENT 'Conversion factor',
    effective_date  DATE        NOT NULL                      COMMENT 'Valid from this date',
    source          VARCHAR(50) DEFAULT NULL                  COMMENT 'Provider identifier (ECB, Fed …)',
    PRIMARY KEY (id),
    CONSTRAINT uk_exchange_rates UNIQUE KEY (from_code, to_code, effective_date),
    CONSTRAINT chk_exchange_rate_positive CHECK (rate > 0),
    CONSTRAINT fk_exchange_from FOREIGN KEY (from_code) REFERENCES currencies(code),
    CONSTRAINT fk_exchange_to   FOREIGN KEY (to_code)   REFERENCES currencies(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 3. AUDIT LOGS — Immutable append-only audit trail
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS audit_logs;
CREATE TABLE audit_logs (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    employee_id    BIGINT UNSIGNED NOT NULL                      COMMENT 'Affected employee',
    table_name     VARCHAR(50)     NOT NULL                      COMMENT 'employees | salaries | payroll_records',
    record_id      BIGINT UNSIGNED NOT NULL                      COMMENT 'PK of modified record',
    field_name     VARCHAR(100)    NOT NULL                      COMMENT 'Changed column name',
    old_value      TEXT            DEFAULT NULL                  COMMENT 'Previous value (TEXT for safety)',
    new_value      TEXT            DEFAULT NULL                  COMMENT 'New value',
    change_type    ENUM('INSERT','UPDATE','DELETE') NOT NULL    COMMENT 'Mutation type',
    changed_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by     BIGINT UNSIGNED NOT NULL                      COMMENT 'Actor user ID',
    PRIMARY KEY (id),
    INDEX idx_audit_employee   (employee_id),
    INDEX idx_audit_record     (table_name, record_id),
    INDEX idx_audit_changed_at (changed_at DESC),
    CONSTRAINT fk_audit_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_actor  FOREIGN KEY (changed_by)   REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 4. EMPLOYEES — Core employee profile table
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
    id                BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    emp_no            VARCHAR(10)         NOT NULL COMMENT 'Employee number e.g. EMP-00001',
    first_name        VARCHAR(50)         NOT NULL,
    last_name         VARCHAR(50)         NOT NULL,
    email             VARCHAR(255)        NOT NULL COMMENT 'Corporate email address',
    phone             VARCHAR(30)         DEFAULT NULL COMMENT 'Phone number (E.164 or local)',
    address_line1     VARCHAR(255)        NOT NULL,
    city              VARCHAR(100)        NOT NULL,
    state_province    VARCHAR(100)        DEFAULT NULL,
    zip_code          VARCHAR(20)         NOT NULL,
    country           VARCHAR(80)         NOT NULL COMMENT 'Country name or code',
    dept              VARCHAR(80)         NOT NULL,
    job_title         VARCHAR(120)        NOT NULL,
    employment_status ENUM('ACTIVE','TERMINATED','SUSPENDED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Current status',
    start_date        DATE                NOT NULL COMMENT 'Employment start date',
    end_date          DATE                DEFAULT NULL COMMENT 'Termination date (nullable)',
    is_active         BOOLEAN             NOT NULL DEFAULT TRUE  COMMENT 'Soft-delete flag',
    created_at        DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        BIGINT UNSIGNED     NOT NULL,
    updated_by        BIGINT UNSIGNED     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employees_emp_no  (emp_no),
    UNIQUE KEY uk_employees_email   (email),
    INDEX idx_employees_country     (country),
    INDEX idx_employees_dept        (dept),
    INDEX idx_employees_status      (employment_status),
    INDEX idx_employees_active      (is_active),
    INDEX idx_employees_search_idx  (country, dept, employment_status),
    CONSTRAINT fk_emp_created_by    FOREIGN KEY (created_by) REFERENCES employees(id),
    CONSTRAINT fk_emp_updated_by    FOREIGN KEY (updated_by) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 5. SALARIES — Salary history with multi-currency support
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS salaries;
CREATE TABLE salaries (
    id                        BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    employee_id               BIGINT UNSIGNED     NOT NULL,
    salary_amount             DECIMAL(15,2)       NOT NULL CHECK (salary_amount > 0),
    currency_code             VARCHAR(3)          NOT NULL,
    effective_date            DATE                NOT NULL,
    status                    ENUM('CURRENT','SUPERSEDED','PENDING') NOT NULL DEFAULT 'CURRENT' COMMENT 'Salary history status',
    previous_salary_amount    DECIMAL(15,2)       DEFAULT NULL COMMENT 'Snapshot of salary before this change',
    notes                     VARCHAR(500)        DEFAULT NULL,
    updated_by                BIGINT UNSIGNED     NOT NULL,
    created_at                DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_salaries_employee_effective  (employee_id, effective_date DESC),
    INDEX idx_salaries_effective_date      (effective_date),
    INDEX idx_salaries_currency            (currency_code),
    INDEX idx_salaries_status              (status),
    CONSTRAINT fk_salary_employee       FOREIGN KEY (employee_id)   REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_salary_currency       FOREIGN KEY (currency_code) REFERENCES currencies(code),
    CONSTRAINT fk_salary_updated_by     FOREIGN KEY (updated_by)    REFERENCES employees(id),
    CONSTRAINT chk_salary_amount_positive CHECK (salary_amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 6. PAYROLL RECORDS — Pay-period detail with flexible deductions
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS payroll_records;
CREATE TABLE payroll_records (
    id                      BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    employee_id             BIGINT UNSIGNED     NOT NULL,
    emp_no                  VARCHAR(10)         NOT NULL COMMENT 'Denormalized for reporting speed',
    period_start            DATE                NOT NULL COMMENT 'Pay period start date',
    period_end              DATE                NOT NULL COMMENT 'Pay period end date (>= period_start)',
    hours_worked            DECIMAL(6,2)        NOT NULL CHECK (hours_worked > 0 AND hours_worked <= 504),
    pay_date                DATE                NOT NULL,
    gross_pay               DECIMAL(12,2)       NOT NULL CHECK (gross_pay >= 0),
    net_pay                 DECIMAL(12,2)       NOT NULL CHECK (net_pay >= 0),
    currency_code           VARCHAR(3)          NOT NULL,
    deductions              JSON                DEFAULT NULL COMMENT 'Flexible per country e.g. {state_tax:…, federal_tax:…}',
    payroll_status          ENUM('PENDING','PROCESSED','ERROR') NOT NULL DEFAULT 'PENDING',
    updated_by              BIGINT UNSIGNED     NOT NULL,
    created_at              DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_payroll_employee_period  (employee_id, period_start DESC),
    INDEX idx_payroll_pay_date         (pay_date),
    INDEX idx_payroll_status           (payroll_status),
    CONSTRAINT fk_payroll_employee      FOREIGN KEY (employee_id)   REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_currency      FOREIGN KEY (currency_code) REFERENCES currencies(code),
    CONSTRAINT fk_payroll_updated_by    FOREIGN KEY (updated_by)    REFERENCES employees(id),
    CONSTRAINT chk_period_dates         CHECK (period_end >= period_start),
    CONSTRAINT chk_gross_nonneg         CHECK (gross_pay >= 0),
    CONSTRAINT chk_net_nonneg           CHECK (net_pay >= 0),
    CONSTRAINT chk_hours_range          CHECK (hours_worked > 0 AND hours_worked <= 504)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
