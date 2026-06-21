# Checklist demo runtime bảo mật

Tài liệu này dùng để trình bày nhanh trên lớp sau khi stack Docker đã chạy thật với `Nginx HTTPS edge`. Nội dung đã được viết cho cả hai chế độ truy cập: `localhost` và `public domain`.

## 1. Mục tiêu demo

Trong buổi trình bày cần chứng minh 5 ý chính:

1. Hệ thống chạy qua `HTTPS`, không public raw backend.
2. Authentication và authorization hoạt động đúng với `JWT`.
3. Password không lưu plaintext; hash mới dùng `Argon2id`.
4. Dữ liệu nhạy cảm của customer là ciphertext do `AES-GCM + HKDF + AAD + key_version`.
5. Hệ thống đã có bằng chứng DAST bằng `OWASP ZAP`.

## 2. Chọn chế độ demo

| Chế độ | Base URL | Khi nào dùng |
|---|---|---|
| `Localhost` | `https://localhost` | Test nội bộ, fallback cho nhóm, chứng minh `301` local |
| `Public domain` | `https://demo.hackerlo.online` | Cho người khác truy cập từ máy ngoài khi `Cloudflare Tunnel` đang bật |

URL tương ứng:

| Hạng mục | Localhost | Public domain |
|---|---|---|
| Frontend | `https://localhost` | `https://demo.hackerlo.online` |
| Swagger UI | `https://localhost/swagger-ui.html` | `https://demo.hackerlo.online/swagger-ui.html` |
| OpenAPI JSON | `https://localhost/v3/api-docs` | `https://demo.hackerlo.online/v3/api-docs` |
| Health | `https://localhost/api/health` | `https://demo.hackerlo.online/api/health` |

Quy ước trong file này:

- `<BASE_URL>` là một trong hai giá trị trên.
- Riêng bằng chứng `HTTP -> HTTPS 301` được lấy ở local origin `http://localhost`.

## 3. Chuẩn bị trước khi demo

### Chạy local mặc định

```powershell
docker compose up -d --build
docker compose ps
```

### Nếu muốn bật thêm public domain

```powershell
docker compose -f docker-compose.yml -f docker-compose.public-domain.yml up -d --build
powershell -ExecutionPolicy Bypass -File .\deploy\cloudflared\start-hackerlo-tunnel.ps1
```

### Kiểm tra nhanh

```powershell
curl.exe -I http://localhost/api/health
curl.exe -k https://localhost/api/health
curl.exe https://demo.hackerlo.online/api/health
```

Kỳ vọng:

- `http://localhost/api/health` trả `301` về `https://localhost/api/health`
- `https://localhost/api/health` trả `200`
- `https://demo.hackerlo.online/api/health` trả `200` khi `Cloudflare Tunnel` đang bật

Lưu ý lúc trình bày:

- Tại lần rà ngày `2026-06-21`, `http://demo.hackerlo.online/api/health` vẫn trả `200` ở lớp public edge.
- Vì vậy, bằng chứng redirect `301` chỉ nên lấy ở local origin `http://localhost`.

Tài khoản demo:

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

## 4. Thứ tự demo khuyến nghị

### Bước 1. Chứng minh lớp mạng dùng HTTPS

Chạy:

```powershell
curl.exe -I http://localhost/api/health
curl.exe -k -i https://localhost/api/health
curl.exe -i https://demo.hackerlo.online/api/health
```

Điểm cần nói:

- `localhost` chứng minh rõ `HTTP -> HTTPS` bằng redirect `301`.
- `public domain` chứng minh hệ thống có thể được truy cập từ máy ngoài qua `HTTPS` khi `Cloudflare Tunnel` đang bật.
- `Nginx` đang phát `HSTS`, `CSP`, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`.
- Đây là lớp `network security`, không phải chỉ là cấu hình giao diện.

### Bước 2. Đăng nhập và nhận JWT

Mở một trong hai địa chỉ:

- `https://localhost/swagger-ui.html`
- `https://demo.hackerlo.online/swagger-ui.html`

Thao tác:

1. Gọi `POST /api/auth/login` bằng tài khoản `staff@securityapp.local`
2. Copy `accessToken`
3. Bấm `Authorize`
4. Dán `Bearer <accessToken>`

Kỳ vọng:

- Login đúng trả `200`
- Có `accessToken`, `tokenType=Bearer`, `role`

### Bước 3. Chứng minh `401` và `403`

Có thể làm bằng Swagger hoặc Postman trên `localhost` hoặc `public domain`.

Case bắt buộc:

1. `GET /api/auth/me` không có token trả `401`
2. Login bằng `user@securityapp.local`
3. Dùng token `USER` gọi `GET /api/customers` trả `403`
4. Dùng token `USER` gọi `GET /api/admin/summary` trả `403`

Điểm cần nói:

- `401` là chưa xác thực.
- `403` là đã xác thực nhưng sai quyền.
- Đây là bằng chứng tách rõ `Authentication` và `Authorization`.

### Bước 4. Tạo customer và chứng minh DB không lưu plaintext

Login bằng `staff` hoặc `admin`, rồi tạo customer qua Swagger, Postman hoặc UI.

