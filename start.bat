@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   秒杀商城 - 启动中...
echo ========================================
echo.

:: 获取项目根目录
set "ROOT_DIR=%~dp0"

:: ==================== 获取本机 IP ====================
echo [1/5] 获取本机 IP...
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
echo       本机 IP: %LOCAL_IP%

:: ==================== 更新小程序配置 ====================
echo.
echo [2/5] 更新小程序配置...

:: 更新源码配置
(
echo export const API_HOST = 'http://%LOCAL_IP%:8080'
echo export const APP_NAME = '秒杀商城'
echo export const DEBUG = true
) > "%ROOT_DIR%seckill-miniapp\src\config.js"

:: 同步更新 dist 编译输出（微信开发者工具读取的是 dist 目录）
if exist "%ROOT_DIR%seckill-miniapp\dist\dev\mp-weixin\config.js" (
    (
    echo "use strict";
    echo const API_HOST = "http://%LOCAL_IP%:8080";
    echo exports.API_HOST = API_HOST;
    ) > "%ROOT_DIR%seckill-miniapp\dist\dev\mp-weixin\config.js"
    echo       已同步 dist 目录配置
)

echo       小程序 API: http://%LOCAL_IP%:8080

:: ==================== 检查端口 ====================
echo.
echo [3/5] 检查端口...
netstat -ano | findstr ":8080.*LISTEN" >nul 2>&1
if not errorlevel 1 (
    echo       [错误] 端口 8080 已被占用，请先运行 stop.bat
    pause
    exit /b 1
)
netstat -ano | findstr ":3000.*LISTEN" >nul 2>&1
if not errorlevel 1 (
    echo       [错误] 端口 3000 已被占用，请先运行 stop.bat
    pause
    exit /b 1
)
echo       端口检查通过

:: ==================== 启动后端 ====================
echo.
echo [4/5] 启动后端...
cd /d "%ROOT_DIR%"

:: 检查 jar 文件是否存在
if not exist "target\seckill-mall-1.0.0.jar" (
    echo       [错误] 未找到 target\seckill-mall-1.0.0.jar
    echo       请先运行 mvn clean package -DskipTests
    pause
    exit /b 1
)

:: 使用 start 命令启动后端（后台运行）
echo       正在启动后端服务 (端口 8080)...
start "秒杀商城-后端" /min cmd /c "cd /d "%ROOT_DIR%" && java -jar target\seckill-mall-1.0.0.jar"

:: 等待后端启动
echo       等待后端启动...
:wait_backend
timeout /t 2 /nobreak >nul
netstat -ano | findstr ":8080.*LISTEN" >nul 2>&1
if errorlevel 1 goto wait_backend
echo       后端已启动

:: ==================== 启动前端 ====================
echo.
echo [5/5] 启动前端...
cd /d "%ROOT_DIR%seckill-frontend"

:: 使用 start 命令启动前端（后台运行）
echo       正在启动前端服务 (端口 3000)...
start "秒杀商城-前端" /min cmd /c "cd /d "%ROOT_DIR%seckill-frontend" && npm run dev"

:: 等待前端启动
echo       等待前端启动...
:wait_frontend
timeout /t 2 /nobreak >nul
netstat -ano | findstr ":3000.*LISTEN" >nul 2>&1
if errorlevel 1 goto wait_frontend
echo       前端已启动

:: ==================== 完成 ====================
echo.
echo ========================================
echo   启动完成！
echo ========================================
echo.
echo   访问地址:
echo   - PC 前端:    http://localhost:3000
echo   - 后端 API:   http://localhost:8080
echo   - Swagger:    http://localhost:8080/swagger-ui.html
echo.
echo   小程序配置:
echo   - API 地址:   http://%LOCAL_IP%:8080
echo.
echo   测试账号:
echo   - 管理员:     admin / admin123
echo   - 普通用户:   user1 / 123456
echo.
echo   运行 stop.bat 可停止所有服务
echo.
echo ========================================
echo.
pause
