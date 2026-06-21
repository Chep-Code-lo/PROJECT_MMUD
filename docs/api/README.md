# Tài liệu API

Thư mục này chứa tài liệu `OpenAPI/Swagger` của backend đang chạy thật.

## 1. File chính

- `openapi.json`: bản export từ `GET /v3/api-docs`

## 2. URL truy cập

| Hạng mục | Localhost | Public domain |
|---|---|---|
| Swagger UI | `https://localhost/swagger-ui.html` | `https://demo.hackerlo.online/swagger-ui.html` |
| OpenAPI JSON | `https://localhost/v3/api-docs` | `https://demo.hackerlo.online/v3/api-docs` |

Lưu ý:

- Ở runtime hiện tại, `.../swagger-ui.html` sẽ redirect `302` sang `.../swagger-ui/index.html`. Đây là hành vi bình thường của `springdoc-openapi`.
- `https://localhost/v3/api-docs` và `https://demo.hackerlo.online/v3/api-docs` đều đã được rà lại ngày `2026-06-21` và đều trả `200`.

Nếu chạy backend độc lập ngoài `Nginx` để phát triển nhanh, có thể dùng trực tiếp:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

## 3. Nhóm endpoint hiện có

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET/POST/PUT/DELETE /api/customers`
- `GET /api/audit-logs`
- `GET /api/admin/summary`
- `GET /api/health`

## 4. Matrix role tóm tắt

- Public: `POST /api/auth/register`, `POST /api/auth/login`, Swagger, health
- Authenticated: `GET /api/auth/me`
- `ADMIN`: `/api/admin/**`, `/api/audit-logs/**`
- `ADMIN`, `STAFF`: `/api/customers/**`

## 5. Tài khoản demo

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

## 6. Cách export lại `openapi.json`

Export từ local HTTPS edge:

```powershell
curl.exe -k https://localhost/v3/api-docs -o docs/api/openapi.json
```

Export từ public domain:

```powershell
curl.exe https://demo.hackerlo.online/v3/api-docs -o docs/api/openapi.json
```

Export từ backend chạy trực tiếp:

```powershell
curl.exe http://localhost:8080/v3/api-docs -o docs/api/openapi.json
```

## 7. Điều cần đối chiếu

OpenAPI cần phản ánh đúng các status code chính của runtime:

- `201` cho `register`, `create customer`
- `204` cho `delete customer`
- `401` cho request không có token vào endpoint cần auth
- `403` cho role không đủ quyền vào `/api/customers` hoặc `/api/audit-logs`
- `302` từ `/swagger-ui.html` sang `/swagger-ui/index.html`

Lưu ý:

- `openapi.json` hiện được export từ runtime có public server là `https://demo.hackerlo.online`.
- Khi test local, vẫn dùng đúng schema này qua `https://localhost/v3/api-docs`.
- OpenAPI dùng để tài liệu hóa endpoint, schema và flow cơ bản; các case security như `401`, `403`, role matrix nên đối chiếu thêm bằng Postman/Newman và ZAP.
