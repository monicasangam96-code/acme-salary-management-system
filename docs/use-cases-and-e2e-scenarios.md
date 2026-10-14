# ACME Salary Management System — Use Case Scenarios & E2E Test Scenarios

## Table of Contents
1. [Authentication & Authorization](#1-authentication--authorization)
2. [Employee Directory](#2-employee-directory)
3. [Salary Management](#3-salary-management)
4. [Compensation Analytics Dashboard](#4-compensation-analytics-dashboard)
5. [Reporting & CSV Export](#5-reporting--csv-export)
6. [International Compensation](#6-international-compensation)
7. [Audit Trail](#7-audit-trail)
8. [Data Integrity & Validation](#8-data-integrity--validation)
9. [Performance & Edge Cases](#9-performance--edge-cases)

---

## 1. Authentication & Authorization

### UC-01: HR Manager Login
**Use Case ID:** UC-01  
**Actor:** HR Manager (admin role)  
**Description:** An authorized HR Manager authenticates to the system using corporate credentials and gains full administrative access to all modules.  
**Preconditions:** The application is running; Docker Compose stack is up.  
**Postconditions:** User session is established; redirects to Dashboard landing page.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open browser, navigate to `http://localhost:4200` | Redirects to login page |
| 2 | Enter email (`hrmanager@acme.com`) and password (valid) in login form | Form shows valid state; submit button enabled |
| 3 | Click "Sign In" | API call `POST /api/v1/auth/login` with credentials returns 200 OK with session token in HttpOnly cookie |
| 4 | Observe page transition | Redirected to Compensation Dashboard (default landing) |
| 5 | Verify navigation rail | Shows [Directory] [Compensation] [Reports]; user profile dropdown displays "HR Manager" |

**E2E Assertions:**
- HTTP 200 response from login endpoint with session cookie (`HttpOnly`, `Secure`, `SameSite=Strict`)
- Page renders dashboard with KPI cards populated (total payroll, avg salary, median salary, employee count = 10,000)
- Navigation rail is visible; all three tabs clickable

---

### UC-02: HR Analyst Login (Read-Only Access)
**Use Case ID:** UC-02  
**Actor:** HR Analyst (analyst role)  
**Description:** An HR Analyst logs in and gains read-only access to directory, analytics, and CSV export — but cannot edit salary or employee data.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open login page | Login form visible |
| 2 | Enter analyst credentials (`hranalyst@acme.com` / valid password) | Form valid |
| 3 | Click "Sign In" | 200 OK; redirected to Dashboard |
| 4 | Navigate to Employee Directory | Page loads; table with all employee rows visible |
| 5 | Try clicking on an employee row (view detail) | Detail page loads (read-only mode — no "Edit" button) |
| 6 | Navigate to Compensation | Dashboard analytics visible |
| 7 | Navigate to Reports | CSV export available |

**E2E Assertions:**
- Employee list shows no "Edit Salary" button or column for any row
- Clicking an employee row loads a detail view without edit fields (all inputs disabled)
- Analytics dashboard renders charts and KPI cards normally
- Reports page shows CSV export button
- Profile dropdown confirms role is "HR Analyst"

---

### UC-03: Unauthenticated Access Attempt
**Use Case ID:** UC-03  
**Actor:** Anonymous user  
**Description:** An unauthenticated user attempts to access protected pages.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open browser, navigate directly to `http://localhost:4200/dashboard` | Redirected to login page |
| 2 | Attempt direct API call: `GET /api/v1/employees` without session token | HTTP 401 Unauthorized |
| 3 | Attempt `POST /api/v1/salaries` with valid JSON but no token | HTTP 401 Unauthorized |
| 4 | Enter invalid credentials on login page | Error toast: "Invalid email or password" |

**E2E Assertions:**
- Direct URL access redirects to `/login`
- All API calls without session cookie return 401
- Login with wrong password shows validation error (never reveals whether email exists)

---

### UC-04: Analyst Attempting Write Access
**Use Case ID:** UC-04  
**Actor:** HR Analyst (analyst role)  
**Description:** An HR Analyst attempts actions reserved for HR Managers.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Login as HR Analyst | Dashboard loads normally |
| 2 | Open Employee Directory → select employee → attempt to trigger "Edit" | "Edit Salary" button hidden or disabled with tooltip: "View-only access" |
| 3 | Direct API call `PUT /api/v1/salaries` with analyst session token and valid payload | HTTP 403 Forbidden; error: `{ "error": "insufficient_permissions", "message": "Analyst role cannot modify salary data" }` |
| 4 | Direct API call `POST /api/v1/employees` | HTTP 403 Forbidden |

**E2E Assertions:**
- UI hides edit functionality for analyst role
- Server enforces RBAC on all mutating endpoints; returns 403 (never 200 or 401)

---

### UC-05: Session Expiration (Idle Timeout)
**Use Case ID:** UC-05  
**Actor:** HR Manager  
**Description:** An idle session expires per policy.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Login as HR Manager, navigate to Employee Directory | Dashboard loads |
| 2 | Wait for session idle timeout (30 min) or trigger programmatic timeout | Session cookie expires |
| 3 | Refresh the page or click any link | Redirected back to login page with message: "Your session has expired. Please sign in again." |

**E2E Assertions:**
- After timeout, any API call returns 401 (session invalid)
- Frontend guard intercepts and redirects to `/login`
- No data lost; upon re-login, user lands on Dashboard (not the previous page)

---

## 2. Employee Directory

### UC-06: Browse Paginated Employee List (Default View)
**Use Case ID:** UC-06  
**Actor:** HR Manager  
**Description:** The HR Manager views the employee directory with pagination as the default landing experience.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Login as HR Manager; land on Dashboard, click "Directory" | Employee Directory page loads |
| 2 | Verify table headers | Headers: Employee ID, Name, Country, Department, Job Title, Employment Status — matching schema |
| 3 | Verify first page of data | Page size = 25 rows (page 0); `Page` metadata shows totalElements=10,000, totalPages=400 |
| 4 | Observe pagination controls | Shows: "← 1 2 3 ... 400 →" with current page highlighted; "Showing 25 of 10,000" text |

**E2E Assertions:**
- API call `GET /api/v1/employees?page=0&size=25` returns exactly 25 employees in `content[]`
- `page.totalElements` = 10,000; `page.totalPages` = 400 (or 399 if last page < 25)
- Table renders all 25 rows correctly; pagination UI matches API response

---

### UC-07: Search Employees by Name
**Use Case ID:** UC-07  
**Actor:** HR Manager  
**Description:** The HR Manager searches for a specific employee by name.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory | Full employee list displayed |
| 2 | Type "Jane Doe" in search bar | Input shows text; debounce triggers after 300ms |
| 3 | Wait for API response | `GET /api/v1/employees?search=Jane+Doe&page=0&size=25` returns matching employee(s) |
| 4 | Verify table content | Table displays the matched employee(s); pagination metadata updates (totalElements reduced) |

**E2E Assertions:**
- API request includes `search=Jane+Doe` query parameter
- Returned rows contain "Jane Doe" in the name field (case-insensitive match)
- Pagination total elements decreases from 10,000 to match count
- Typing clears and re-types (e.g., "Jane" → delete → "John") fires new API call with new params

---

### UC-08: Filter Employees by Country and Department
**Use Case ID:** UC-08  
**Actor:** HR Manager  
Description:** The HR Manager filters employees by country and department simultaneously.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory | Full list displayed |
| 2 | Click "Country" dropdown, select "United States (US)" | Filter chip appears: `Country: US ×` |
| 3 | Click "Department" dropdown, select "Engineering" | Second filter chip appears: `Department: Engineering ×` |
| 4 | Wait for results | API call: `GET /api/v1/employees?country=US&dept=Engineering&page=0&size=25` |
| 5 | Verify table content | Only employees matching both criteria shown; total elements reflects filtered count |

**E2E Assertions:**
- Query params include `country=US&dept=Engineering`
- Returned rows all have country=US and dept=Engineering
- Clicking "×" on filter chip removes that filter and fires new API call with updated params
- All filters removed resets to full 10,000 employee list

---

### UC-09: Sort Employee Table by Column
**Use Case ID:** UC-09  
**Actor:** HR Manager  
**Description:** The HR Manager sorts the employee table by any column header.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory | Unsorted list displayed (default sort: empNo ascending) |
| 2 | Click "Country" column header | Rows sorted alphabetically by country (A→Z); ascending icon shown on header |
| 3 | Click "Country" again | Rows sorted descending (Z→A); descending icon toggled |
| 4 | Click "Salary" (or another numeric) column header | Rows sorted descending by salary; highest salary first |
| 5 | Verify pagination respects sort | Navigate to page 2 — rows continue in same sort order from where page 1 ended |

**E2E Assertions:**
- Each header click sends `sort=field,asc` or `sort=field,desc` query param (alternating)
- All returned rows are in correct ascending/descending order per clicked column
- Sort persists across pagination pages and filters
- Reset sort button (or double-click header) returns to default empNo ascending

---

### UC-10: View Employee Detail
**Use Case ID:** UC-10  
**Actor:** HR Manager  
**Description:** The HR Manager views the full profile of a specific employee.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory | Full list displayed |
| 2 | Click on an employee row (e.g., Jane Doe) | Redirects to `/employees/{id}` detail page |
| 3 | Verify profile fields display correctly | All fields rendered: empNo, first name, last name, email, phone, address, country, dept, job title, start date, employment status, current salary (amount + currency) |
| 4 | Observe section tabs | Sub-sections visible: [Profile] [Salary History] [Payroll Records] |

**E2E Assertions:**
- API call `GET /api/v1/employees/{id}` returns 200 with full employee object including current salary
- All field values in UI match API response exactly (including date formatting)
- URL matches pattern `/employees/<id>` and updates on navigation to a different employee

---

### UC-11: Create New Employee (HR Manager)
**Use Case ID:** UC-11  
**Actor:** HR Manager  
**Description:** The HR Manager creates a new employee record with all required fields.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory | Table displayed |
| 2 | Click "Add Employee" button | Modal/form opens with empty fields; required field indicators (red asterisks) visible |
| 3 | Fill all required fields: empNo=EMP-10001, firstName="Carlos", lastName="Garcia", email="carlos.garcia@acme.com", phone="+447911123456", address_line1="10 Downing St", city="London", zip_code="SW1A 2AA", country="United Kingdom (UK)", dept="Engineering", jobTitle="Software Engineer", start_date=today, employmentStatus=ACTIVE | Fields accept valid input; submit button enabled |
| 4 | Click "Save" | API call `POST /api/v1/employees` with payload; returns 201 Created |
| 5 | Verify new employee appears in directory | New row visible on first page of results (or wherever empNo sorts to); toast notification: "Employee EMP-10001 created successfully" |

**E2E Assertions:**
- API request body includes all submitted fields; response contains newly assigned `id` and `empNo`
- Audit log entry created with change_type=INSERT, table_name='employees', changed_by=current user
- Employee persists: reload page → employee still present

---

### UC-12: Update Employee Information (HR Manager)
**Use Case ID:** UC-12  
**Actor:** HR Manager  
**Description:** The HR Manager edits an existing employee's job title and department.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory; click on Jane Doe | Detail page loads; "Edit" button visible |
| 2 | Click "Edit" | Edit form opens pre-populated with current values |
| 3 | Change jobTitle from "Senior Software Engineer" → "Lead Software Engineer"; change dept from "Engineering" → "Engineering-Lead" | Form accepts changes; no validation errors |
| 4 | Click "Save Changes" | API call `PUT /api/v1/employees/{id}` returns 200 OK with updated object |
| 5 | Verify update reflected in directory table | Employee row now shows new job title and department; toast: "Employee EMP-00042 updated" |

**E2E Assertions:**
- API request body includes only changed fields (partial update) or full object — implementation-dependent but verified
- Audit log entries created for each modified field (job_title, dept), preserving old_value and new_value
- Re-fetching employee detail shows the updated values
- Salary history unaffected by this edit

---

### UC-13: Toggle Employment Status to Terminated
**Use Case ID:** UC-13  
**Actor:** HR Manager  
**Description:** The HR Manager changes an active employee's status to terminated and sets end date.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory; click on an ACTIVE employee | Detail page loads |
| 2 | Click "Edit" → Change employmentStatus from ACTIVE → TERMINATED; set endDate=today | Form accepts; validation shows note: "Terminated employees will not appear in active reports" |
| 3 | Click "Save" | API call `PATCH /api/v1/employees/{id}/status` returns 200 OK |
| 4 | Verify employee no longer appears in default directory view (which filters for active by default) | Employee hidden from table results; note: filter toggle "[Show inactive]" would reveal it |

**E2E Assertions:**
- API correctly updates employment_status and sets end_date
- Audit log captures change_type=UPDATE with old_value='ACTIVE', new_value='TERMINATED'
- Default directory query no longer returns this employee (default filter is `status=ACTIVE` or `is_active=true`)

---

### UC-14: Delete Employee (Soft Delete) — HR Manager
**Use Case ID:** UC-14  
**Actor:** HR Manager  
**Description:** The HR Manager soft-deletes an employee record.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory; click on target employee | Detail page loads |
| 2 | Click "Delete" button | Confirmation dialog: "Are you sure you want to delete EMP-00XXX? This action will hide the employee from all active views but preserve their records for audit purposes." [Cancel] [Confirm Delete] |
| 3 | Click "Confirm Delete" | API call `DELETE /api/v1/employees/{id}` returns 204 No Content |
| 4 | Verify removal | Employee no longer appears in directory; toast: "Employee EMP-00XXX removed" |

**E2E Assertions:**
- Soft-delete implemented (is_active set to false, not physical DELETE)
- Salary records and payroll records still exist (cascaded FK preserved)
- Audit log entry created with change_type=DELETE
- Employee can be recovered via admin function or filter "Show all" if needed

---

## 3. Salary Management

### UC-15: View Current Salary for an Employee
**Use Case ID:** UC-15  
**Actor:** HR Manager  
**Description:** The HR Manager views the current salary and historical salary records for a specific employee.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory; click on an employee | Detail page loads with [Salary History] tab |
| 2 | Click "Salary History" tab | Table displayed with columns: effective_date, salary_amount, currency, previous_salary, status, updated_by |
| 3 | Verify current salary row | Marked as status='CURRENT'; amount and currency match the employee's profile display |

**E2E Assertions:**
- API call `GET /api/v1/employees/{id}/salary-history` returns all salary records for the employee sorted by effective_date DESC
- Current salary shown in both Profile tab and Salary History tab are identical values

---

### UC-16: Set a New Salary (Merit Increase)
**Use Case ID:** UC-16  
**Actor:** HR Manager  
**Description:** The HR Manager updates an employee's salary with an effective date in the future.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Detail → Salary History tab | Current salary displayed (e.g., $125,000 USD) |
| 2 | Click "Add Salary" or "Update Salary" | Form opens with current salary pre-filled as previous_salary; fields: amount (editable), currency (auto-set from current/preferred for country), effective_date (defaults to next month), notes |
| 3 | Enter new salary amount: 140,000.00; leave currency as USD; set effective_date to 2026-11-01; enter notes: "Annual merit increase 12%" | All fields accepted |
| 4 | Click "Save" | API call `POST /api/v1/salaries` returns 201 Created |
| 5 | Verify salary history table updates | New row appears at top with status='CURRENT', previous_salary_amount=$125,000.00; previous current record status changes to 'SUPERSEDED' |

**E2E Assertions:**
- API response matches expected format with `previousSalaryAmount` captured
- Both old and new salary records exist in the database; exactly one row has status='CURRENT'
- Audit log entries created for both INSERT (new) and UPDATE (old → SUPERSEDED)
- Dashboard analytics reflect the new salary amount for this employee going forward

---

### UC-17: Edit Salary with Invalid Amount
**Use Case ID:** UC-17  
**Actor:** HR Manager  
**Description:** The HR Manager attempts to save a salary with invalid input.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Detail → Salary History → "Add Salary" | Form opens |
| 2 | Enter amount: `-5000` (negative) | Angular form validation rejects; inline error: "Salary amount must be greater than zero" |
| 3 | Enter amount: `999999999999` (exceeds reasonable max) | Server-side validation triggers on save; returns HTTP 422 Unprocessable Entity with detail: `{ "error": "validation_failed", "details": [{ "field": "salaryAmount", "message": "Value exceeds maximum allowed salary of $500,000" }] }` |
| 4 | Enter amount: `125000` (duplicate of current) | Warning shown: "Same as current salary" — save still permitted with note field required |

**E2E Assertions:**
- Negative values rejected client-side (form invalid, submit disabled) or server-side (400 Bad Request)
- Over-limit values return 422 with structured error details on each field
- Duplicate amount warning is visual only — saves if user confirms

---

### UC-18: Set Salary in Different Currency (Country Change)
**Use Case ID:** UC-18  
**Actor:** HR Manager  
**Description:** The HR Manager transfers an employee to a different country and updates their salary currency accordingly.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Detail; click "Edit" | Edit form opens |
| 2 | Change country from "United States (US)" → "Japan (JP)" | Currency field auto-suggests "JPY ¥" based on country selection; previous salary shown in USD for reference |
| 3 | Enter new salary: ¥12,000,000 in JPY; set effective_date | Form accepts JPY amount with Yen formatting |
| 4 | Click "Save Changes" | API call `PUT /api/v1/employees/{id}` updates country AND `POST /api/v1/salaries` creates new salary record with currency=JPY |

**E2E Assertions:**
- Employee's country updated in employees table; salary record has currency_code='JPY'
- Salary history now shows two entries: old (USD) and new (JPY) — currencies correctly separated, not mixed
- Analytics dashboard does NOT aggregate JPY with USD amounts (currency grouping enforced)

---

### UC-19: View Payroll Records for an Employee
**Use Case ID:** UC-19  
**Actor:** HR Manager  
**Description:** The HR Manager views the payroll history for a specific employee.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Detail → [Payroll Records] tab | Table displays columns: period_start, period_end, hours_worked, gross_pay, net_pay, deductions breakdown, pay_date, status |
| 2 | Verify data integrity | All monetary values shown with correct currency symbol; deduction categories expandable per row (accordion) |
| 3 | Click a payroll row to expand deductions | Deductions JSON parsed and displayed: state_tax, federal_tax, social_security, medicare, disability_insurance, health_insurance — each with amount and percentage |

**E2E Assertions:**
- API call `GET /api/v1/employees/{id}/payroll` returns array of payroll records ordered by period_start DESC
- All monetary values have proper decimal precision (DECIMAL → formatted display)
- Deductions JSON rendered correctly; gross_pay = net_pay + sum(deductions)

---

## 4. Compensation Analytics Dashboard

### UC-20: View Dashboard Overview KPI Cards
**Use Case ID:** UC-20  
**Actor:** HR Manager (any role)  
**Description:** The HR Manager views the compensation dashboard showing organizational-wide summary metrics.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Login and land on Compensation Dashboard (default page) | Page loads with four KPI cards: Total Annual Payroll, Average Salary, Median Salary, Employee Count |
| 2 | Verify Total Payroll | Matches sum of all current salaries × appropriate conversion factor (per-currency totals shown separately below); e.g., $5,200,000 across all currencies |
| 3 | Verify Average Salary | Correctly computed: Σ(current_salary_amount) / count of employees with salary; not a simple average of medians |
| 4 | Verify Median Salary | Midpoint value when all current salaries are sorted — different from average; displayed for comparison |
| 5 | Verify Employee Count | Total = 10,000 active employees; "with salary" count shown as secondary label (e.g., "9,847 with active salary records") |

**E2E Assertions:**
- Dashboard totals match independent database query: `SELECT SUM(salary_amount), AVG(salary_amount), PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary_amount) FROM salaries WHERE status='CURRENT'`
- Totals are NOT aggregated across currencies without clear labeling
- KPI cards load within 1 second

---

### UC-21: Compensation Breakdown by Country
**Use Case ID:** UC-21  
**Actor:** HR Manager  
**Description:** The HR Manager views the salary distribution broken down by country.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | On Dashboard, locate "Compensation by Country" chart section | Horizontal bar chart renders with country names on Y-axis, salary amounts on X-axis |
| 2 | Verify bars correspond to correct data | Hovering over each bar shows tooltip: country name, employee count, total payroll, average salary — all matching API response |
| 3 | Click a country bar (e.g., "United States") | Filters the directory view to that country; dashboard analytics update to show only that country's breakdown by department |

**E2E Assertions:**
- API call `GET /api/v1/analytics/by-country` returns grouped data with counts and totals per country
- Bar chart renders in correct descending order by total payroll
- Clicking a bar sends filter to directory (same params as UC-08)
- Different currencies are displayed with their own symbols (no USD conversion assumed)

---

### UC-22: Compensation Breakdown by Department
**Use Case ID:** UC-22  
**Actor:** HR Manager  
**Description:** The HR Manager views payroll breakdown by department.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | On Dashboard, locate "Payroll by Department" chart section | Bar or stacked bar chart renders with departments on Y-axis |
| 2 | Verify data accuracy | Top department (Engineering) has the highest total; values match `SELECT dept, SUM(salary_amount), COUNT(*) FROM salaries WHERE status='CURRENT' GROUP BY dept ORDER BY SUM DESC` |

**E2E Assertions:**
- Chart renders all departments present in the dataset (minimum 8 departments per seed data profile)
- Values match database query independently verified
- Clicking a department bar filters directory view to that department

---

### UC-23: Salary Distribution by Job Level
**Use Case ID:** UC-23  
**Actor:** HR Manager  
**Description:** The HR Manager views salary distribution across job levels.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | On Dashboard, locate "Salary by Job Level" chart section | Histogram or box-and-whisker plot renders with job levels on X-axis and salary ranges on Y-axis |
| 2 | Verify median and range per level | Minimum (Junior), Maximum (Director) displayed correctly; quartile marks visible |

**E2E Assertions:**
- Data matches `SELECT job_title, AVG(salary_amount), MIN(salary_amount), MAX(salary_amount), PERCENTILE_CONT(0.25), PERCENTILE_CONT(0.75) FROM salaries WHERE status='CURRENT' GROUP BY job_title`
- Different currencies NOT aggregated within a single bar (each currency group displayed separately or clearly distinguished)

---

### UC-24: Currency Filter on Analytics
**Use Case ID:** UC-24  
**Actor:** HR Manager  
**Description:** The HR Manager filters all analytics views by a specific currency.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | On Dashboard, locate currency filter dropdown (top of analytics section) | Default shows "All Currencies" |
| 2 | Select "USD ($)" | All charts and KPI cards update to show only USD-denominated salary data |
| 3 | Verify totals | Total payroll reflects ONLY USD salaries; employee count matches USD-only employees |
| 4 | Select "EUR (€)" | Analytics recalculates for EUR salaries only |

**E2E Assertions:**
- API calls include `currency=USD` or `currency=EUR` query parameter
- KPI cards recalculate correctly for filtered currency subset
- Charts re-render with only the selected currency's data; no residual data from other currencies visible

---

### UC-25: HR Question — "What is the average salary in each country?"
**Use Case ID:** UC-25  
**Actor:** HR Manager / HR Analyst  
**Description:** The HR Manager or Analyst asks a common compensation question and views the results.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Reports section; locate "HR Question Templates" dropdown | Template options visible: average by country, highest payroll departments, salary distribution by level, etc. |
| 2 | Select template: "Average Salary by Country" | Filtered table renders with columns: Country, Employee Count, Average Salary, Median Salary, Currency Breakdown (if multiple currencies) |
| 3 | Verify data accuracy | Each row's average computed as `AVG(salary_amount)` for that country; rows sorted descending by average salary |

**E2E Assertions:**
- Table data matches database query: `SELECT country, COUNT(*) AS emp_count, AVG(salary_amount) AS avg_salary, PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary_amount) AS median_salary FROM salaries JOIN employees ON ... WHERE status='CURRENT' GROUP BY country ORDER BY avg_salary DESC`
- Currencies displayed correctly; no cross-currency aggregation without explicit conversion note

---

### UC-26: HR Question — "Which departments have the highest payroll?"
**Use Case ID:** UC-26  
**Actor:** HR Manager / HR Analyst  
**Description:** The HR Manager identifies departments with the largest total compensation budgets.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Reports section; select template: "Department Payroll Rankings" | Table renders with columns: Department, Employee Count, Total Annual Payroll, Average Salary |
| 2 | Verify ordering | Rows sorted descending by total payroll |

**E2E Assertions:**
- Data matches `SELECT dept, COUNT(*) AS emp_count, SUM(salary_amount) AS total_payroll, AVG(salary_amount) AS avg_salary FROM salaries JOIN employees ON ... WHERE status='CURRENT' GROUP BY dept ORDER BY total_payroll DESC`
- Dashboard chart (if visible) reflects the same ordering

---

### UC-27: HR Question — "How are salaries distributed by job level?"
**Use Case ID:** UC-27  
**Actor:** HR Manager / HR Analyst  
**Description:** The HR Manager views salary distribution patterns across job levels.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Reports; select template: "Salary Distribution by Job Level" | Table or chart renders showing count, avg, min, max per job level |

**E2E Assertions:**
- Data matches histogram from UC-23; table and chart agree on values
- Different currencies displayed separately within each job level (not aggregated)

---

## 5. Reporting & CSV Export

### UC-28: Export All Employees as CSV
**Use Case ID:** UC-28  
**Actor:** HR Manager / HR Analyst  
**Description:** The HR Manager or Analyst exports the current employee directory view as a CSV file.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory | Table displayed with current filters (if any) active |
| 2 | Click "Export CSV" button in top-right corner | Confirmation prompt: "Export will include all filtered rows. Continue?" [Cancel] [Export] |
| 3 | Click "Export" | Browser downloads `acme_employees_export_YYYYMMDD_HHMMSS.csv` file |
| 4 | Open downloaded CSV | Header row: emp_no,first_name,last_name,email,phone,address_line1,city,state_province,zip_code,country,dept,job_title,employment_status,start_date,current_salary,current_currency |
| 5 | Verify row count | Number of data rows matches the total elements shown in the current filtered view (not always 10,000 — matches active filters) |

**E2E Assertions:**
- API call `GET /api/v1/reports/csv?export=employees&country={filter}&dept={filter}...` streams CSV response with header `Content-Disposition: attachment; filename="acme_employees_export_YYYYMMDD.csv"`
- CSV encoding is UTF-8; salary amounts rendered as decimal numbers (no currency symbol in value column — separate currency column)
- Export matches exactly what the current table view shows (including active filters)

---

### UC-29: Export Filtered Salary Data as CSV
**Use Case ID:** UC-29  
**Actor:** HR Manager / HR Analyst  
**Description:** The HR Manager exports a filtered subset of salary records.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | On Dashboard, set filter: Country=US, Department=Engineering | Dashboard shows only US Engineering analytics |
| 2 | Navigate to Reports; click "Export CSV" | Export configuration shows the active filters summarized: "Exporting: US, Engineering employees (N=XXX) salary records" |
| 3 | Click "Export" | Downloads `acme_salaries_export_US_Engineering_YYYYMMDD.csv` |

**E2E Assertions:**
- Downloaded CSV contains only US Engineering employee salary data matching the current dashboard filters
- File headers include: emp_no, employee_id, effective_date, salary_amount, currency_code, status, previous_salary_amount, updated_by, notes

---

### UC-30: Export Analytics Summary as CSV (No Data Rows)
**Use Case ID:** UC-30  
**Actor:** HR Manager  
**Description:** The HR Manager exports a summary analytics report.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | On Dashboard with no filters, navigate to Reports | Analytics visible: KPI cards and breakdowns |
| 2 | Click "Export CSV" on the analytics section | Downloads `acme_analytics_summary_YYYYMMDD.csv` with aggregated rows (not per-employee) |

**E2E Assertions:**
- File contains summary rows: metric_name, value — Total Payroll, Average Salary, Median Salary, Min Salary, Max Salary, Employee Count, etc.
- Not a row-per-employee dump; only aggregated metrics

---

## 6. International Compensation

### UC-31: View Multi-Currency Salaries in Directory
**Use Case ID:** UC-31  
**Actor:** HR Manager  
**Description:** The HR Manager views employee salaries displayed in each employee's original currency, not converted to a single reporting currency.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory | Salary column displays values with appropriate currency symbol (e.g., "$" for USD, "€" for EUR, "¥" for JPY) |
| 2 | Verify currency symbols match employee countries | US employees → $; UK → £; Germany/other EU → €; Japan → ¥ |
| 3 | Hover over a salary amount with currency icon | Tooltip: "Original currency: EUR (Euro)" — explicitly states this is NOT converted |

**E2E Assertions:**
- Every row's currency_code matches the employee's country's common currency (from seed data profile)
- No implicit conversion occurs; amounts are displayed in original currency only
- Currency symbol aligns with the code in the `currencies` master table

---

### UC-32: Salary Records Preserve Original Currency
**Use Case ID:** UC-32  
**Actor:** HR Manager  
**Description:** When reviewing salary history, each record retains its original currency — no forced conversion.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Detail (for an employee who has had a country/currency change) → Salary History tab | All historical salary records displayed with their original currencies |
| 2 | Verify each row has its own currency_code | Row A: $125,000 USD; Row B (after transfer): €98,000 EUR — different columns, never mixed |

**E2E Assertions:**
- Database `salaries.currency_code` preserves original value on every insert/update
- No field aggregates salaries across currencies without explicit grouping and labeling
- Chart rendering separates currencies by series/color when multiple currencies present in a filter group

---

## 7. Audit Trail

### UC-33: View Complete Salary Audit History
**Use Case ID:** UC-33  
**Actor:** HR Manager  
**Description:** The HR Manager reviews the full audit trail for all salary changes on a specific employee.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Detail → Salary History tab | List of salary records with "Audit Trail" button/link per row |
| 2 | Click "Audit Trail" for a specific salary record | Side panel or modal opens showing the complete audit entries for that record: field_name, old_value, new_value, change_type, changed_by (user name), changed_at (timestamp) |
| 3 | Verify completeness | Every salary insert, update, and status change has at least one corresponding audit entry |

**E2E Assertions:**
- API call `GET /api/v1/employees/{id}/audit?table=salaries` returns ordered list of audit entries for that employee's salary records
- All timestamps are UTC; changed_by resolves to the full name of the user who made the change
- Audit entries are append-only — no "deleted" or modified entries visible (immutable)

---

### UC-34: Verify Audit Entry Created on Employee Creation
**Use Case ID:** UC-34  
**Actor:** HR Manager / System | 
**Description:** When a new employee is created, an audit entry is automatically generated.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Create a new employee via `POST /api/v1/employees` (UC-11) | Employee created successfully |
| 2 | Query audit logs for this employee's records | One audit entry with: table_name='employees', record_id=new_employee.id, change_type='INSERT', changed_by=creating_user, changed_at≈creation_timestamp |

**E2E Assertions:**
- Audit entry exists in `audit_logs` table immediately after employee creation
- Entry preserves the full state at time of insert (old_value is NULL or 'NEW')

---

## 8. Data Integrity & Validation

### UC-35: Server-Side Validation — Required Fields
**Use Case ID:** UC-35  
**Actor:** HR Manager  
**Description:** The system enforces required field validation on both client and server.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to "Add Employee" form (UC-11) | All required fields marked with red asterisk |
| 2 | Leave first_name blank; click "Save" | Client-side: inline error "First name is required"; submit disabled |
| 3 | Disable JavaScript in browser; submit the same empty-form request | Server returns HTTP 400 Bad Request with structured errors: `{ "error": "validation_failed", "details": [{ "field": "firstName", "message": "must not be null" }, { "field": "lastName", "message": "must not be null" }, ...] }` |

**E2E Assertions:**
- Client-side validation triggers before any API call for obvious missing fields
- Server-side validation catches ALL client-side bypass attempts (same rules enforced)
- Error response includes field name, message, and HTTP 400 status code

---

### UC-36: Uniqueness Validation — Duplicate Employee Number
**Use Case ID:** UC-36  
**Actor:** HR Manager  
**Description:** The system rejects duplicate employee numbers.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Add a new employee with empNo="EMP-00042" (already exists for Jane Doe) | Client validation may or may not catch it depending on async uniqueness check |
| 2 | Attempt save | Server returns HTTP 409 Conflict: `{ "error": "duplicate_entry", "message": "Employee number EMP-00042 already exists", "details": [{ "field": "empNo" }] }` |

**E2E Assertions:**
- API returns 409 (not 500 or 400) for uniqueness violations
- Duplicate emp_no is NOT partially created; no record in database
- Client shows clear duplicate error message with the conflicting value highlighted

---

### UC-37: Foreign Key Integrity — Invalid Currency Code
**Use Case ID:** UC-37  
**Actor:** HR Manager  
**Description:** The system rejects invalid currency codes.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | On Salary Update form, type currency code "XYZ" (not in currencies master table) | Client validation shows: "Invalid currency code — select from the list" |
| 2 | Disable JS; send raw POST with currency_code="XYZ" | Server returns HTTP 422 Unprocessable Entity with FK validation error |

**E2E Assertions:**
- Database FK constraint rejects insert; application catches it and returns user-friendly error
- List of valid currencies shown in form dropdown (populated from `currencies` table)

---

### UC-38: Transaction Rollback on Validation Failure
**Use Case ID:** UC-38  
**Actor:** HR Manager  
**Description:** When a multi-step operation has one failing step, all changes are rolled back.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Update an employee (UC-12) AND update their salary simultaneously via single PUT /api/v1/employees/{id} with nested salary payload | — |
| 2 | Introduce a validation error in the salary portion (e.g., negative amount) | Both the employee update AND any partial salary update are rolled back |

**E2E Assertions:**
- HTTP response contains validation error on the salary portion only (not employee fields)
- Employee record remains UNCHANGED — transaction did not partially commit
- No audit log entries created for this failed attempt

---

### UC-39: Payroll Deduction Integrity
**Use Case ID:** UC-39  
**Actor:** HR Manager  
**Description:** The system validates that gross pay, deductions, and net pay are consistent.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Enter payroll record: gross_pay=5000, deductions={state_tax:100, federal_tax:200, social_security:310, medicare:72.50}, net_pay=4317.50 | All values accepted (sum of deductions = 682.50; gross - deductions = net ✓) |
| 2 | Enter payroll record: gross_pay=5000, deductions={...total=800}, net_pay=4200 | Validation error: "Net pay does not match calculated value (gross_pay − sum(deductions)). Expected $4,200.00 but got $4,200.00" — or if mismatch: error shown |

**E2E Assertions:**
- Server computes expected_net = gross_pay - Σ(all_deductions) and compares to submitted net_pay (within 0.01 tolerance for rounding)
- If inconsistent, returns HTTP 422 with detail pointing to the specific inconsistency
- Deduction amounts are all ≥ 0; no negative deductions

---

## 9. Performance & Edge Cases

### UC-40: Pagination on 10,000 Employee Records
**Use Case ID:** UC-40  
**Actor:** HR Manager  
**Description:** The system handles large datasets via server-side pagination without performance degradation.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory (unfiltered, 10,000 records) | Page loads within 2 seconds; only first 25 rows rendered |
| 2 | Navigate to page 400 (last page) | Loads within 2 seconds; shows remaining rows (< 25) |
| 3 | Apply filter: Country=US → total drops to ~3,500 | New pagination computed; 140 pages shown |

**E2E Assertions:**
- All page requests return in < 2s (measured by Playwright/Cypress timing assertions)
- No full-table fetch client-side; always paginated API call
- Memory usage stable across pagination navigation (no DOM growth)

---

### UC-41: Concurrent Access to Same Employee Record
**Use Case ID:** UC-41  
**Actor:** Two HR Managers | 
**Description:** Two users edit the same employee's salary simultaneously.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | User A opens Jane Doe's salary: current = $125,000; starts editing to $130,000 | — |
| 2 | User B simultaneously opens Jane Doe's salary; also sees $125,000; edits to $135,000 | — |
| 3 | User B saves first ($135,000) | Save succeeds; audit entry created |
| 4 | User A then tries to save their version ($130,000) | Conflict detected (optimistic lock or version check); HTTP 409 Conflict: "This record was modified by another user. Please refresh and try again." |

**E2E Assertions:**
- Second writer gets 409 with clear conflict message
- User A's UI shows a toast/notification about the conflict
- User A is prompted to refresh; re-fetching shows User B's update ($135,000) as current

---

### UC-42: Empty State — No Employees Match Filter
**Use Case ID:** UC-42  
**Actor:** HR Manager  
**Description:** The system handles a filter that returns zero results gracefully.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to Employee Directory; apply Country=US, Department=Legal, Status=ACTIVE | — |
| 2 | No employees match this combination (by design in seed data or realistic scenario) | Table shows "No employees found matching your filters" message with icon; pagination controls hidden or disabled |

**E2E Assertions:**
- API returns `{ "content": [], "page": { "totalElements": 0, ... } }`
- UI handles empty content gracefully — no console errors, no broken layout
- User can clear filters to restore the full list

---

### UC-43: Large Salary Amount Display (Precision)
**Use Case ID:** UC-43  
**Actor:** HR Manager  
**Description:** The system correctly displays and handles large salary amounts with decimal precision.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Employee with salary: $220,000.50 (unusual but valid) | Displayed as "$220,000.50" — two decimal places maintained |
| 2 | Employee with salary: €98,000.00 | Displayed as "€98,000.00" (or localized format "€98.000,00" depending on locale) |
| 3 | View analytics summary for this employee | Dashboard totals include the exact cent value in calculations (no rounding until final display) |

**E2E Assertions:**
- All monetary values stored as DECIMAL(15,2) — no floating-point drift
- Frontend formats correctly per locale; backend sends raw decimal in JSON
- Analytics aggregation matches exact stored values

---

### UC-44: Date Field Validation — Invalid Pay Period
**Use Case ID:** UC-44  
**Actor:** HR Manager  
**Description:** The system validates payroll date ranges.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Enter payroll record with period_start > period_end (e.g., start=2026-07-15, end=2026-06-01) | Server returns HTTP 422: "period_end must be after period_start" |
| 2 | Enter payroll record with period_start = period_end | Error: "Pay period must span more than one day" (or single-day allowed — implementation-dependent) |
| 3 | Enter payroll record with pay_date before period_end | Error: "Pay date cannot be before the end of the pay period" |

**E2E Assertions:**
- All date relationships validated on server
- Client-side validation mirrors server rules
- Error messages are specific (field name + nature of violation)

---

### UC-45: Application Startup & Health Check
**Use Case ID:** UC-45  
**Actor:** System Admin / Deployment Script  
**Description:** The Docker Compose stack starts successfully and all services become healthy.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Run `docker compose up -d` | All three services start: mysql, api, angular-app |
| 2 | Wait for MySQL health check to pass | MySQL reports healthy within 10s |
| 3 | Verify Spring Boot application startup completes (Liquibase migrations run if needed) | Logs show "Started SalaryManagementApplication in X.XX seconds" |
| 4 | Navigate to `http://localhost:4200` | Application loads; Dashboard renders with seed data populated |
| 5 | Verify API health endpoint | `GET /actuator/health` returns `{"status":"UP"}` |

**E2E Assertions:**
- All services healthy within expected startup time (MySQL < 30s, Spring Boot < 60s)
- Seed data present and queryable immediately after startup
- No database migration errors in logs

---

## Appendix A: Mapping to Success Criteria

| Success Criterion | Use Cases Addressing It | E2E Test Coverage |
|-------------------|------------------------|-------------------|
| Successfully seed and query 10,000 employees | UC-45 (startup), UC-40 (pagination) | UC-45 + UC-40 E2E assertions |
| Complete employee search, filtering, pagination, salary editing | UC-06 through UC-14 | All directory use cases covered |
| Display accurate compensation metrics matching DB queries | UC-20 through UC-27 | KPI card comparison in UC-20; per-country/department/level in UC-21–27 |
| Demonstrate server-side validation, access control, audit history, CSV export | UC-35 (validation), UC-04 (RBAC), UC-33/34 (audit), UC-28/29/30 (export) | All covered by E2E assertions |
| Pass automated tests and deploy runnable app with documented setup & demo | UC-45 (deployment verification), seed data generation | UC-45 + demo script validation |

## Appendix B: Use Case Summary Table

| Category | Use Case IDs | Count |
|----------|-------------|-------|
| Authentication & Authorization | UC-01 through UC-05 | 5 |
| Employee Directory | UC-06 through UC-14 | 9 |
| Salary Management | UC-15 through UC-19 | 5 |
| Compensation Analytics | UC-20 through UC-27 | 8 |
| Reporting & CSV Export | UC-28 through UC-30 | 3 |
| International Compensation | UC-31, UC-32 | 2 |
| Audit Trail | UC-33, UC-34 | 2 |
| Data Integrity & Validation | UC-35 through UC-39 | 5 |
| Performance & Edge Cases | UC-40 through UC-45 | 6 |
| **Total** | | **45** |

---

*Document version: 1.0 — October 13, 2026*  
*Derived from: ACME Salary Management System MVP Requirements*  
*Intended for: E2E test automation (Playwright/Cypress), QA validation, and acceptance testing*
