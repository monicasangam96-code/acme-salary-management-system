# ACME Salary Management — Operational Scripts

Scripts for building, testing, and deploying the ACME dev stack.

## Available Scripts

| Script | Bash (.sh) | Batch (.bat) | Description |
|--------|-----------|-------------|-------------|
| **clean-docker** | `clean-docker.sh` | `clean-docker.bat` | Stop/remove containers, networks, volumes (with prompt to keep data), and dangling images |
| **build-dev** | `build-dev.sh` | `build-dev.bat` | Full-stack dev build: builds images, starts services, waits for health checks |
| **test-dev** | `test-dev.sh` | `test-dev.bat` | Run tests with selective options: `--api`, `--ui`, `--e2e`, or all (default) |
| **test-and-fix** | `test-and-fix.sh` | `test-and-fix.bat` | Full test cycle: run → review → fix → repeat until all pass 3 consecutive times |

## Usage

### Linux / macOS / WSL / Git Bash

```bash
# Build and start the stack
./scripts/build-dev.sh

# Run all tests (backend + UI + E2E)
./scripts/test-dev.sh

# Run only specific test suites
./scripts/test-dev.sh --api    # Backend only
./scripts/test-dev.sh --ui     # Angular Karma only
./scripts/test-dev.sh --e2e    # Playwright E2E only

# Full test-and-fix cycle (repeats until stable)
./scripts/test-and-fix.sh                    # default: 10 max cycles
./scripts/test-and-fix.sh --max-cycles 5     # custom limit

# Clean up everything
./scripts/clean-docker.sh   # interactive (confirms volume removal)
```

### Windows (Command Prompt)

```cmd
REM Build and start the stack
.\scripts\build-dev.bat

REM Run all tests
.\scripts\test-dev.bat

REM Selective test suites
.\scripts\test-dev.bat --api
.\scripts\test-dev.bat --ui
.\scripts\test-dev.bat --e2e

REM Full test-and-fix cycle
.\scripts\test-and-fix.bat                    ^&  REM 10 max cycles default
.\scripts\test-and-fix.bat --max-cycles 5     ^&  REM custom limit

REM Clean up everything
.\scripts\clean-docker.bat
```

### Windows (PowerShell)

```powershell
# Build and start the stack
& ".\scripts\build-dev.ps1"   # if available, or use .bat via pwsh

# Or simply run batch files directly:
& ".\scripts\test-dev.bat" --api
```

## Test Suites Detail

### Backend (Spring Boot)
- Unit tests + Integration tests via Maven (`mvn test`)
- All controller, service, and repository layer tests
- Passes when `mvn test` reports 0 failures

### UI / Angular Karma
- Component unit tests + Karma runner
- Headless Chrome browser for CI/fresh environments
- Passes when all components render without errors

### E2E (Playwright)
- Browser-level tests against running stack
- Tests: login, navigation, authentication, routing, logout
- Requires `http://localhost:8080` (backend) and `http://localhost:4200` (frontend) to be healthy
- Passes when all 6+ scenarios pass 3 consecutive times

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `E2E_BASE_URL` | `http://localhost:4200` | Base URL for E2E test suite |

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | All tests passed |
| 1 | One or more tests failed |
