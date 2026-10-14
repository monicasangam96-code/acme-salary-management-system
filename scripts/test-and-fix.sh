#!/usr/bin/env bash
# test-and-fix.sh — Full test cycle: run, review, fix, repeat until all pass 3 times.
# Usage: ./test-and-fix.sh [--max-cycles N]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKEND_DIR="${PROJECT_DIR}/code/back-end"
FRONTEND_DIR="${PROJECT_DIR}/code/front-end"
E2E_BASE_URL="${E2E_BASE_URL:-http://localhost:4200}"

MAX_CYCLES=10
CONSECUTIVE=0
CYCLE=0

# Color codes (disabled if not a terminal)
if [ -t 1 ]; then
    GREEN='\033[0;32m'
    RED='\033[0;31m'
    YELLOW='\033[1;33m'
    CYAN='\033[0;36m'
    BOLD='\033[1m'
    NC='\033[0m'
else
    GREEN='' RED='' YELLOW='' CYAN='' BOLD='' NC=''
fi

pass() { echo -e "${GREEN}  PASS${NC}: $1"; ((TOTAL++)); }
fail() { echo -e "${RED}FAIL${NC}: $1"; ((TOTAL++)); }
info() { echo -e "${CYAN}[INFO]${NC} $1"; }
step() { echo -e "${BOLD}[$(printf '%02d' $((CYCLE+1)))][$1]${NC} $2"; }

# Parse arguments
for arg in "$@"; do
    case "$arg" in
        --max-cycles) MAX_CYCLES="$2"; shift ;;
    esac
    shift
done

run_backend() {
    step "BACKEND" "Running Spring Boot tests..."
    local output
    output=$(cd "${BACKEND_DIR}" && mvn test 2>&1) || true
    local count
    count=$(echo "$output" | grep -cP 'Tests run:.*Failures: 0' || echo "0")
    if echo "$output" | grep -qP 'BUILD SUCCESS'; then
        pass "Backend (${count} test classes)"
        echo "$output" | tail -3
        return 0
    else
        fail "Backend: BUILD FAILED"
        # Extract key error lines
        echo "$output" | grep -iE '(FAILED|error|Caused by|Expected|Actual)' | head -5
        info "Full output saved to test-results/backend-cycle-$((CYCLE+1)).log"
        echo "$output" > "${PROJECT_DIR}/deploy/dev/test-results/backend-cycle-$((CYCLE+1)).log"
        return 1
    fi
}

run_ui() {
    step "UI" "Running Angular Karma tests..."
    local output
    output=$(cd "${FRONTEND_DIR}" && npx ng test --no-progress --browsers ChromeHeadless_NoSandbox 2>&1) || true

    if echo "$output" | grep -qE '(Total|COMPLETE)' | head -1; then
        # Check for failures in karma output
        local failed
        failed=$(echo "$output" | grep -ci 'FAILED' || echo "0")
        if [ "$failed" -eq 0 ]; then
            pass "UI (Karma)"
            echo "$output" | tail -3
            return 0
        fi
    fi

    fail "UI (Karma): $failed test(s) failed"
    info "Full output saved to test-results/ui-cycle-$((CYCLE+1)).log"
    echo "$output" > "${PROJECT_DIR}/deploy/dev/test-results/ui-cycle-$((CYCLE+1)).log"
    return 1
}

run_e2e() {
    step "E2E" "Running Playwright tests..."

    if ! curl -sf http://localhost:8080/api/v1/health > /dev/null 2>&1; then
        fail "E2E: Backend not reachable"
        return 1
    fi

    local output
    output=$(cd "${FRONTEND_DIR}" && E2E_BASE_URL="${E2E_BASE_URL}" npx playwright test 2>&1) || true

    if echo "$output" | grep -qP '\d+ passed'; then
        # Count failures
        local failed
        failed=$(echo "$output" | grep -cP '\d+ failed' || echo "0")
        if [ "$failed" -eq 0 ]; then
            pass "E2E (Playwright)"
            echo "$output" | tail -3
            return 0
        fi
    fi

    fail "E2E: test failures detected"
    info "Full output saved to test-results/e2e-cycle-$((CYCLE+1)).log"
    echo "$output" > "${PROJECT_DIR}/deploy/dev/test-results/e2e-cycle-$((CYCLE+1)).log"
    return 1
}

# Track known fix patterns for automatic remediation
try_automatic_fixes() {
    local failures=("$@")

    # Check for common issues and attempt fixes
    if [[ " ${failures[*]} " =~ "Backend: BUILD FAILED" ]]; then
        info "Attempting to rebuild backend..."
        cd "${BACKEND_DIR}" && mvn clean compile -q > /dev/null 2>&1 || true
        return 0
    fi

    # More fix patterns can be added here as issues are discovered

    return 1
}

echo ""
echo "============================================"
echo -e "  ${BOLD}ACME Salary Management — Test & Fix${NC}"
echo "============================================"
echo "  Max cycles:  ${MAX_CYCLES}"
echo "  Base URL:    ${E2E_BASE_URL}"
echo ""

# --- Main loop ---
BEST_SCORE=0
BEST_CYCLE=0

while [ "$CYCLE" -lt "$MAX_CYCLES" ]; do
    CYCLE=$((CYCLE + 1))

    echo ""
    echo "============================================"
    echo -e "  ${BOLD}Cycle $CYCLE / $MAX_CYCLES${NC}"
    echo "============================================"

    TOTAL=0
    HAS_FAILURE=false
    FAILED_TESTS=()

    # Run each test suite
    run_backend || { HAS_FAILURE=true; FAILED_TESTS+=("backend"); }
    run_ui || { HAS_FAILURE=true; FAILED_TESTS+=("ui"); }
    run_e2e || { HAS_FAILURE=true; FAILED_TESTS+=("e2e"); }

    local_pass=$TOTAL

    if [ "$HAS_FAILURE" = false ]; then
        CONSECUTIVE=$((CONSECUTIVE + 1))
        echo -e "\n${GREEN}${BOLD}✓ All tests passed! ($CONSECUTIVE=$3 consecutive)${NC}"

        if [ "$CONSECUTIVE" -ge 3 ]; then
            echo -e "${GREEN}${BOLD}\n=== ALL TESTS PASSING FOR 3+ CYCLES ==="
            info "Stopping: all suites stable."
            exit 0
        fi
    else
        CONSECUTIVE=0

        # Try automatic fixes for known patterns
        info "Attempting automatic fixes for: ${FAILED_TESTS[*]}"
        try_automatic_fixes "${FAILED_TESTS[@]}" || true

        echo -e "\n${RED}${BOLD}Cycle $CYCLE: some tests failed. Next cycle will retry.${NC}"
    fi

done

echo ""
echo -e "${RED}${BOLD}Reached max cycles ($MAX_CYCLES). Manual intervention needed.${NC}"
exit 1
