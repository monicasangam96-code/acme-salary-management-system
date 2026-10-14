# ACME Salary Management System — Demo Recording Script

## Prerequisites

1. **Docker Desktop** running on your machine
2. **Screen recording software** (OBS Studio, QuickTime, or Windows Game Bar)
3. Open a terminal in the `ACME` project root directory

## Step 0: Start the Stack (2 min)

```bash
cd deploy/dev
docker compose up -d
docker compose ps  # Verify all 3 services are healthy
```

Wait ~90s for MySQL initialization and Spring Boot startup.

## Step 1: Health Check — Show All Services Are Up (30 sec)

**Terminal:**
```bash
curl http://localhost:8080/api/v1/health
# Expected: {"status":"UP","database":"connected"}
```

**Narration:** "All three services are running — MySQL, Spring Boot API on port 8080, and Angular/Nginx on port 4200."

## Step 2: Login Page (30 sec)

1. Open browser to `http://localhost:4200`
2. Show the ACME Salary Management login page with email/password fields

**Narration:** "The application loads the Angular SPA behind Nginx, which proxies API calls to the Spring Boot backend."

## Step 3: Login as HR Manager (15 sec)

| Field | Value |
|-------|-------|
| Email | `admin@acme.com` |
| Password | `admin123` |

Click **Sign In** → redirect to `/dashboard`

## Step 4: Dashboard Overview (1 min)

At `http://localhost:4200/dashboard`:

1. **Show KPI cards:** Total Payroll, Average Salary, Median Salary, Employees
   - Narration: "Four key metrics give an instant view of the organization's compensation data."
2. **Scroll to Country Breakdown chart**
   - Narration: "This bar chart shows total payroll by country, supporting 34 countries with correct currency symbols."
3. **Show Department Breakdown chart**
   - Narration: "Department-level payroll distribution across Engineering, Sales, Marketing, HR, Finance, Operations, and Legal."

## Step 5: Employee Directory — Search & Filter (1 min)

Navigate to `http://localhost:4200/directory`:

1. **Show all columns:** ID, Name, Country, Department, Job Title, Status, Salary, Actions
2. **Type in search box:** `John`
   - Narration: "Full-text search filters by employee name or email with 300ms debounce."
3. **Use country filter dropdown:** Select `United States (US)`
4. **Use department filter dropdown:** Select `Engineering`
5. **Use status filter dropdown:** Select `ACTIVE`
6. Show the updated table showing filtered results

## Step 6: Add a New Employee (1 min)

1. Click **+ Add Employee** button
2. Fill the form:

| Field | Value |
|-------|-------|
| First Name | `Jane` |
| Last Name | `Doe` |
| Email | `jane.doe@acme.com` |
| Country | United States (US) |
| Department | Engineering |
| Job Title | Senior Engineer |
| Employment Status | ACTIVE |

3. Click **Save Employee** → toast notification "Employee saved successfully"
4. Verify Jane Doe appears in the directory table

## Step 7: View & Edit Employee (1 min)

1. Find Jane Doe in the directory
2. Click on her name/ID row → navigates to `/salary/:id` (employee detail page)
3. **Show tabs:**
   - "Salary History" tab — shows previous salary entries with effective dates
   - "Payroll Records" tab — shows pay periods with gross/net pay
4. Click **✏️ Edit** button on the directory page to modify an existing employee:

| Field | Old Value → New Value |
|-------|----------------------|
| Job Title | `Engineer` → `Senior Engineer` |

5. Click **Save** → toast "Employee updated"

## Step 8: Add Salary Entry (1 min)

On the `/salary/:id` detail page, click **"New Salary Entry"**:

| Field | Value |
|-------|-------|
| Salary Amount | `95000.00` |
| Currency | USD ($) |
| Effective Date | Today's date (use date picker) |
| Notes | `Annual review adjustment` |

Click **Save Salary** → toast notification appears

## Step 9: Analytics & Reports (1 min)

Navigate to `http://localhost:4200/reports/custom`:

1. **Select a HR Question Template:**
   - "Average salary by country" — click it
   - Show the results table

2. **Select another template:**
   - "Highest payroll departments" — click it  
   - Show the results table

3. Click **"Download CSV Export"** button
   - Narration: "One-click export produces a comma-separated file with all employee data."
   - Verify a `.csv` file downloads to your Downloads folder

## Step 10: Logout (15 sec)

Click the **Logout** button in the navbar.

Show the login page reloads.

## Step 11: Verify Test Results (30 sec)

**Terminal — Backend tests:**
```bash
cd ../code/back-end
mvn test -q 2>&1 | tail -5
# Expected: "Tests run: 107, Failures: 0, Errors: 0"
```

**Terminal — E2E tests:**
```bash
cd ../code/front-end
npx playwright test
# Expected: "4 passed"
```

**Narration:** "All 107 backend integration/unit tests and 4 end-to-end E2E tests pass, confirming the entire stack works correctly."

---

## Alternate Credentials (HR Analyst Role)

| Field | Value |
|-------|-------|
| Email | `hranalyst@acme.com` |
| Password | `analyst123` |

**Narration:** "The HR Analyst role has read + edit access — they can view all data and add salary entries but cannot delete employees. Notice the delete button is hidden in the Actions column."

---

## Docker Compose Services Reference

```yaml
# acme-db — MySQL 9.7 on port 3310
acme-db   | health: healthy
            | socket: /var/run/mysqld/mysql.sock
            | database: salary_mgmt

# acme-api — Spring Boot 3.2.5 on port 8080
acme-api   | health: UP
            | endpoint: /api/v1/health
            | database: connected

# acme-web — Angular/Nginx on port 4200
acme-web   | health: healthy
            | static: dist/acme-salary-management/browser
            | proxy: localhost:8080
```

---

## Architecture Diagram (for narration)

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Browser     │────▶│  Nginx (4200) │────▶│  Spring Boot │
│   localhost   │     │  Static +    │     │  (8080)      │
│               │◀────│  Reverse     │◀────│              │
│  Login, KPIs  │     │  Proxy       │     │  REST API    │
│  Charts, Forms│     │              │     │              │
└──────────────┘     └──────────────┘     └──────┬───────┘
                                                   │
                                            ┌──────▼───────┐
                                            │  MySQL (3310) │
                                            │  salary_mgmt  │
                                            └──────────────┘
```

---

## Key File Paths for Reference

| Purpose | Path |
|---------|------|
| Docker Compose | `deploy/dev/docker-compose.yml` |
| Backend config | `code/back-end/src/main/resources/application.properties` |
| Security config | `code/back-end/src/main/java/com/acme/salarymgmt/config/SecurityConfig.java` |
| Health controller | `code/back-end/src/main/java/com/acme/salarymgmt/controller/HealthController.java` |
| Frontend routes | `code/front-end/src/app/routes.ts` |
| Dashboard component | `code/front-end/src/app/features/dashboard/` |
| Employee directory | `code/front-end/src/app/features/directory/` |
| Salary entry form | `code/front-end/src/app/features/salary-entry/` |
| Reports builder | `code/front-end/src/app/features/report-builder/` |
| E2E tests | `code/front-end/e2e/acme.spec.ts` |
| Backend tests | `code/back-end/src/test/java/` |

---

## Total Recording Time Estimate

- Startup & health check: **1 min**
- Login + Dashboard: **1.5 min**
- Directory search/filter: **1.5 min**
- Add employee + salary entry: **2 min**
- Analytics + reports + export: **1.5 min**
- Logout + test verification: **1 min**

**Total: ~8 minutes** of demonstration content.
