# Checklist đầy đủ cho bằng chứng kiểm thử bảo mật

Tài liệu này dùng để chứng minh đồ án Security ở mức runtime, database và DAST. Mỗi security control đều có test case, cách chạy, expected result và evidence cần chụp.

## 1. Phạm vi chứng minh

Checklist này bao phủ 6 nhóm:

1. `Network / HTTPS / TLS`
2. `Authentication`
3. `Authorization`
4. `Password storage`
5. `Sensitive data encryption`
6. `DAST / OWASP ZAP`

## 2. Tiền điều kiện

### Chạy stack local

```powershell
docker compose up -d --build
docker compose ps
```

### Nếu cần bật thêm public domain qua `Cloudflare Tunnel`

```powershell
docker compose -f docker-compose.yml -f docker-compose.public-domain.yml up -d --build
powershell -ExecutionPolicy Bypass -File .\deploy\cloudflared\start-hackerlo-tunnel.ps1
```

Kỳ vọng:

- `securityapp-nginx` chạy trên `80/443`
- `securityapp-backend` chạy nội bộ `8080`
- `securityapp-frontend` chạy nội bộ `3000`
- `securityapp-db` healthy và map `3307` ra host để query bằng chứng

Tài khoản demo:

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

URL runtime:

| Hạng mục | Localhost | Public domain |
|---|---|---|
| Frontend | `https://localhost` | `https://demo.hackerlo.online` |
| Swagger UI | `https://localhost/swagger-ui.html` | `https://demo.hackerlo.online/swagger-ui.html` |
| OpenAPI JSON | `https://localhost/v3/api-docs` | `https://demo.hackerlo.online/v3/api-docs` |
| Health | `https://localhost/api/health` | `https://demo.hackerlo.online/api/health` |

Quy ước:

- `<BASE_URL>` là `https://localhost` hoặc `https://demo.hackerlo.online`.
- Riêng test redirect `301` chỉ áp dụng cho local origin `http://localhost`.
- Tại lần rà ngày `2026-06-21`, `http://demo.hackerlo.online/api/health` vẫn trả `200` ở lớp public edge, nên checklist không dùng public HTTP làm bằng chứng redirect.

## 3. Bảng checklist tổng hợp

| ID | Nhóm | Áp dụng cho | Test case | Công cụ | Expected result |
|---|---|---|---|---|---|
| `NET-01` | Network | `Localhost` | Local origin HTTP bị redirect sang HTTPS | `curl` | `301` về `https://localhost/...` |
| `NET-02` | Network | `Localhost`, `Public domain` | HTTPS health check thành công | `curl` | `200` |
| `NET-03` | Network | `Localhost` | Local HTTPS response có security headers | `curl` | Có `HSTS`, `CSP`, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` |
| `AUTH-01` | AuthN | `Localhost`, `Public domain` | Register user mới | Swagger/Postman/curl | `201` |
| `AUTH-02` | AuthN | `Localhost`, `Public domain` | Login đúng password | Swagger/Postman/curl | `200` + có JWT |
| `AUTH-03` | AuthN | `Localhost`, `Public domain` | Login sai password | Swagger/Postman/curl | `401` |
| `AUTH-04` | AuthN | `Localhost`, `Public domain` | Gọi API không JWT | Swagger/Postman/curl | `401` |
| `AUTHZ-01` | AuthZ | `Localhost`, `Public domain` | Token `USER` gọi customer API | Swagger/Postman/curl | `403` |
| `AUTHZ-02` | AuthZ | `Localhost`, `Public domain` | Token `USER` gọi admin summary | Swagger/Postman/curl | `403` |
| `AUTHZ-03` | AuthZ | `Localhost`, `Public domain` | Token `STAFF` gọi customer API | Swagger/Postman/curl | `200` |
| `PWD-01` | Password | `Localhost`, `Public domain` | DB không lưu plaintext password | MySQL query | Không thấy plaintext |
| `PWD-02` | Password | `Localhost`, `Public domain` | Hash mới có dạng `Argon2id` | MySQL query | Prefix `$argon2id` |
| `ENC-01` | Encryption | `Localhost`, `Public domain` | Tạo customer thành công | Swagger/Postman/UI | `201` |
| `ENC-02` | Encryption | `Localhost`, `Public domain` | DB không lưu plaintext `phone/address/taxCode` | MySQL query | Chỉ thấy ciphertext |
| `ENC-03` | Encryption | `Localhost`, `Public domain` | DB có `key_version` | MySQL query | Hiện tại là `2` |
| `ENC-04` | Encryption | `Localhost`, `Public domain` | Tamper ciphertext rồi đọc lại | MySQL query + API call | `500` hoặc decrypt fail |
| `ZAP-01` | DAST | `Stack nội bộ` | ZAP baseline quét HTTPS edge | ZAP | `FAIL=0` |
| `ZAP-02` | DAST | `Stack nội bộ` | Ghi nhận warnings còn lại | ZAP report | `WARN=2` với mô tả rõ ràng |

## 4. Checklist chi tiết

### 4.1. Network / HTTPS / TLS

#### `NET-01` Local origin HTTP redirect sang HTTPS

Chạy:

```powershell
curl.exe -I http://localhost/api/health
```

Kỳ vọng:

- Status `301`
- Header `Location: https://localhost/api/health`

