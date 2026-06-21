# Bảo mật ứng dụng mạng dựa trên Cloud API cho dịch vụ công ty nhỏ

Monorepo này dùng để demo đồ án MMUD theo hướng `web frontend + REST API backend + database + security controls + security evidence`. Trọng tâm của dự án là bảo mật ứng dụng mạng và bằng chứng kiểm thử, không trình bày như một bài CRUD thông thường.

## 1. Thành phần chính

- `frontend/`: giao diện `Next.js`
- `backend/`: `Spring Boot`, `Spring Security`, `JWT`, `Argon2id`, `AES-GCM`, audit log
- `database/`: `MySQL`, `schema.sql`
- `deploy/nginx/`: reverse proxy `Nginx`, TLS local, security headers
- `deploy/cloudflared/`: script `Cloudflare Tunnel` để publish public domain khi cần demo bên ngoài
- `docs/api/`: `Swagger/OpenAPI`
- `docs/postman/`: `Postman/Newman`
- `docs/security/`: `OWASP ZAP` và artifact quét

Package chính:

```text
com.company.securityapp
```

## 2. Control bảo mật đang có

- `JWT` cho authentication
- `Argon2id` cho hash mật khẩu mới, vẫn verify được `bcrypt` legacy và tự nâng cấp sau login thành công
- `AES-GCM` cho dữ liệu nhạy cảm của customer, kết hợp `HKDF-SHA256`, `AAD` và `key_version`
- `Role-based authorization` cho `ADMIN`, `STAFF`, `USER`
- `Audit log` cho các hành động quan trọng
- `Swagger/OpenAPI`, `Postman/Newman`, `OWASP ZAP` để tạo bằng chứng kiểm thử

Ba trường customer được mã hóa trước khi lưu DB:

- `phone`
- `address`
- `taxCode`

Các cột lưu trữ thật trong DB:

- `phone_encrypted`
- `address_encrypted`
- `tax_code_encrypted`
- `key_version`

## 3. Hai chế độ truy cập

| Chế độ | Base URL | Dùng khi nào | Ghi chú |
|---|---|---|---|
| `Localhost` | `https://localhost` | Test nội bộ, fallback cho cả nhóm | Có thể chứng minh rõ `http://localhost -> 301 -> https://localhost` |
| `Public domain` | `https://demo.hackerlo.online` | Demo cho người khác truy cập từ máy ngoài | Chỉ hoạt động khi máy chủ đang bật stack và `Cloudflare Tunnel` đang chạy |

URL tương ứng:

| Hạng mục | Localhost | Public domain |
|---|---|---|
| Frontend | `https://localhost` | `https://demo.hackerlo.online` |
| Swagger UI | `https://localhost/swagger-ui.html` | `https://demo.hackerlo.online/swagger-ui.html` |
| OpenAPI JSON | `https://localhost/v3/api-docs` | `https://demo.hackerlo.online/v3/api-docs` |
| Health | `https://localhost/api/health` | `https://demo.hackerlo.online/api/health` |

Lưu ý:

- `localhost` luôn nên được giữ lại để các thành viên nhóm có thể tự test trên máy chạy dự án.
- `public domain` chỉ là lớp publish thêm để phục vụ demo hoặc truy cập từ máy khác; không thay thế chế độ local.
- Bằng chứng `HTTP -> HTTPS 301` hiện được lấy ở local origin. Không dùng public HTTP redirect làm tiêu chí chấm chính.

### Trạng thái đã rà ngày `2026-06-21`

- `docker compose ps`: `securityapp-nginx`, `securityapp-frontend`, `securityapp-backend` đều `Up`; `securityapp-db` đang `healthy`.
- Local runtime: `http://localhost/api/health -> 301 -> https://localhost/api/health`, `https://localhost/api/health -> 200`, `https://localhost/swagger-ui.html -> 302 -> /swagger-ui/index.html`, `https://localhost/v3/api-docs -> 200`.
- Public runtime: `https://demo.hackerlo.online/api/health -> 200`. Ở thời điểm rà này, `http://demo.hackerlo.online/api/health` vẫn trả `200` ở lớp public edge, nên không dùng route đó làm bằng chứng redirect.
- Smoke test bảo mật: `register -> 201`, `login đúng -> 200 + JWT`, `login sai -> 401`, `GET /api/auth/me` không token -> `401`, token `USER` gọi `GET /api/customers` -> `403`.
- Bằng chứng lưu trữ: user mới có `password_hash` prefix `$argon2id$`, customer mới có `phone_encrypted`, `address_encrypted`, `tax_code_encrypted` và `key_version = 2`.
- Newman: local `15 requests`, `22 assertions`, `0 failed`; public domain `15 requests`, `22 assertions`, `0 failed`.
- OWASP ZAP baseline: `PASS=59`, `WARN=2`, `FAIL=0`.

