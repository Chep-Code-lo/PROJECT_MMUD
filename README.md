# Bảo mật ứng dụng mạng dựa trên Cloud API cho dịch vụ công ty nhỏ

`README.md` này là file **hướng dẫn chạy dự án**. Nếu chỉ cần dựng hệ thống, hãy đọc theo thứ tự:

1. `Chạy local`
2. `Chạy public domain`
3. `Test nhanh sau khi chạy`

Các tài liệu giải thích sâu hơn về kiến trúc, checklist, sơ đồ và tài liệu tham khảo được để ở cuối file.

## 1. Stack dự án

- `frontend/`: `Next.js`
- `backend/`: `Spring Boot`, `Spring Security`, `JWT`, `Argon2id`, `AES-GCM`
- `database/`: `MySQL`
- `deploy/nginx/`: `Nginx` reverse proxy + TLS local
- `deploy/cloudflared/`: script publish public domain qua `Cloudflare Tunnel`
- `docs/api/`: `Swagger/OpenAPI`
- `docs/postman/`: `Postman/Newman`
- `docs/security/`: `OWASP ZAP`

## 2. Hai chế độ chạy

| Chế độ | Base URL | Dùng khi nào | Cần gì |
|---|---|---|---|
| `Local` | `https://localhost` | Chạy mặc định trên máy đang mở dự án | Chỉ cần Docker |
| `Public domain` | `https://demo.hackerlo.online` | Muốn cho máy khác truy cập từ bên ngoài | Docker + `cloudflared` + tunnel đang bật |

Lưu ý:

- `Local` là chế độ chính và luôn nên giữ lại.
- `Public domain` chỉ publish **cùng stack local đang chạy**, không phải một hệ thống khác.
- Người truy cập public domain **không cần** đăng nhập Cloudflare; chỉ máy chủ chạy dự án mới cần bật tunnel.
- Bằng chứng `HTTP -> HTTPS 301` hiện lấy ở `http://localhost`, không dùng public HTTP làm tiêu chí chính.

## 3. Điều kiện trước khi chạy

### Bắt buộc cho cả 2 chế độ

- Đã bật `Docker Desktop`
- Máy còn trống các port `80`, `443`, `3307`
- Đang đứng ở thư mục project:

```powershell
cd E:\PROJECT_MMUD
```

### Chỉ cần nếu muốn chạy public domain

- Đã cài `cloudflared.exe` ở:

```text
C:\Cloudflared\bin\cloudflared.exe
```

- Đã có file config tunnel:

```text
deploy\cloudflared\config.hackerlo.local.yml
```

- Đã login/cấu hình Cloudflare từ trước

## 4. Chạy local

Đây là cách chạy mặc định cho đồ án. Chỉ cần Docker, không cần bật public domain.

### 4.1. Khởi động local

```powershell
docker compose up -d --build
docker compose ps
```

Kỳ vọng:

- `securityapp-nginx` đang `Up`
- `securityapp-frontend` đang `Up`
- `securityapp-backend` đang `Up`
- `securityapp-db` đang `healthy`

### 4.2. URL local cần dùng

| Hạng mục | URL |
|---|---|
| Frontend | `https://localhost` |
| Login | `https://localhost/login` |
| Swagger UI | `https://localhost/swagger-ui.html` |
| OpenAPI JSON | `https://localhost/v3/api-docs` |
| Health | `https://localhost/api/health` |

Lưu ý:

- Local đang dùng cert demo/self-signed, nên lần đầu mở bằng trình duyệt có thể cần chấp nhận cảnh báo certificate.

### 4.3. Kiểm tra nhanh local

```powershell
curl.exe -I http://localhost/api/health
curl.exe -k https://localhost/api/health
curl.exe -k -I https://localhost/swagger-ui.html
curl.exe -k https://localhost/v3/api-docs
```

Kỳ vọng:

- `http://localhost/api/health` trả `301`
- `https://localhost/api/health` trả `200`
- `https://localhost/swagger-ui.html` redirect sang `/swagger-ui/index.html`
- `https://localhost/v3/api-docs` trả `200`

### 4.4. Dừng local

```powershell
docker compose down
```

### 4.5. Reset dữ liệu local

```powershell
docker compose down -v
docker compose up -d --build
```

### 4.6. Xem log local

```powershell
docker compose logs nginx
docker compose logs backend
docker compose logs frontend
docker compose logs database
```

## 5. Chạy public domain

Chỉ dùng mục này khi muốn người khác truy cập hệ thống từ máy ngoài qua:

```text
https://demo.hackerlo.online
```

### 5.1. Cách hiểu đúng

- Public domain vẫn dùng **chính stack local** đang chạy trên máy của bạn.
- Nếu tunnel tắt, public domain sẽ không dùng được.
- Khi tunnel tắt, `https://localhost` vẫn chạy bình thường.

### 5.2. Khởi động stack cho public domain

Nếu đang chạy local rồi, có thể chạy lại bằng file override để cập nhật `APP_API_BASE_URL` và `CORS` cho public mode:

```powershell
docker compose -f docker-compose.yml -f docker-compose.public-domain.yml up -d --build
docker compose ps
```

### 5.3. Bật tunnel

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\cloudflared\start-hackerlo-tunnel.ps1
powershell -ExecutionPolicy Bypass -File .\deploy\cloudflared\status-hackerlo-tunnel.ps1
```

### 5.4. URL public cần dùng

| Hạng mục | URL |
|---|---|
| Frontend | `https://demo.hackerlo.online` |
| Login | `https://demo.hackerlo.online/login` |
| Swagger UI | `https://demo.hackerlo.online/swagger-ui.html` |
| OpenAPI JSON | `https://demo.hackerlo.online/v3/api-docs` |
| Health | `https://demo.hackerlo.online/api/health` |