Evidence cần chụp:

- Terminal output có `301 Moved Permanently`

Trạng thái runtime đã xác minh:

- `PASS`

#### `NET-02` HTTPS health check

Chạy local:

```powershell
curl.exe -k -i https://localhost/api/health
```

Chạy public domain:

```powershell
curl.exe -i https://demo.hackerlo.online/api/health
```

Kỳ vọng:

- Status `200`
- Body có `status=UP`

Evidence cần chụp:

- Terminal output với `HTTP/1.1 200`

Trạng thái runtime đã xác minh:

- Localhost: `PASS`
- Public domain: `PASS`

#### `NET-03` Security headers ở local HTTPS edge

Chạy:

```powershell
curl.exe -k -I https://localhost/api/health
```

Kỳ vọng:

- Có `Strict-Transport-Security`
- Có `Content-Security-Policy`
- Có `X-Content-Type-Options`
- Có `X-Frame-Options`
- Có `Referrer-Policy`

Evidence cần chụp:

- Phần header response

Trạng thái runtime đã xác minh:

- `PASS`

### 4.2. Authentication

#### `AUTH-01` Register user mới

Ví dụ body:

```json
{
  "fullName": "Verify User",
  "email": "verify+demo@example.test",
  "password": "Password@123"
}
```

Endpoint:

- `POST <BASE_URL>/api/auth/register`

Kỳ vọng:

- trả `201`
- response có email vừa đăng ký
- `role=USER`

Evidence cần chụp:

- Swagger/Postman response

Trạng thái runtime đã xác minh:

- `PASS`

#### `AUTH-02` Login đúng password

Ví dụ body:

```json
{
  "email": "verify+demo@example.test",
  "password": "Password@123"
}
```

Endpoint:

- `POST <BASE_URL>/api/auth/login`

Kỳ vọng:

- trả `200`
- response có `accessToken`
- có `tokenType=Bearer`

Evidence cần chụp:

- JWT trong response

Trạng thái runtime đã xác minh:

- `PASS`

#### `AUTH-03` Login sai password

Ví dụ body:

```json
{
  "email": "verify+demo@example.test",
  "password": "WrongPassword@123"
}
```

Kỳ vọng:

- `401`

Evidence cần chụp:

- Response status

Trạng thái runtime đã xác minh:

- `PASS`

#### `AUTH-04` Gọi API không có JWT

Chạy local:

```powershell
curl.exe -k -i https://localhost/api/auth/me
```

Chạy public domain:

```powershell
curl.exe -i https://demo.hackerlo.online/api/auth/me
```

Kỳ vọng:

- `401`

Evidence cần chụp:

- Response status

Trạng thái runtime đã xác minh:

- `PASS`

### 4.3. Authorization

#### `AUTHZ-01` Token `USER` gọi customer API

Thao tác:

1. Login bằng `user@securityapp.local`
2. Dùng token gọi `GET <BASE_URL>/api/customers`

Kỳ vọng:

- `403`

Evidence cần chụp:

- Response `403`

