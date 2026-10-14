#!/usr/bin/env bash
# clean-docker.sh — Stop and remove all ACME dev containers, networks, volumes, and images
set -euo pipefail

PROJECT="acme"
COMPOSE_DIR="$(cd "$(dirname "$0")/.." && pwd)/deploy/dev"
DOCKER_COMPOSE="docker compose -f ${COMPOSE_DIR}/docker-compose.yml"

echo "=== ACME Dev Stack — Clean Docker ==="
echo ""

# --- 1. Down containers & network (keep volumes by default) ---
echo "[1/4] Bringing down services (keeping data volumes)..."
if docker compose -f "${COMPOSE_DIR}/docker-compose.yml" ps --format name 2>/dev/null | grep -qE '^('"${PROJECT}"'-)' ; then
    docker compose -f "${COMPOSE_DIR}/docker-compose.yml" down
else
    echo "  No running containers found."
fi

# --- 2. Optional: remove data volumes (WARNING: destroys all data) ---
echo ""
echo "[2/4] Remove persistent MySQL volume?"
read -r -p "[y/N] " confirm
if [[ "${confirm}" =~ ^[Yy]$ ]]; then
    echo "  Removing mysql-data volume..."
    docker volume rm ${PROJECT}_mysql-data 2>/dev/null || true
    echo "  Volume removed. All database data is lost."
else
    echo "  Skipping volume removal (data preserved)."
fi

# --- 3. Remove dangling images ---
echo ""
echo "[3/4] Removing dangling (untagged) images..."
docker image prune -f --filter "label!=acme.keep" 2>/dev/null || true

# --- 4. Remove by label (if we tag our builds) ---
echo ""
echo "[4/4] Removing ACME-specific images..."
for img in acme-api acme-web acme-db; do
    docker image rm "${img}" 2>/dev/null || true
done

echo ""
echo "=== Done ==="
echo "  Containers : removed (docked down)"
echo "  Networks   : removed"
echo "  Volumes    : $(if [[ "${confirm}" =~ ^[Yy]$ ]]; then echo 'removed'; else echo 'kept'; fi)"
echo "  Images     : dangling cleaned, ACME tagged removed"
echo ""
echo "To fully reset everything, rerun and confirm 'y' at the volume prompt."
