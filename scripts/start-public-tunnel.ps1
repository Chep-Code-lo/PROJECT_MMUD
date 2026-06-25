param(
    [switch]$Quick,
    [string]$PublicBaseUrl
)

$ErrorActionPreference = "Stop"

$composeArgs = @(
    "-f", "docker-compose.yml",
    "-f", "docker-compose.public-domain.yml"
)

if ($Quick) {
    $profile = "quick-tunnel"
    $service = "cloudflared-quick"
    $containerName = "securityapp-cloudflared-quick"
} else {
    $profile = "public-tunnel"
    $service = "cloudflared"
    $containerName = "securityapp-cloudflared"

    if (-not (Test-Path "deploy/cloudflared/config.local.yml")) {
        throw "Missing deploy/cloudflared/config.local.yml. Copy deploy/cloudflared/config.example.yml first."
    }
}

if ($PublicBaseUrl) {
    $env:PUBLIC_BASE_URL = $PublicBaseUrl
}

Write-Host "Starting tunnel profile '$profile'..." -ForegroundColor Cyan
& docker compose @composeArgs --profile $profile up -d $service

Write-Host ""
Write-Host "Tunnel container status:" -ForegroundColor Green
& docker ps --filter "name=$containerName" --format "table {{.Names}}`t{{.Status}}"

Write-Host ""
Write-Host "Recent tunnel logs:" -ForegroundColor Green
& docker logs --tail 40 $containerName

Write-Host ""
if ($Quick) {
    Write-Host "Look for the generated trycloudflare.com URL in the logs above." -ForegroundColor Yellow
} else {
    Write-Host "Open your configured PUBLIC_BASE_URL after the tunnel reports Connected." -ForegroundColor Yellow
}
