# Seckill Mall - Stop Script (PowerShell)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Seckill Mall - Stopping..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Define ports to stop
$ports = @(8080, 3000, 5173)
$names = @{"8080"="Backend"; "3000"="Frontend"; "5173"="Miniapp"}

foreach ($port in $ports) {
    $name = $names["$port"]
    Write-Host "Stopping $name (port $port)..." -ForegroundColor Yellow

    try {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conns) {
            $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
            foreach ($procId in $pids) {
                try {
                    Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
                    Write-Host "  Stopped (PID: $procId)" -ForegroundColor Green
                } catch {
                    Write-Host "  Failed to stop (PID: $procId)" -ForegroundColor Red
                }
            }
        } else {
            Write-Host "  Not running" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  Not running" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All services stopped" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
