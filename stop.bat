@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   Seckill Mall - Stopping...
echo ========================================
echo.

:: Stop backend
echo [1/3] Stopping backend...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set "PID=%%a"
    if not "!PID!"=="" (
        taskkill /F /PID !PID! >nul 2>&1
        echo       Backend stopped (PID: !PID!)
    )
)

:: Stop frontend
echo [2/3] Stopping frontend...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000" ^| findstr "LISTENING"') do (
    set "PID=%%a"
    if not "!PID!"=="" (
        taskkill /F /PID !PID! >nul 2>&1
        echo       Frontend stopped (PID: !PID!)
    )
)

:: Stop miniapp
echo [3/3] Stopping miniapp...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    set "PID=%%a"
    if not "!PID!"=="" (
        taskkill /F /PID !PID! >nul 2>&1
        echo       Miniapp stopped (PID: !PID!)
    )
)

echo.
echo ========================================
echo   All services stopped
echo ========================================
echo.
pause
