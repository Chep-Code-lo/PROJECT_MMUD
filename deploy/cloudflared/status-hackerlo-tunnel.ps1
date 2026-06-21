$logPath = Join-Path $PSScriptRoot "securityapp-demo.log"
$errLogPath = Join-Path $PSScriptRoot "securityapp-demo.err.log"

$processes = Get-CimInstance Win32_Process |
    Where-Object { $_.Name -eq "cloudflared.exe" -and $_.CommandLine -like "*config.hackerlo.local.yml*" } |
    Select-Object ProcessId, CreationDate, CommandLine

if ($processes) {
    Write-Output "Tunnel process:"
    $processes
} else {
    Write-Output "No hackerlo tunnel process is running."
}

if (Test-Path $logPath) {
    Write-Output ""
    Write-Output "Last tunnel log lines:"
    Get-Content $logPath -Tail 20
}

if (Test-Path $errLogPath) {
    Write-Output ""
    Write-Output "Last tunnel error log lines:"
    Get-Content $errLogPath -Tail 20
}
