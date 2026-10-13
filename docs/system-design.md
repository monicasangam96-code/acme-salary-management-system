# ACME Salary Management System — Architecture Design Document

## Table of Contents
1. [System Overview](#1-system-overview)
2. [Architecture Principles](#2-architecture-principles)
3. [High-Level Architecture](#3-high-level-architecture)
4. [Component Architecture](#4-component-architecture)
5. [Data Model & Database Design](#5-data-model--database-design)
6. [API Design](#6-api-design)
7. [Frontend Architecture](#7-frontend-architecture)
8. [Security Architecture](#8-security-architecture)
9. [Deployment Architecture](#9-deployment-architecture)
10. [Testing Strategy](#10-testing-strategy)
11. [Seed Data & Demonstration](#11-seed-data--demonstration)
12. [Scalability & Performance](#12-scalability--performance)
13. [Risks & Mitigations](#13-risks--mitigations)

---

## 1. System Overview

### 1.1 Purpose
Build a secure, web-based salary management system enabling ACME's HR Manager to:
- Maintain employee salary records for ~10,000 employees across multiple countries
- Replace spreadsheet-based workflows with a structured application
- Analyze organizational compensation through dashboards and reports

### 1.2 Key Stakeholders
| Role | Responsibility | Access Level |
|------|---------------|--------------|
| HR Manager (primary) | Full CRUD on employee records, salary data, reports | Administrator |
| HR Analyst (secondary) | Read-only access to compensation analytics, export data | Viewer / Exporter |
| System Admin | Infrastructure monitoring, backup management | Infrastructure |

### 1.3 Technology Stack
| Layer | Technology | Version |
|-------|-----------|---------|
| UI | Angular | 22 |
| UI Testing | Karma / Jasmine | latest |
| Backend API | Spring Boot | 4.x |
| Framework | Spring Framework | 7.x |
| Java Platform | JDK | 25 |
| Enterprise APIs | Jakarta EE | 11 |
| Relational Database | MySQL | 9.7 |
| API Style | REST / JSON | — |
| Backend Testing | JUnit | latest |
| E2E Testing | Browser automation (Playwright/Cypress) | latest |
| Data Seeding | Excel / CSV import | — |
| Deployment | Docker Compose | latest |

---

## 2. Architecture Principles

1. **Salary data sensitivity** — All salary information is treated as PII-sensitive; encrypted at rest, TLS in transit, strict RBAC.
2. **Currency integrity** — Every salary stored with original currency; conversions only when a verified exchange rate provider is available.
3. **Auditability** — Every change to salary records produces an immutable audit entry.
4. **Multi-country correctness** — Country-specific data fields (tax, deductions, address formats) are extensible but never assumed identical.
5. **Pagination-first queries** — No query returns more than a page's worth of rows; aggregates are computed server-side.
6. **Docker-native deployment** — Application and database run via Docker Compose; repeatable setup is mandatory.
7. **Out-of-scope discipline** — Payroll execution, tax calculations, benefits, equity, recruiting, performance reviews, and self-service are explicitly excluded.

---

## 3. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Client Browser                              │
│                     (Chrome / Edge / Firefox)                      │
│                                                                      │
│   ┌──────────────────────────────────────────────────────────┐     │
│   │                  Angular 22 Application                    │     │
│   │                                                             │     │
│   │  ┌───────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐  │     │
│   │  │ Employee  │ │ Salary  │ │ Dashboard │ │ Reports │  │     │
│   │  │ Directory │ │ Manager │ │ & Metrics │ │  Export │  │     │
│   │  └───────────┘ └──────────┘ └───────────┘ └──────────┘  │     │
│   │                                                             │     │
│   │  Services (NgRx / @angular/common/http)                     │     │
│   └──────────────────────────────────────────────────────────┘     │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │ HTTPS (REST/JSON)
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Spring Boot 4.x Application                      │
│                                                                      │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                   API Gateway / Filter Chain                 │   │
│   │         CORS · Helmet · Rate Limiting · Request Logging      │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                      │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐     │
│   │  Employee    │  │  Salary      │  │  Compensation        │     │
│   │  Controller  │  │  Controller  │  │  Analytics Controller│     │
│   └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘     │
│          │                  │                      │                 │
│   ┌──────▼───────┐  ┌──────▼───────┐  ┌──────────▼───────────┐     │
│   │  Employee    │  │  Salary      │  │  Compensation        │     │
│   │  Service     │  │  Service     │  │  Analytics Service   │     │
│   └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘     │
│          │                  │                      │                 │
│   ┌──────▼───────┐  ┌──────▼───────┐  ┌──────────▼───────────┐     │
│   │  Employee    │  │  Salary      │  │  Report                │     │
│   │  Repository  │  │  Repository  │  │  Generator             │     │
│   └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘     │
│          │                  │                      │                 │
│   ┌──────▼──────────────────▼──────────────────────▼───────────┐     │
│   │              Audit Logging & Validation Layer                │     │
│   │         Server-side validation · RBAC · Audit Trail          │     │
│   └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │ JDBC / Hibernate
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         MySQL 9.7 Database                           │
│                                                                      │
│   ┌──────────────────────────────────────────────────────────┐     │
│   │                  Schema: salary_mgmt                       │     │
│   │                                                            │     │
│   │   employees  · salaries  · payroll_records                 │     │
│   │   audit_logs  · currencies  · exchange_rates               │     │
│   └──────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     Docker Compose Stack                             │
│                                                                      │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐    │
│   │  angular-app │  │  spring-boot │  │     mysql            │    │
│   │  (nginx)     │→ │  API server  │→ │  salary_mgmt DB      │    │
│   └──────────────┘  └──────────────┘  └──────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 4. Component Architecture

### 4.1 Angular Frontend (angular-app)

#### Module Structure
```
src/app/
├── core/                          # Singleton services & guards
│   ├── auth/
│   │   ├── auth.service.ts        # JWT/session management
│   │   ├── auth.guard.ts          # Route protection
│   │   └── roles.model.ts         # RBAC enum
│   ├── interceptors/
│   │   ├── auth.interceptor.ts    # Attach session token
│   │   └── error.interceptor.ts  # Global error handling
│   ├── models/
│   │   ├── employee.model.ts
│   │   ├── salary.model.ts
│   │   ├── payroll.model.ts
│   │   └── analytics.model.ts
│   └── services/
│       ├── session.service.ts     # User session lifecycle
│       └── notification.service.ts
│
├── features/
│   ├── employee-directory/
│   │   ├── employee-list/
│   │   │   ├── employee-list.component.html
│   │   │   ├── employee-list.component.ts
│   │   │   └── employee-list.component.scss
│   │   ├── employee-search/       # Search bar, filters (country, dept, title)
│   │   ├── employee-paginator/    # Server-side pagination controls
│   │   ├── employee-sorter/       # Column-based sorting
│   │   ├── employee-detail/       # Full employee profile view
│   │   └── employee-edit/         # Inline form for edits
│   │
│   ├── salary-manager/
│   │   ├── salary-entry-form/     # Add / edit salary records
│   │   ├── salary-history/        # Audit trail viewer
│   │   ├── currency-selector/     # Country-appropriate currency
│   │   └── validation-messages/   # Field-level validation feedback
│   │
│   ├── compensation-analytics/
│   │   ├── dashboard-overview/    # KPI summary cards (total payroll, avg, median)
│   │   ├── country-breakdown/     # Salary by country chart
│   │   ├── department-breakdown/  # Payroll by department chart
│   │   ├── level-distribution/    # Job level salary histogram
│   │   └── currency-filter/       # Filter analytics by currency
│   │
│   └── reports/
│       ├── report-builder/        # Dynamic filter + export configuration
│       ├── csv-export/            # Server-driven CSV download
│       └── hr-question-templates/ # Pre-built queries for common HR questions
│
├── shared/                        # Reusable components
│   ├── data-table/                # Generic paginated, sortable table
│   ├── kpi-card/                  # Summary metric tile
│   ├── chart-wrapper/             # Chart.js / ng2-charts wrapper
│   ├── confirmation-dialog/       # Delete / confirm modals
│   └── responsive-layout/         # Mobile/tablet/desktop breakpoints
│
├── app.routes.ts                  # Angular Router configuration
├── app.component.ts
└── app.config.ts                  # Global providers
```

#### Key UI Workflows
1. **Employee Directory** — Default landing page: server-side filtered, paginated table with search → click row → detail view → edit modal → save triggers optimistic/pessimistic update → audit entry created.
2. **Compensation Dashboard** — Summary cards load first (aggregates via `/api/analytics/summary`) → filterable drill-down charts follow.
3. **Salary Editing** — Focused form with server-side validation feedback, currency auto-detect from country, mandatory-field highlighting.
4. **Reports** — Pre-built HR question templates (dropdown) + custom filter bar → generate filtered table → export as CSV.

---

### 4.2 Backend API (spring-boot-app)

#### Package Structure
```
com.acme.salarymgmt/
├── config/                        # Application configuration
│   ├── SecurityConfig.java        # JWT / session auth, CORS
│   ├── DatabaseConfig.java        # HikariCP pooling, Hibernate dialect
│   ├── JacksonConfig.java         # BigDecimal serializers for currency
│   └── AuditorAwareImpl.java      # Spring Data JPA audit listener
│
├── controller/                    # REST endpoints
│   ├── EmployeeController.java
│   ├── SalaryController.java
│   ├── PayrollRecordController.java
│   ├── AnalyticsController.java
│   ├── ReportController.java     # CSV export endpoint
│   └── AuthController.java       # Login / session management
│
├── service/                       # Business logic
│   ├── EmployeeService.java
│   ├── SalaryService.java
│   ├── PayrollService.java
│   ├── AnalyticsService.java
│   ├── ReportService.java
│   ├── AuditLogService.java
│   └── ValidationService.java    # Server-side validation rules
│
├── repository/                    # Spring Data JPA repositories
│   ├── EmployeeRepository.java
│   ├── SalaryRepository.java
│   ├── PayrollRecordRepository.java
│   ├── AuditLogRepository.java
│   └── CurrencyRepository.java   # Master data for currencies
│
├── model/                         # Entity classes
│   ├── Employee.java
│   ├── Salary.java
│   ├── PayrollRecord.java
│   ├── AuditLogEntry.java
│   └── Currency.java
│
├── dto/                           # Data Transfer Objects
│   ├── request/                   # Request DTOs (validation annotations)
│   │   ├── CreateEmployeeRequest.java
│   │   ├── UpdateSalaryRequest.java
│   │   └── FilterQueryRequest.java
│   └── response/                  # Response DTOs
│       ├── EmployeeDetailResponse.java
│       ├── SalarySummaryResponse.java
│       └── AnalyticsResponse.java
│
├── exception/                     # Global error handling
│   ├── ResourceNotFoundException.java
│   ├── ValidationException.java
│   ├── InsufficientPermissionsException.java
│   └── GlobalExceptionHandler.java
│
├── audit/                         # Audit trail
│   ├── AuditableEntity.java       # Base class with @Created_by, @Last_modified_by
│   └── AuditTrailAspect.java      # AOP aspect for automatic logging
│
└── SalaryManagementApplication.java
```

#### API Layer Responsibilities
```
API Gateway / Filter Chain (Spring Security filter chain):
├── CORS Configuration → Allow angular-app origin only
├── Request Logging → Log method, path, user, timestamp (NO salary in logs)
├── Rate Limiting → Prevent abuse (configurable per endpoint)
├── Auth Filter → Validate session token / JWT
└── RBAC Filter → Check role for resource access

Controller Layer:
├── Input validation via @Valid + Jakarta Bean Validation
├── HTTP status codes: 200/201/400/401/403/404/409/422/500
└── Error responses: { "error": "code", "message": "...", "details": [...] }

Service Layer:
├── Business logic (currency rules, salary range checks, payroll calculations)
├── Audit log invocation on every mutation
├── Transaction management via @Transactional
└── Delegate validation to ValidationService

Repository Layer:
├── Spring Data JPA with derived queries for common patterns
├── Custom JPQL/QueryDSL for complex analytics aggregations
├── Pagination via Pageable (Spring Data)
└── Cascade audit fields automatically
```

---

## 5. Data Model & Database Design

### 5.1 Entity-Relationship Diagram

```
┌─────────────┐       ┌──────────────────┐       ┌─────────────┐
│   employees  │       │     salaries      │       │ currencies   │
├─────────────┤       ├──────────────────┤       ├─────────────┤
│ id (PK)     │◄──────│ employee_id (FK) │       │ code (PK)   │
│ emp_no      │  N:1  │ id (PK)          │  1:N  │ name        │
│ first_name  │       │ salary_amount    │       │ symbol      │
│ last_name   │       │ currency_code    │◄──────│ native_name  │
│ email       │       │ effective_date   │ FK    │ is_active   │
│ phone       │       │ status           │       └─────────────┘
│ address_line1│      │ previous_salary  │
│ city        │       │ notes            │
│ state_prov  │       │ updated_by (FK)  │──────►│ employees    │
│ zip_code    │       │ effective_until  │  N:1  ├─────────────┤
│ country     │       └──────────────────┘       │ id (PK)      │
│ dept        │                                    │ emp_no, etc. │
│ job_title   │       ┌──────────────────┐       └─────────────┘
│ start_date  │       │  payroll_records  │
│ end_date    │       ├──────────────────┤
│ is_active   │       │ id (PK)          │
│ created_at  │       │ employee_id (FK) │
│ updated_by  │       │ emp_no           │
└─────────────┘       │ period_start     │
                      │ period_end       │
┌─────────────┐       │ hours_worked     │
│audit_logs    │       │ pay_date         │
├─────────────┤       │ gross_pay        │
│ id (PK)     │       │ net_pay          │
│ employee_id │◄──────│ currency_code    │
│ table_name  │       │ deductions JSON  │
│ record_id   │       │ payroll_status   │
│ field_name  │       │ updated_by (FK)  │
│ old_value   │       │ created_at       │
│ new_value   │       └──────────────────┘
│ change_type │
│ changed_at  │
│ changed_by  │
└─────────────┘

── Additional master tables:
currencies (code, name, symbol) — reference data for valid currency codes
exchange_rates (from_code, to_code, rate, effective_date) — reserved for future FX
```

### 5.2 Table Definitions

#### `employees`
| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGINT | PK, AUTO_INCREMENT | Internal surrogate key |
| emp_no | VARCHAR(10) | UNIQUE, NOT NULL | Employee number (e.g. EMP-00001) |
| first_name | VARCHAR(50) | NOT NULL | — |
| last_name | VARCHAR(50) | NOT NULL | — |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Corporate email |
| phone | VARCHAR(30) | — | E.164 or local format |
| address_line1 | VARCHAR(255) | NOT NULL | — |
| city | VARCHAR(100) | NOT NULL | — |
| state_province | VARCHAR(100) | — | Optional per country |
| zip_code | VARCHAR(20) | NOT NULL | — |
| country | VARCHAR(80) | NOT NULL, INDEX | ISO 3166-1 alpha-2 or full name |
| dept | VARCHAR(80) | NOT NULL, INDEX | Department code/name |
| job_title | VARCHAR(120) | NOT NULL | — |
| employment_status | ENUM('ACTIVE','TERMINATED','SUSPENDED') | NOT NULL, DEFAULT 'ACTIVE' | |
| start_date | DATE | NOT NULL | Employment start date |
| end_date | DATE | NULL | Termination date (nullable) |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | Soft-delete flag |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Audit timestamp |
| updated_at | TIMESTAMP | NOT NULL, ON UPDATE NOW() | Audit timestamp |
| created_by | BIGINT | NOT NULL, FK→employees.id | User who created the record |
| updated_by | BIGINT | NOT NULL, FK→employees.id | Last modifier |

**Indexes:**
- `idx_emp_no` (emp_no UNIQUE)
- `idx_country` (country)
- `idx_dept` (dept)
- `idx_employment_status` (employment_status)
- `idx_active` (is_active)
- Composite: `idx_search_idx(country, dept, job_title, employment_status)` for filter queries

#### `salaries`
| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGINT | PK, AUTO_INCREMENT | — |
| employee_id | BIGINT | NOT NULL, FK→employees.id ON DELETE CASCADE | — |
| salary_amount | DECIMAL(15,2) | NOT NULL, > 0 | Annual base salary |
| currency_code | VARCHAR(3) | NOT NULL, FK→currencies.code | ISO 4217 |
| effective_date | DATE | NOT NULL | When this salary took effect |
| status | ENUM('CURRENT','SUPERSeded','PENDING') | NOT NULL, DEFAULT 'CURRENT' | Salary history |
| previous_salary_amount | DECIMAL(15,2) | NULL | Snapshot before change |
| notes | VARCHAR(500) | — | Optional reason / context |
| updated_by | BIGINT | NOT NULL, FK→employees.id | Last modifier |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | — |
| updated_at | TIMESTAMP | NOT NULL, ON UPDATE NOW() | — |

**Indexes:**
- `idx_employee_id` (employee_id)
- `idx_effective_date` (effective_date)
- `idx_currency` (currency_code)
- Composite: `idx_employee_effective(employee_id, effective_date DESC)` for history lookup

#### `payroll_records`
| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGINT | PK, AUTO_INCREMENT | — |
| employee_id | BIGINT | NOT NULL, FK→employees.id ON DELETE CASCADE | — |
| emp_no | VARCHAR(10) | NOT NULL | Denormalized for reporting speed |
| period_start | DATE | NOT NULL | Pay period start |
| period_end | DATE | NOT NULL | Pay period end (≥ period_start) |
| hours_worked | DECIMAL(6,2) | NOT NULL, > 0, ≤ 504 | Max ~168 hrs/mo |
| pay_date | DATE | NOT NULL | Actual payment date |
| gross_pay | DECIMAL(12,2) | NOT NULL, ≥ 0 | Pre-deduction pay |
| net_pay | DECIMAL(12,2) | NOT NULL, ≥ 0 | Post-deduction pay |
| currency_code | VARCHAR(3) | NOT NULL, FK→currencies.code | — |
| deductions | JSON | — | Flexible per country: `{ "state_tax": ..., "federal_tax": ..., "social_security": ..., "medicare": ..., "disability_insurance": ..., "health_insurance": ... }` |
| payroll_status | ENUM('PENDING','PROCESSED','ERROR') | NOT NULL, DEFAULT 'PENDING' | Processing state |
| updated_by | BIGINT | NOT NULL, FK→employees.id | Last modifier |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | — |
| updated_at | TIMESTAMP | NOT NULL, ON UPDATE NOW() | — |

**Indexes:**
- `idx_employee_period` (employee_id, period_start DESC)
- `idx_pay_date` (pay_date)
- `idx_status` (payroll_status)

#### `audit_logs`
| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGINT | PK, AUTO_INCREMENT | — |
| employee_id | BIGINT | NOT NULL, FK→employees.id ON DELETE CASCADE | — |
| table_name | VARCHAR(50) | NOT NULL | 'employees' or 'salaries' |
| record_id | BIGINT | NOT NULL | Primary key of modified record |
| field_name | VARCHAR(100) | NOT NULL | Changed column name |
| old_value | TEXT | — | Previous value |
| new_value | TEXT | — | New value |
| change_type | ENUM('INSERT','UPDATE','DELETE') | NOT NULL | Type of mutation |
| changed_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | — |
| changed_by | BIGINT | NOT NULL, FK→employees.id | Actor |

**Indexes:**
- `idx_employee_id` (employee_id)
- `idx_record` (table_name, record_id)
- `idx_changed_at` (changed_at DESC)

#### `currencies` (reference / master data)
| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| code | VARCHAR(3) | PK | ISO 4217 (USD, EUR, GBP, JPY, …) |
| name | VARCHAR(50) | NOT NULL | "US Dollar", "Euro", … |
| symbol | VARCHAR(5) | NOT NULL | "$", "€", "£", "¥" |
| native_name | VARCHAR(80) | — | Native language display name |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | Soft-disable obsolete codes |

#### `exchange_rates` (reserved)
| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGINT | PK, AUTO_INCREMENT | — |
| from_code | VARCHAR(3) | NOT NULL, FK→currencies.code | Source currency |
| to_code | VARCHAR(3) | NOT NULL, FK→currencies.code | Target currency |
| rate | DECIMAL(12,8) | NOT NULL, > 0 | Conversion factor |
| effective_date | DATE | NOT NULL | Valid from this date |
| source | VARCHAR(50) | — | Provider identifier (e.g., ECB, Federal Reserve) |

**Composite PK:** `(from_code, to_code, effective_date)`

---

## 6. API Design

### 6.1 Endpoint Summary

#### Employees (`/api/v1/employees`)
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/employees` | List employees (paginated, filterable) | HR role |
| GET | `/api/v1/employees/{id}` | Get employee detail | HR role |
| POST | `/api/v1/employees` | Create employee | HR Manager |
| PUT | `/api/v1/employees/{id}` | Update employee | HR Manager |
| PATCH | `/api/v1/employees/{id}/status` | Toggle employment status | HR Manager |
| DELETE | `/api/v1/employees/{id}` | Soft-delete employee | HR Manager |
| GET | `/api/v1/employees/{id}/salary-history` | Get salary history for employee | HR role |

#### Salary (`/api/v1/salaries`)
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/salaries/summary` | Current salary summary (aggregates) | HR role |
| POST | `/api/v1/salaries` | Set / update salary for employee | HR Manager |
| GET | `/api/v1/salaries/{id}` | Get specific salary record | HR role |

#### Analytics (`/api/v1/analytics`)
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/analytics/summary` | Total payroll, avg, median, count by filters | HR role |
| GET | `/api/v1/analytics/by-country` | Compensation breakdown by country | HR role |
| GET | `/api/v1/analytics/by-department` | Payroll breakdown by department | HR role |
| GET | `/api/v1/analytics/by-job-level` | Salary distribution by job level | HR role |
| GET | `/api/v1/analytics/by-currency` | Salary counts and totals by currency | HR role |

#### Reports (`/api/v1/reports`)
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/reports/csv?filters=...` | Export filtered data as CSV download | HR role |
| POST | `/api/v1/reports/custom` | Save custom report definition (optional) | HR Manager |

#### Auth (`/api/v1/auth`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/login` | Authenticate and receive session token |
| POST | `/api/v1/auth/logout` | Invalidate session |
| GET | `/api/v1/auth/me` | Get current user profile |

### 6.2 Request / Response Examples

#### GET `/api/v1/employees?page=0&size=25&country=US&dept=Engineering&status=ACTIVE`
**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 42,
      "empNo": "EMP-00042",
      "firstName": "Jane",
      "lastName": "Doe",
      "email": "jane.doe@acme.com",
      "country": "US",
      "dept": "Engineering",
      "jobTitle": "Senior Software Engineer",
      "employmentStatus": "ACTIVE",
      "currentSalary": {
        "amount": 125000.00,
        "currency": "USD",
        "effectiveDate": "2026-01-15"
      }
    }
  ],
  "page": {
    "number": 0,
    "size": 25,
    "totalElements": 10000,
    "totalPages": 400,
    "sort": "empNo"
  }
}
```

#### POST `/api/v1/salaries`
**Request:**
```json
{
  "employeeId": 42,
  "salaryAmount": 135000.00,
  "currencyCode": "USD",
  "effectiveDate": "2026-11-01",
  "notes": "Annual merit increase"
}
```
**Response (201 Created):**
```json
{
  "id": 156,
  "employeeId": 42,
  "salaryAmount": 135000.00,
  "currencyCode": "USD",
  "effectiveDate": "2026-11-01",
  "status": "CURRENT",
  "previousSalaryAmount": 125000.00,
  "updatedBy": { "id": 1, "name": "HR Manager" },
  "createdAt": "2026-10-13T14:30:00Z"
}
```

#### GET `/api/v1/analytics/summary?country=US&dept=Engineering`
**Response (200 OK):**
```json
{
  "totalAnnualPayroll": 5250000.00,
  "averageSalary": 140000.00,
  "medianSalary": 135000.00,
  "minSalary": 85000.00,
  "maxSalary": 220000.00,
  "employeeCount": 37,
  "currencyBreakdown": [
    { "currency": "USD", "count": 25, "totalPayroll": 3500000.00 },
    { "currency": "EUR", "count": 8,  "totalPayroll": 960000.00 },
    { "currency": "GBP", "count": 4,  "totalPayroll": 790000.00 }
  ]
}
```

---

## 7. Frontend Architecture

### 7.1 State Management
- **Approach:** Angular Signals + RxJS `BehaviorSubject` for complex streams; avoid full NgRx unless state complexity demands it.
- **State layers:**
  - Component-level state (form values, pagination params)
  - Service-level cache (intercepted HTTP responses cached via HttpClient with manual invalidation)
  - Global auth state (session service, shared across all components)

### 7.2 Angular 22 Considerations
- Use **standalone components** (no NgModule unless required for legacy library compatibility).
- Leverage **new control flow** (`@if`, `@for`) syntax.
- **Signals-based reactivity** throughout.
- Verify Angular 22 + Spring Boot 4.x + Java 25 compatibility at setup time.

### 7.3 Key Component Patterns

#### Paginated Data Table (`shared/data-table`)
```
┌─────────────────────────────────────────────────┐
│ [Search...]  [Country ▼] [Dept ▼] [Status ▼]   │ ← Filter bar
├─────────────────────────────────────────────────┤
│ ☐ EMP-00001 │ John Smith │ US │ Engineering │   │
│ ☐ EMP-00042 │ Jane Doe   │ UK │ Marketing  │   │ ← Data rows
│ ...                                         │   │
├─────────────────────────────────────────────────┤
│  ←  1  2  3 ... 400  →    Showing 25 of 10,000 │ ← Paginator
└─────────────────────────────────────────────────┘
```
- Server-side pagination: each page change fires `GET /api/v1/employees?page=N&size=25`
- Debounced search (300ms)
- Filters combine as AND in query params
- Column header click = sort toggle (ascending/descending/null)

#### Dashboard Layout
```
┌─────────────────────────────────────────────────────┐
│  ACME Salary Management          [👤 HR Manager ▼] │ ← Top nav
├─────────────────────────────────────────────────────┤
│  [Directory]  [Compensation]  [Reports]             │ ← Side rail / tabs
├──────────────┼──────────────────────────────────────┤
│              │  ┌──────────┐ ┌──────────┐          │
│  KPI Cards   │  │ Total    │ │ Average  │          │
│  (summary)   │  │ Payroll  │ │ Salary   │          │
│              │  │ $5.2M    │ │ $138K    │          │
│              │  └──────────┘ └──────────┘          │
│              │  ┌──────────┐ ┌──────────┐          │
│              │  │ Median   │ │ Employees│          │
│              │  │ $132K    │ │  10,000  │          │
│              │  └──────────┘ └──────────┘          │
│              │                                      │
│              │  ┌─────────────────────────────┐     │
│              │  │  Payroll by Department       │     │
│              │  │  [  Bar chart ...           ]│     │
│              │  └─────────────────────────────┘     │
│              │                                      │
│              │  ┌─────────────────────────────┐     │
│              │  │  Salary Distribution         │     │
│              │  │  [  Histogram ...           ]│     │
│              │  └─────────────────────────────┘     │
└──────────────┴──────────────────────────────────────┘
```

#### Salary Edit Form Pattern
- Validation errors shown inline (red border + message)
- Required fields highlighted at top
- Currency auto-populated from employee's country on load
- Save triggers optimistic update → success toast or rollback with error toast
- Audit trail link available on salary history view

---

## 8. Security Architecture

### 8.1 Authentication
```
┌───────────┐        ┌──────────────┐        ┌──────────────┐
│  Browser   │───────→│  Auth Filter │───────→│  Token Valid │
│            │ POST   │ (Spring Sec) │  OK    │  ation       │
│ HR Manager │ /login │              │ Fail   │  (JWT/Session)│
│ enters     │        └──────────────┘        └──────┬───────┘
│ credentials   │                                     │
│              │                              Valid   │
│              │                                     ▼
│              │                              ┌──────────────┐
│              │                             │  RBAC Check  │
│              │                             │ HR Manager:    │
│              │                             │  CRUD          │
│              │                             │ HR Analyst:    │
│              │                             │  Read + Export │
│              │                              └──────────────┘
└───────────┘        └──────────────┘
```

- **Mechanism:** Session-based auth with HttpOnly, Secure, SameSite=Strict cookies (preferred for internal HR tool) or JWT in HttpOnly cookie.
- **Password policy:** Minimum 12 chars, complexity rules, bcrypt/Argon2 hashing.
- **Session timeout:** 30 min idle; max 8 hr absolute.

### 8.2 Authorization (RBAC)
| Role | Employee CRUD | Salary Edit | Audit View | Analytics | CSV Export |
|------|--------------|-------------|------------|-----------|------------|
| HR Manager | ✅ Full | ✅ | ✅ | ✅ | ✅ |
| HR Analyst | 🔒 Read-only | ❌ | 🔒 Read-only | ✅ | ✅ |

### 8.3 Data Protection
- **In transit:** TLS 1.3 for all external communication; internal Docker networking (no TLS needed between containers on same host).
- **At rest:** MySQL data directory encrypted via LUKS or MySQL Enterprise Encryption (MVP: document as a production requirement).
- **Salary values:** Never logged, never in URL query params (use POST body / request context only).
- **Audit trail:** Immutable append-only; HR Manager can view but not modify.

### 8.4 Input Validation (Defense in Depth)
```
Layer                    Rules                                    Consequence
─────────────────────────────────────────────────────────────────────────
Angular Form             Required fields, types, ranges   Visual feedback + disable Submit
Jakarta Bean Val.        @NotNull, @DecimalMin, etc.      400 Bad Request with detail list
Service Layer            Business rules (currency valid,  Throws
                         dates non-overlapping, status    ValidationException
                         transitions)
Database FK / Constraints| Referential integrity, NOT NULL  Rollback + 409 Conflict
Audit Trail            Every mutation logged              Immutable record created
```

### 8.5 Audit Log Flow
```
Service layer method annotated with @Transactional
├── Before: snapshot current values
├── Execute business logic (persist changes)
└── After commit: audit log entry appended
    ├── table_name: 'salaries'
    ├── record_id: salary.id
    ├── field_name: 'salary_amount'
    ├── old_value: "125000.00"
    ├── new_value: "135000.00"
    ├── change_type: "UPDATE"
    ├── changed_by: current user's emp_no (FK)
    └── changed_at: timestamp
```

---

## 9. Deployment Architecture

### 9.1 Docker Compose Stack (`docker-compose.yml`)
```yaml
services:
  mysql:
    image: mysql:9.7
    environment:
      MYSQL_DATABASE: salary_mgmt
      MYSQL_USER: acme_user
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${ROOT_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
      - ./db/init.sql:/docker-entrypoint-initdb.d/01-init.sql  # Seed reference data
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks: [salary-network]

  api:
    build: ./spring-boot-app
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/salary_mgmt?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER:-acme_user}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD:-change_me}
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8080:8080"
    networks: [salary-network]

  angular-app:
    build: ./angular-app
    volumes:
      - ./angular-app/nginx.conf:/etc/nginx/conf.d/default.conf
    depends_on:
      - api
    ports:
      - "4200:80"
    networks: [salary-network]

volumes:
  mysql-data:

networks:
  salary-network:
    driver: bridge
```

### 9.2 Build Pipeline (per environment)
```
┌─────────┐     ┌──────────┐     ┌──────────────┐     ┌──────────┐
│ Angular │ →   │ Nginx    │ →   │  Static assets │ →  │ Docker   │
│ build   │     │ serve    │     │  /usr/share/ng │     │ image    │
└─────────┘     └──────────┘     └──────────────┘     └────┬─────┘
                                                            │
┌─────────┐     ┌──────────┐     ┌──────────────┐         │
│ Spring  │ →   │ JAR      │ →   │  Executable    │ →  Docker │
│ Boot    │     │ package  │     │  + dependencies│     │ image │
└─────────┘     └──────────┘     └──────────────┘     └────┬─────┘
                                                            │
                                              ┌─────────────▼──────────┐
                                              │ docker-compose up -d   │
                                              └────────────────────────┘
```

### 9.3 Database Migration Strategy
- **Liquibase** for schema versioning (even though MVP may start with a single init script, plan for migrations).
- `db/changelog/` directory: each change is a separate `.sql` or `.yaml` file.
- First migration: create all tables + seed `currencies` reference data.
- Second migration: seed 10,000 synthetic employees via stored procedure or script-driven insert batches.

---

## 10. Testing Strategy

### 10.1 Test Pyramid
```
         ┌───────────────┐
        │   E2E Tests    │  ← Complete user workflows (search → edit → report → export)
       │  (Playwright)   │
      ├──────────────────┤
     │  Integration Tests│  ← Controller + Service + Testcontainers (MySQL)
    │   (JUnit + @SpringBootTest) │
   ├────────────────────┤
  │  Unit Tests         │  ← Repository queries, domain logic, DTO mapping
 │   (JUnit + Mockito)  │
 └──────────────────────┘
```

### 10.2 Test Categories

| Category | Tools | Coverage | Example Scenarios |
|----------|-------|----------|-------------------|
| **Data Seeding** | JUnit + Testcontainers | Seed script, CSV importer | Import succeeds → 10,000 employees visible; mandatory fields validated; currency codes verified |
| **Database** | JUnit + Testcontainers + H2/test MySQL | CRUD, constraints, transactions | INSERT with invalid FK rejected; UPDATE rolls back on constraint violation; DELETE cascades correctly |
| **API (JUnit)** | JUnit 5 + MockMvc / @WebMvcTest | All REST endpoints | GET returns correct status + schema; POST validates input; PUT updates persist; DELETE soft-deletes |
| **Auth & RBAC** | JUnit + Spring Security test | Login flow, role checks | Unauthenticated → 401; Analyst tries write → 403; Manager writes → 200 + audit entry |
| **Analytics Accuracy** | JUnit + Testcontainers | Aggregation queries | Dashboard totals match independent SQL SUM/COUNT; different currencies not aggregated |
| **UI (Karma/Jasmine)** | Angular CLI test runner | Forms, tables, validation | Salary form rejects negative amount; search filters → correct HTTP call; paginator sends page params |
| **E2E (Playwright/Cypress)** | Playwright / Cypress | Full workflows | Login → navigate to directory → search "Engineering" → edit salary → verify dashboard update → export CSV |
| **Performance** | JMeter / k6 (optional MVP) | Pagination, aggregates | 10,000 row page loads < 2s; analytics aggregates < 1s; no N+1 queries via query plan analysis |

### 10.3 Test Data Management
- **Seed script:** Generates deterministic data with known properties (e.g., "Country DE has exactly 847 employees with avg salary €92,500").
- **Test fixtures:** Use factory methods in test code to create Employee/Salary objects inline.
- **Transaction rollback:** All integration tests run within transactional boundaries that roll back after each test.

---

## 11. Seed Data & Demonstration

### 11.1 Synthetic Data Generator (`seed-data/`)
```
seed-data/
├── generate.py                    # Python script generating synthetic data
├── employees.csv                  # Generated CSV (10,000 rows)
├── salaries.csv                   # Generated salary records (variable per employee)
├── currencies.csv                 # Master currency reference data
├── countries.json                 # Country → currency → locale mapping
└── README.md                      # Usage instructions
```

### 11.2 Data Distribution Profile
| Dimension | Target Distribution |
|-----------|-------------------|
| Countries | ~15 countries (US: 35%, UK: 12%, DE: 10%, JP: 8%, FR: 7%, IN: 10%, others: 18%) |
| Departments | Engineering (40%), Sales (15%), Marketing (10%), HR (5%), Finance (10%), Operations (10%), Legal (5%), Other (5%) |
| Job Levels | Junior (25%), Mid (35%), Senior (25%), Lead/Manager (12%), Director (3%) |
| Currencies | Aligned with country distribution; ~8 distinct currencies |
| Salary Range | Country-specific bands: US $60K–$220K, UK £30K–£140K, DE €35K–€130K, JP ¥3M–¥15M, etc. |

### 11.3 Demo Setup Script
```bash
#!/bin/bash
# scripts/demo-setup.sh
docker compose up -d mysql        # Wait for health
docker compose exec api java -jar app.jar --spring.profiles.active=seed
./scripts/generate.py             # Generate + import seed data
echo "Demo ready at http://localhost:4200"
echo "Default credentials: admin / admin123!"
```

---

## 12. Scalability & Performance

### 12.1 Performance Targets (MVP)
| Metric | Target | Rationale |
|--------|--------|-----------|
| Employee list page load | < 2s for 10,000 rows (paginated) | Acceptable UX at scale |
| Analytics aggregate query | < 1s across full dataset | Real-time reporting expectation |
| Salary edit + audit | < 500ms per request | Responsive form interaction |
| Concurrent HR users | 50+ | Small, authorized user base |
| Database disk | ≥ 50 GB (growth buffer) | 10K employees × salary history × payroll data |

### 12.2 Optimization Strategies
- **Pagination:** Server-side only; never fetch full table client-side.
- **Indexed columns:** `country`, `dept`, `job_title`, `employment_status`, `effective_date`.
- **Query optimization:** Use derived/CTE queries for analytics; avoid subquery N+1 patterns.
- **Connection pooling:** HikariCP with pool size tuned to expected concurrency (50–100 connections).
- **N+1 prevention:** EntityGraph / @NamedEntityGraph or JOIN FETCH for employee-detail endpoint.

---

## 13. Risks & Mitigations

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Angular 22 compatibility with JDK 25 | Build fails at setup | Medium | Verify compatibility early; have Angular 17–20 as fallback |
| Spring Boot 4.x not yet GA | Framework instability | High (if pre-release) | Confirm release status; use latest stable (3.x) if 4.x unavailable |
| MySQL 9.7 JDBC driver issues | Connection failures | Medium | Test driver version compatibility early; document workaround |
| Synthetic data not representative | Demo quality suffers | Low | Use realistic distributions; validate against real-world ratios |
| Salary data sensitivity breach | Regulatory compliance violation | Low (if secured) | Strict RBAC, audit trail, no salary in logs/URLs, TLS everywhere |
| 10K rows performance surprises | Slow queries at scale | Low | Profile query plans early; add composite indexes as needed |

---

*Document version: 1.0 — October 13, 2026*
*Based on: ACME Salary Management System — MVP Requirements*
