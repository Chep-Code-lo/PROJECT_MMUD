# Ghi chú TLS cho local và public domain

Thư mục này chứa certificate dùng cho `Nginx` local origin.

## 1. File cần có

- `fullchain.pem`
- `privkey.pem`

Reverse proxy được mô tả tại [deploy/nginx/securityapp.conf](/E:/PROJECT_MMUD/deploy/nginx/securityapp.conf).

## 2. Cách hiểu theo từng chế độ

### Localhost

- `Nginx` dùng certificate trong thư mục này để phục vụ `https://localhost`
- mode này dùng cho test nội bộ và là fallback chính cho cả nhóm

### Public domain

- `https://demo.hackerlo.online` publish cùng local origin khi `Cloudflare Tunnel` đang bật
- certificate public nằm ở lớp `Cloudflare edge`; thư mục này vẫn chủ yếu phục vụ local origin `https://localhost`
- không cần tạo thêm một bộ cert public riêng trong repo chỉ để phục vụ mode publish

## 3. Mục tiêu của lớp TLS

1. Frontend và backend đi qua `HTTPS`
2. Backend không bị public trực tiếp qua HTTP ngoài internet
3. `Nginx` terminate TLS rồi proxy:
   - `/` -> frontend
   - `/api/*` -> backend
   - `/swagger-ui/*` và `/v3/api-docs/*` -> backend

## 4. Tạo cert self-signed cho local

Nếu cần test local với self-signed certificate:

```powershell
openssl req -x509 -nodes -days 365 -newkey rsa:2048 ^
  -keyout deploy/ssl/privkey.pem ^
  -out deploy/ssl/fullchain.pem ^
  -subj "/CN=localhost"
```

## 5. Ghi chú thêm

- File cert local đã được đưa vào `.gitignore`.
- Nếu cần sinh lại cert local, có thể xóa `deploy/ssl/fullchain.pem` và `deploy/ssl/privkey.pem`, sau đó chạy lại `docker compose up -d --build`.