Trạng thái runtime đã xác minh:

- `PASS`

#### `AUTHZ-02` Token `USER` gọi admin summary

Thao tác:

1. Login bằng `user@securityapp.local`
2. Dùng token gọi `GET <BASE_URL>/api/admin/summary`

Kỳ vọng:

- `403`

Evidence cần chụp:

- Response `403`

Trạng thái runtime đã xác minh:

- `PASS`

#### `AUTHZ-03` Token `STAFF` gọi customer API

Thao tác:

1. Login bằng `staff@securityapp.local`
2. Gọi `GET <BASE_URL>/api/customers`

Kỳ vọng:

- `200`

Evidence cần chụp:

- Response `200`

Trạng thái runtime đã xác minh:

- `PASS`

### 4.4. Password storage

#### `PWD-01` DB không lưu plaintext password

Chạy:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Kỳ vọng:

- Không thấy password gốc

Evidence cần chụp:

- Output SQL

Trạng thái runtime đã xác minh:

- `PASS`

#### `PWD-02` Hash mới có dạng `Argon2id`

Chạy cùng lệnh ở `PWD-01`.

Kỳ vọng:

- `password_hash` của user mới bắt đầu bằng `$argon2id`

Evidence cần chụp:

- Prefix `$argon2id`

Trạng thái runtime đã xác minh:

- `PASS`

Ghi chú thêm:

- `bcrypt` legacy vẫn được hỗ trợ để verify user cũ
- sau login thành công, hash legacy sẽ được nâng cấp sang `Argon2id`

### 4.5. Mã hóa dữ liệu nhạy cảm

#### `ENC-01` Tạo customer thành công

Ví dụ body:

```json
{
  "name": "Tamper Proof Customer",
  "email": "customer+demo@example.test",
  "phone": "0909888777",
  "address": "123 Security Street",
  "taxCode": "TAX-SEC-001"
}
```

Kỳ vọng:

- `POST <BASE_URL>/api/customers` trả `201`

Evidence cần chụp:

- Response body có `id`

Trạng thái runtime đã xác minh:

- `PASS`

#### `ENC-02` DB chỉ lưu ciphertext

Chạy:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,phone_encrypted,address_encrypted,tax_code_encrypted,key_version FROM customers;"
```

Kỳ vọng:

- Không thấy plaintext `phone`
- Không thấy plaintext `address`
- Không thấy plaintext `taxCode`
- Chỉ thấy chuỗi ciphertext

Evidence cần chụp:

- Output SQL

Trạng thái runtime đã xác minh:

- `PASS`

#### `ENC-03` Có `key_version`

Chạy cùng lệnh `ENC-02`.

Kỳ vọng:

- `key_version = 2`

Evidence cần chụp:

- Giá trị `key_version`

Trạng thái runtime đã xác minh:

- `PASS`

#### `ENC-04` Tamper ciphertext rồi đọc lại

Bước 1. Sửa tay ciphertext:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "UPDATE customers SET phone_encrypted=CONCAT('AA', SUBSTRING(phone_encrypted,3)) WHERE id=<ID_CUSTOMER>;"
```

Bước 2. Gọi lại API local:

```powershell
curl.exe -k -i -H "Authorization: Bearer <STAFF_OR_ADMIN_TOKEN>" https://localhost/api/customers/<ID_CUSTOMER>
```

Bước 3. Gọi lại API public domain:

```powershell
curl.exe -i -H "Authorization: Bearer <STAFF_OR_ADMIN_TOKEN>" https://demo.hackerlo.online/api/customers/<ID_CUSTOMER>
```

Kỳ vọng:

- API trả lỗi `500`
- backend không trả dữ liệu giả

Evidence cần chụp:

- Lệnh update DB
- Response `500`

Trạng thái runtime đã xác minh:

- `PASS`

Ý nghĩa bảo mật:

- `AES-GCM` bảo vệ cả `Confidentiality` lẫn `Integrity`

### 4.6. DAST / OWASP ZAP

#### `ZAP-01` Chạy baseline trên HTTPS edge

Chạy:

