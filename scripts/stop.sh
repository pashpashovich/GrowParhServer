#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================="
echo "GrowPath Server Stop Script"
echo "=========================================="

cd "$PROJECT_ROOT"

COMPOSE_FILE="docker-compose.yml"

echo "Остановка контейнеров..."
docker-compose -f "$COMPOSE_FILE" down

echo ""
echo "Все контейнеры остановлены"
echo ""
