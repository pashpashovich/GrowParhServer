@echo off
REM
REM

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..

echo ==========================================
echo GrowPath Server Deployment Script
echo Local Development Environment
echo ==========================================
echo.

REM
where docker >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker не установлен. Установите Docker и повторите попытку.
    exit /b 1
)

REM
docker compose version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    docker-compose version >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Docker Compose не установлен. Установите Docker Compose и повторите попытку.
        exit /b 1
    )
    set DOCKER_COMPOSE=docker-compose
) else (
    set DOCKER_COMPOSE=docker compose
)

REM
if not exist "%PROJECT_ROOT%\.env" (
    echo [WARNING] Файл .env не найден. Создаю из .env.example...
    if exist "%PROJECT_ROOT%\.env.example" (
        copy "%PROJECT_ROOT%\.env.example" "%PROJECT_ROOT%\.env" >nul
        echo [OK] Файл .env создан. Пожалуйста, отредактируйте его перед продолжением.
        pause
    ) else (
        echo [ERROR] Файл .env.example не найден. Создайте .env файл вручную.
        exit /b 1
    )
)

cd /d "%PROJECT_ROOT%"

set COMPOSE_FILE=docker-compose.yml

REM
echo [INFO] Остановка существующих контейнеров...
%DOCKER_COMPOSE% -f "%COMPOSE_FILE%" down

REM
echo [INFO] Сборка Docker образов для микросервисов...
%DOCKER_COMPOSE% -f "%COMPOSE_FILE%" build --no-cache

REM
echo [INFO] Запуск инфраструктуры (базы данных, Kafka, MinIO)...
%DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d trainee-db notification-db keycloak-db zookeeper kafka minio

REM
echo [INFO] Ожидание готовности баз данных...
timeout /t 15 /nobreak >nul

REM
echo [INFO] Запуск Keycloak...
%DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d keycloak

REM
echo [INFO] Ожидание готовности Keycloak...
timeout /t 30 /nobreak >nul

REM
echo [INFO] Запуск микросервисов...
%DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d trainee-service notification-service

REM
echo [INFO] Ожидание готовности микросервисов...
timeout /t 20 /nobreak >nul

REM
echo [INFO] Запуск API Gateway...
%DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d api-gateway

REM
echo [INFO] Ожидание готовности всех сервисов...
timeout /t 20 /nobreak >nul

echo.
echo ==========================================
echo [OK] Развертывание завершено!
echo ==========================================
echo.
echo Доступные сервисы:
echo   - API Gateway:      http://localhost:8080
echo   - Trainee Service:  http://localhost:8081
echo   - Notification:     http://localhost:8082
echo   - Keycloak:         http://localhost:8090
echo   - MinIO Console:    http://localhost:9001
echo.
echo Проверка статуса: %DOCKER_COMPOSE% -f %COMPOSE_FILE% ps
echo Просмотр логов:   %DOCKER_COMPOSE% -f %COMPOSE_FILE% logs -f [service-name]
echo Остановка:         %DOCKER_COMPOSE% -f %COMPOSE_FILE% down
echo.

endlocal
