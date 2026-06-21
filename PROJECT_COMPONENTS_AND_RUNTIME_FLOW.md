# Thành phần dự án và luồng chạy thực tế

Tài liệu này giải thích dự án đang có những gì, mỗi phần dùng để làm gì và khi chạy thì dữ liệu đi như thế nào. Mục tiêu là giúp trả lời nhanh các câu hỏi như:

- “Trong dự án của em có những thành phần gì?”
- “Mỗi thư mục hoặc file chính làm gì?”
- “Luồng thực thi từ frontend đến database diễn ra như thế nào?”

## 1. Tổng quan ngắn

Dự án hiện tại là một ứng dụng mạng theo hướng `cloud/API-based security demo`, gồm:

- frontend `Next.js`
- backend `Spring Boot + Spring Security`
- authentication bằng `JWT`
- password hashing bằng `Argon2id` và hỗ trợ `bcrypt` legacy
- dữ liệu nhạy cảm customer được mã hóa bằng `AES-GCM + HKDF + AAD + key_version`
- database `MySQL`
- edge `Nginx HTTPS`
- công cụ chứng minh gồm `Swagger/OpenAPI`, `Postman/Newman`, `OWASP ZAP`

## 2. Hai chế độ chạy của dự án

| Chế độ | URL gốc | Vai trò |
|---|---|---|
| `Localhost` | `https://localhost` | Mode mặc định để nhóm tự chạy và tự test |
| `Public domain` | `https://demo.hackerlo.online` | Mode công khai để truy cập từ máy ngoài khi `Cloudflare Tunnel` đang bật |

Lưu ý:

- `localhost` là mode nền tảng và luôn nên giữ lại.
- `public domain` chỉ publish thêm cùng một stack đang chạy; không phải một hệ thống khác.

## 3. Repo có những gì

### 3.1. Thư mục gốc

| Đường dẫn | Có gì | Dùng để làm gì |
|---|---|---|
| `frontend/` | Mã nguồn giao diện `Next.js` | Người dùng login, xem customer, xem audit log |
| `backend/` | Mã nguồn `Spring Boot` | Xử lý auth, authz, encrypt/decrypt, audit |
| `database/` | `schema.sql` | Cấu trúc DB |
| `deploy/nginx/` | `Nginx`, TLS local, security headers | Reverse proxy HTTPS edge |
| `deploy/cloudflared/` | Script `Cloudflare Tunnel` | Đưa cùng stack local ra `https://demo.hackerlo.online` khi cần |
| `deploy/ssl/` | Cert local demo | Phục vụ `https://localhost` |
| `docs/api/` | `openapi.json`, README | Tài liệu API |
| `docs/postman/` | Collection + environment | Kiểm thử API |
| `docs/security/` | `zap.yaml`, report HTML/JSON/XML | Bằng chứng DAST |
| `PROJECT_SECURITY_ARCHITECTURE_AND_DEMO_DIAGRAMS.html` | File HTML xem sơ đồ | Mở trực tiếp trong VSCode hoặc trình duyệt để xem diagram mà không phụ thuộc extension Mermaid |
| `PROJECT_SECURITY_SOLUTION_ARCHITECTURE.svg/.png` | Sơ đồ kiến trúc giải pháp | Mở trực tiếp khi cần chèn Word hoặc thuyết trình |
| `PROJECT_SECURITY_DEMONSTRATION_ARCHITECTURE.svg/.png` | Sơ đồ kiến trúc demo | Dùng cho phần trình bày kịch bản kiểm thử |
| `PROJECT_SECURITY_DEMONSTRATION_RESULTS_DATAFLOW.svg/.png` | Sơ đồ data-flow và kết quả demo | Dùng cho phần giải thích đường đi và bằng chứng bảo mật |
| `docker-compose.yml` | Stack local chuẩn | Dựng `db + backend + frontend + nginx` |
| `docker-compose.public-domain.yml` | Override cho public domain | Cập nhật base URL và CORS cho mode public |

### 3.2. Frontend có gì

| Đường dẫn | Có gì | Vai trò |
|---|---|---|
| `frontend/app/login` | Trang đăng nhập | Gửi `POST /api/auth/login` |
| `frontend/app/register` | Trang đăng ký | Gửi `POST /api/auth/register` |
| `frontend/app/dashboard` | Trang dashboard | Điều hướng sau login |
| `frontend/app/customers` | Trang customer list, create, edit | Gọi customer API |
| `frontend/app/audit-logs` | Trang audit log | Chỉ `ADMIN` xem được |
| `frontend/components/ProtectedRoute.tsx` | Guard phía UI | Kiểm tra token và role trước khi vào trang |
| `frontend/lib/axiosClient.ts` | Axios client | Gắn `Authorization: Bearer <token>` vào request |
| `frontend/lib/tokenStorage.ts` | Lưu token | Hiện tại lưu `accessToken` ở `localStorage` |
| `frontend/services/*.ts` | Gói gọi API | `authService`, `customerService`, `auditLogService` |

