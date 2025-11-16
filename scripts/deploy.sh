#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================="
echo "GrowPath Server Deployment Script"
echo "Local Development Environment"
echo "=========================================="

if ! command -v docker &> /dev/null; then
    echo "Docker не установлен. Установите Docker и повторите попытку."
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "Docker Compose не установлен. Установите Docker Compose и повторите попытку."
    exit 1
fi

if [ ! -f "$PROJECT_ROOT/.env" ]; then
    echo "Файл .env не найден. Создаю из .env.example..."
    if [ -f "$PROJECT_ROOT/.env.example" ]; then
        cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
        echo "Файл .env создан. Пожалуйста, отредактируйте его перед продолжением."
        echo "Нажмите Enter для продолжения или Ctrl+C для отмены..."
        read
    else
        echo "Файл .env.example не найден. Создайте .env файл вручную."
        exit 1
    fi
fi

cd "$PROJECT_ROOT"

COMPOSE_FILE="docker-compose.yml"

echo "Остановка существующих контейнеров..."
docker-compose -f "$COMPOSE_FILE" down || true

echo "Сборка Docker образов для микросервисов..."
docker-compose -f "$COMPOSE_FILE" build --no-cache

echo "Запуск инфраструктуры (базы данных, Kafka, MinIO, Keycloak)..."
docker-compose -f "$COMPOSE_FILE" up -d trainee-db notification-db keycloak-db zookeeper kafka minio

echo "Ожидание готовности баз данных..."
sleep 15

echo "Запуск Keycloak..."
docker-compose -f "$COMPOSE_FILE" up -d keycloak

echo "Ожидание готовности Keycloak..."
sleep 30

echo "Запуск микросервисов..."
docker-compose -f "$COMPOSE_FILE" up -d trainee-service notification-service

echo "Ожидание готовности микросервисов..."
sleep 20

echo "Запуск API Gateway..."
docker-compose -f "$COMPOSE_FILE" up -d api-gateway

echo "Ожидание готовности всех сервисов..."
sleep 20

echo ""
echo "=========================================="
echo "Развертывание завершено!"
echo "=========================================="
echo ""
echo "Доступные сервисы:"
echo "  - API Gateway:      http://localhost:${API_GATEWAY_PORT:-8080}"
echo "  - Trainee Service:  http://localhost:${TRAINEE_SERVICE_PORT:-8081}"
echo "  - Notification:     http://localhost:${NOTIFICATION_SERVICE_PORT:-8082}"
echo "  - Keycloak:         http://localhost:${KEYCLOAK_PORT:-8090}"
echo "  - MinIO Console:    http://localhost:${MINIO_CONSOLE_PORT:-9001}"
echo ""
echo "Проверка статуса: docker-compose -f $COMPOSE_FILE ps"
echo "Просмотр логов:   docker-compose -f $COMPOSE_FILE logs -f [service-name]"
echo "Остановка:         docker-compose -f $COMPOSE_FILE down"
echo ""
