-- =============================================================================
-- ACME Salary Management System — Init / Reference Data
-- Run once during deployment to populate currencies and a few sample records.
-- ---------------------------------------------------------------------------
-- IMPORTANT: The large seed dataset (10,000 employees) is loaded separately
-- via seed_data.sql after the initial demo setup.
-- =============================================================================

-- ----- CURRENCIES (34 countries/regions represented) -----
INSERT INTO currencies (code, name, symbol, native_name, is_active) VALUES
('USD', 'US Dollar', '$', 'US Dollar', TRUE),
('EUR', 'Euro', '€', 'Euro', TRUE),
('GBP', 'British Pound', '£', 'Pound Sterling', TRUE),
('JPY', 'Japanese Yen', '¥', '日本円', TRUE),
('INR', 'Indian Rupee', '₹', 'भारतीय rupee', TRUE),
('CNY', 'Chinese Yuan', '¥', '人民币', TRUE),
('CAD', 'Canadian Dollar', 'C$', 'Dollar Canadien', TRUE),
('AUD', 'Australian Dollar', 'A$', 'Australian Dollar', TRUE),
('CHF', 'Swiss Franc', 'Fr', 'Schweizer Franken', TRUE),
('KRW', 'South Korean Won', '₩', '한국 원', TRUE),
('SGD', 'Singapore Dollar', 'S$', 'Singapore Dollar', TRUE),
('HKD', 'Hong Kong Dollar', 'HK$', 'Hong Kong Dollar', TRUE),
('TWD', 'New Taiwan Dollar', 'NT$', '新台幣', TRUE),
('THB', 'Thai Baht', '฿', 'บาท', TRUE),
('VND', 'Vietnamese Dong', '₫', 'Đồng Việt Nam', TRUE),
('IDR', 'Indonesian Rupiah', 'Rp', 'Rupiah Indonesia', TRUE),
('MYR', 'Malaysian Ringgit', 'RM', 'Ringgit Malaysia', TRUE),
('PHP', 'Philippine Peso', '₱', 'Piso Pilipino', TRUE),
('NZD', 'New Zealand Dollar', 'NZ$', 'New Zealand Dollar', TRUE),
('ZAR', 'South African Rand', 'R', 'South African Rand', TRUE),
('BRL', 'Brazilian Real', 'R$', 'Real Brasileiro', TRUE),
('MXN', 'Mexican Peso', '$', 'Peso Mexicano', TRUE),
('ARS', 'Argentine Peso', '$', 'Peso Argentino', TRUE),
('CLP', 'Chilean Peso', '$', 'Peso Chileno', TRUE),
('COP', 'Colombian Peso', '$', 'Peso Colombiano', TRUE),
('RUB', 'Russian Ruble', '₽', 'Российский рубль', TRUE),
('PLN', 'Polish Zloty', 'zł', 'Złoty Polski', TRUE),
('CZK', 'Czech Koruna', 'Kč', 'Česká koruna', TRUE),
('HUF', 'Hungarian Forint', 'Ft', 'Magyar forint', TRUE),
('RON', 'Romanian Leu', 'lei', 'Leu Românesc', TRUE),
('TRY', 'Turkish Lira', '₺', 'Türk Lirası', TRUE),
('SEK', 'Swedish Krona', 'kr', 'Svensk krona', TRUE),
('NOK', 'Norwegian Krone', 'kr', 'Norsk krone', TRUE),
('DKK', 'Danish Krone', 'kr', 'Dansk krone', TRUE);

-- ----- SAMPLE EXCHANGE RATES (base USD) -----
INSERT INTO exchange_rates (from_code, to_code, rate, effective_date, source) VALUES
('EUR', 'USD', 1.0850, '2026-01-01', 'ECB'),
('GBP', 'USD', 1.2700, '2026-01-01', 'ECB'),
('JPY', 'USD', 0.0067, '2026-01-01', 'ECB'),
('INR', 'USD', 0.0120, '2026-01-01', 'ECB'),
('CNY', 'USD', 0.1380, '2026-01-01', 'ECB'),
('CHF', 'USD', 1.1300, '2026-01-01', 'ECB'),
('KRW', 'USD', 0.0007, '2026-01-01', 'ECB'),
('SGD', 'USD', 0.8400, '2026-01-01', 'ECB'),
('CAD', 'USD', 0.7400, '2026-01-01', 'ECB'),
('AUD', 'USD', 0.6500, '2026-01-01', 'ECB');

-- ----- ONE ADMIN / DEMO USER (password hash would be set at runtime) -----
INSERT INTO employees (id, emp_no, first_name, last_name, email, phone,
    address_line1, city, state_province, zip_code, country, dept, job_title,
    employment_status, start_date, is_active, created_by, updated_by)
VALUES
(1, 'EMP-00000', 'System', 'Admin', 'admin@acme.com', '+1-555-000-0000',
    '127.0.0.1', 'Demo City', NULL, '00000', 'United States (US)', 'IT', 'System Administrator',
    'ACTIVE', CURDATE(), TRUE, 1, 1);
