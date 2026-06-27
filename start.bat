@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   Seckill Mall - Starting...
echo ========================================
echo.

:: Get project root directory
set "ROOT_DIR=%~dp0"

:: ==================== Get Local IP ====================
echo [1/6] Detecting local IP...
set "LOCAL_IP="
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /R "IPv4"') do (
    for /f "tokens=1" %%b in ("%%a") do (
        set "IP=%%b"
        if not "!IP:~0,4!"=="127." if not "!IP:~0,4!"=="169." (
            if "!IP:~0,8!"=="192.168." set "LOCAL_IP=!IP!"
            if "!IP:~0,3!"=="10." if not defined LOCAL_IP set "LOCAL_IP=!IP!"
            if "!IP:~0,4!"=="172." if not defined LOCAL_IP set "LOCAL_IP=!IP!"
        )
    )
)
if not defined LOCAL_IP set "LOCAL_IP=localhost"
echo       Local IP: %LOCAL_IP%

:: ==================== Update Miniapp Config ====================
echo.
echo [2/6] Updating miniapp config...

:: Update source config
(
echo export const API_HOST = 'http://%LOCAL_IP%:8080'
echo export const APP_NAME = 'SeckillMall'
echo export const DEBUG = true
) > "%ROOT_DIR%seckill-miniapp\src\config.js"

:: Sync dist config (WeChat devtools reads from dist)
if exist "%ROOT_DIR%seckill-miniapp\dist\dev\mp-weixin\config.js" (
    (
    echo "use strict";
    echo const API_HOST = "http://%LOCAL_IP%:8080";
    echo exports.API_HOST = API_HOST;
    ) > "%ROOT_DIR%seckill-miniapp\dist\dev\mp-weixin\config.js"
    echo       Synced dist config
)

echo       Miniapp API: http://%LOCAL_IP%:8080

:: ==================== Check Ports ====================
echo.
echo [3/6] Checking ports...
netstat -ano | findstr ":8080.*LISTEN" >nul 2>&1
if not errorlevel 1 (
    echo       [ERROR] Port 8080 is in use. Run stop.bat first.
    pause
    exit /b 1
)
netstat -ano | findstr ":3000.*LISTEN" >nul 2>&1
if not errorlevel 1 (
    echo       [ERROR] Port 3000 is in use. Run stop.bat first.
    pause
    exit /b 1
)
netstat -ano | findstr ":5173.*LISTEN" >nul 2>&1
if not errorlevel 1 (
    echo       [ERROR] Port 5173 is in use. Run stop.bat first.
    pause
    exit /b 1
)
echo       Port check passed

:: ==================== Start Backend ====================
echo.
echo [4/6] Starting backend...
cd /d "%ROOT_DIR%"

if not exist "target\seckill-mall-1.0.0.jar" (
    echo       [ERROR] target\seckill-mall-1.0.0.jar not found
    echo       Please run: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo       Starting backend on port 8080...
:: Find javaw in the same directory as java
for /f "tokens=*" %%i in ('where java') do set "JAVA_DIR=%%~dpi"
set "JAVAW=%JAVA_DIR%javaw.exe"
if not exist "%JAVAW%" set "JAVAW=javaw"
powershell -Command "Start-Process -FilePath '%JAVAW%' -ArgumentList '-jar','target\seckill-mall-1.0.0.jar' -WorkingDirectory '%ROOT_DIR%' -WindowStyle Hidden"

echo       Waiting for backend...
:wait_backend
timeout /t 2 /nobreak >nul
netstat -ano | findstr ":8080.*LISTEN" >nul 2>&1
if errorlevel 1 goto wait_backend
echo       Backend started

:: ==================== Start PC Frontend ====================
echo.
echo [5/6] Starting PC frontend...
cd /d "%ROOT_DIR%seckill-frontend"

echo       Starting frontend on port 3000...
powershell -Command "Start-Process -FilePath 'cmd' -ArgumentList '/c','npm run dev' -WorkingDirectory '%ROOT_DIR%seckill-frontend' -WindowStyle Hidden"

echo       Waiting for frontend...
:wait_frontend
timeout /t 2 /nobreak >nul
netstat -ano | findstr ":3000.*LISTEN" >nul 2>&1
if errorlevel 1 goto wait_frontend
echo       Frontend started

:: ==================== Start Miniapp ====================
echo.
echo [6/6] Starting miniapp...
cd /d "%ROOT_DIR%seckill-miniapp"

echo       Starting miniapp on port 5173...
powershell -Command "Start-Process -FilePath 'cmd' -ArgumentList '/c','npm run dev:h5' -WorkingDirectory '%ROOT_DIR%seckill-miniapp' -WindowStyle Hidden"

echo       Waiting for miniapp...
:wait_miniapp
timeout /t 2 /nobreak >nul
netstat -ano | findstr ":5173.*LISTEN" >nul 2>&1
if errorlevel 1 goto wait_miniapp
echo       Miniapp started

:: ==================== Done ====================
echo.
echo ========================================
echo   All services started!
echo ========================================
echo.
echo   Access URLs:
echo   - PC Frontend:  http://localhost:3000
echo   - Miniapp H5:   http://localhost:5173
echo   - Backend API:  http://localhost:8080
echo   - Swagger:      http://localhost:8080/swagger-ui.html
echo.
echo   Miniapp Config:
echo   - API Host:     http://%LOCAL_IP%:8080
echo   - WeChat DevTools: open dist/dev/mp-weixin
echo.
echo   Test Accounts:
echo   - Admin:  admin / admin123
echo   - User:   user1 / 123456
echo.
echo   Run stop.bat to stop all services
echo.
echo ========================================
echo.
pause
