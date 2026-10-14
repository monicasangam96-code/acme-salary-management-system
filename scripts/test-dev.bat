@echo off
REM test-dev.bat — Run ACME dev tests with selectable options.
REM Usage:
REM   test-dev.bat              ^&  Run all tests (default)
REM   test-dev.bat --api        ^&  Only backend tests
REM   test-dev.bat --ui         ^&  Only Angular unit (Karma) tests
REM   test-dev.bat --e2e        ^&  Only Playwright E2E tests

setlocal enabledelayedexpiation

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."
set "BACKEND_DIR=%PROJECT_DIR%\code\back-end"
set "FRONTEND_DIR=%PROJECT_DIR%\code\front-end"
set "E2E_BASE_URL=%E2E_BASE_URL:http://localhost:4200%"

set PASSED=0
set FAILED=0
set TOTAL=0

REM --- Help ---
if "%~1"=="--help" (
    echo Usage: test-dev.bat [OPTIONS]
    echo.
    echo Run ACME Salary Management System tests.
    echo.
    echo Options:
    echo   --api       Run only backend tests (Spring Boot unit + integration)
    echo   --ui        Run only frontend UI tests (Angular Karma)
    echo   --e2e       Run only Playwright E2E tests
    echo   --help, -h  Show this help message
    echo.
    echo Without options, runs all test suites in order:
    echo   1. Backend (Spring Boot): unit + integration tests via Maven
    echo   2. UI (Karma): Angular component/unit tests
    echo   3. E2E (Playwright): end-to-end browser tests against running stack
    exit /b 0
)

REM --- Determine which tests to run ---
set RUN_ALL=1
if "%~1"=="--api" ( set "RUN_API=1" ) else if "%~1"=="--ui" ( set "RUN_UI=1" ) else if "%~1"=="--e2e" ( set "RUN_E2E=1" ) else ( set RUN_ALL=1 )

echo.
echo === ACME Salary Management - Dev Test Runner ===
if "%RUN_ALL%"=="1" (
    echo Running all test suites...
) else (
    echo Running selected test suite(s)...
)
echo Base URL: %E2E_BASE_URL%

REM --- Backend tests ---
if "%RUN_ALL%"=="1" set "RUN_BACKEND=1"
if "%~1"=="--api" set "RUN_BACKEND=1"
if defined RUN_BACKEND (
    echo.
    echo ============================================
    echo   Backend Tests (Spring Boot)
    echo ============================================

    if not exist "%BACKEND_DIR%\pom.xml" (
        echo FAIL: Backend directory not found: %BACKEND_DIR%
        goto :summary
    )

    cd /d "%BACKEND_DIR%"
    call mvn test -q >nul 2>&1
    if !errorlevel! equ 0 (
        echo   PASS: Backend: all tests passed
        set /a PASSED+=1
        set /a TOTAL+=1
    ) else (
        echo   FAIL: Backend: some tests failed
        cd /d "%PROJECT_DIR%"
        goto :summary
    )
    cd /d "%PROJECT_DIR%"
)

REM --- UI (Karma) tests ---
if "%RUN_ALL%"=="1" set "RUN_UI_TESTS=1"
if "%~1"=="--ui" set "RUN_UI_TESTS=1"
if defined RUN_UI_TESTS (
    echo.
    echo ============================================
    echo   UI Tests (Angular Karma)
    echo ============================================

    if not exist "%FRONTEND_DIR%\angular.json" (
        echo FAIL: Frontend directory not found: %FRONTEND_DIR%
        goto :summary
    )

    cd /d "%FRONTEND_DIR%"
    call npx ng test --no-progress --browsers ChromeHeadless_NoSandbox >nul 2>&1
    if !errorlevel! equ 0 (
        echo   PASS: UI (Karma): all tests passed
        set /a PASSED+=1
        set /a TOTAL+=1
    ) else (
        echo   FAIL: UI (Karma): some tests failed
        cd /d "%PROJECT_DIR%"
        goto :summary
    )
    cd /d "%PROJECT_DIR%"
)

REM --- E2E (Playwright) tests ---
if "%RUN_ALL%"=="1" set "RUN_E2E_TESTS=1"
if "%~1"=="--e2e" set "RUN_E2E_TESTS=1"
if defined RUN_E2E_TESTS (
    echo.
    echo ============================================
    echo   E2E Tests (Playwright)
    echo ============================================

    curl -sf http://localhost:8080/api/v1/health >nul 2>&1
    if !errorlevel! neq 0 (
        echo   FAIL: Backend API not reachable at http://localhost:8080
        echo   Info: Start the stack first: .\scripts\build-dev.bat
        goto :summary
    )

    cd /d "%FRONTEND_DIR%"
    set E2E_BASE_URL=%E2E_BASE_URL%
    call npx playwright test >nul 2>&1
    if !errorlevel! equ 0 (
        echo   PASS: E2E (Playwright): all tests passed
        set /a PASSED+=1
        set /a TOTAL+=1
    ) else (
        echo   FAIL: E2E (Playwright): some tests failed
        cd /d "%PROJECT_DIR%"
        goto :summary
    )
    cd /d "%PROJECT_DIR%"
)

REM --- Summary ---
echo.
echo ============================================
echo   Test Summary
echo ============================================
echo   Total:  %TOTAL%
echo   Passed: %PASSED%
echo   Failed: %FAILED%
echo.

if %FAILED% gtr 0 (
    exit /b 1
) else (
    exit /b 0
)
