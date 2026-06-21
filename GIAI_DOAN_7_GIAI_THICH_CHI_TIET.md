# Giai đoạn 7: Giải thích chi tiết code và ý tưởng

Giai đoạn 7 là bước hoàn thiện `authentication` và `role authorization` thật cho backend. Đây là phần biến hệ thống từ một API có dữ liệu sang một ứng dụng mạng có kiểm soát truy cập đúng nghĩa.

## 1. Mục tiêu của giai đoạn 7

- chốt 3 role: `ADMIN`, `STAFF`, `USER`
- phân biệt rõ `401` và `403`
- phát và kiểm tra `JWT`
- không cho role thường truy cập route quản trị
- vẫn giữ `Swagger` và `health` ở trạng thái public để demo và test

## 2. Các file chính của giai đoạn

- `backend/src/main/java/com/company/securityapp/config/SecurityConfig.java`
- `backend/src/main/java/com/company/securityapp/entity/User.java`
- `backend/src/main/java/com/company/securityapp/entity/Role.java`
- `backend/src/main/java/com/company/securityapp/repository/UserRepository.java`
- `backend/src/main/java/com/company/securityapp/security/CustomUserDetailsService.java`
- `backend/src/main/java/com/company/securityapp/security/JwtService.java`
- `backend/src/main/java/com/company/securityapp/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/company/securityapp/security/ModernPasswordEncoder.java`
- `backend/src/main/java/com/company/securityapp/service/AuthService.java`
- `backend/src/main/java/com/company/securityapp/controller/AuthController.java`
- `backend/src/main/java/com/company/securityapp/config/DemoUserInitializer.java`
- `backend/src/test/java/com/company/securityapp/AuthSecurityIntegrationTest.java`

## 3. Role matrix hiện tại

| Route | Quyền |
|---|---|
| `/api/auth/register`, `/api/auth/login` | Public |
| `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`, `/api/health` | Public |
| `/api/auth/me` | Chỉ cần đã xác thực |
| `/api/admin/**` | `ADMIN` |
| `/api/audit-logs/**` | `ADMIN` |
| `/api/customers/**` | `ADMIN`, `STAFF` |

Role matrix này là nền cho toàn bộ test case `401/403` trong Swagger, Postman và UI.

## 4. Vì sao phải hoàn thiện auth thật trước khi khóa role

Nếu auth chỉ là stub thì role authorization không có giá trị thực tế, vì:

- không có token thật để validate
- không có principal thật trong `SecurityContext`
- không có user và role thật trong database

Vì vậy thứ tự triển khai hợp lý là:

1. tạo `User` entity thật
2. hoàn thiện register và login
3. hash password đúng chuẩn
4. phát `JWT`
5. nạp principal vào `SecurityContext`
6. sau đó mới áp role rule

## 5. Password được xử lý như thế nào ở phiên bản hiện tại

Phiên bản hiện tại không còn dùng `bcrypt` như cơ chế chính.

`ModernPasswordEncoder` đang làm 3 việc:

- hash mới bằng `Argon2id`
- vẫn verify được hash `bcrypt` cũ
- tự nâng cấp hash cũ sang `Argon2id` khi user login thành công

Ý nghĩa:

- `Argon2id` là hướng mới và an toàn hơn cho password storage
- hệ thống không làm gãy dữ liệu user cũ nếu trước đây từng dùng `bcrypt`

Đây là điểm rất phù hợp để giải thích với giảng viên khi bị hỏi “chuẩn hiện tại em dùng là gì”.

## 6. JWT flow hoạt động ra sao

### `AuthService`

- `register`: tạo user mới, role mặc định `USER`, hash password bằng `Argon2id`
- `login`: authenticate, nâng cấp hash nếu cần, tạo `JWT`, trả `AuthResponse`
- `me`: đọc principal hiện tại để trả user đang đăng nhập

### `JwtService`

Service này chịu trách nhiệm:

- tạo token
- đọc email từ token
- kiểm tra hạn
- validate token với `UserDetails`

### `JwtAuthenticationFilter`

Filter này thực hiện:

1. đọc header `Authorization`
2. cắt prefix `Bearer `
3. validate token
4. load user từ DB
5. đặt `Authentication` vào `SecurityContext`

## 7. `401` và `403` được tách như thế nào

Trong `SecurityConfig`:

- `AuthenticationEntryPoint` xử lý `401`
- `AccessDeniedHandler` xử lý `403`

Ý nghĩa:

- không có token hoặc token sai => `401`
- có token hợp lệ nhưng role không đủ => `403`

Đây là một trong những điểm người chấm rất hay hỏi vì nó thể hiện sự khác nhau giữa `authentication` và `authorization`.

## 8. Demo user được tạo để làm gì

`DemoUserInitializer` tạo sẵn:

- `admin@securityapp.local`
- `staff@securityapp.local`
- `user@securityapp.local`

Tác dụng:

- demo nhanh không cần seed tay
- `Swagger`, `Postman` và UI có account ổn định để test
- giảm công chuẩn bị mỗi lần chạy lại stack

## 9. Liên hệ với runtime hiện tại

Các entrypoint đang dùng để demo auth:

- local: `https://localhost/login`, `https://localhost/swagger-ui.html`
- public domain: `https://demo.hackerlo.online/login`, `https://demo.hackerlo.online/swagger-ui.html`

Trong đó:

- `localhost` là mode chính cho test nội bộ
- `demo.hackerlo.online` là mode công khai khi `Cloudflare Tunnel` đang bật

## 10. Test của giai đoạn 7 chứng minh điều gì

`AuthSecurityIntegrationTest` đang cover các điểm chính:

- register, login và `me` flow chạy thật
- thiếu token hoặc token sai bị `401`
- `USER` bị chặn khỏi customer API và admin summary
- `STAFF` vào được customer API
- hash `bcrypt` legacy được nâng cấp sang `Argon2id` sau login thành công

## 11. Cách tóm tắt khi thuyết trình

> Giai đoạn 7 của em không chỉ viết role rule, mà hoàn thiện luôn auth thật bằng JWT để role rule có giá trị thực tế. Hệ thống có 3 role `ADMIN`, `STAFF`, `USER`, phân biệt rõ `401` và `403`, password hash mới dùng `Argon2id` và vẫn hỗ trợ `bcrypt` legacy để tương thích dữ liệu cũ.
