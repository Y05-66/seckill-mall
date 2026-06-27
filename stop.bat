@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   秒杀商城 - 停止中...
echo ========================================
echo.

:: 停止后端
echo [1/2] 停止后端服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set "PID=%%a"
    if not "!PID!"=="" (
        taskkill /F /PID !PID! >nul 2>&1
        echo       已停止后端 (PID: !PID!)
    )
)

:: 停止前端
echo [2/2] 停止前端服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000" ^| findstr "LISTENING"') do (
    set "PID=%%a"
    if not "!PID!"=="" (
        taskkill /F /PID !PID! >nul 2>&1
        echo       已停止前端 (PID: !PID!)
    )
)

:: 停止小程序开发服务器
echo [3/3] 停止小程序开发服务器...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    set "PID=%%a"
    if not "!PID!"=="" (
        taskkill /F /PID !PID! >nul 2>&1
        echo       已停止小程序 (PID: !PID!)
    )
)

echo.
echo ========================================
echo   所有服务已停止
echo ========================================
echo.
pause
