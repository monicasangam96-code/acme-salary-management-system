# Liquibase Changelog Reference

## Purpose

Liquibase manages schema evolution for the ACME Salary Management System across development, staging, and production environments. The `schema.sql` file serves as the baseline DDL for local development; all subsequent changes should be applied via Liquibase changelogs.

## Directory Structure

```
code/db/
├── liquibase/
│   ├── changelog.xml              ← master changelog (entry point)
│   ├── v1.0/
│   │   ├── 001-create-employees.sql
│   │   ├── 002-create-salaries.sql
│   │   ├── 003-create-payroll_records.sql
│   │   ├── 004-create-audit_logs.sql
│   │   ├── 005-create-currencies.sql
│   │   └── 006-create-exchange_rates.sql
│   └── v2.0/
│       └── ...                    ← future versioned changelogs
```

## Master Changelog (`changelog.xml`)

```xml
<databaseChangeLog
    xmlns="http://www.liquibase.com/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.com/xml/ns/dbchangelog
        https://docs.liquibase.com/schemas/dbchangelog/4.20/dbchangelog-4.20.xsd">

    <!-- Baseline: first release matches schema.sql -->
    <includeAll path="v1.0"/>
    <includeAll path="v2.0" relativeToChangelogFile="false"/>
</databaseChangeLog>
```

## Conventions

### 1. Each changelog file = one logical change set

```sql
-- liquibase formatted sql
-- changeset author:1

CREATE TABLE salaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    salary_amount DECIMAL(15,2) NOT NULL CHECK (salary_amount > 0),
    currency_code CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CURRENT' CHECK (status IN ('CURRENT', 'SUPERSEDED', 'PENDING')),
    effective_date DATE NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_salary_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT chk_salary_currency CHECK (LENGTH(currency_code) = 3)
);

CREATE INDEX idx_salary_employee_status ON salaries(employee_id, status);
```

### 2. Naming

- File: `{number}-{description}.sql` (zero-padded to 4 digits)
- Changelog entry: describe the change in past tense ("Create salaries table")
- No SQL comments inside `<sql>` tags — use `-- liquibase formatted sql` header

### 3. Idempotency

Every changelog must be safe to run twice. Use `IF NOT EXISTS`, `DROP TABLE IF EXISTS` before recreate, or preconditions:

```xml
<changeSet id="002" author="acme">
    <preConditions onFail="MARK_RAN">
        <not><tableExists tableName="salaries"/></not>
    </preConditions>
    <sqlFile path="v1.0/002-create-salaries.sql" relativeToChangelogFile="true"/>
</changeset>
```

### 4. Rolling back

Every changeset should include a rollback block:

```xml
<changeSet id="002" author="acme">
    <sqlFile path="v1.0/002-create-salaries.sql" relativeToChangelogFile="true"/>
    <rollback>
        DROP TABLE IF EXISTS salaries;
    </rollback>
</changeset>
```

## Spring Boot Integration

### `application.yml` (production profiles)

```yaml
spring:
  liquibase:
    enabled: true
    changelog-location: classpath:db/liquibase/changelog.xml
    default-schema: public
    drop-first: false  # NEVER true in prod
```

### Local development override (`application-dev.yml`)

```yaml
spring:
  liquibase:
    # Enable for local dev; disable when using schema.sql directly
    enabled: true
```

## Deployment Procedure

1. **Backup**: `mysqldump -u root salary_mgmt > backup_pre_migrate.sql`
2. **Migrate**: Deploy new container image (Liquibase runs on startup)
3. **Verify**: Check API `/actuator/health` and Liquibase changelog table:
   ```sql
   SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC LIMIT 10;
   ```

## Key Constraints to Track

| Constraint | Why It Matters | Changelog Reference |
|-----------|----------------|-------------------|
| `chk_salary_amount` | Must always be > 0 | v1.0/002-create-salaries.sql |
| `fk_salary_employee` | CASCADE delete cascades to payroll_records | v1.0/002-create-salaries.sql |
| `idx_salary_employee_status` | Performance-critical query path | v1.0/002-create-salaries.sql |
| IMMUTABLE audit_logs | Append-only — no UPDATE allowed | v1.0/004-create-audit_logs.sql |

## Never Do These Things

- ❌ Edit rows in `DATABASECHANGELOG` or `DATABASECHANGELOGLOCK`
- ❌ Run `drop-first: true` against any non-local environment
- ❌ Modify a changeset that has been applied to production — create a *new* one instead
- ❌ Use `schema.sql` and Liquibase simultaneously in the same environment

## Related

- [`deploy/dev/docker-compose.yml`](../../deploy/dev/docker-compose.yml) — includes Liquibase via Spring Boot auto-start
- [`code/db/schema.sql`](../schema.sql) — baseline DDL; matches v1.0 changelogs exactly
