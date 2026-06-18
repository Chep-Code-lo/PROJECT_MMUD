# Báo cáo kiểm thử bảo mật API và ứng dụng

## 1. Phạm vi và nguồn đối chiếu

Báo cáo này được xây dựng từ source code thực tế trong các thư mục:

- `backend/`
- `frontend/`
- `database/`
- `deploy/`
- `docs/`
- `docker-compose.yml`

Các xác minh runtime đã được thực hiện trực tiếp ngày `2026-06-18` trên môi trường `docker compose` đang chạy tại:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- MySQL host port: `3307`

Nguyên tắc sử dụng trong tài liệu này:

- Chỉ ghi nhận những gì tìm thấy từ source code thực tế hoặc đã xác minh runtime.
- Nếu một tính năng không thấy implementation chạy thật, tài liệu sẽ ghi rõ: `Chưa xác định từ source code hiện tại.`
- Không tự giả định endpoint ngoài những endpoint có thật trong controller/OpenAPI.

### 1.1. Mục tiêu kiểm thử dưới góc nhìn mật mã ứng dụng

Nếu nhìn dự án này như một bài kiểm thử API thông thường thì chỉ cần kiểm `200/401/403/409` là chưa đủ. Với hướng mật mã ứng dụng, mục tiêu cần chứng minh phải cụ thể hơn:

1. Password của user không được lưu ở dạng có thể đọc lại.
2. Token phiên đăng nhập phải có khả năng chống giả mạo và chống sửa đổi.
3. Dữ liệu nhạy cảm của customer khi nằm trong database phải ở dạng không đọc được nếu chỉ lộ DB.
4. Dữ liệu khi truyền trên đường mạng phải có lớp bảo vệ transport phù hợp, nếu không thì bcrypt và AES ở backend vẫn chưa đủ.
5. Cơ chế mật mã phải được gắn đúng vào luồng nghiệp vụ thật, không chỉ tồn tại trong code nhưng không được dùng.

Vì vậy, phần trọng tâm của báo cáo này không phải là mô tả lý thuyết JWT, bcrypt hay AES nói chung, mà là chứng minh chính xác:

- cơ chế nào đang được dùng trong source hiện tại
- nó đang bảo vệ tài sản nào
- tester phải kiểm gì để kết luận cơ chế đó đang hoạt động đúng trong dự án này

### 1.2. Tài sản và bí mật cần bảo vệ

| Tài sản / bí mật | Nơi xuất hiện trong dự án | Cơ chế đang bảo vệ | Ý nghĩa mật mã ứng dụng |
|---|---|---|---|
| Password gốc của user | `POST /api/auth/register`, `POST /api/auth/login` | `BCryptPasswordEncoder` | Nếu DB lộ, attacker không đọc lại plaintext password trực tiếp |
| JWT access token | `AuthResponse.accessToken`, `localStorage`, header `Authorization` | HMAC `HS256` qua `JwtService` | Chống giả mạo/sửa token nếu secret chưa lộ |
| JWT secret | `APP_JWT_SECRET`, `app.jwt.secret` | Bí mật đối xứng của backend | Nếu lộ secret này thì attacker có thể forge token hợp lệ |
| AES app secret | `APP_AES_SECRET`, `app.aes.secret` | Nguồn để dẫn xuất AES key | Nếu lộ secret này thì attacker có thể giải mã toàn bộ dữ liệu customer đã mã hóa |
| `phone`, `address`, `taxCode` của customer | bảng `customers` | `AES/GCM/NoPadding` | Bảo vệ dữ liệu nhạy cảm khi lưu trữ |
| Dữ liệu login và token trên đường truyền | request frontend -> backend, deploy qua Nginx | TLS trong `deploy/nginx/securityapp.conf` | Chống nghe lén khi truyền password/JWT |
| Audit trace | bảng `audit_logs` | Không thấy mã hóa | Phục vụ truy vết, nhưng không phải vùng dữ liệu đang được mật mã hóa trong source hiện tại |

Điểm cần nhấn mạnh:

- bcrypt bảo vệ password khi lưu trữ, không bảo vệ password trên đường truyền.
- AES bảo vệ trường dữ liệu nhạy cảm trong DB, không bảo vệ JWT.
- JWT signature bảo vệ tính toàn vẹn và tính xác thực của token, không mã hóa nội dung payload.
- TLS là lớp bảo vệ lúc truyền, không thay thế cho bcrypt hay AES.

## 2. Kiến trúc hệ thống

| Tầng | Thực tế từ source | Nhận xét |
|---|---|---|
| Frontend | Next.js `14.2.x`, App Router trong `frontend/app`, gọi API qua `frontend/lib/axiosClient.ts` | JWT được lưu trong `localStorage` qua `frontend/lib/tokenStorage.ts` |
| Backend | Spring Boot `3.3.5`, Java `17`, REST API, Spring Security, JPA, Validation | Bảo vệ API bằng JWT stateless |
| Database | MySQL `8.4` trong Docker Compose, H2 in-memory cho profile `local` test | Schema chính nằm ở `database/schema.sql` |
| Docker | `docker-compose.yml` gồm `database`, `backend`, `frontend` | Chạy local bằng HTTP, chưa gắn Nginx/TLS vào compose |
| Reverse Proxy | Có file mẫu `deploy/nginx/securityapp.conf` | Có redirect `80 -> 443` và TLS `1.2/1.3`, nhưng chưa được nối vào compose hiện tại |
| API docs | `springdoc-openapi`, file export `docs/api/openapi.json` | Có Swagger UI, nhưng metadata security trong OpenAPI chưa phản ánh đầy đủ runtime auth |

### 2.1. Mô hình bảo vệ dữ liệu đầu cuối

| Bước dữ liệu | Dữ liệu đang đi qua | Cơ chế bảo vệ | Điều tester cần chứng minh |
|---|---|---|---|
| User nhập password để login | plaintext password | Chưa có mật mã ở client side; dự án trông chờ TLS ở transport | Nếu chạy production không có TLS thì password vẫn lộ trên đường truyền |
| Backend xác thực login | email + password | Spring Security `DaoAuthenticationProvider` + bcrypt verify | Password trong DB không phải plaintext và login chỉ pass khi hash match |
| Backend phát hành phiên | access token | JWT `HS256` | Token giả, token sửa, token hết hạn phải bị chặn |
| Frontend gọi API sau login | bearer token | `Authorization: Bearer ...` | Role matrix phải hoạt động đúng với token hợp lệ |
| Backend lưu dữ liệu customer | `phone`, `address`, `taxCode` | AES-GCM | DB phải chỉ thấy ciphertext |
| Backend đọc dữ liệu customer | ciphertext -> plaintext response | AES-GCM decrypt trong service | Nếu ciphertext hỏng thì phải fail, chứng minh GCM có integrity |
| User truy cập qua internet khi deploy | password, JWT, dữ liệu response | TLS qua Nginx | Phải có HTTPS để tránh sniffing |

Nhìn từ mô hình này, dự án đang dùng mật mã ở 3 lớp khác nhau:

- `bcrypt` cho bí mật xác thực ở trạng thái lưu trữ
- `JWT HMAC` cho phiên và quyền truy cập
- `AES-GCM` cho dữ liệu nhạy cảm của customer trong database

Đây là đúng tinh thần của mật mã ứng dụng: mỗi loại dữ liệu được bảo vệ bằng cơ chế phù hợp với vai trò của nó, thay vì cố dùng một kỹ thuật cho mọi thứ.

### 2.2. Frontend

Route thực tế từ source:

- `/login`
- `/register`
- `/dashboard`
- `/customers`
- `/customers/new`
- `/audit-logs`
- `/` redirect sang `/login`

Luồng frontend thực tế:

1. Người dùng login ở `/login`.
2. `authService.login()` gọi `POST /api/auth/login`.
3. `accessToken` được lưu vào `localStorage`.
4. `axiosClient` tự gắn `Authorization: Bearer <token>` cho request sau đó.
5. `ProtectedRoute` gọi `GET /api/auth/me` để xác thực phiên và kiểm tra role.
6. Logout chỉ là xóa token ở frontend, không có logout endpoint ở backend.

### 2.3. Backend

Kiến trúc backend thực tế:

- Controller -> Service -> Repository -> Entity
- DTO được dùng cho request/response
- `GlobalExceptionHandler` chuẩn hóa lỗi `400/409/500`
- `SecurityConfig` xử lý `401/403` dạng JSON

### 2.4. Database

Bảng thực tế trong `database/schema.sql`:

- `users`
- `customers`
- `audit_logs`

Không tìm thấy bảng:

- `refresh_tokens`
- `oauth_clients`
- `tickets`
- `file_uploads`

### 2.5. Docker và Reverse Proxy

`docker-compose.yml` hiện tại chạy:

- `securityapp-db`
- `securityapp-backend`
- `securityapp-frontend`

`deploy/nginx/securityapp.conf` tồn tại để deploy thật:

- redirect HTTP sang HTTPS
- proxy `/` về frontend
- proxy `/api/*`, `/swagger-ui/*`, `/v3/api-docs/*` về backend

Nhưng Nginx chưa được định nghĩa trong `docker-compose.yml` hiện tại.

## 3. Inventory source code

### 3.1. Controller

- `AuthController`: `register`, `login`, `me`
- `CustomerController`: customer CRUD
- `AdminController`: summary thống kê
- `AuditLogController`: list audit logs
- `SystemController`: health check

### 3.2. Service

- `AuthService`: register, login, current user
- `CustomerService`: CRUD customer, gọi AES encrypt/decrypt, ghi audit log
- `AuditLogService`: ghi và trả audit logs
- `EncryptionService`: AES-GCM encrypt/decrypt

### 3.3. Repository

- `UserRepository`
- `CustomerRepository`
- `AuditLogRepository`

### 3.4. Entity

- `User`
- `Customer`
- `AuditLog`
- `Role`

### 3.5. DTO

- `RegisterRequest`
- `LoginRequest`
- `AuthResponse`
- `UserResponse`
- `CustomerRequest`
- `CustomerResponse`
- `AuditLogResponse`
- `ApiResponse`

Ghi chú:

- `ApiResponse` hiện đang là class rỗng, không thấy được dùng trong controller/service hiện tại.

### 3.6. Config

- `SecurityConfig`
- `CorsConfig`
- `SwaggerConfig`
- `DemoUserInitializer`

### 3.7. Security

- `JwtAuthenticationFilter`
- `JwtService`
- `CustomUserDetailsService`

### 3.8. Test có sẵn trong repo

- `AuthSecurityIntegrationTest`
- `CustomerControllerIntegrationTest`
- `AuditLogIntegrationTest`
- `SecurityAppApplicationTests`

## 4. Chức năng bảo mật tìm thấy từ source