```powershell
docker run --rm --network project_mmud_default -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

Target:

```text
https://nginx/swagger-ui.html
```

Kỳ vọng:

- Job thành công
- `FAIL=0`

Evidence cần chụp:

- Terminal output của ZAP
- Report HTML/JSON/XML

Trạng thái runtime đã xác minh:

- `PASS`

#### `ZAP-02` Đọc warning còn lại

Mở:

- [docs/security/zap-baseline-report.html](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.html:1)
- [docs/security/zap-baseline-report.json](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.json:1)

Kỳ vọng:

- `PASS=59`
- `WARN=2`
- `FAIL=0`

Warning hiện tại:

- `CSP-related [10055]`
- `Modern Web Application [10109]`

Ý nghĩa bảo mật:

- Đây là hardening finding còn lại cho Swagger UI
- Không phải fail nghiêm trọng ở auth, JWT hay dữ liệu mã hóa

## 5. Bảng kết quả runtime đã xác minh

Các giá trị dưới đây được chụp lại từ runtime ngày `2026-06-21`.

| Test case | Actual result runtime |
|---|---|
| `NET-01` | `301` ở `http://localhost/api/health` |
| `NET-02` | `200` ở `https://localhost/api/health` và `https://demo.hackerlo.online/api/health` |
| `NET-03` | Có `HSTS`, `CSP`, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` |
| `AUTH-01` | `201` với user runtime mới |
| `AUTH-02` | `200` + có JWT, sample token length `165` |
| `AUTH-03` | `401` |
| `AUTH-04` | `401` |
| `AUTHZ-01` | `403` |
| `AUTHZ-02` | `403` |
| `AUTHZ-03` | `200` |
| `PWD-01` | Không có plaintext password |
| `PWD-02` | Prefix `$argon2id$...` |
| `ENC-01` | `201` |
| `ENC-02` | Row customer runtime lưu ciphertext ở `phone_encrypted`, `address_encrypted`, `tax_code_encrypted` |
| `ENC-03` | `key_version = 2` |
| `ENC-04` | `500` sau khi tamper |
| `AUTO-01` | Newman local: `15 requests`, `22 assertions`, `0 failed` |
| `AUTO-02` | Newman public domain: `15 requests`, `22 assertions`, `0 failed` |
| `ZAP-01` | Job thành công |
| `ZAP-02` | `PASS=59`, `WARN=2`, `FAIL=0` |

## 6. Evidence nên chụp để nộp

Ít nhất nên có:

1. `docker compose ps`
2. `curl` chứng minh `301` local và `200 HTTPS`
3. Swagger hoặc Postman login trả JWT
4. `401` khi không token
5. `403` khi token `USER` gọi sai quyền
6. Query `users.password_hash`
7. Query `customers.*_encrypted` và `key_version`
8. Request sau khi tamper ciphertext trả lỗi
9. ZAP terminal output
10. ZAP report HTML

## 7. Cách nói ngắn khi giảng viên hỏi “em chứng minh ở đâu?”

Có thể trả lời:

> Em chứng minh ở 3 tầng.  
> Tầng mạng: em dùng `curl` để chứng minh local origin bị redirect sang HTTPS và public domain truy cập qua HTTPS.  
> Tầng ứng dụng: em demo `201`, `200`, `401`, `403` bằng Swagger hoặc Postman để chứng minh authentication và authorization.  
> Tầng dữ liệu: em query DB để chứng minh password là `Argon2id hash`, còn `phone/address/taxCode` là ciphertext do `AES-GCM`; sau đó em tamper ciphertext để chứng minh backend không chấp nhận dữ liệu bị sửa.  
> Cuối cùng em có thêm bằng chứng DAST bằng `OWASP ZAP` với kết quả `PASS 59`, `WARN 2`, `FAIL 0`.

## 8. Tài liệu liên quan

- [DEMO_SECURITY_RUNTIME_CHECKLIST.md](/E:/PROJECT_MMUD/DEMO_SECURITY_RUNTIME_CHECKLIST.md)
- [docs/postman/README.md](/E:/PROJECT_MMUD/docs/postman/README.md)
- [docs/security/README.md](/E:/PROJECT_MMUD/docs/security/README.md)
- [docs/security/zap-baseline-report.html](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.html:1)
