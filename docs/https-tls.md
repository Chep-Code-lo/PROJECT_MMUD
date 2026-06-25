# Hướng dẫn HTTPS / TLS

Project này ưu tiên demo HTTPS/TLS theo cách: **Nginx reverse proxy terminate TLS**, sau đó chuyển tiếp request tới frontend và backend trong mạng Docker nội bộ.

## 1. Vì sao cần TLS

- Bảo vệ JWT, mật khẩu và dữ liệu API khi truyền trên mạng
- Giảm nguy cơ lộ nội dung request và response nếu chỉ dùng HTTP
- Tạo môi trường phù hợp để kiểm thử bảo mật API qua `https://localhost`

## 2. Các file liên quan

- `deploy/nginx/securityapp.conf`
- `deploy/ssl/fullchain.pem`
- `deploy/ssl/privkey.pem`
- `docker-compose.yml`

## 3. Tạo certificate self-signed cho local

Nếu cần tạo mới certificate local:

```powershell
openssl req -x509 -nodes -days 365 -newkey rsa:2048 `
  -keyout deploy/ssl/privkey.pem `
  -out deploy/ssl/fullchain.pem `
  -subj "/CN=localhost"
```

Nếu không có `openssl`, có thể dùng:

- `mkcert`
- certificate nội bộ của công ty, phòng lab hoặc giảng viên cung cấp

## 4. Cách chạy demo local qua HTTPS

### Bước 1: Tạo file môi trường

```powershell
Copy-Item .env.example .env
```

### Bước 2: Khởi động hệ thống

```powershell
docker compose up --build -d
```

### Bước 3: Truy cập các địa chỉ chính

- `https://localhost`
- `https://localhost/api/health`
- `https://localhost/swagger-ui.html` trên chính máy host

## 5. Cấu trúc cổng đang dùng

- Cổng `443`: phục vụ frontend, API và Swagger
- Cổng `80`: chỉ dùng để chuyển hướng sang HTTPS

Ý nghĩa:

- API và giao diện người dùng đi qua HTTPS bình thường
- Swagger tránh lỗi lệch cổng khi thử trực tiếp trên UI
- Tài liệu API vẫn không bị lộ ra hostname/IP public

## 6. Cách chứng minh HTTP bị ép sang HTTPS

Chạy:

```powershell
curl.exe -I http://localhost/api/health
curl.exe -k https://localhost/api/health
```

Kỳ vọng:

- lệnh HTTP trả `301`
- lệnh HTTPS trả `200`

## 7. Lưu ý với Swagger

Swagger/OpenAPI dùng cùng cổng `443` tại:

- `https://localhost/swagger-ui.html`
- `https://localhost/v3/api-docs`
- `https://host.docker.internal/swagger-ui.html` nếu truy cập từ container trên cùng máy host

Nếu bạn bật Cloudflare Tunnel hoặc reverse proxy public, Swagger cũng có thể mở qua domain public. Tuy nhiên khi kiểm thử nội bộ vẫn nên ưu tiên hostname local để tránh nhiễu từ mạng ngoài. Có thể tự kiểm tra nhanh bằng:

```powershell
curl.exe -k -I https://localhost/swagger-ui.html -H "Host: demo-public.example"
curl.exe -k -I https://localhost/v3/api-docs -H "Host: demo-public.example"
```

Kỳ vọng:

- trả `404`

## 8. Khi triển khai Internet

- Nên dùng reverse proxy có certificate hợp lệ, ví dụ `Nginx + Let's Encrypt`
- Hoặc đặt sau cloud load balancer / reverse proxy của nhà cung cấp
- Không nên public backend HTTP thuần ra Internet

## 9. Tùy chọn public domain qua tunnel

Nếu cần cho người bên ngoài truy cập mà không mở port public trực tiếp trên router hoặc VPS, project đã bổ sung cấu hình tunnel riêng:

- `docker-compose.public-domain.yml`
- `deploy/cloudflared/config.example.yml`
- `scripts/start-public-tunnel.ps1`
- `scripts/stop-public-tunnel.ps1`

Có 2 cách dùng:

- **Quick tunnel**: sinh domain public tạm thời
- **Named tunnel**: gắn với domain riêng qua Cloudflare

Xem chi tiết tại:

- `docs/public-domain-tunnel.md`

## 10. Tài liệu liên quan

- `deploy/ssl/README.md`
- `docs/demo-07-https-tls.md`
- `docs/owasp-zap-testing.md`
