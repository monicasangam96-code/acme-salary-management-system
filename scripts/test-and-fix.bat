@echo off
REM test-and-fix.bat — Full test cycle: run, review, fix, repeat until all pass 3 times.
REM Usage: test-and-fix.bat [--max-cycles N]

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."
set "BACKEND_DIR=%PROJECT_DIR%\code\back-end"
set "FRONTEND_DIR=%PROJECT_DIR%\code\front-end"
set "E2E_BASE_URL=%E2E_BASE_URL:http://localhost:4200%"
set "MAX_CYCLES=10"
set "CONSECUTIVE=0"
set "CYCLE=0"

REM Parse arguments
if "%~1"=="--max-cycles" set "MAX_CYCLES=%2"

echo.
echo ============================================
echo   ACME Salary Management - Test & Fix
echo ============================================
echo   Max cycles:  %MAX_CYCLES%
echo   Base URL:    %E2E_BASE_URL%
echo.

:main_loop
if !CYCLE! geq !MAX_CYCLES! (
    echo.
    echo !!! Reached max cycles (!MAX_CYCICLES!). Manual intervention needed. !!!
    exit /b 1
)

set /a CYCLE+=1
echo.
echo ============================================
echo   Cycle !CYCLE! / !MAX_CYCLES!
echo ============================================

set "HAS_FAILURE=0"
set "BACKEND_OK=0"
set "UI_OK=0"
set "E2E_OK=0"

REM --- Backend tests ---
echo.
echo [BACKEND] Running Spring Boot tests...
cd /d "%BACKEND_DIR%"
call mvn test -q >nul 2>&1
if !errorlevel! equ 0 (
    echo   PASS: Backend: all tests passed
    set "BACKEND_OK=1"
) else (
    echo   FAIL: Backend: some tests failed
    echo   Full output saved to %PROJECT_DIR%\deploy\dev\test-results\backend-cycle-!CYCLE!.log
    cd /d "%BACKEND_DIR%" & mvn test 2>&1 > "%PROJECT_DIR%\deploy\dev\test-results\backend-cycle-!CYCLE!.log"
    set "HAS_FAILURE=1"
)
cd /d "%PROJECT_DIR%"

REM --- UI (Karma) tests ---
echo.
echo [UI] Running Angular Karma tests...
if not exist "%FRONTEND_DIR%\angular.json" (
    echo   FAIL: Frontend directory not found
    set "HAS_FAILURE=1"
    set "UI_OK=0"
) else (
    cd /d "%FRONTEND_DIR%"
    call npx ng test --no-progress --browsers ChromeHeadless_NoSandbox 2>&1 > "%PROJECT_DIR%\deploy\dev\test-results\ui-cycle-!CYCLE!.log"
    if !errorlevel! equ 0 (
        echo   PASS: UI (Karma): all tests passed
        set "UI_OK=1"
    ) else (
        echo   FAIL: UI (Karma): some tests failed
        set "HAS_FAILURE=1"
    )
    cd /d "%PROJECT_DIR%"
)

REM --- E2E (Playwright) tests ---
echo.
echo [E2E] Running Playwright tests...
curl -sf http://localhost:8080/api/v1/health >nul 2>&1
if !errorlevel! neq 0 (
    echo   FAIL: Backend API not reachable at http://localhost:8080
    set "HAS_FAILURE=1"
    set "E2E_OK=0"
) else (
    cd /d "%FRONTEND_DIR%"
    call E2E_BASE_URL=!E2E_BASE_URL! npx playwright test 2>&1 > "%PROJECT_DIR%\deploy\dev\test-results\e2e-cycle-!CYCLE!.log"
    if !errorlevel! equ 0 (
        echo   PASS: E2E (Playwright): all tests passed
        set "E2E_OK=1"
    ) else (
        echo   FAIL: E2E (Playwright): some tests failed
        set "HAS_FAILURE=1"
    )
    cd /d "%PROJECT_DIR%"
)

REM --- Check results ---
if "!HAS_FAILURE!"=="0" (
    set /a CONSECUTIVE+=1
    echo.
    echo *** All tests passed! (!CONSECUTIVE!/3 consecutive) ***

    if !CONSECUTIVE! geq 3 (
        echo.
        echo !!! ALL TESTS PASSING FOR 3+ CYCLES !!!
        echo Stopping: all suites stable.
        exit /b 0
    )
) else (
    set "CONSECUTIVE=0"
    echo.
    echo Cycle !CYCLE!: some tests failed. Next cycle will retry.

    REM Try automatic backend rebuild on failure
    if "!BACKEND_OK!"=="0" (
        echo.
        [INFO] Attempting to rebuild backend...
        cd /d "%BACKEND_DIR%" && call mvn clean compile -q >nul 2>&1 || true
        cd /d "%PROJECT_DIR%"
    )
)

goto main_loop