### 5.5. Kiểm tra nhanh public

```powershell
curl.exe https://demo.hackerlo.online/api/health
curl.exe -I https://demo.hackerlo.online/swagger-ui.html
curl.exe https://demo.hackerlo.online/v3/api-docs
```

Kỳ vọng:

- `https://demo.hackerlo.online/api/health` trả `200`
- Public Swagger mở được
- Public OpenAPI mở được

Lưu ý:

- Ở lần rà hiện tại, `http://demo.hackerlo.online` **không** được dùng làm bằng chứng redirect `301`.
- Hãy dùng `https://demo.hackerlo.online` làm URL demo công khai.

### 5.6. Tắt public domain

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\cloudflared\stop-hackerlo-tunnel.ps1
```

Sau khi tắt tunnel:

- `https://demo.hackerlo.online` sẽ không còn truy cập được
- `https://localhost` vẫn dùng được nếu stack Docker còn chạy

## 6. Tài khoản demo

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

## 7. Test nhanh sau khi chạy

### 7.1. Swagger

Mở một trong hai địa chỉ:

- `https://localhost/swagger-ui.html`
- `https://demo.hackerlo.online/swagger-ui.html`

Flow test nhanh:

1. `POST /api/auth/login`
2. Copy `accessToken`
3. Bấm `Authorize`
4. Nhập `Bearer <accessToken>`
5. Test `GET /api/auth/me`, `GET /api/customers`, `GET /api/admin/summary`

### 7.2. Newman

Chạy local:

```powershell
npx --yes newman run docs\postman\securityapp.postman_collection.json -e docs\postman\securityapp.local.postman_environment.json --insecure --reporters cli
```

Chạy public domain:

```powershell
npx --yes newman run docs\postman\securityapp.postman_collection.json -e docs\postman\securityapp.demo.hackerlo_environment.json --reporters cli
```

### 7.3. ZAP baseline

```powershell
docker run --rm --network project_mmud_default -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

Target nội bộ hiện dùng:

```text
https://nginx/swagger-ui.html
```

### 7.4. Query DB để chứng minh bảo mật

Kiểm tra password hash:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Kiểm tra dữ liệu customer đã mã hóa:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,phone_encrypted,address_encrypted,tax_code_encrypted,key_version FROM customers;"
```

## 8. Kết quả đã rà ngày `2026-06-21`

- Local: `http://localhost/api/health -> 301`, `https://localhost/api/health -> 200`
- Local Swagger: `https://localhost/swagger-ui.html -> 302 -> /swagger-ui/index.html`
- Local OpenAPI: `https://localhost/v3/api-docs -> 200`
- Public health: `https://demo.hackerlo.online/api/health -> 200`
- Smoke security: `register 201`, `login đúng 200 + JWT`, `login sai 401`, `no token 401`, `USER -> /api/customers = 403`
- Password hash mới: prefix `$argon2id$...`
- Customer data trong DB: ciphertext + `key_version = 2`
- Newman local: `15 requests`, `22 assertions`, `0 failed`
- Newman public: `15 requests`, `22 assertions`, `0 failed`
- ZAP baseline: `PASS=59`, `WARN=2`, `FAIL=0`

## 9. File nào dùng để làm gì

- [README.md](/E:/PROJECT_MMUD/README.md): hướng dẫn chạy dự án
- [DEMO_SECURITY_RUNTIME_CHECKLIST.md](/E:/PROJECT_MMUD/DEMO_SECURITY_RUNTIME_CHECKLIST.md): checklist demo nhanh trên lớp
- [FULL_SECURITY_TEST_EVIDENCE_CHECKLIST.md](/E:/PROJECT_MMUD/FULL_SECURITY_TEST_EVIDENCE_CHECKLIST.md): checklist đầy đủ để chụp bằng chứng
- [PROJECT_COMPONENTS_AND_RUNTIME_FLOW.md](/E:/PROJECT_MMUD/PROJECT_COMPONENTS_AND_RUNTIME_FLOW.md): dự án có những gì và luồng chạy ra sao
- [PROJECT_SECURITY_ARCHITECTURE_AND_DEMO_DIAGRAMS.html](/E:/PROJECT_MMUD/PROJECT_SECURITY_ARCHITECTURE_AND_DEMO_DIAGRAMS.html): file xem sơ đồ
- [SECURITY_REFERENCES_DIRECT_LINKS.md](/E:/PROJECT_MMUD/SECURITY_REFERENCES_DIRECT_LINKS.md): tài liệu tham khảo và link trực tiếp
- [docs/api/README.md](/E:/PROJECT_MMUD/docs/api/README.md): tài liệu Swagger/OpenAPI
- [docs/postman/README.md](/E:/PROJECT_MMUD/docs/postman/README.md): tài liệu Postman/Newman
- [docs/security/README.md](/E:/PROJECT_MMUD/docs/security/README.md): tài liệu OWASP ZAP
- [deploy/ssl/README.md](/E:/PROJECT_MMUD/deploy/ssl/README.md): ghi chú TLS local

## 10. Ghi chú cuối

- Không public backend thuần HTTP ra internet.
- TLS nên terminate ở reverse proxy/edge.
- Nếu chỉ cần nhóm tự test, dùng `https://localhost` là đủ.
- Nếu cần demo cho máy ngoài, bật thêm `public domain` bằng `Cloudflare Tunnel`.
