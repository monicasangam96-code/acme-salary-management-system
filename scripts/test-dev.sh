#!/usr/bin/env bash
# test-dev.sh — Run ACME dev tests (db, api, ui, e2e) with selectable options.
# Usage:
#   ./test-dev.sh              # Run all tests (default)
#   ./test-dev.sh --api        # Only backend tests
#   ./test-dev.sh --ui         # Only Angular unit (Karma) tests
#   ./test-dev.sh --e2e        # Only Playwright E2E tests
#   ./test-dev.sh --help       # Show help
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKEND_DIR="${PROJECT_DIR}/code/back-end"
FRONTEND_DIR="${PROJECT_DIR}/code/front-end"
E2E_BASE_URL="${E2E_BASE_URL:-http://localhost:4200}"

TOTAL=0
PASSED=0
FAILED=0

# Color codes (disabled if not a terminal)
if [ -t 1 ]; then
    GREEN='\033[0;32m'
    RED='\033[0;31m'
    YELLOW='\033[1;33m'
    NC='\033[0m'
else
    GREEN=''
    RED=''
    YELLOW=''
    NC=''
fi

pass() { echo -e "${GREEN}  PASS${NC}: $1"; ((PASSED++)); ((TOTAL++)); }
fail() { echo -e "${RED}FAIL${NC}: $1"; ((FAILED++)); ((TOTAL++)); }
info() { echo -e "${YELLOW}[INFO]${NC} $1"; }

# --- Help ---
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    cat << 'EOF'
Usage: ./test-dev.sh [OPTIONS]

Run ACME Salary Management System tests.

Options:
  --api       Run only backend tests (Spring Boot unit + integration)
  --ui        Run only frontend UI tests (Angular Karma)
  --e2e       Run only Playwright E2E tests
  --help, -h  Show this help message

Without options, runs all test suites in order:
  1. Backend (Spring Boot): unit + integration tests via Maven
  2. UI (Karma): Angular component/unit tests
  3. E2E (Playwright): end-to-end browser tests against running stack

Environment variables:
  E2E_BASE_URL   Base URL for E2E tests (default: http://localhost:4200)

Exit codes:
  0    All tests passed
  1    One or more tests failed
EOF
    exit 0
fi

# --- Determine which tests to run ---
RUN_ALL=true
if [[ $# -gt 0 ]]; then
    RUN_ALL=false
    for arg in "$@"; do
        case "$arg" in
            --api) RUN_API=true ;;
            --ui)  RUN_UI=true ;;
            --e2e) RUN_E2E=true ;;
            *)     echo "Unknown option: $arg"; exit 1 ;;
        esac
    done
    if [[ "$RUN_ALL" == "false" ]]; then
        # Default to all if no specific test flags given (beyond --help)
        true
    fi
fi

# --- Backend tests ---
run_backend() {
    echo ""
    echo "============================================"
    echo "  Backend Tests (Spring Boot)"
    echo "============================================"

    if [ ! -f "${BACKEND_DIR}/pom.xml" ]; then
        fail "Backend directory not found: ${BACKEND_DIR}"
        return 1
    fi

    cd "${BACKEND_DIR}"
    mvn test -q 2>&1 | tail -5

    # Check Maven exit code by running it directly
    if mvn test -q > /dev/null 2>&1; then
        pass "Backend: all tests passed"
        return 0
    else
        fail "Backend: some tests failed — check output above"
        return 1
    fi
}

# --- UI (Karma) tests ---
run_ui() {
    echo ""
    echo "============================================"
    echo "  UI Tests (Angular Karma)"
    echo "============================================"

    if [ ! -f "${FRONTEND_DIR}/angular.json" ]; then
        fail "Frontend directory not found: ${FRONTEND_DIR}"
        return 1
    fi

    cd "${FRONTEND_DIR}"

    if npx ng test --no-progress --browsers ChromeHeadless_NoSandbox > /dev/null 2>&1; then
        pass "UI (Karma): all tests passed"
        return 0
    else
        fail "UI (Karma): some tests failed — check output above"
        return 1
    fi
}

# --- E2E (Playwright) tests ---
run_e2e() {
    echo ""
    echo "============================================"
    echo "  E2E Tests (Playwright)"
    echo "============================================"

    if [ ! -d "${FRONTEND_DIR}/node_modules/@playwright" ]; then
        fail "Playwright not installed. Run: cd code/front-end && npm install"
        return 1
    fi

    # Check that services are running
    if ! curl -sf http://localhost:8080/api/v1/health > /dev/null 2>&1; then
        fail "Backend API not reachable at http://localhost:8080"
        info "Start the stack first: ./scripts/build-dev.sh"
        return 1
    fi

    cd "${FRONTEND_DIR}"

    if E2E_BASE_URL="${E2E_BASE_URL}" npx playwright test > /dev/null 2>&1; then
        pass "E2E (Playwright): all tests passed"
        return 0
    else
        fail "E2E (Playwright): some tests failed — check output above"
        return 1
    fi
}

# --- Main ---
echo ""
echo "=== ACME Salary Management — Dev Test Runner ==="
if [[ "$RUN_ALL" == "true" ]]; then
    echo "Running all test suites..."
else
    echo "Running selected test suite(s)..."
fi
echo "Base URL: ${E2E_BASE_URL}"

# --- Execute based on flags ---
if [[ "$RUN_ALL" == "true" || "$1" == "--api" ]]; then
    run_backend || true
fi
if [[ "$RUN_ALL" == "true" || "$1" == "--ui" ]]; then
    run_ui || true
fi
if [[ "$RUN_ALL" == "true" || "$1" == "--e2e" ]]; then
    run_e2e || true
fi

# --- Summary ---
echo ""
echo "============================================"
echo "  Test Summary"
echo "============================================"
echo -e "  Total:  ${TOTAL}"
echo -e "  Passed: ${GREEN}${PASSED}${NC}"
if [ "$FAILED" -gt 0 ]; then
    echo -e "  Failed: ${RED}${FAILED}${NC}"
else
    echo -e "  Failed: ${FAILED}"
fi
echo ""

if [ "$FAILED" -gt 0 ]; then
    exit 1
else
    exit 0
fi
