$cloudflaredExe = "C:\Cloudflared\bin\cloudflared.exe"
$configPath = Join-Path $PSScriptRoot "config.hackerlo.local.yml"
$logPath = Join-Path $PSScriptRoot "securityapp-demo.log"
$errLogPath = Join-Path $PSScriptRoot "securityapp-demo.err.log"

if (!(Test-Path $cloudflaredExe)) {
    Write-Error "cloudflared.exe not found at $cloudflaredExe"
    exit 1
}

if (!(Test-Path $configPath)) {
    Write-Error "Tunnel config not found at $configPath"
    exit 1
}

$existing = Get-CimInstance Win32_Process |
    Where-Object { $_.Name -eq "cloudflared.exe" -and $_.CommandLine -like "*config.hackerlo.local.yml*" }

if ($existing) {
    Write-Output "Tunnel is already running."
    $existing | Select-Object ProcessId, CommandLine
    exit 0
}

$process = Start-Process `
    -FilePath $cloudflaredExe `
    -ArgumentList @("tunnel", "--config", $configPath, "run", "securityapp-demo") `
    -WindowStyle Hidden `
    -RedirectStandardOutput $logPath `
    -RedirectStandardError $errLogPath `
    -PassThru

Start-Sleep -Seconds 3

Write-Output "Started tunnel process."
Write-Output "PID: $($process.Id)"
Write-Output "Log: $logPath"
