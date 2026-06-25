$ErrorActionPreference = "SilentlyContinue"

$composeArgs = @(
    "-f", "docker-compose.yml",
    "-f", "docker-compose.public-domain.yml"
)

Write-Host "Stopping public tunnel containers..." -ForegroundColor Cyan
& docker compose @composeArgs stop cloudflared cloudflared-quick | Out-Null
& docker compose @composeArgs rm -sf cloudflared cloudflared-quick | Out-Null

Write-Host "Done." -ForegroundColor Green
