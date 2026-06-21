#!/bin/sh
set -eu

SSL_DIR="/etc/nginx/ssl"
FULLCHAIN="$SSL_DIR/fullchain.pem"
PRIVKEY="$SSL_DIR/privkey.pem"

mkdir -p "$SSL_DIR"

if [ ! -f "$FULLCHAIN" ] || [ ! -f "$PRIVKEY" ]; then
  echo "Generating local self-signed certificate for HTTPS..."
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout "$PRIVKEY" \
    -out "$FULLCHAIN" \
    -subj "/CN=localhost" \
    -addext "subjectAltName=DNS:localhost,DNS:host.docker.internal,DNS:nginx,DNS:securityapp-nginx,IP:127.0.0.1"
fi
