#!/usr/bin/env bash
# build-dev.sh — Full-stack dev build via Docker Compose
# Builds backend, frontend, and starts all services.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_DIR}/deploy/dev/docker-compose.yml"
ENV_FILE="${PROJECT_DIR}/build/dev/.env"

echo "=== ACME Salary Management — Dev Build ==="
echo ""

# --- Pre-flight checks ---
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed or not in PATH." >&2
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "ERROR: Docker daemon is not running." >&2
    exit 1
fi

# --- Ensure .env exists ---
if [ ! -f "${ENV_FILE}" ]; then
    echo "[WARN] ${ENV_FILE} not found. Creating with defaults..."
    mkdir -p "$(dirname "${ENV_FILE}")"
    cat > "${ENV_FILE}" << 'ENVEOF'
# ============ Database ============
MYSQL_ROOT_PASSWORD=root99*
MYSQL_DATABASE=salary_mgmt
MYSQL_USER=acme_user
MYSQL_PASSWORD=acme_pass_2025

# ============ Backend ============
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/salary_mgmt?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=\${MYSQL_USER}
SPRING_DATASOURCE_PASSWORD=\${MYSQL_PASSWORD}
SERVER_PORT=8080

# ============ Frontend Proxy ============
NGINX_UPSTREAM=api:8080
ENVEOF
    echo "  Created default .env. Edit it to customize credentials."
fi

# --- Build and start services ---
echo "[1/3] Building images..."
docker compose -f "${COMPOSE_FILE}" build

echo ""
echo "[2/3] Starting containers..."
docker compose -f "${COMPOSE_FILE}" up -d

echo ""
echo "[3/3] Waiting for services to become healthy..."

# Wait for MySQL
echo "  Waiting for MySQL..."
for i in $(seq 1 60); do
    if docker exec acme-db mysqladmin ping -h 127.0.0.1 --silent &>/dev/null; then
        echo "  MySQL is ready."
        break
    fi
    sleep 1
done

# Wait for Backend API
echo "  Waiting for Backend API..."
for i in $(seq 1 30); do
    if curl -sf http://localhost:8080/api/v1/health &>/dev/null; then
        echo "  Backend API is ready."
        break
    fi
    sleep 2
done

# Wait for Frontend
echo "  Waiting for Frontend..."
for i in $(seq 1 30); do
    if curl -sf http://localhost:4200 &>/dev/null; then
        echo "  Frontend is ready."
        break
    fi
    sleep 1
done

echo ""
echo "=== Build Complete ==="
echo ""
echo "Services:"
echo "  Frontend (Angular): http://localhost:4200"
echo "  Backend API (Spring Boot): http://localhost:8080"
echo "  Database (MySQL): localhost:3310 (SSH tunnel via db container)"
echo ""
echo "Login credentials:"
echo "  HR Manager: admin@acme.com / admin123"
echo "  HR Analyst: hranalyst@acme.com / analyst123"
