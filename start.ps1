# 秒杀商城 - 启动脚本 (PowerShell)

$ROOT_DIR = $PSScriptRoot
$FRONTEND_DIR = Join-Path $ROOT_DIR "seckill-frontend"
$MINIAPP_DIR = Join-Path $ROOT_DIR "seckill-miniapp"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  秒杀商城 - 启动中..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取本机 IP
Write-Host "[1/5] 获取本机 IP..." -ForegroundColor Yellow
$LOCAL_IP = "localhost"
$adapters = Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -notmatch "^127\." -and
    $_.IPAddress -notmatch "^169\." -and
    $_.IPAddress -notmatch "^0\."
}

foreach ($adapter in $adapters) {
    $ip = $adapter.IPAddress
    if ($ip -match "^192\.168\.") {
        $LOCAL_IP = $ip
        break
    } elseif ($ip -match "^10\.") {
        if ($LOCAL_IP -eq "localhost") { $LOCAL_IP = $ip }
    } elseif ($ip -match "^172\.") {
        if ($LOCAL_IP -eq "localhost") { $LOCAL_IP = $ip }
    }
}

Write-Host "  本机 IP: $LOCAL_IP" -ForegroundColor Green

# 更新小程序配置
Write-Host ""
Write-Host "[2/5] 更新小程序配置..." -ForegroundColor Yellow
$configContent = @"
export const API_HOST = 'http://${LOCAL_IP}:8080'
export const APP_NAME = '秒杀商城'
export const DEBUG = true
"@
$configPath = Join-Path $MINIAPP_DIR "src\config.js"
Set-Content -Path $configPath -Value $configContent -Encoding UTF8

# 同步更新 dist 编译输出（微信开发者工具读取的是 dist 目录）
$distConfigPath = Join-Path $MINIAPP_DIR "dist\dev\mp-weixin\config.js"
if (Test-Path $distConfigPath) {
    $distConfigContent = @"
"use strict";
const API_HOST = "http://${LOCAL_IP}:8080";
exports.API_HOST = API_HOST;
"@
    Set-Content -Path $distConfigPath -Value $distConfigContent -Encoding UTF8
    Write-Host "  已同步 dist 目录配置" -ForegroundColor Gray
}
Write-Host "  小程序 API: http://${LOCAL_IP}:8080" -ForegroundColor Green

# 检查端口
Write-Host ""
Write-Host "[3/5] 检查端口..." -ForegroundColor Yellow
$backendPort = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
$frontendPort = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue

if ($backendPort) {
    Write-Host "  [错误] 端口 8080 已被占用" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}
if ($frontendPort) {
    Write-Host "  [错误] 端口 3000 已被占用" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}
Write-Host "  端口检查通过" -ForegroundColor Green

# 启动后端
Write-Host ""
Write-Host "[4/5] 启动后端..." -ForegroundColor Yellow
$backendCmd = "cd /d `"$ROOT_DIR`" ; java -jar target\seckill-mall-1.0.0.jar"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $backendCmd -WindowStyle Hidden
Write-Host "  正在启动后端服务 (端口 8080)..." -ForegroundColor Gray

# 等待后端启动
Write-Host "  等待后端启动..." -ForegroundColor Gray
$timeout = 60
$elapsed = 0
while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    $backendConn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
    if ($backendConn) {
        Write-Host "  后端已启动" -ForegroundColor Green
        break
    }
}

# 启动前端
Write-Host ""
Write-Host "[5/5] 启动前端..." -ForegroundColor Yellow
$frontendCmd = "cd /d `"$FRONTEND_DIR`" ; npm run dev"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $frontendCmd -WindowStyle Hidden
Write-Host "  正在启动前端服务 (端口 3000)..." -ForegroundColor Gray

# 等待前端启动
Write-Host "  等待前端启动..." -ForegroundColor Gray
$elapsed = 0
while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    $frontendConn = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
    if ($frontendConn) {
        Write-Host "  前端已启动" -ForegroundColor Green
        break
    }
}

# 完成
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  访问地址:" -ForegroundColor White
Write-Host "  - PC 前端:    http://localhost:3000" -ForegroundColor White
Write-Host "  - 后端 API:   http://localhost:8080" -ForegroundColor White
Write-Host "  - Swagger:    http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "  小程序配置:" -ForegroundColor White
Write-Host "  - API 地址:   http://${LOCAL_IP}:8080" -ForegroundColor Yellow
Write-Host ""
Write-Host "  测试账号:" -ForegroundColor White
Write-Host "  - 管理员:     admin / admin123" -ForegroundColor White
Write-Host "  - 普通用户:   user1 / 123456" -ForegroundColor White
Write-Host ""
Write-Host "  运行 stop.bat 可停止所有服务" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
