@echo off
REM clean-docker.bat — Stop and remove all ACME dev containers, networks, volumes, and images
setlocal enabledelayedexpansion

set "COMPOSE_FILE=%~dp0..\deploy\dev\docker-compose.yml"

echo === ACME Dev Stack - Clean Docker ===
echo.

REM --- 1. Down containers & network (keep volumes by default) ---
echo [1/4] Bringing down services (keeping data volumes)...
docker compose -f "%COMPOSE_FILE%" down 2>nul || echo   No running containers found or docker not available.

REM --- 2. Remove persistent volume ---
echo.
echo [2/4] Remove persistent MySQL volume?
set /p confirm="Type 'y' to remove (WARNING: destroys all data): "
if /i "!confirm!"=="y" (
    echo   Removing mysql-data volume...
    docker volume rm acme_mysql-data 2>nul || echo   Volume not found (already removed or doesn't exist).
    echo   Volume removed. All database data is lost.
) else (
    echo   Skipping volume removal (data preserved).
)

REM --- 3. Remove dangling images ---
echo.
echo [3/4] Removing dangling (untagged) images...
docker image prune -f 2>nul || true

REM --- 4. Remove ACME-specific images ---
echo.
echo [4/4] Removing ACME-specific images...
docker image rm acme-api 2>nul || true
docker image rm acme-web 2>nul || true
docker image rm acme-db 2>nul || true

echo.
echo === Done ===
if /i "!confirm!"=="y" (
    echo   Volumes: removed
) else (
    echo   Volumes: kept
)
echo   Containers: removed
echo   Networks: removed
echo.
echo To fully reset everything, rerun and confirm 'y' at the volume prompt.
