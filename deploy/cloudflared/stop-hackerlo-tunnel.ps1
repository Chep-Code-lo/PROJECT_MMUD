$processes = Get-CimInstance Win32_Process |
    Where-Object { $_.Name -eq "cloudflared.exe" -and $_.CommandLine -like "*config.hackerlo.local.yml*" }

if (!$processes) {
    Write-Output "No hackerlo tunnel process is running."
    exit 0
}

$processes | ForEach-Object {
    Stop-Process -Id $_.ProcessId -Force
    Write-Output "Stopped PID $($_.ProcessId)"
}
