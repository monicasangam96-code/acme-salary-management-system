@echo off
REM build-dev.bat — Full-stack dev build via Docker Compose
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."
set "COMPOSE_FILE=%PROJECT_DIR%\deploy\dev\docker-compose.yml"
set "ENV_FILE=%PROJECT_DIR%\build\dev\.env"

echo === ACME Salary Management - Dev Build ===
echo.

REM --- Pre-flight checks ---
where docker >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed or not in PATH.
    exit /b 1
)

docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker daemon is not running.
    exit /b 1
)

REM --- Ensure .env exists ---
if not exist "%ENV_FILE%" (
    echo [WARN] %ENV_FILE% not found. Creating with defaults...
    if not exist "%PROJECT_DIR%\build\dev" mkdir "%PROJECT_DIR%\build\dev"
    echo MYSQL_ROOT_PASSWORD=root99* > "%ENV_FILE%"
    echo MYSQL_DATABASE=salary_mgmt >> "%ENV_FILE%"
    echo MYSQL_USER=acme_user >> "%ENV_FILE%"
    echo MYSQL_PASSWORD=acme_pass_2025 >> "%ENV_FILE%"
    echo. >> "%ENV_FILE%"
    echo SPRING_DATASOURCE_URL^=jdbc:mysql://db:3306/salary_mgmt?useSSL^=false^&allowPublicKeyRetrieval^=true^&useUnicode^=true^&characterEncoding^=UTF-8 >> "%ENV_FILE%"
    echo SPRING_DATASOURCE_USERNAME^=\!MYSQL_USER! >> "%ENV_FILE%"
    echo SPRING_DATASOURCE_PASSWORD^=\!MYSQL_PASSWORD! >> "%ENV_FILE%"
    echo SERVER_PORT^=8080 >> "%ENV_FILE%"
    echo. >> "%ENV_FILE%"
    echo NGINX_UPSTREAM^=api:8080 >> "%ENV_FILE%"
    echo   Created default .env. Edit it to customize credentials.
)

REM --- Build and start services ---
echo [1/3] Building images...
docker compose -f "!COMPOSE_FILE!" build

echo.
echo [2/3] Starting containers...
docker compose -f "!COMPOSE_FILE!" up -d

echo.
echo [3/3] Waiting for services to become healthy...

echo   Waiting for MySQL...
set /a wait_count=0
:wait_mysql
if !wait_count! geq 60 (
    echo   MySQL timeout after 60s
    goto :done_waiting
)
docker exec acme-db mysqladmin ping -h 127.0.0.1 --silent >nul 2>&1 || curl -sf http://localhost:3310 >nul 2>&1
if !errorlevel! equ 0 (
    echo   MySQL is ready.
) else (
    timeout /t 1 /nobreak >nul
    set /a wait_count+=1
    goto :wait_mysql
)

echo   Waiting for Backend API...
:wait_api
curl -sf http://localhost:8080/api/v1/health >nul 2>&1
if !errorlevel! equ 0 (
    echo   Backend API is ready.
) else if !wait_count! leq 30 (
    timeout /t 2 /nobreak >nul
) else (
    echo   Backend API timeout after 60s
)

echo   Waiting for Frontend...
curl -sf http://localhost:4200 >nul 2>&1
if !errorlevel! equ 0 (
    echo   Frontend is ready.
) else (
    echo   Frontend may need a moment longer.
)

:done_waiting
echo.
echo === Build Complete ===
echo.
echo Services:
echo   Frontend (Angular): http://localhost:4200
echo   Backend API (Spring Boot): http://localhost:8080
echo   Database (MySQL): localhost:3310
echo.
echo Login credentials:
echo   HR Manager: admin@acme.com / admin123
echo   HR Analyst: hranalyst@acme.com / analyst123