## 4. Phân quyền hiện tại

| Route | Quyền |
|---|---|
| `/api/auth/register`, `/api/auth/login`, Swagger, health | Public |
| `/api/auth/me` | Đã đăng nhập |
| `/api/admin/**` | `ADMIN` |
| `/api/audit-logs/**` | `ADMIN` |
| `/api/customers/**` | `ADMIN`, `STAFF` |

## 5. Tài khoản demo

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

## 6. Cách chạy khuyến nghị

### Chạy local mặc định

Đây là cách nên dùng cho cả nhóm vì dựng đủ `Nginx HTTPS edge + frontend + backend + MySQL`.

```powershell
cd E:\PROJECT_MMUD
docker compose up -d --build
docker compose ps
```

Nếu cần reset dữ liệu demo:

```powershell
docker compose down -v
docker compose up -d --build
```

Nếu cần xem log:

```powershell
docker compose logs nginx
docker compose logs backend
docker compose logs frontend
docker compose logs database
```

### Bật thêm public domain

Nếu muốn người khác truy cập từ bên ngoài qua domain, dùng thêm file override và script `Cloudflare Tunnel` đã có sẵn trong repo:

```powershell
docker compose -f docker-compose.yml -f docker-compose.public-domain.yml up -d --build
powershell -ExecutionPolicy Bypass -File .\deploy\cloudflared\start-hackerlo-tunnel.ps1
```

Lưu ý:

- Nếu không bật publish domain, dự án vẫn chạy bình thường ở `https://localhost`.
- Nếu bật public domain, local vẫn dùng được song song.

Dừng hệ thống:

```powershell
docker compose down
```

## 7. Chạy dev không dùng Docker

### Backend

Test nhanh:

```powershell
cd backend
mvn test
```

Chạy backend với profile dev:

```powershell
cd backend
mvn clean package -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Biến môi trường quan trọng:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`
- `APP_AES_SECRET`
- `APP_DEMO_USERS_ENABLED`
- `APP_DEMO_USERS_PASSWORD`

### Frontend

```powershell
cd frontend
npm install
npm run build
npm run dev
```

Nếu chạy frontend tách khỏi `Nginx`, cần tự cấu hình `NEXT_PUBLIC_API_URL`.

## 8. Kiểm thử bằng Swagger/OpenAPI

Swagger dùng để:

- tài liệu hóa API
- test nhanh request/response
- đối chiếu schema và status code thật

Cách test:

1. Mở Swagger UI ở một trong hai địa chỉ:
   - `https://localhost/swagger-ui.html`
   - `https://demo.hackerlo.online/swagger-ui.html`
2. Gọi `POST /api/auth/login` bằng tài khoản `admin`
3. Copy `accessToken`
4. Bấm `Authorize`
5. Nhập:

```text
Bearer <accessToken>
```

6. Test các nhóm API:

- `GET /api/auth/me`
- `GET /api/customers`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`
- `GET /api/admin/summary`
- `GET /api/audit-logs`

Điểm cần kiểm tra:

- `register` và `create customer` trả `201`
- `delete customer` trả `204`
- request không có token vào `/api/auth/me` trả `401`
- token `USER` gọi customer API trả `403`

Tài liệu API:

- [docs/api/openapi.json](/E:/PROJECT_MMUD/docs/api/openapi.json)
- [docs/api/README.md](/E:/PROJECT_MMUD/docs/api/README.md)

## 9. Kiểm thử bằng Postman/Newman

File liên quan:

- [docs/postman/securityapp.postman_collection.json](/E:/PROJECT_MMUD/docs/postman/securityapp.postman_collection.json)
- [docs/postman/securityapp.local.postman_environment.json](/E:/PROJECT_MMUD/docs/postman/securityapp.local.postman_environment.json)
- [docs/postman/securityapp.demo.hackerlo_environment.json](/E:/PROJECT_MMUD/docs/postman/securityapp.demo.hackerlo_environment.json)
- [docs/postman/README.md](/E:/PROJECT_MMUD/docs/postman/README.md)

Collection hiện tập trung vào:

- auth
- admin summary
- customer CRUD
- audit log
- security checks cho `401` và `403`

Chạy Newman local:

```powershell
npx --yes newman run docs\postman\securityapp.postman_collection.json -e docs\postman\securityapp.local.postman_environment.json --insecure --reporters cli
```

Chạy Newman public domain:

```powershell
npx --yes newman run docs\postman\securityapp.postman_collection.json -e docs\postman\securityapp.demo.hackerlo_environment.json --reporters cli
```

## 10. Kiểm thử bằng OWASP ZAP

File liên quan:

- [docs/security/zap.yaml](/E:/PROJECT_MMUD/docs/security/zap.yaml)
- [docs/security/zap-baseline-report.html](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.html)
- [docs/security/zap-baseline-report.json](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.json)
- [docs/security/zap-baseline-report.xml](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.xml)
- [docs/security/README.md](/E:/PROJECT_MMUD/docs/security/README.md)

Chạy lại baseline scan:

```powershell
docker run --rm --network project_mmud_default -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