### 3.3. Backend có gì

| Package hoặc file | Có gì | Vai trò |
|---|---|---|
| `config/` | `SecurityConfig`, `SwaggerConfig`, `DemoUserInitializer` | Cấu hình security, OpenAPI, demo users |
| `controller/` | `AuthController`, `CustomerController`, `AuditLogController`, `AdminController`, `SystemController` | Expose REST API |
| `service/` | `AuthService`, `CustomerService`, `AuditLogService`, `EncryptionService` | Logic nghiệp vụ và security controls |
| `security/` | `JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService`, `ModernPasswordEncoder` | JWT, filter chain, password encoder |
| `entity/` | `User`, `Customer`, `AuditLog`, `Role` | Mapping bảng DB |
| `dto/` | Request/response models | Tách model API khỏi entity |
| `repository/` | `UserRepository`, `CustomerRepository`, `AuditLogRepository` | Truy cập DB |
| `exception/` | `ApiException`, `GlobalExceptionHandler` | Trả lỗi có kiểm soát |

### 3.4. Database có gì

| Bảng | Chứa gì | Lưu ý bảo mật |
|---|---|---|
| `users` | User, email, `password_hash`, role | Không lưu plaintext password |
| `customers` | Name, email, dữ liệu mã hóa customer | `phone/address/taxCode` lưu ở dạng ciphertext + `key_version` |
| `audit_logs` | Login success/failed, create/update/delete customer | Phục vụ truy vết |

## 4. Khi chạy thì dự án làm gì

### 4.1. Luồng khởi động

Khi chạy:

```powershell
docker compose up -d --build
```

stack khởi động theo thứ tự:

1. `MySQL` lên trước
2. `Spring Boot backend` kết nối DB
3. `Next.js frontend` lên
4. `Nginx` public `80/443` trên host

Kết quả:

- người dùng chỉ đi vào qua `Nginx`
- `backend` và `frontend` không public raw port ra host
- DB chỉ map `3307` ra host để phục vụ proof/demo

### 4.2. Luồng local runtime

Luồng:

`Browser nội bộ -> http://localhost -> 301 -> https://localhost -> Nginx -> frontend/backend -> MySQL`

Giải thích:

- `Nginx` là origin local của ứng dụng
- `Nginx` terminate TLS và phát security headers
- local runtime là nơi thuận tiện nhất để chứng minh `HTTP -> HTTPS`

### 4.3. Luồng public runtime

Luồng:

`Browser bên ngoài -> https://demo.hackerlo.online -> Cloudflare edge -> cloudflared trên máy host -> https://localhost -> Nginx -> frontend/backend -> MySQL`

Giải thích:

- public domain chỉ publish cùng stack local đang chạy
- khi publish domain tắt, local vẫn chạy bình thường ở `https://localhost`
- mode này phù hợp khi cần chia sẻ cho máy khác truy cập
- ở lần rà ngày `2026-06-21`, `https://demo.hackerlo.online` hoạt động bình thường nhưng `http://demo.hackerlo.online` chưa phải bằng chứng redirect `301`; vì vậy redirect được chứng minh ở local origin `http://localhost`

### 4.4. Luồng login

Luồng:

`Browser -> frontend login page -> /api/auth/login -> AuthService -> ModernPasswordEncoder -> JwtService -> trả accessToken`

Giải thích:

- frontend gọi `authService.login()`
- backend xác thực bằng `AuthenticationManager`
- `ModernPasswordEncoder` verify được cả hash `Argon2id` mới và `bcrypt` legacy
- nếu user legacy login đúng, backend tự rehash sang `Argon2id`
- login thành công thì backend sinh `JWT`
- frontend lưu token vào `localStorage`

### 4.5. Luồng JWT cho request tiếp theo

Luồng:

`frontend -> axiosClient -> Authorization header -> Nginx -> backend -> JwtAuthenticationFilter -> SecurityContext`

Giải thích:

- `axiosClient` tự gắn `Authorization: Bearer <token>`
- `JwtAuthenticationFilter` verify token
- token sai, hết hạn hoặc không hợp lệ thì trả `401`
- token đúng nhưng role không đủ thì `SecurityConfig` trả `403`

