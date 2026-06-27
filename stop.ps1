# 秒杀商城 - 停止脚本 (PowerShell)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  秒杀商城 - 停止中..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定义要停止的端口
$ports = @(8080, 3000, 5173, 5174)
$names = @{"8080"="后端"; "3000"="前端"; "5173"="小程序"; "5174"="小程序"}

foreach ($port in $ports) {
    $name = $names["$port"]
    Write-Host "停止 $name 服务 (端口 $port)..." -ForegroundColor Yellow

    try {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conns) {
            $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
            foreach ($procId in $pids) {
                try {
                    Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
                    Write-Host "  已停止 (PID: $procId)" -ForegroundColor Green
                } catch {
                    Write-Host "  停止失败 (PID: $procId)" -ForegroundColor Red
                }
            }
        } else {
            Write-Host "  未运行" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  未运行" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  所有服务已停止" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