Target quét hiện tại:

```text
https://nginx/swagger-ui.html
```

Giải thích:

- ZAP chạy trong Docker network và quét đúng `nginx` service của stack đang chạy.
- Bên ngoài container, cùng surface này tương ứng với `https://localhost/swagger-ui.html` và `https://demo.hackerlo.online/swagger-ui.html`.
- Không dùng `host.docker.internal` trong repo này để tránh quét nhầm dịch vụ khác trên máy host.

Kết quả artifact hiện có:

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

Nhóm cảnh báo còn lại:

- `CSP-related [10055]` của Swagger UI
- `Modern Web Application [10109]`

## 11. Kiểm chứng lưu trữ password và dữ liệu mã hóa

### Kiểm tra password hash

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Kỳ vọng:

- user mới có `password_hash` dạng `$argon2id$...`
- user cũ vẫn có thể đang ở `bcrypt` cho đến lần login thành công tiếp theo
- không thấy plaintext password

### Kiểm tra ciphertext của customer

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,name,email,phone_encrypted,address_encrypted,tax_code_encrypted,key_version FROM customers;"
```

Kỳ vọng:

- `email` đọc được
- `phone_encrypted`, `address_encrypted`, `tax_code_encrypted` là ciphertext
- có `key_version`
- không thấy plaintext số điện thoại, địa chỉ, mã số thuế

### Kiểm tra audit log

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,action,actor_email,success,created_at FROM audit_logs ORDER BY id DESC LIMIT 10;"
```

Kỳ vọng:

- có `LOGIN_SUCCESS`, `LOGIN_FAILED`, `CREATE_CUSTOMER`, `UPDATE_CUSTOMER`, `DELETE_CUSTOMER`
- không log full JWT hoặc AES secret

## 12. Kiểm tra nhanh bằng giao diện

Có thể dùng một trong hai địa chỉ:

- `https://localhost/login`
- `https://demo.hackerlo.online/login`

Mạch test nhanh:

1. Đăng nhập `admin`
2. Vào `Customers`, tạo customer mới
3. Sửa hoặc xóa customer để sinh thêm audit log
4. Vào `Audit Logs`, xem các action vừa phát sinh
5. Đăng xuất
6. Đăng nhập `user`
7. Thử vào route customer và xác nhận bị chặn `403`

## 13. Bộ tài liệu nên đọc

- [DEMO_SECURITY_RUNTIME_CHECKLIST.md](/E:/PROJECT_MMUD/DEMO_SECURITY_RUNTIME_CHECKLIST.md)
- [FULL_SECURITY_TEST_EVIDENCE_CHECKLIST.md](/E:/PROJECT_MMUD/FULL_SECURITY_TEST_EVIDENCE_CHECKLIST.md)
- [PROJECT_COMPONENTS_AND_RUNTIME_FLOW.md](/E:/PROJECT_MMUD/PROJECT_COMPONENTS_AND_RUNTIME_FLOW.md)
- [PROJECT_SECURITY_ARCHITECTURE_AND_DEMO_DIAGRAMS.html](/E:/PROJECT_MMUD/PROJECT_SECURITY_ARCHITECTURE_AND_DEMO_DIAGRAMS.html)
- [SECURITY_REFERENCES_DIRECT_LINKS.md](/E:/PROJECT_MMUD/SECURITY_REFERENCES_DIRECT_LINKS.md)
- [docs/api/README.md](/E:/PROJECT_MMUD/docs/api/README.md)
- [docs/postman/README.md](/E:/PROJECT_MMUD/docs/postman/README.md)
- [docs/security/README.md](/E:/PROJECT_MMUD/docs/security/README.md)
- [deploy/ssl/README.md](/E:/PROJECT_MMUD/deploy/ssl/README.md)

## 14. Ghi chú TLS và deploy

- Không public backend thuần HTTP ra ngoài internet.
- TLS nên được terminate ở reverse proxy/edge.
- `localhost` là mode fallback để nhóm tự test.
- `demo.hackerlo.online` là mode public để trình bày hoặc cho máy khác truy cập khi `Cloudflare Tunnel` đang bật.
