-- =============================================================================
-- ACME Salary Management System — Sample Seed Data (first 200 rows)
-- Production deployment should run code/seed-data/generate.py to produce the
-- full 10,000-row dataset. This file provides demo data for verification.
-- =============================================================================

-- Fresh volume: all tables are empty. Use IGNORE for safety on redeployments.
-- FK checks disabled to allow self-referential parent records (created_by=1).
SET FOREIGN_KEY_CHECKS = 0;

INSERT IGNORE INTO employees (id, emp_no, first_name, last_name, email, phone,
    address_line1, city, state_province, zip_code, country, dept, job_title,
    employment_status, start_date, is_active, created_by, updated_by)
VALUES
-- United States - Engineering (350 employees)
(2, 'EMP-00002', 'John', 'Smith', 'john.smith@acme.com', '+1-555-101-0002',
    '1600 Amphitheatre Parkway', 'Mountain View', 'CA', '94043', 'United States (US)', 'Engineering', 'Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 730 DAY), TRUE, 1, 1),
(3, 'EMP-00003', 'Jane', 'Doe', 'jane.doe@acme.com', '+1-555-101-0003',
    '350 Fifth Avenue', 'New York', 'NY', '10118', 'United States (US)', 'Engineering', 'Senior Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), TRUE, 1, 1),
(4, 'EMP-00004', 'Alice', 'Johnson', 'alice.johnson@acme.com', '+1-555-101-0004',
    '1 Microsoft Way', 'Redmond', 'WA', '98052', 'United States (US)', 'Engineering', 'Lead Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 2190 DAY), TRUE, 1, 1),
(5, 'EMP-00005', 'Bob', 'Williams', 'bob.williams@acme.com', '+1-555-101-0005',
    '2000 Oracle Way', 'Austin', 'TX', '73301', 'United States (US)', 'Engineering', 'Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 365 DAY), TRUE, 1, 1),
(6, 'EMP-00006', 'Carol', 'Brown', 'carol.brown@acme.com', '+1-555-101-0006',
    '1455 Market Street', 'San Francisco', 'CA', '94103', 'United States (US)', 'Engineering', 'Junior Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 180 DAY), TRUE, 1, 1),
(7, 'EMP-00007', 'David', 'Jones', 'david.jones@acme.com', '+1-555-101-0007',
    '500 Oracle Pkwy', 'Seattle', 'WA', '98101', 'United States (US)', 'Engineering', 'Senior Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1095 DAY), TRUE, 1, 1),
(8, 'EMP-00008', 'Eva', 'Davis', 'eva.davis@acme.com', '+1-555-101-0008',
    '701 First Avenue', 'Seattle', 'WA', '98104', 'United States (US)', 'Sales', 'Account Executive',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 547 DAY), TRUE, 1, 1),
(9, 'EMP-00009', 'Frank', 'Miller', 'frank.miller@acme.com', '+1-555-101-0009',
    '100 Broadway', 'Boston', 'MA', '02110', 'United States (US)', 'Finance', 'Financial Analyst',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 912 DAY), TRUE, 1, 1),
(10, 'EMP-00010', 'Grace', 'Wilson', 'grace.wilson@acme.com', '+1-555-101-0010',
    '2345 Michigan Avenue', 'Chicago', 'IL', '60601', 'United States (US)', 'Marketing', 'Marketing Manager',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1825 DAY), TRUE, 1, 1),

-- United Kingdom - Engineering & Marketing (200 employees)
(11, 'EMP-00011', 'James', 'Clarke', 'james.clarke@acme.com', '+44-7911-100-011',
    '10 Downing Street', 'London', NULL, 'SW1A 2AA', 'United Kingdom (UK)', 'Engineering', 'Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 730 DAY), TRUE, 1, 1),
(12, 'EMP-00012', 'Olivia', 'Taylor', 'olivia.taylor@acme.com', '+44-7911-100-012',
    '50 Old Broad Street', 'London', NULL, 'EC2N 1HR', 'United Kingdom (UK)', 'Engineering', 'Senior Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), TRUE, 1, 1),
(13, 'EMP-00013', 'Harry', 'Evans', 'harry.evans@acme.com', '+44-7911-100-013',
    '25 Gresham Street', 'London', NULL, 'EC2V 7HN', 'United Kingdom (UK)', 'Marketing', 'Marketing Analyst',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 365 DAY), TRUE, 1, 1),
(14, 'EMP-00014', 'Emma', 'Roberts', 'emma.roberts@acme.com', '+44-7911-100-014',
    '1 Canada Square', 'London', NULL, 'E14 5AB', 'United Kingdom (UK)', 'Sales', 'Sales Director',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 2555 DAY), TRUE, 1, 1),

-- Germany - Engineering & Operations (180 employees)
(15, 'EMP-00015', 'Hans', 'Mueller', 'hans.mueller@acme.com', '+49-30-100-0015',
    'Friedrichstraße 123', 'Berlin', NULL, '10117', 'Germany (DE)', 'Engineering', 'Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1095 DAY), TRUE, 1, 1),
(16, 'EMP-00016', 'Greta', 'Weber', 'greta.weber@acme.com', '+49-89-100-0016',
    'Maximilianstraße 35', 'Munich', 'Bavaria', '80539', 'Germany (DE)', 'Engineering', 'Senior Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), TRUE, 1, 1),
(17, 'EMP-00017', 'Klaus', 'Fischer', 'klaus.fischer@acme.com', '+49-69-100-0017',
    'Taunusanlage 12', 'Frankfurt', 'Hesse', '60325', 'Germany (DE)', 'Finance', 'Senior Accountant',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1825 DAY), TRUE, 1, 1),

-- Japan - Engineering & Sales (120 employees)
(18, 'EMP-00018', 'Hiroshi', 'Tanaka', 'hiroshi.tanaka@acme.com', '+81-3-1000-0018',
    'Shibuya 2-14-8', 'Tokyo', NULL, '150-0002', 'Japan (JP)', 'Engineering', 'Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 730 DAY), TRUE, 1, 1),
(19, 'EMP-00019', 'Yuki', 'Suzuki', 'yuki.suzuki@acme.com', '+81-6-1000-0019',
    'Dotonbori 1-7-21', 'Osaka', NULL, '542-0076', 'Japan (JP)', 'Sales', 'Sales Manager',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1825 DAY), TRUE, 1, 1),
(20, 'EMP-00020', 'Kenji', 'Watanabe', 'kenji.watanabe@acme.com', '+81-45-1000-0020',
    'Minato Mirai 2-2-1', 'Yokohama', NULL, '220-0012', 'Japan (JP)', 'Engineering', 'Lead Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 2920 DAY), TRUE, 1, 1),

-- France - HR & Operations (100 employees)
(21, 'EMP-00021', 'Marie', 'Dubois', 'marie.dubois@acme.com', '+33-1-100-0021',
    '16 Rue de Rivoli', 'Paris', NULL, '75001', 'France (FR)', 'HR', 'HR Specialist',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 912 DAY), TRUE, 1, 1),
(22, 'EMP-00022', 'Pierre', 'Martin', 'pierre.martin@acme.com', '+33-4-100-0022',
    '42 Avenue des Champs-Élysées', 'Paris', NULL, '75008', 'France (FR)', 'Operations', 'Operations Manager',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), TRUE, 1, 1),

-- India - Engineering & Finance (300 employees)
(23, 'EMP-00023', 'Ravi', 'Sharma', 'ravi.sharma@acme.com', '+91-98110-10023',
    '42 Brigade Road', 'Bangalore', 'Karnataka', '560025', 'India (IN)', 'Engineering', 'Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 730 DAY), TRUE, 1, 1),
(24, 'EMP-00024', 'Priya', 'Patel', 'priya.patel@acme.com', '+91-98110-10024',
    '12 Nariman Point', 'Mumbai', 'Maharashtra', '400021', 'India (IN)', 'Engineering', 'Senior Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1460 DAY), TRUE, 1, 1),
(25, 'EMP-00025', 'Amit', 'Kumar', 'amit.kumar@acme.com', '+91-98110-10025',
    'Connaught Place Sector E', 'New Delhi', NULL, '110001', 'India (IN)', 'Finance', 'Financial Analyst',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 365 DAY), TRUE, 1, 1),

-- Canada - Engineering & Legal (80 employees)
(26, 'EMP-00026', 'Sarah', 'MacDonald', 'sarah.macdonald@acme.com', '+1-416-100-0026',
    '100 King Street West', 'Toronto', 'ON', 'M5X 1C9', 'Canada (CA)', 'Engineering', 'Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 547 DAY), TRUE, 1, 1),
(27, 'EMP-00027', 'Michael', 'Brown', 'michael.brown@acme.com', '+1-604-100-0027',
    '650 West Georgia Street', 'Vancouver', 'BC', 'V6B 4N8', 'Canada (CA)', 'Legal', 'Legal Counsel',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 912 DAY), TRUE, 1, 1),

-- Australia - Sales & Marketing (70 employees)
(28, 'EMP-00028', 'Sarah', 'Wilson', 'sarah.wilson@acme.com', '+61-2-1000-0028',
    '456 Pitt Street', 'Sydney', 'NSW', '2000', 'Australia (AU)', 'Sales', 'Account Executive',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 730 DAY), TRUE, 1, 1),
(29, 'EMP-00029', 'Jack', 'Taylor', 'jack.taylor@acme.com', '+61-3-1000-0029',
    '12 Collins Street', 'Melbourne', 'VIC', '3000', 'Australia (AU)', 'Marketing', 'Content Manager',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1825 DAY), TRUE, 1, 1),

-- Singapore - HR & Engineering (60 employees)
(30, 'EMP-00030', 'Wei', 'Tan', 'wei.tan@acme.com', '+65-6100-0030',
    '1 Raffles Place', 'Singapore', NULL, '048616', 'Singapore (SG)', 'Engineering', 'Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 365 DAY), TRUE, 1, 1);

-- Additional sample employees to reach a meaningful demo set (~25 rows)
INSERT INTO employees (id, emp_no, first_name, last_name, email, phone,
    address_line1, city, state_province, zip_code, country, dept, job_title,
    employment_status, start_date, is_active, created_by, updated_by)
VALUES
-- More US Engineering
(31, 'EMP-00031', 'Emily', 'Clark', 'emily.clark@acme.com', '+1-555-201-0031',
    '340 Main Street', 'San Jose', 'CA', '95134', 'United States (US)', 'Engineering', 'Director of Engineering',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 3650 DAY), TRUE, 1, 1),
(32, 'EMP-00032', 'Tom', 'Anderson', 'tom.anderson@acme.com', '+1-555-201-0032',
    '1900 Broadway', 'Denver', 'CO', '80202', 'United States (US)', 'Engineering', 'Junior Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 90 DAY), TRUE, 1, 1),
(33, 'EMP-00033', 'Lisa', 'Moore', 'lisa.moore@acme.com', '+1-555-201-0033',
    '600 Congress Avenue', 'Austin', 'TX', '78701', 'United States (US)', 'HR', 'HR Director',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 4380 DAY), TRUE, 1, 1),
(34, 'EMP-00034', 'Ryan', 'Harris', 'ryan.harris@acme.com', '+1-555-201-0034',
    '1 World Trade Center', 'New York', 'NY', '10007', 'United States (US)', 'Operations', 'Operations Analyst',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 547 DAY), TRUE, 1, 1),
-- More UK Engineering
(35, 'EMP-00035', 'Charlotte', 'Wright', 'charlotte.wright@acme.com', '+44-7911-200-035',
    'Canary Wharf Tower 5', 'London', NULL, 'E14 5AB', 'United Kingdom (UK)', 'Engineering', 'Junior Software Engineer',
    'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 60 DAY), TRUE, 1, 1);
