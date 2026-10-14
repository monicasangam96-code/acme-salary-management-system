-- =============================================================================
-- ACME Salary Management System — Salary Seed Data (first 35 employees)
-- Run to populate the salaries table so dashboard/analytics display real data.
-- ---------------------------------------------------------------------------
-- Uses INSERT IGNORE for safe re-runs on fresh or partially-seeded volumes.
-- Salaries are realistic, country-appropriate figures in local currency.
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
INSERT IGNORE INTO salaries (employee_id, salary_amount, currency_code, effective_date, status, previous_salary_amount, notes, updated_by, created_at, updated_at) VALUES
-- United States (USD) — Engineering & non-Engineering
(2,  95000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', 85000.00, 'Annual salary for Software Engineer - senior tier',        1, NOW(), NOW()),
(3, 120000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), 'CURRENT', 105000.00, 'Annual salary for Senior Software Engineer - principal tier',  1, NOW(), NOW()),
(4, 135000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 2190 DAY), 'CURRENT', 115000.00, 'Annual salary for Lead Software Engineer - staff tier',        1, NOW(), NOW()),
(5,  88000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 365 DAY), 'CURRENT', NULL,     'Annual salary for Software Engineer',                            1, NOW(), NOW()),
(6,  72000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 180 DAY), 'CURRENT', NULL,     'Annual salary for Junior Software Engineer - entry level',      1, NOW(), NOW()),
(7, 110000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 1095 DAY), 'CURRENT', 95000.00, 'Annual salary for Senior Software Engineer',                     1, NOW(), NOW()),
(8,  82000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 547 DAY), 'CURRENT', NULL,     'Annual salary for Account Executive',                            1, NOW(), NOW()),
(9,  85000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 912 DAY), 'CURRENT', 78000.00, 'Annual salary for Financial Analyst - senior tier',             1, NOW(), NOW()),
(10, 80000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 1825 DAY), 'CURRENT', 68000.00, 'Annual salary for Marketing Manager',                           1, NOW(), NOW()),
(31,  78000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 547 DAY), 'CURRENT', NULL,     'Annual salary for Junior Software Engineer - entry level',      1, NOW(), NOW()),
(32, 115000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 1095 DAY), 'CURRENT', 98000.00, 'Annual salary for Senior Software Engineer - mid tier',        1, NOW(), NOW()),
(33,  76000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', NULL,     'Annual salary for HR Specialist',                               1, NOW(), NOW()),
(34,  82000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 547 DAY), 'CURRENT', NULL,     'Annual salary for Operations Analyst',                          1, NOW(), NOW()),
-- United Kingdom (GBP)
(11, 62000.00, 'GBP', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', 55000.00, 'Annual salary for Software Engineer - mid tier',       1, NOW(), NOW()),
(12, 85000.00, 'GBP', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), 'CURRENT', 72000.00, 'Annual salary for Senior Software Engineer - principal tier',  1, NOW(), NOW()),
(13, 58000.00, 'GBP', DATE_SUB(CURDATE(), INTERVAL 912 DAY), 'CURRENT', 50000.00, 'Annual salary for Marketing Analyst',                    1, NOW(), NOW()),
(14, 72000.00, 'GBP', DATE_SUB(CURDATE(), INTERVAL 1825 DAY), 'CURRENT', 60000.00, 'Annual salary for Sales Director - mid tier',              1, NOW(), NOW()),
(35, 55000.00, 'GBP', DATE_SUB(CURDATE(), INTERVAL 547 DAY), 'CURRENT', NULL,     'Annual salary for Junior Software Engineer',               1, NOW(), NOW()),
-- Germany (EUR)
(15, 68000.00, 'EUR', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', 60000.00, 'Annual salary for Software Engineer - mid tier',       1, NOW(), NOW()),
(16, 92000.00, 'EUR', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), 'CURRENT', 78000.00, 'Annual salary for Senior Software Engineer - principal tier',  1, NOW(), NOW()),
(17, 75000.00, 'EUR', DATE_SUB(CURDATE(), INTERVAL 2190 DAY), 'CURRENT', 65000.00, 'Annual salary for Senior Accountant - senior tier',      1, NOW(), NOW()),
-- Japan (JPY)
(18, 7500000.00, 'JPY', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', 6500000.00, 'Annual salary for Software Engineer - mid tier',     1, NOW(), NOW()),
(19, 8200000.00, 'JPY', DATE_SUB(CURDATE(), INTERVAL 1095 DAY), 'CURRENT', 7000000.00, 'Annual salary for Sales Manager - senior tier',   1, NOW(), NOW()),
(20, 9800000.00, 'JPY', DATE_SUB(CURDATE(), INTERVAL 2190 DAY), 'CURRENT', 8500000.00, 'Annual salary for Lead Software Engineer',        1, NOW(), NOW()),
-- France (EUR)
(21, 48000.00, 'EUR', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', NULL,     'Annual salary for HR Specialist',                  1, NOW(), NOW()),
(22, 65000.00, 'EUR', DATE_SUB(CURDATE(), INTERVAL 1825 DAY), 'CURRENT', 55000.00, 'Annual salary for Operations Manager - senior tier', 1, NOW(), NOW()),
-- India (INR)
(23, 1200000.00, 'INR', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', NULL,     'Annual salary for Software Engineer - mid tier',   1, NOW(), NOW()),
(24, 1800000.00, 'INR', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), 'CURRENT', 1500000.00, 'Annual salary for Senior Software Engineer - principal tier', 1, NOW(), NOW()),
(25, 900000.00, 'INR', DATE_SUB(CURDATE(), INTERVAL 912 DAY), 'CURRENT', NULL,     'Annual salary for Financial Analyst - entry tier', 1, NOW(), NOW()),
-- Canada (CAD)
(26, 85000.00, 'CAD', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', NULL,     'Annual salary for Software Engineer - mid tier',   1, NOW(), NOW()),
(27, 95000.00, 'CAD', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), 'CURRENT', 80000.00, 'Annual salary for Legal Counsel - senior tier',    1, NOW(), NOW()),
-- Australia (AUD)
(28, 72000.00, 'AUD', DATE_SUB(CURDATE(), INTERVAL 547 DAY), 'CURRENT', NULL,     'Annual salary for Sales Manager - mid tier',       1, NOW(), NOW()),
(29, 68000.00, 'AUD', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', 58000.00, 'Annual salary for Marketing Analyst - senior tier', 1, NOW(), NOW()),
-- Singapore (SGD)
(30, 72000.00, 'SGD', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'CURRENT', NULL,     'Annual salary for Software Engineer - mid tier',   1, NOW(), NOW());

-- System Admin (EMP-00000) - executive level compensation
INSERT IGNORE INTO salaries (employee_id, salary_amount, currency_code, effective_date, status, previous_salary_amount, notes, updated_by, created_at, updated_at) VALUES
(1, 150000.00, 'USD', DATE_SUB(CURDATE(), INTERVAL 2555 DAY), 'CURRENT', NULL, 'Annual salary for System Administrator - executive tier', 1, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;