| Hạng mục | Trạng thái | Bằng chứng từ source | Nhận xét |
|---|---|---|---|
| JWT | Có | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService` | Cơ chế auth chính của backend |
| OAuth2 | Chưa xác định từ source code hiện tại | Chỉ thấy dependency `spring-boot-starter-oauth2-client` trong `backend/pom.xml` | Không thấy `oauth2Login()`, callback, provider config, refresh token flow |
| BCrypt | Có | `passwordEncoder()` trả `new BCryptPasswordEncoder()` | Dùng cho register và demo users |
| AES-GCM | Có | `EncryptionService` dùng `AES/GCM/NoPadding` | Mã hóa `phone`, `address`, `taxCode` |
| RSA | Không tìm thấy | Không thấy class/config/keypair RSA | Không nên mô tả là đang dùng |
| SHA-256 | Có | `JwtService` và `EncryptionService` | Chỉ dùng để dẫn xuất key từ secret string |
| Refresh Token | Không tìm thấy | Không có entity/table/endpoint tương ứng | Không có trong runtime hiện tại |
| Logout endpoint | Không tìm thấy | `SecurityConfig` disable logout | Frontend chỉ xóa token local |
| File Upload | Không tìm thấy | Không có `MultipartFile`/upload endpoint | Không đưa vào phạm vi test |
| Swagger/OpenAPI | Có | `SwaggerConfig`, `docs/api/openapi.json` | Có `/swagger-ui.html` và `/v3/api-docs` |
| Postman/Newman | Có | `docs/postman/` | Có collection và environment chạy local |
| OWASP ZAP | Có | `docs/security/zap.yaml`, report HTML/JSON/XML | Report baseline đã có sẵn |
| HTTPS/TLS | Có ở mức deploy config | `deploy/nginx/securityapp.conf`, `deploy/ssl/README.md` | Chưa bật trong compose local |

### 4.1. Bản đồ vật liệu mật mã trong source

| Vật liệu mật mã | Nguồn | Cách dùng trong source | Nhận xét |
|---|---|---|---|
| `APP_JWT_SECRET` | env / `application.properties` | `JwtService.deriveKey()` -> `Keys.hmacShaKeyFor(...)` | Đây là bí mật đối xứng dùng để ký token |
| `APP_AES_SECRET` | env / `application.properties` | `EncryptionService.deriveKey()` -> `SecretKeySpec(..., "AES")` | Đây là bí mật gốc để dẫn xuất key AES |
| Salt bcrypt | phát sinh nội bộ bởi BCrypt | không lưu riêng, nằm trong chuỗi hash | Đây là hành vi đúng của bcrypt |
| IV GCM | `SecureRandom.nextBytes(iv)` | ghép vào đầu payload trước khi Base64 | Mỗi lần encrypt tạo IV mới |
| Authentication tag GCM | sinh ra khi `cipher.doFinal(...)` | nằm trong phần encrypted payload | Dùng để phát hiện ciphertext bị sửa |

Điểm quan trọng về mặt mật mã ứng dụng:

- Dự án hiện không dùng RSA, ECC hay key pair bất đối xứng.
- JWT và AES đều đang phụ thuộc vào cùng kiểu secret text nhập từ cấu hình, sau đó băm `SHA-256` để lấy key.
- Không thấy key rotation, key versioning hoặc secret separation theo tenant/user.

### 4.2. Ranh giới của mật mã trong dự án

Những gì đang được bảo vệ bởi mật mã:

- password user trong DB
- `phone`, `address`, `taxCode` của customer trong DB
- tính toàn vẹn và xác thực của JWT

Những gì hiện không được mã hóa trong source:

- `email` và `name` của customer
- nội dung `audit_logs.details`
- `actorEmail` trong audit log
- JWT payload

Ý nghĩa:

- JWT payload không được mã hóa là bình thường, vì JWT ở đây đang dùng để ký chứ không phải để che giấu dữ liệu.
- `email` customer để plaintext là trade-off thực tế của source hiện tại, vì repository cần check unique theo email.
- Audit log hiện nghiêng về truy vết hơn là bảo mật nội dung log bằng mật mã.

Do đó, khi kiểm thử theo hướng mật mã ứng dụng, không nên viết kết luận kiểu “hệ thống đã mã hóa toàn bộ dữ liệu nhạy cảm”. Kết luận đúng phải là:

- hệ thống đã mã hóa một phần dữ liệu business nhạy cảm cụ thể của customer
- nhưng vẫn còn nhiều trường plaintext vì nhu cầu truy vấn, định danh hoặc truy vết

## 5. Danh sách API thực tế

Quy ước lỗi từ source hiện tại:

- `400`: validation lỗi hoặc request body malformed
- `401`: thiếu token hoặc token không hợp lệ
- `403`: token hợp lệ nhưng role không đủ quyền
- `404`: object không tồn tại
- `409`: conflict dữ liệu
- `500`: lỗi server không mong muốn

| Module | Method | URL | Request DTO | Response DTO | Auth required | Role required | Ghi chú |
|---|---|---|---|---|---|---|---|
| System | `GET` | `/api/health` | Không có | `Map<String,Object>` | Không | Public | Trả `status`, `service`, `activeProfiles` |
| Auth | `POST` | `/api/auth/register` | `RegisterRequest` | `UserResponse` | Không | Public | Thành công `201`, role mặc định `USER` |
| Auth | `POST` | `/api/auth/login` | `LoginRequest` | `AuthResponse` | Không | Public | Trả `accessToken`, `tokenType`, `role`, `user` |
| Auth | `GET` | `/api/auth/me` | Không có | `UserResponse` | Có | Bất kỳ user đã login | Trả user hiện tại lấy từ SecurityContext |
| Customers | `GET` | `/api/customers` | Không có | `List<CustomerResponse>` | Có | `ADMIN`, `STAFF` | Không có pagination |
| Customers | `GET` | `/api/customers/{id}` | Path `id` | `CustomerResponse` | Có | `ADMIN`, `STAFF` | `404` nếu không tìm thấy |
| Customers | `POST` | `/api/customers` | `CustomerRequest` | `CustomerResponse` | Có | `ADMIN`, `STAFF` | Thành công `201`, dữ liệu nhạy cảm được AES ở backend |
| Customers | `PUT` | `/api/customers/{id}` | `CustomerRequest` | `CustomerResponse` | Có | `ADMIN`, `STAFF` | `404` hoặc `409` |
| Customers | `DELETE` | `/api/customers/{id}` | Path `id` | Không có body | Có | `ADMIN`, `STAFF` | Thành công `204` |
| Audit | `GET` | `/api/audit-logs` | Không có | `List<AuditLogResponse>` | Có | `ADMIN` | Không có pagination/filter |
| Admin | `GET` | `/api/admin/summary` | Không có | `Map<String,Long>` | Có | `ADMIN` | Trả `users`, `customers`, `auditLogs` |

### 5.1. DTO request thực tế

`RegisterRequest`

- `fullName`: required, max `150`
- `email`: required, email format, max `255`
- `password`: required, min `8`, max `100`

`LoginRequest`

- `email`: required, email format
- `password`: required, min `8`, max `100`

`CustomerRequest`

- `name`: required, max `150`
- `email`: required, email format, max `255`
- `phone`: required, max `50`
- `address`: required, max `500`
- `taxCode`: required, max `50`

### 5.2. Ghi chú về OpenAPI hiện tại

File `docs/api/openapi.json` có khai báo `bearerAuth` trong `components.securitySchemes`, nhưng từng operation hiện đang không có `security` requirement tương ứng.

Kết luận:

- Swagger/OpenAPI có thể dùng để xem schema và gọi thử API.
- Nhưng metadata auth trong tài liệu hiện tại chưa phản ánh đầy đủ runtime security matrix.

## 6. Ma trận kiểm thử

| Module | API/Chức năng | Trọng tâm bảo mật | Mức ưu tiên |
|---|---|---|---|
| Authentication | `register`, `login`, `me` | Validation, JWT issuance, brute force, 401 | Rất cao |
| JWT | Token structure, exp, signature, forgery, tamper | Broken Authentication, authn/authz | Rất cao |
| Authorization | `/api/admin/**`, `/api/audit-logs/**`, `/api/customers/**` | Role matrix, 401/403 | Rất cao |
| User | Public register, current user | Data exposure, auth flow | Cao |
| Admin | Summary thống kê | Function-level authorization | Cao |
| Customer | CRUD và dữ liệu nhạy cảm | AES, data exposure, validation | Rất cao |
| Audit Log | Login success/failed, CRUD log | Truy vết, phân quyền ADMIN | Cao |
| Sensitive Data | `phone`, `address`, `taxCode` | Confidentiality, integrity | Rất cao |
| Swagger/OpenAPI | `/swagger-ui.html`, `/v3/api-docs` | Tài liệu hóa, auth metadata, public exposure | Cao |
| Postman/Newman | Collection hiện tại | Regression, 401/403, basic assertions | Cao |
| OWASP ZAP | Baseline report và import OpenAPI | Passive/active scan | Cao |
| OAuth2 | Dependency có trong pom | Chưa xác định từ source code hiện tại | Thấp |
| Refresh Token | Không có endpoint/table | Chưa triển khai | Trung bình |
| Logout | Frontend local logout | Không có revoke/blacklist | Trung bình |
| File Upload | Không tìm thấy | Ngoài phạm vi hiện tại | Không áp dụng |
| Docker Security | Compose, Dockerfile, ports, secrets | Hardcoded secret, exposed ports, container user | Rất cao |
| HTTPS/TLS | Compose local và Nginx deploy config | HTTP/HTTPS, redirect, certificate | Rất cao |

## 7. Hướng dẫn truy cập hệ thống

### 7.1. Chạy hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up -d --build
docker compose ps
```

Service dự kiến:

- `securityapp-db`
- `securityapp-backend`
- `securityapp-frontend`

### 7.2. URL thực tế

- Frontend: `http://localhost:3000`
- Backend API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/api/health`
- MySQL host: `localhost:3307`

### 7.3. Tài khoản demo

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

### 7.4. Nginx/TLS

Trong source có cấu hình deploy:

- redirect HTTP sang HTTPS
- TLS `1.2` và `1.3`

Nhưng domain deploy thật:

- `Chưa xác định từ source code hiện tại.`

Lý do:

- `docker-compose.yml` hiện tại chưa khai báo service Nginx và chưa mount cert.

## 8. Kết quả xác minh runtime đã thực hiện

| Kiểm tra | Kết quả thực tế |
|---|---|
| `GET /api/health` | `200`, profile đang chạy là `dev` |
| Login admin | `200`, trả `accessToken`, `tokenType=Bearer`, `role=ADMIN` |
| `GET /api/auth/me` với JWT hợp lệ | `200` |
| `GET /api/auth/me` không gửi JWT | `401` |
| `GET /api/auth/me` với JWT giả chữ ký sai | `401` |
| `GET /api/auth/me` với JWT bị sửa | `401` |
| `GET /api/auth/me` với JWT hết hạn | `401` |
| `GET /api/auth/me` với JWT ký đúng nhưng `sub` không tồn tại trong DB | `401` |
| `GET /api/admin/summary` bằng token `USER` | `403` |
| `GET /api/customers` bằng token `STAFF` | `200` |
| `GET /api/customers` bằng token `USER` | `403` |
| `POST /api/auth/register` với password `123456` | `400`, lỗi validation vì min length là `8` |
| DB `users.password_hash` | Dạng `$2a$10$...`, cùng password nhưng hash khác nhau |
| DB `customers.phone_encrypted/address_encrypted/tax_code_encrypted` | Lưu ciphertext, không phải plaintext |
| Sửa/cắt ciphertext trong DB rồi gọi `GET /api/customers/{id}` | `500`, dữ liệu lỗi không giải mã được |
| `mvn test` trong `backend/` | Pass `8` test, `0` failures |
| `npm run build` trong `frontend/` | Build pass thành công |

### 8.1. Chuỗi chứng minh mật mã ứng dụng từ runtime

Nếu cần trình bày ngắn gọn nhưng đúng trọng tâm trước giảng viên hoặc reviewer, chuỗi chứng minh có thể đi theo thứ tự này:

1. Login thành công để lấy JWT thật.
   Ý nghĩa: chứng minh hệ thống dùng password đã hash để xác thực, sau đó mới phát hành token.
2. Decode JWT và kiểm tra `sub`, `iat`, `exp`, `alg`.
   Ý nghĩa: chứng minh access token không phải chuỗi ngẫu nhiên vô danh mà là token có cấu trúc, có hạn dùng và có chữ ký.
3. Gọi `GET /api/auth/me` với token hợp lệ, token giả, token sửa, token hết hạn.
   Ý nghĩa: chứng minh Spring Security đang kiểm integrity và validity của token thật.
4. Gọi `/api/admin/summary` hoặc `/api/customers` bằng token `USER`.
   Ý nghĩa: chứng minh token hợp lệ vẫn bị chặn nếu quyền không đủ.
5. Tạo customer mới rồi query DB.
   Ý nghĩa: chứng minh business flow thật có gắn AES vào dữ liệu nhạy cảm trước khi lưu.
6. Sửa ciphertext trong DB rồi gọi API đọc lại.
   Ý nghĩa: chứng minh AES-GCM không chỉ che giấu dữ liệu mà còn phát hiện dữ liệu bị sửa.
7. Query bảng `users`.
   Ý nghĩa: chứng minh password trong DB ở dạng bcrypt hash khác nhau, không phải plaintext.

Đây là chuỗi bằng chứng đúng với hướng mật mã ứng dụng, vì nó nối được:

- đầu vào người dùng
- cơ chế mật mã trong code
- trạng thái dữ liệu trong DB
- hành vi bảo mật ở runtime

## 9. Đánh giá JWT

### 9.1. Cách JWT được triển khai

Từ source hiện tại:

- `JwtService` dùng `JJWT`
- Secret HMAC được dẫn xuất bằng `SHA-256(secret)`
- JJWT ký theo `HS256`
- Claims được set khi generate token:
  - `sub`
  - `iat`
  - `exp`
- Không thấy set thêm:
  - `role`
  - `iss`
  - `aud`
  - `jti`
  - `nbf`

### 9.2. Luồng JWT end-to-end trong dự án

Luồng JWT thực tế trong source hiện tại là:

1. `AuthController.login()` nhận `LoginRequest`.
2. `AuthService.login()` gọi `authenticationManager.authenticate(...)`.
3. `DaoAuthenticationProvider` dùng `CustomUserDetailsService` + `BCryptPasswordEncoder` để verify password.
4. Nếu xác thực thành công, `JwtService.generateToken(user)` tạo JWT mới.
5. Frontend lưu `accessToken` vào `localStorage`.
6. `axiosClient` tự động gắn `Authorization: Bearer <token>`.
7. `JwtAuthenticationFilter` lấy token từ header, parse chữ ký và đọc `sub`.
8. Filter load lại user thật từ DB bằng email trong `sub`.
9. Nếu token hợp lệ và user tồn tại, `SecurityContext` được set.
10. `SecurityConfig` mới là nơi quyết định quyền route dựa trên role của user trong DB.

Ý nghĩa mật mã ứng dụng của luồng này:

- password chỉ được dùng để mở phiên, không đi lại trong các request business sau login
- JWT trở thành vật mang quyền truy cập ngắn hạn
- chữ ký của JWT chỉ là bước đầu; role cuối cùng vẫn lấy từ DB, giúp giảm rủi ro nếu client cố sửa claim role

### 9.3. JWT runtime đã decode

Mẫu token admin lấy trực tiếp sau login ngày `2026-06-18` có nội dung:

| Thành phần | Giá trị thực tế |
|---|---|
| Header `alg` | `HS256` |
| Header `typ` | Không thấy trong token thực tế |
| Payload `sub` | `admin@securityapp.local` |
| Payload `iat` | `2026-06-18T14:59:56Z` |
| Payload `exp` | `2026-06-19T14:59:56Z` |
| Payload `role` | Không có trong token thực tế |
| Signature | Có, dạng HMAC SHA-256 |
| Lifetime | `86400` giây, tương đương `24` giờ |

### 9.4. Ý nghĩa mật mã học của header, payload, signature

Từ token runtime thực tế của dự án này có thể rút ra 4 điểm quan trọng:

1. `alg=HS256` nghĩa là đây là token ký bằng khóa đối xứng.
   Điều này có nghĩa ai giữ được `APP_JWT_SECRET` sẽ có khả năng tạo token hợp lệ.
2. Payload chỉ chứa `sub`, `iat`, `exp`.
   Điều này tốt ở góc nhìn giảm lộ dữ liệu, vì token không chứa thông tin business nhạy cảm hay PII thừa.
3. Payload của JWT không được mã hóa.
   Đây là hành vi bình thường của JWT ký; token này đảm bảo integrity/authenticity chứ không đảm bảo confidentiality.
4. `exp` cách `iat` đúng `24` giờ.
   Điều này cho thấy token có hạn dùng, nhưng TTL còn khá dài nếu áp dụng production.

Nói cách khác:

- chữ ký JWT trong dự án này đang bảo vệ `tính toàn vẹn` và `tính xác thực`
- nó không hề che giấu nội dung payload
- vì vậy thiết kế hiện tại đúng khi không nhét dữ liệu nhạy cảm của customer vào token

### 9.5. Ý nghĩa kiểm thử quan trọng

Điểm rất quan trọng của source hiện tại:

- Role không nằm trong JWT.
- `JwtAuthenticationFilter` chỉ dùng JWT để lấy `sub` rồi tải user thật từ DB qua `CustomUserDetailsService`.
- Quyền được quyết định bởi role trong bảng `users`, không phải từ claim `role` trong token.

Hệ quả kiểm thử:

- Test “sửa claim role từ USER thành ADMIN” không có ý nghĩa thực tế với source hiện tại vì runtime không dùng claim đó.
- Test đúng hơn là:
  - token sai chữ ký
  - token hết hạn
  - token có `sub` không tồn tại
  - token hợp lệ nhưng user role không đủ quyền

### 9.6. Bảng test JWT

| Test case | Expected | Actual | Giải thích |
|---|---|---|---|
| JWT hợp lệ gọi `/api/auth/me` | `200` | `200` | Filter xác thực thành công, `SecurityContext` được set |
| Không gửi JWT gọi `/api/auth/me` | `401` | `401` | `SecurityConfig` trả lỗi auth |
| JWT giả chữ ký sai | `401` | `401` | JJWT parse fail, filter clear context |
| JWT bị sửa | `401` | `401` | Signature không còn hợp lệ |
| JWT hết hạn | `401` | `401` | `isTokenExpired()` trả true |
| JWT user gọi `/api/admin/summary` | `403` | `403` | Token hợp lệ nhưng role không đủ quyền |
| JWT userA truy cập dữ liệu userB | `Chưa xác định từ source code hiện tại.` | `Chưa xác định từ source code hiện tại.` | Không có endpoint user-owned kiểu `/api/users/{id}` để test BOLA userA/userB |
| JWT ký đúng nhưng `sub` không tồn tại trong DB | `401` | `401` | `loadUserByUsername()` ném exception, request rơi về unauthorized |

### 9.7. Đánh giá Spring Security

Điểm tốt:

- Stateless session
- Tách rõ `401` và `403`
- Role matrix được khai báo tập trung trong `SecurityConfig`
- Role lấy từ DB, không tin claim `role` phía client

Điểm còn thiếu:

- Không có refresh token
- Không có token revocation/blacklist
- Token TTL `24h` khá dài cho môi trường production
- Frontend lưu JWT trong `localStorage`
- Secret mặc định đang hardcode trong `docker-compose.yml` và `application.properties`

Từ góc nhìn mật mã ứng dụng, điểm đáng lưu ý nhất không phải là “JWT có dùng hay không”, mà là:

- secret ký token đang là tài sản cực kỳ nhạy cảm
- token không được mã hóa nên không được đặt dữ liệu nhạy cảm vào payload
- lớp ký token chỉ an toàn khi secret không lộ và transport có TLS

## 10. Đánh giá OAuth2

### 10.1. Những gì tìm thấy

| Hạng mục | Kết quả |
|---|---|
| Dependency OAuth2 client trong `pom.xml` | Có |
| `oauth2Login()` trong `SecurityConfig` | Không thấy |
| `ClientRegistration` / provider config trong `application*.properties` | Không thấy |
| OAuth2 controller/callback endpoint | Không thấy |
| OAuth2 URL trong OpenAPI/Postman | Không thấy |

### 10.2. Kết luận

Đối với Phase 6:

- OAuth2 login flow: `Chưa xác định từ source code hiện tại.`
- OAuth2 callback: `Chưa xác định từ source code hiện tại.`
- OAuth2 access token: `Chưa xác định từ source code hiện tại.`
- OAuth2 refresh token: `Chưa xác định từ source code hiện tại.`

Nhận xét thực tế:

- Repo có dependency mở đường cho OAuth2.
- Nhưng source đang chạy thật hiện nay không có implementation OAuth2 để kiểm thử chức năng.

### 10.3. Ý nghĩa với hướng mật mã ứng dụng

Với đề tài này, việc có dependency OAuth2 trong `pom.xml` chưa đủ để xem như dự án đang có một cơ chế mật mã/ủy quyền thứ hai đã sẵn sàng kiểm thử. Để đánh giá đúng theo source hiện tại:

- cơ chế xác thực đang vận hành thật là `username/password + JWT`
- OAuth2 hiện mới ở mức khả năng mở rộng, chưa phải bằng chứng bảo mật đang chạy thật

Do đó, không nên ghi trong báo cáo rằng dự án “đã triển khai OAuth2 login” nếu không có:

- provider config
- redirect URI
- callback handler
- token exchange
- phiên đăng nhập OAuth2 chạy được ở runtime

## 11. Đánh giá bcrypt

### 11.1. Bằng chứng từ source

| Điểm kiểm tra | Kết quả |
|---|---|
| `PasswordEncoder` có phải `BCryptPasswordEncoder` không | Có |
| Register có dùng encoder không | Có, trong `AuthService.register()` |
| Demo users có dùng encoder không | Có, trong `DemoUserInitializer` |
| Xác thực password có dùng Spring Security provider không | Có, `DaoAuthenticationProvider` |

### 11.2. Ý nghĩa mật mã ứng dụng của bcrypt trong dự án

Trong dự án này, bcrypt không được dùng để “mã hóa password” mà để:

- làm password không thể đọc ngược trực tiếp từ DB
- làm cho cùng một password sinh ra hash khác nhau nhờ salt
- buộc backend phải xác thực bằng phép so khớp hash thay vì đọc plaintext

Ý nghĩa thực tế nếu DB bị lộ:

- attacker không thể chỉ nhìn bảng `users` rồi biết ngay password gốc
- việc bẻ hash sẽ tốn công hơn nhiều so với lưu plaintext hoặc hash yếu

Điểm này rất quan trọng với hướng mật mã ứng dụng vì nó chứng minh hệ thống đang bảo vệ `bí mật xác thực` ở trạng thái lưu trữ, không chỉ bảo vệ dữ liệu business.

### 11.3. Ghi chú quan trọng về test password `123456`

Yêu cầu test “tạo 3 tài khoản với password `123456`” không đi được đến bước hash trong source hiện tại vì:

- `RegisterRequest.password` yêu cầu min `8`
- Runtime đã xác minh `POST /api/auth/register` với `123456` trả `400`

Do đó:

- Test đúng theo source hiện tại là dùng cùng một password hợp lệ có độ dài từ `8` trở lên
- Hoặc dùng 3 tài khoản demo đang cùng password `Password@123`

### 11.4. Bằng chứng DB runtime

Từ bảng `users` runtime:

| Email | Role | Prefix hash thực tế | Nhận xét |
|---|---|---|---|
| `admin@securityapp.local` | `ADMIN` | `$2a$10$...` | bcrypt |
| `staff@securityapp.local` | `STAFF` | `$2a$10$...` | bcrypt |
| `user@securityapp.local` | `USER` | `$2a$10$...` | bcrypt |

Nhận xét:

- Cả 3 tài khoản demo cùng mật khẩu nhưng hash khác nhau.
- Điều này cho thấy bcrypt đang dùng salt ngẫu nhiên đúng như kỳ vọng.
- Không thấy plaintext password trong DB.

### 11.5. Cách đọc chuỗi bcrypt từ DB

Từ dữ liệu runtime đã query được, các hash đều bắt đầu bằng dạng:

```text
$2a$10$...
```

Ý nghĩa của phần prefix này trong bối cảnh dự án:

- `$2a$`: biến thể bcrypt đang được thư viện sinh ra
- `10`: cost factor đang dùng là `10`

Điều này cho phép tester kết luận:

- hệ thống không chỉ “trông giống bcrypt”, mà hash runtime thực tế cũng mang format đặc trưng của bcrypt
- cost factor không phải là giá trị trống hay hash do tự cài tay

### 11.6. Kết luận bcrypt

| Tiêu chí | Đánh giá |
|---|---|
| Không lưu plaintext | Đạt |
| Hash khác nhau khi cùng password | Đạt |
| Có salt | Đạt, suy ra từ hash khác nhau |
| Plaintext trong DB | Không phát hiện |

Nếu phát hiện plaintext:

- Mức độ sẽ là `Critical`.

Hiện tại:

- Không có dấu hiệu lưu plaintext từ source và DB runtime đã kiểm tra.
- bcrypt đang là lớp bảo vệ đúng chỗ cho password at rest trong dự án này.

## 12. Đánh giá AES

### 12.1. Dữ liệu nào được mã hóa

`CustomerService.applyRequest()` mã hóa các trường:

- `phone` -> `phone_encrypted`
- `address` -> `address_encrypted`
- `taxCode` -> `tax_code_encrypted`

Không thấy mã hóa:

- `email`
- `name`

### 12.2. Ý nghĩa mật mã ứng dụng của các trường được mã hóa

Ba trường đang được mã hóa là:

- `phone`
- `address`
- `taxCode`

Đây đều là dữ liệu business nhạy cảm có khả năng gây ảnh hưởng thực tế nếu lộ:

- `phone`: thông tin liên hệ cá nhân/doanh nghiệp
- `address`: vị trí, địa chỉ giao dịch
- `taxCode`: mã số doanh nghiệp/thuế, mang giá trị định danh

Trong khi đó `email` và `name` vẫn ở plaintext. Từ source hiện tại có thể thấy:

- `email` còn được dùng để check unique trong repository
- `name` và `email` được trả thẳng cho UI

Điều này cho thấy một trade-off rất điển hình của mật mã ứng dụng:

- trường nào cần truy vấn/đối chiếu trực tiếp thì thường bị giữ plaintext hoặc phải có thiết kế chỉ mục riêng
- trường nào nhạy cảm hơn và không cần query phức tạp thì dễ được đưa vào AES encryption

Vì vậy, kết luận đúng không phải là “toàn bộ bản ghi customer đều đã được mã hóa”, mà là:

- source hiện tại đang bảo vệ có chọn lọc các trường được xem là nhạy cảm nhất của customer

### 12.3. Cách AES được triển khai

| Thuộc tính | Giá trị thực tế |
|---|---|
| Algorithm | `AES` |
| Transformation | `AES/GCM/NoPadding` |
| IV length | `12` bytes |
| Authentication tag | `128` bits |
| Key derivation | `SHA-256(secret)` |
| Payload format | `Base64(IV + ciphertext+tag)` |

### 12.4. Luồng dữ liệu mã hóa từ request đến DB rồi quay lại response

Luồng thực tế từ source:

1. UI gửi `CustomerRequest` với plaintext `phone/address/taxCode`.
2. `CustomerService.applyRequest()` gọi `encryptionService.encrypt(...)` cho từng trường.
3. Entity `Customer` chỉ lưu các cột `_encrypted`.
4. Khi đọc dữ liệu, `CustomerService.toResponse()` lại gọi `decrypt(...)`.
5. API trả `CustomerResponse` ở dạng plaintext business data cho client đã được authorize.

Ý nghĩa mật mã ứng dụng:

- client không tự mã hóa trước khi gửi; trách nhiệm mã hóa nằm ở backend
- DB là nơi nhìn thấy ciphertext
- API response chỉ được giải mã sau khi request đã qua lớp authn/authz

### 12.5. Bằng chứng runtime

DB runtime hiện lưu:

- `phone_encrypted`, `address_encrypted`, `tax_code_encrypted` dưới dạng ciphertext Base64
- API `GET /api/customers` trả plaintext đã giải mã

Điều này xác nhận:

- Dữ liệu nhạy cảm không nằm plaintext trong DB
- Ứng dụng giải mã lại ở backend trước khi trả `CustomerResponse`

### 12.6. Kiểm thử sai key, sai IV, ciphertext sửa, ciphertext cắt bớt

| Tình huống | Kết quả suy ra từ source | Kết quả runtime | Nhận xét |
|---|---|---|---|
| Sai key | Giải mã fail, ném `IllegalStateException` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Do GCM tag không khớp |
| Sai IV | Giải mã fail | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Do GCM tag không khớp |
| Ciphertext bị sửa | Giải mã fail | Đã xác minh tương đương bằng case sửa ciphertext, API trả `500` | Integrity của GCM hoạt động |
| Ciphertext cắt bớt | Giải mã fail | Đã xác minh runtime, `GET /api/customers/{id}` trả `500` | Payload bị hỏng không giải mã được |

### 12.7. Ý nghĩa của GCM tag, IV ngẫu nhiên và cách dẫn xuất key

Ba điểm mật mã quan trọng nhất của implementation hiện tại là:

1. `IV` được tạo mới bằng `SecureRandom` cho mỗi lần encrypt.
   Điều này phù hợp với GCM, vì nếu tái sử dụng IV với cùng key sẽ rất nguy hiểm.
2. Payload chứa cả `IV + ciphertext + tag`.
   Điều này cho phép backend giải mã đúng record mà không cần cột IV riêng.
3. Key được dẫn xuất bằng `SHA-256(secret)` từ một secret text cấu hình.
   Cách này đủ để tạo key có độ dài phù hợp cho runtime hiện tại, nhưng vẫn là một static app-wide key.

Ý nghĩa rủi ro:

- nếu `APP_AES_SECRET` lộ, toàn bộ dữ liệu customer đã mã hóa bằng cùng secret đều có nguy cơ bị giải mã
- nếu ciphertext bị sửa, GCM tag sẽ làm decrypt fail, đó là lý do case tamper trả lỗi thay vì cho ra dữ liệu rác
- source hiện tại chưa có key rotation hoặc key versioning, nên mọi record cùng phụ thuộc vào một bí mật gốc

### 12.8. Đánh giá confidentiality và integrity

| Tiêu chí | Đánh giá |
|---|---|
| Confidentiality | Đạt ở mức source hiện tại vì DB chỉ lưu ciphertext |
| Integrity | Đạt ở mức crypto vì AES-GCM phát hiện ciphertext bị sửa |
| Error handling khi dữ liệu mã hóa bị hỏng | Chưa tối ưu vì hiện trả `500` generic |

Kết luận theo hướng mật mã ứng dụng:

- lớp bảo vệ dữ liệu at rest của customer đang được triển khai thật và đang hoạt động
- bằng chứng mạnh nhất là DB chỉ có ciphertext, còn tamper ciphertext làm decrypt fail

## 13. Đánh giá HTTPS/TLS

### 13.1. Trạng thái thực tế

| Hạng mục | Kết quả |
|---|---|
| Backend trong `docker-compose.yml` | HTTP `:8080` |
| Frontend trong `docker-compose.yml` | HTTP `:3000` |
| Reverse proxy Nginx mẫu | Có |
| HTTP -> HTTPS redirect | Có trong `deploy/nginx/securityapp.conf` |
| TLS version | `TLSv1.2`, `TLSv1.3` trong Nginx config |
| Certificate path | `/etc/nginx/ssl/fullchain.pem`, `/etc/nginx/ssl/privkey.pem` |

### 13.2. Ý nghĩa của TLS trong chuỗi mật mã ứng dụng

TLS là lớp rất dễ bị xem nhẹ khi đã có JWT, bcrypt và AES. Nhưng với source hiện tại:

- bcrypt chỉ bảo vệ password trong DB
- AES chỉ bảo vệ `phone/address/taxCode` trong DB
- JWT signature chỉ bảo vệ token khỏi bị sửa hoặc giả mạo

Không cơ chế nào trong ba cơ chế trên bảo vệ:

- plaintext password lúc user vừa bấm login
- bearer token lúc frontend gửi request
- response API chứa dữ liệu customer đã giải mã

Vì vậy, nếu triển khai thực tế mà không có HTTPS:

- attacker cùng mạng có thể sniff được password
- attacker có thể lấy JWT rồi replay request
- attacker có thể nhìn thấy dữ liệu business trả về từ API

Đây là lý do TLS không phải tính năng “ngoài lề”, mà là mắt xích cuối của chuỗi mật mã ứng dụng.

### 13.3. Password/JWT có xuất hiện trong URL không

Kết quả từ source:

- Login dùng request body `POST`
- JWT dùng header `Authorization: Bearer ...`
- Không thấy dùng query string để truyền password/JWT

Kết luận:

- Không phát hiện password hoặc JWT đi trong URL từ source hiện tại.

### 13.4. Password/JWT có xuất hiện trong log không

Kết quả từ source và log tail runtime:

- Không thấy request logging tự in ra `Authorization` header
- Không thấy token hoặc plaintext password trong tail log runtime đã kiểm tra
- Có `spring.jpa.show-sql=true` ở profile `dev`, nhưng log chỉ in câu SQL với placeholder `?`, không in bind value trong phần đã kiểm tra

Điểm cần lưu ý:

- Frontend lưu JWT trong `localStorage`, không phải `httpOnly cookie`
- Nếu có XSS ở frontend trong tương lai, JWT sẽ dễ bị lấy cắp hơn

### 13.5. Kết luận TLS

- Ở môi trường local compose hiện tại: chưa có HTTPS.
- Ở mức source deploy: có cấu hình Nginx/TLS mẫu.
- HTTP -> HTTPS redirect chỉ mới có ở file cấu hình deploy, chưa được chứng minh từ runtime compose hiện tại.

Kết luận theo hướng mật mã ứng dụng:

- nếu đứng riêng lẻ, JWT/bcrypt/AES của dự án đang làm đúng việc của chúng
- nhưng để tạo thành một chuỗi bảo vệ hoàn chỉnh ngoài môi trường thật, TLS vẫn là điều kiện bắt buộc

## 14. Đánh giá Postman/Newman

### 14.1. Collection và environment thực tế

Collection hiện có:

- `docs/postman/securityapp.postman_collection.json`
- `docs/postman/securityapp.local.postman_environment.json`

Environment hiện dùng các biến:

- `baseUrl`
- `adminEmail`, `adminPassword`
- `staffEmail`, `staffPassword`
- `userEmail`, `userPassword`
- `token`
- `adminToken`
- `staffToken`
- `userToken`
- `customerId`
- `customerEmail`

Không có biến:

- `refresh_token`

Lý do:

- Source hiện tại không có refresh token flow.

### 14.2. Request coverage hiện có

- Login as Admin
- Get Current User
- Login as Staff
- Login as User
- Admin Summary
- Customer list/create/get/update/delete
- Audit log list
- Security checks cho `401` và `403`

### 14.3. Script coverage hiện có

Collection hiện tại đang kiểm:

- status code
- token tồn tại sau login
- một số key cơ bản trong response
- lưu token/customerId vào environment

Collection hiện tại chưa thấy script cho:

- response time threshold
- JSON schema validation đầy đủ

### 14.4. Vai trò của Postman/Newman trong kiểm chứng mật mã ứng dụng

Postman/Newman trong dự án này không chứng minh trực tiếp bản thân thuật toán mật mã là “đúng về mặt toán học”. Điều nó chứng minh được là:

- JWT được phát hành đúng sau login thành công
- request không có token hoặc token sai bị chặn đúng
- role matrix thật đang hoạt động ở biên API
- dữ liệu customer chỉ được trả về sau khi request qua được auth/authz

Nói cách khác:

- Postman/Newman rất mạnh để chứng minh `hành vi bảo mật ở lớp API`
- nhưng không thay thế được kiểm tra DB để chứng minh bcrypt/AES đang bảo vệ dữ liệu at rest

## 15. Đánh giá OWASP ZAP

### 15.1. Artifact thực tế trong repo

Repo hiện có:

- `docs/security/zap.yaml`
- `docs/security/zap-baseline-report.html`
- `docs/security/zap-baseline-report.json`
- `docs/security/zap-baseline-report.xml`

### 15.2. Kiểu scan thực tế trong repo

`zap.yaml` hiện tại quét:

- `http://host.docker.internal:8080/swagger-ui.html`

Nó chưa phải là bài quét import OpenAPI trực tiếp từ `/v3/api-docs`.

### 15.3. Kết quả report hiện có

| Chỉ số | Giá trị |
|---|---|
| PASS | `59` |
| WARN | `2` |
| FAIL | `0` |

Chi tiết alert trích từ `zap-baseline-report.json`:

| Alert | Risk | CWE | URL | Evidence |
|---|---|---|---|---|
| `Content Security Policy (CSP) Header Not Set` | `Medium (High)` | `693` | `http://host.docker.internal:8080/swagger-ui.html` | Không có evidence cụ thể trong instance |
| `Modern Web Application` | `Informational (Medium)` | `-1` | `http://host.docker.internal:8080/swagger-ui.html` | `<script src="./swagger-ui-bundle.js" charset="UTF-8"> </script>` |

### 15.4. Nhận xét

- Alert hiện tại tập trung ở Swagger UI public.
- Chưa có bằng chứng scan authenticated API bằng JWT trong artifact repo hiện có.
- Muốn scan theo đúng Phase 11 bằng OpenAPI `/v3/api-docs`, cần chạy thêm phiên ZAP import OpenAPI riêng.

### 15.5. Ranh giới giữa ZAP và kiểm thử mật mã ứng dụng

ZAP hữu ích để tìm:

- header thiếu
- bề mặt public
- inventory endpoint
- dấu hiệu misconfiguration

Nhưng ZAP không thay thế được các kiểm tra sau:

- query DB để chứng minh bcrypt không lưu plaintext
- query DB để chứng minh AES lưu ciphertext
- tamper ciphertext để chứng minh GCM tag đang bảo vệ integrity
- decode JWT để chứng minh payload/signature thực tế

Vì vậy, trong dự án này ZAP là một phần của bằng chứng bảo mật, nhưng không phải là phần mạnh nhất cho hướng mật mã ứng dụng.

## 16. Đánh giá OWASP API Security Top 10

| Hạng mục | Cách test áp vào source hiện tại | Kết quả từ source/runtime | Rủi ro | Khuyến nghị |
|---|---|---|---|---|
| API1 Broken Object Level Authorization | Dùng token `USER` gọi `/api/customers/{id}` hoặc thử userA/userB | `USER` bị chặn ngay ở mức route `403`. Không có endpoint user-owned để test userA/userB. Tách tenant/ownership: `Chưa xác định từ source code hiện tại.` | Trung bình nếu sau này có multi-tenant | Nếu bổ sung owner/tenant, cần thêm kiểm tra object-level theo owner |
| API2 Broken Authentication | Kiểm token fake/tamper/expired, kiểm brute force controls | JWT invalid/expired bị chặn đúng `401`. Nhưng không thấy rate limiting, lockout, refresh token, revoke, và secret mặc định đang hardcode | Cao | Thêm rate limit, lockout, rotate secret, refresh/revoke, giảm TTL |
| API3 Broken Object Property Level Authorization | So sánh field trả về cho `ADMIN` và `STAFF` | `CustomerResponse` trả plaintext `phone/address/taxCode` cho cả `ADMIN` và `STAFF`. Nhu cầu che field cho `STAFF`: `Chưa xác định từ source code hiện tại.` | Trung bình | Xác định chính sách field-level rõ ràng, cân nhắc mask dữ liệu |
| API4 Unrestricted Resource Consumption | Kiểm list endpoint có limit/page/rate limit không | `/api/customers` và `/api/audit-logs` trả full list, không có pagination, filter hay quota | Trung bình | Thêm pagination, limit, sort/filter có kiểm soát, rate limiting |
| API5 Broken Function Level Authorization | Dùng `USER` vào `/api/admin/**`, `/api/audit-logs/**`, `/api/customers/**` | Role matrix đang chạy đúng, runtime đã xác minh `403` | Thấp đến Trung bình | Giữ integration test 401/403 khi thêm endpoint mới |
| API6 Unrestricted Access to Sensitive Business Flows | Kiểm login/register có anti-automation không | `register` và `login` public, không thấy CAPTCHA, throttle, verification flow | Trung bình | Thêm throttle, CAPTCHA hoặc chống automation ở gateway |
| API7 SSRF | Tìm endpoint nhập URL từ người dùng hoặc server-side fetch | Không tìm thấy bề mặt SSRF trong source hiện tại | Thấp | Giữ nguyên, nếu sau này thêm fetch URL thì dùng whitelist |
| API8 Security Misconfiguration | Kiểm secret, HTTP, Swagger public, OpenAPI auth metadata, SQL/dev profile | Có nhiều điểm misconfig: secret mặc định, HTTP compose, Swagger public, `ddl-auto=update`, `show-sql=true`, OpenAPI security metadata rỗng | Cao | Tách profile prod, bỏ secret mặc định, hạn chế Swagger ngoài dev, cấu hình TLS đầy đủ |
| API9 Improper Inventory Management | Đối chiếu source với OpenAPI/Postman/ZAP | Repo có OpenAPI/Postman/ZAP, nhưng chưa version hóa path và OpenAPI chưa phản ánh security requirement đầy đủ | Trung bình | Version hóa API, đồng bộ OpenAPI security, quản lý inventory runtime/proxy |
| API10 Unsafe Consumption of APIs | Tìm outbound API call tới bên thứ ba | Không thấy backend/frontend gọi upstream third-party API | Thấp | Nếu sau này thêm tích hợp ngoài, cần kiểm TLS, timeout, schema validation |

## 17. Đánh giá Docker Security

| Điểm kiểm tra | Bằng chứng thực tế | Rủi ro | Đánh giá |
|---|---|---|---|
| Hardcoded DB password | `MYSQL_ROOT_PASSWORD: root` | Cao | Không phù hợp production |
| Hardcoded JWT secret mặc định | `APP_JWT_SECRET` trong compose và default trong `application.properties` | Cao | Có thể bị lạm dụng để forge token nếu deploy nguyên cấu hình này |
| Hardcoded AES secret mặc định | `APP_AES_SECRET` trong compose và default trong `application.properties` | Cao | Rủi ro lộ khóa giải mã dữ liệu |
| Hardcoded demo password | `APP_DEMO_USERS_PASSWORD: Password@123` | Trung bình đến Cao | Thuận tiện demo, không phù hợp production |
| Exposed DB port | `3307:3306` | Cao | Mở DB ra host local |
| Exposed backend port | `8080:8080` | Trung bình đến Cao | API public HTTP trực tiếp |
| Exposed frontend port | `3000:3000` | Trung bình | Chấp nhận cho local demo, không nên production trực tiếp |
| Containers chạy user nào | Dockerfile không có `USER` | Trung bình | Khả năng đang chạy user mặc định của image |
| Backend build bỏ qua test | `mvn clean package -DskipTests` | Trung bình | Image build không tự chặn regression |
| Frontend install dependency | `npm install` thay vì `npm ci` | Thấp đến Trung bình | Vấn đề reproducibility/hardening |
| Volume database init | `./database:/docker-entrypoint-initdb.d:ro` | Thấp | Đã gắn read-only, điểm tốt |
| Secret management | Không thấy Docker secrets / external secret store | Cao | Không phù hợp môi trường thật |

### 17.1. Tác động trực tiếp của Docker/compose tới bí mật mật mã

Trong dự án này, Docker không chỉ là câu chuyện vận hành. Nó ảnh hưởng trực tiếp tới an toàn của các bí mật mật mã:

- `APP_JWT_SECRET` quyết định ai có thể forge JWT
- `APP_AES_SECRET` quyết định ai có thể giải mã dữ liệu customer
- `MYSQL_ROOT_PASSWORD` quyết định mức độ dễ dàng để đọc được DB

Do đó, phần Docker Security ở đây gắn trực tiếp với mật mã ứng dụng, không phải hạng mục tách rời. Nếu compose hoặc môi trường deploy để lộ secret, thì:

- lớp chữ ký JWT mất giá trị
- lớp mã hóa AES mất giá trị
- attacker có thể đọc ciphertext và đồng thời có key để giải mã

## 18. Phát hiện chính

| Mức độ | Phát hiện | Bằng chứng | Tác động |
|---|---|---|---|
| Cao | Secret mặc định đang hardcode trong repo/compose | `APP_JWT_SECRET`, `APP_AES_SECRET`, `MYSQL_ROOT_PASSWORD`, `APP_DEMO_USERS_PASSWORD` | Nếu deploy không đổi secret, attacker có thể forge JWT hoặc giải mã dữ liệu |
| Cao | Compose local chạy HTTP, chưa ghép TLS reverse proxy | `docker-compose.yml` chỉ expose `3000/8080`; Nginx chỉ nằm ở `deploy/` | Password và JWT không được bảo vệ bởi TLS khi truy cập local/proxy thô |
| Cao | Không thấy rate limiting hoặc anti-brute-force cho login/register | Không thấy filter/gateway/rate-limit config tương ứng | Tăng nguy cơ brute force và automation abuse |
| Trung bình | JWT được lưu trong `localStorage` | `frontend/lib/tokenStorage.ts` | Nếu có XSS trong frontend tương lai, token dễ bị lấy |
| Trung bình | `CustomerResponse` trả dữ liệu nhạy cảm plaintext cho cả `ADMIN` và `STAFF` | `CustomerService.toResponse()` | Có thể dư quyền nếu nghiệp vụ muốn STAFF bị giới hạn |
| Trung bình | `/api/customers` và `/api/audit-logs` không có pagination/limit | Repository trả `findAll...`, controller trả list trực tiếp | Nguy cơ resource exhaustion và response phình to |
| Trung bình | OpenAPI có security scheme nhưng operation không gắn security requirement | `docs/api/openapi.json` `security: []` cho các operation | Swagger có thể làm tester hiểu sai mức auth thực tế |
| Trung bình | Ciphertext hỏng gây `500` generic khi đọc customer | Runtime đã xác minh bằng cách sửa ciphertext trong DB | Ảnh hưởng availability và khó phân biệt dữ liệu bị hỏng |
| Trung bình | Container hardening còn mỏng | Dockerfile không đặt `USER`, không secret manager | Tăng bề mặt tấn công khi deploy thật |

## 19. Kế hoạch kiểm thử khuyến nghị

### 19.1. Ưu tiên rất cao

1. JWT validity, tampering, expiration, role authorization.
2. bcrypt trong DB, chứng minh không plaintext.
3. AES ciphertext trong DB và integrity khi ciphertext bị sửa.
4. Docker secrets và exposed ports.
5. HTTP/HTTPS thực tế khi deploy.

### 19.2. Ưu tiên cao

1. Swagger/OpenAPI workflow để tester nhập dữ liệu và đối chiếu schema.
2. Postman/Newman regression cho `200/201/204/401/403/409`.
3. ZAP scan cho surface public và import OpenAPI cho inventory đầy đủ.
4. OWASP API Top 10 review dựa trên source hiện tại.

## 20. Kết luận

Source hiện tại đã có các thành phần bảo mật cốt lõi cho phạm vi đồ án:

- JWT stateless authentication
- Spring Security role authorization
- bcrypt password hashing
- AES-GCM cho dữ liệu nhạy cảm của customer
- audit log cho login và customer CRUD
- Swagger/OpenAPI, Postman/Newman, OWASP ZAP artifact

Tuy nhiên, nếu nhìn dưới góc độ triển khai thực tế ngoài môi trường demo, các khoảng trống quan trọng vẫn còn:

- secret mặc định đang hardcode
- compose local chưa dùng HTTPS/TLS
- chưa có refresh token, revoke, rate limiting
- token lưu ở `localStorage`
- Docker chưa hardening mạnh
- OpenAPI chưa mô tả security requirement đầy đủ

Nếu chỉ xét riêng hướng mật mã ứng dụng, kết luận mạnh nhất của đợt phân tích này là:

1. Dự án đã gắn mật mã vào đúng luồng nghiệp vụ thật, không phải chỉ thêm thư viện cho đủ đầu mục.
2. `bcrypt` đang bảo vệ password at rest.
3. `JWT HS256` đang bảo vệ tính toàn vẹn và tính xác thực của access token.
4. `AES-GCM` đang bảo vệ dữ liệu nhạy cảm của customer trong DB và có bằng chứng integrity khi ciphertext bị sửa.
5. Chuỗi bảo vệ chỉ hoàn chỉnh khi lớp TLS deploy được bật đúng trong môi trường thật.

## 21. Đề xuất cải thiện

1. Bỏ toàn bộ secret/password mặc định khỏi repo và compose, thay bằng secret manager hoặc `.env` ngoài repo.
2. Giảm JWT TTL, bổ sung refresh token và cơ chế revoke/blacklist nếu hệ thống còn phát triển.
3. Thêm rate limiting và anti-brute-force cho `register` và `login`.
4. Không lưu JWT ở `localStorage` nếu có thể; ưu tiên `httpOnly secure cookie` hoặc tăng cường CSP/XSS hardening.
5. Thêm pagination cho `customers` và `audit-logs`.
6. Xác định rõ policy field-level cho `STAFF` với `phone/address/taxCode`.
7. Đồng bộ OpenAPI với security runtime bằng global/per-operation security requirement.
8. Ghép Nginx/TLS thật vào luồng deploy, không public backend HTTP trực tiếp.
9. Hardening Docker bằng non-root user, secret injection, giảm exposed ports và dùng profile production riêng.
10. Nếu muốn chấm thêm OAuth2, cần bổ sung implementation chạy thật thay vì chỉ giữ dependency.