### 4.6. Luồng mã hóa customer

Luồng ghi:

`POST /api/customers -> CustomerService -> EncryptionService.encryptCustomerField() -> lưu ciphertext vào DB`

Luồng đọc:

`GET /api/customers -> CustomerService -> EncryptionService.decryptCustomerField() -> trả plaintext business response`

Giải thích:

- user nhập `phone`, `address`, `taxCode` bình thường
- backend là nơi thực hiện mã hóa và giải mã
- DB chỉ lưu:
  - `phone_encrypted`
  - `address_encrypted`
  - `tax_code_encrypted`
  - `key_version`
- cơ chế hiện tại dùng:
  - `AES-GCM`
  - `HKDF-SHA256`
  - `AAD`
  - `key_version = 2`

### 4.7. Luồng audit log

Luồng:

- login đúng -> `LOGIN_SUCCESS`
- login sai -> `LOGIN_FAILED`
- create, update, delete customer -> ghi `audit_logs`

Giải thích:

- `AuditLogService` ghi actor, action, entity, success, details
- trang audit log tự refresh mỗi `3` giây
- chỉ `ADMIN` mới xem được

### 4.8. Luồng ZAP scan

Luồng:

`ZAP container -> Docker network project_mmud_default -> https://nginx/swagger-ui.html`

Giải thích:

- ZAP scan trực tiếp `nginx` service trong Docker network
- target này tương ứng với `https://localhost/swagger-ui.html` và `https://demo.hackerlo.online/swagger-ui.html` từ phía người dùng
- repo không dùng `host.docker.internal` cho ZAP để tránh quét nhầm dịch vụ khác trên máy host

## 5. Những file lõi nên biết

Nếu giảng viên hỏi “file nào là file quan trọng nhất”, có thể chỉ ra:

| File | Vì sao quan trọng |
|---|---|
| [docker-compose.yml](/E:/PROJECT_MMUD/docker-compose.yml) | Dựng stack local chuẩn |
| [docker-compose.public-domain.yml](/E:/PROJECT_MMUD/docker-compose.public-domain.yml) | Override cho mode public domain |
| [deploy/nginx/securityapp.conf](/E:/PROJECT_MMUD/deploy/nginx/securityapp.conf) | Chứng minh HTTPS edge và security headers |
| [backend/src/main/java/com/company/securityapp/config/SecurityConfig.java](/E:/PROJECT_MMUD/backend/src/main/java/com/company/securityapp/config/SecurityConfig.java) | Route nào public, route nào cần role gì |
| [backend/src/main/java/com/company/securityapp/security/ModernPasswordEncoder.java](/E:/PROJECT_MMUD/backend/src/main/java/com/company/securityapp/security/ModernPasswordEncoder.java) | Hash mới và migrate `bcrypt` legacy |
| [backend/src/main/java/com/company/securityapp/service/AuthService.java](/E:/PROJECT_MMUD/backend/src/main/java/com/company/securityapp/service/AuthService.java) | Login, register, JWT issue, rehash |
| [backend/src/main/java/com/company/securityapp/service/EncryptionService.java](/E:/PROJECT_MMUD/backend/src/main/java/com/company/securityapp/service/EncryptionService.java) | `AES-GCM + HKDF + AAD + key versioning` |
| [backend/src/main/java/com/company/securityapp/service/CustomerService.java](/E:/PROJECT_MMUD/backend/src/main/java/com/company/securityapp/service/CustomerService.java) | Luồng encrypt/decrypt customer data |
| [database/schema.sql](/E:/PROJECT_MMUD/database/schema.sql) | Thể hiện DB lưu gì và có `key_version` |
| [docs/security/zap.yaml](/E:/PROJECT_MMUD/docs/security/zap.yaml) | Cấu hình ZAP scan runtime thật |

## 6. Nếu cần nói 30 giây “dự án của em có gì”

Có thể nói:

> Dự án của em gồm frontend Next.js, backend Spring Boot, database MySQL và Nginx làm HTTPS edge.  
> Backend dùng Spring Security với JWT, password hash bằng Argon2id và vẫn hỗ trợ bcrypt cũ, còn dữ liệu nhạy cảm của customer được mã hóa bằng AES-GCM kết hợp HKDF, AAD và key versioning trước khi lưu DB.  
> Hệ thống chạy local ở `https://localhost` và có thể publish thêm ra `https://demo.hackerlo.online` khi cần demo công khai.  
> Em dùng Swagger/OpenAPI, Postman/Newman và OWASP ZAP để chứng minh bảo mật; trọng tâm là security controls và evidence, không phải chỉ là CRUD.
