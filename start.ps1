# Seckill Mall - Start Script (PowerShell)

$ROOT_DIR = $PSScriptRoot
$FRONTEND_DIR = Join-Path $ROOT_DIR "seckill-frontend"
$MINIAPP_DIR = Join-Path $ROOT_DIR "seckill-miniapp"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Seckill Mall - Starting..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get local IP
Write-Host "[1/6] Detecting local IP..." -ForegroundColor Yellow
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

Write-Host "  Local IP: $LOCAL_IP" -ForegroundColor Green

# Update miniapp config
Write-Host ""
Write-Host "[2/6] Updating miniapp config..." -ForegroundColor Yellow
$configContent = @"
export const API_HOST = 'http://${LOCAL_IP}:8080'
export const APP_NAME = 'SeckillMall'
export const DEBUG = true
"@
$configPath = Join-Path $MINIAPP_DIR "src\config.js"
Set-Content -Path $configPath -Value $configContent -Encoding UTF8

# Sync dist config
$distConfigPath = Join-Path $MINIAPP_DIR "dist\dev\mp-weixin\config.js"
if (Test-Path $distConfigPath) {
    $distConfigContent = @"
"use strict";
const API_HOST = "http://${LOCAL_IP}:8080";
exports.API_HOST = API_HOST;
"@
    Set-Content -Path $distConfigPath -Value $distConfigContent -Encoding UTF8
    Write-Host "  Synced dist config" -ForegroundColor Gray
}
Write-Host "  Miniapp API: http://${LOCAL_IP}:8080" -ForegroundColor Green

# Check ports
Write-Host ""
Write-Host "[3/6] Checking ports..." -ForegroundColor Yellow
$ports = @(8080, 3000, 5173)
$portOk = $true
foreach ($port in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        Write-Host "  [ERROR] Port $port is in use" -ForegroundColor Red
        $portOk = $false
    }
}
if (-not $portOk) {
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "  Port check passed" -ForegroundColor Green

# Start backend
Write-Host ""
Write-Host "[4/6] Starting backend..." -ForegroundColor Yellow
$jarPath = Join-Path $ROOT_DIR "target\seckill-mall-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "  [ERROR] target\seckill-mall-1.0.0.jar not found" -ForegroundColor Red
    Write-Host "  Please run: mvn clean package -DskipTests" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
# Find javaw in the same directory as java
$javaPath = (Get-Command java -ErrorAction SilentlyContinue).Source
$javawPath = if ($javaPath) { Join-Path (Split-Path $javaPath) "javaw.exe" } else { "javaw" }
if (-not (Test-Path $javawPath)) { $javawPath = "javaw" }
Start-Process -FilePath $javawPath -ArgumentList "-jar","target\seckill-mall-1.0.0.jar" -WorkingDirectory $ROOT_DIR -WindowStyle Hidden
Write-Host "  Starting backend on port 8080..." -ForegroundColor Gray

$timeout = 60
$elapsed = 0
while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    $backendConn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
    if ($backendConn) {
        Write-Host "  Backend started" -ForegroundColor Green
        break
    }
}

# Start frontend
Write-Host ""
Write-Host "[5/6] Starting frontend..." -ForegroundColor Yellow
$frontendCmd = "cd /d `"$FRONTEND_DIR`" ; npm run dev"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $frontendCmd -WindowStyle Hidden
Write-Host "  Starting frontend on port 3000..." -ForegroundColor Gray

$elapsed = 0
while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    $frontendConn = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
    if ($frontendConn) {
        Write-Host "  Frontend started" -ForegroundColor Green
        break
    }
}

# Start miniapp
Write-Host ""
Write-Host "[6/6] Starting miniapp..." -ForegroundColor Yellow
$miniappCmd = "cd /d `"$MINIAPP_DIR`" ; npm run dev:h5"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $miniappCmd -WindowStyle Hidden
Write-Host "  Starting miniapp on port 5173..." -ForegroundColor Gray

$elapsed = 0
while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    $miniappConn = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue
    if ($miniappConn) {
        Write-Host "  Miniapp started" -ForegroundColor Green
        break
    }
}

# Done
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All services started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Access URLs:" -ForegroundColor White
Write-Host "  - PC Frontend:  http://localhost:3000" -ForegroundColor White
Write-Host "  - Miniapp H5:   http://localhost:5173" -ForegroundColor White
Write-Host "  - Backend API:  http://localhost:8080" -ForegroundColor White
Write-Host "  - Swagger:      http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "  Miniapp Config:" -ForegroundColor White
Write-Host "  - API Host:     http://${LOCAL_IP}:8080" -ForegroundColor Yellow
Write-Host "  - WeChat DevTools: open dist/dev/mp-weixin" -ForegroundColor Gray
Write-Host ""
Write-Host "  Test Accounts:" -ForegroundColor White
Write-Host "  - Admin:  admin / admin123" -ForegroundColor White
Write-Host "  - User:   user1 / 123456" -ForegroundColor White
Write-Host ""
Write-Host "  Run stop.ps1 to stop all services" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
