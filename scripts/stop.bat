@echo off
REM
REM

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..

echo ==========================================
echo GrowPath Server Stop Script
echo ==========================================
echo.

cd /d "%PROJECT_ROOT%"

set COMPOSE_FILE=docker-compose.yml

REM
docker compose version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set DOCKER_COMPOSE=docker compose
) else (
    set DOCKER_COMPOSE=docker-compose
)

echo [INFO] Остановка контейнеров...
%DOCKER_COMPOSE% -f "%COMPOSE_FILE%" down

echo.
echo [OK] Все контейнеры остановлены
echo.

endlocal