Sau đó query DB:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,password_hash,role FROM users;"
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,phone_encrypted,address_encrypted,tax_code_encrypted,key_version FROM customers;"
```

Điểm cần chỉ ra:

- `password_hash` của user mới có prefix `$argon2id`
- không có plaintext password
- `phone_encrypted`, `address_encrypted`, `tax_code_encrypted` là ciphertext
- có `key_version`

Điểm cần nói:

- Password hash mới dùng `Argon2id`
- dữ liệu customer dùng `AES-GCM` với `HKDF`, `AAD` và `key_version`
- DB leak không đồng nghĩa lộ plaintext nhạy cảm

### Bước 5. Chứng minh tamper ciphertext sẽ decrypt fail

Lấy `id` customer vừa tạo rồi sửa tay ciphertext trong DB:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "UPDATE customers SET phone_encrypted=CONCAT('AA', SUBSTRING(phone_encrypted,3)) WHERE id=<ID_CUSTOMER>;"
```

Sau đó gọi lại API:

```powershell
curl.exe -k -i -H "Authorization: Bearer <STAFF_OR_ADMIN_TOKEN>" https://localhost/api/customers/<ID_CUSTOMER>
curl.exe -i -H "Authorization: Bearer <STAFF_OR_ADMIN_TOKEN>" https://demo.hackerlo.online/api/customers/<ID_CUSTOMER>
```

Kỳ vọng:

- API trả lỗi `500`
- backend không giải mã dữ liệu đã bị sửa

Điểm cần nói:

- `AES-GCM` bảo vệ cả bí mật lẫn tính toàn vẹn.
- Đây là bằng chứng `Integrity`, không chỉ là `Confidentiality`.

Sau demo có thể xóa row bị tamper:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "DELETE FROM customers WHERE id=<ID_CUSTOMER>;"
```

### Bước 6. Chứng minh DAST bằng OWASP ZAP

Chạy:

```powershell
docker run --rm --network project_mmud_default -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

Target scan hiện tại:

```text
https://nginx/swagger-ui.html
```

Giải thích:

- Đây là target nội bộ trong Docker network.
- Cùng surface này từ phía người dùng tương ứng với `https://localhost/swagger-ui.html` và `https://demo.hackerlo.online/swagger-ui.html`.

Kết quả artifact hiện có:

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

Hai nhóm warning còn lại:

- `CSP-related [10055]`
- `Modern Web Application [10109]`

Artifact để mở cho giảng viên:

- [docs/security/zap-baseline-report.html](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.html:1)
- [docs/security/zap-baseline-report.json](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.json:1)

## 5. Bảng kết quả mong đợi

| Test case | Kết quả mong đợi |
|---|---|
| Register user | `201` |
| Login đúng password | `200` + có JWT |
| Login sai password | `401` |
| Gọi API không JWT | `401` |
| Gọi API sai role | `403` |
| Password trong DB | Có `$argon2id`, không có plaintext |
| Dữ liệu nhạy cảm trong DB | Là ciphertext, không có plaintext |
| Tamper ciphertext hoặc tag | API lỗi do decrypt fail |
| `HTTP -> HTTPS` ở local origin | `301` |
| Public domain qua HTTPS | Truy cập `200` khi publish domain đang bật |
| ZAP baseline | `PASS=59`, `WARN=2`, `FAIL=0` |

Snapshot đã rà ngày `2026-06-21`:

- Local: `301` ở `http://localhost/api/health`, `200` ở `https://localhost/api/health`, Swagger local redirect `302` sang `/swagger-ui/index.html`, OpenAPI local trả `200`.
- Public: `https://demo.hackerlo.online/api/health` trả `200`.
- Smoke auth/authz: `register 201`, `login đúng 200 + JWT`, `login sai 401`, `no token 401`, `USER -> /api/customers = 403`.
- Storage: user mới có hash `$argon2id$...`, customer mới có `key_version = 2` và 3 cột dữ liệu nhạy cảm ở dạng ciphertext.
- Newman: local `15 requests`, `22 assertions`, `0 failed`; public domain `15 requests`, `22 assertions`, `0 failed`.
- ZAP baseline: `PASS=59`, `WARN=2`, `FAIL=0`.

## 6. Script nói nhanh trên lớp

Có thể nói theo mạch này:

> Hệ thống của em là ứng dụng mạng dạng cloud/API-based cho dịch vụ công ty nhỏ, gồm frontend Next.js, backend Spring Boot, Spring Security, JWT, MySQL và Nginx làm HTTPS edge.  
> Ở lớp mạng, em chứng minh local origin chuyển từ HTTP sang HTTPS bằng `301`, đồng thời public domain vẫn truy cập qua HTTPS khi publish bật.  
> Ở lớp xác thực và phân quyền, em demo login nhận JWT, request không token bị `401`, còn token sai role bị `403`.  
> Ở lớp lưu trữ, password không lưu plaintext mà hash bằng `Argon2id`, còn dữ liệu nhạy cảm của customer được mã hóa bằng `AES-GCM` kết hợp `HKDF`, `AAD` và `key_version`.  
> Em query trực tiếp DB để chứng minh chỉ thấy hash và ciphertext, sau đó tamper ciphertext để chứng minh backend không chấp nhận dữ liệu bị sửa.  
> Cuối cùng, em dùng OWASP ZAP để quét reverse proxy của stack đang chạy, kết quả hiện tại là `PASS 59`, `WARN 2`, `FAIL 0`.

## 7. Ghi chú thực tế

- `localhost` luôn nên được giữ lại để nhóm tự test nội bộ.
- `public domain` chỉ nên xem là lớp publish thêm cho demo bên ngoài.
- Cert local nằm ở `deploy/ssl/*.pem` và có thể sinh lại bằng cách xóa rồi chạy lại `docker compose up -d --build`.
- Không dùng `host.docker.internal` cho ZAP trong repo này để tránh quét nhầm dịch vụ khác trên máy host.
