# Tài liệu API và Swagger / OpenAPI

Swagger / OpenAPI của dự án được sinh tự động từ backend Spring Boot khi hệ thống khởi chạy. Tài liệu này mô tả nhanh cách mở Swagger, cách test Bearer token và khi nào nên dùng Swagger thay vì Postman.

## 1. Mở Swagger ở đâu

### 1.1. Local

- `https://localhost/swagger-ui.html`
- `https://localhost/v3/api-docs`

Nếu trình duyệt tự chuyển hướng, có thể thấy:

- `https://localhost/swagger-ui/index.html`

### 1.2. Khi quét từ container trên cùng máy host

- `https://host.docker.internal/swagger-ui.html`

### 1.3. Khi đang bật tunnel / domain public

Ví dụ:

- `https://hackerlo.online/swagger-ui.html`
- `https://hackerlo.online/v3/api-docs`

Lưu ý:

- địa chỉ public chỉ dùng được khi tunnel/domain đang hoạt động thật
- để demo kỹ thuật tại máy chủ, vẫn nên ưu tiên `localhost`

## 2. Các nhóm API chính trên Swagger

- `Auth API`
- `Course API`
- `Lesson API`
- `Enrollment API`
- `Certificate API`
- `Admin API`
- `Webhook API`

## 3. Cách test Bearer JWT trên Swagger

### Bước 1: Đăng nhập

Mở `Auth API`, gọi:

```http
POST /api/auth/login
```

Body ví dụ:

```json
{
  "email": "student1@example.com",
  "password": "Password123!"
}
```

### Bước 2: Copy access token

Sau khi request thành công, copy trường:

```text
accessToken
```

### Bước 3: Authorize

Trong Swagger UI:

1. bấm `Authorize`
2. nhập:

```text
Bearer <access-token>
```

3. bấm `Authorize`
4. đóng hộp thoại

### Bước 4: Gọi thử API bảo vệ

Bạn có thể gọi:

- `GET /api/auth/me`
- `GET /api/certificates/me`
- `GET /api/enrollments/me`
- `GET /api/admin/courses` với tài khoản admin
- `GET /api/admin/audit-logs` với tài khoản admin

## 4. Khi nào nên dùng Swagger

Swagger phù hợp cho:

- đăng nhập và lấy JWT
- test `me`, `refresh`, `logout`
- test `forgot-password`, `reset-password`
- xem audit log bằng admin
- xem nhanh status code và schema response

## 5. Khi nào nên dùng Postman

Postman phù hợp hơn cho:

- demo BOLA / IDOR với nhiều token cùng lúc
- demo webhook HMAC-SHA256
- demo replay attack
- quản trị enrollment bằng admin theo chuỗi request

## 6. Lưu ý khi dùng trong đồ án

- nên ưu tiên tài liệu OpenAPI sinh runtime thay vì file export cũ
- nếu trình duyệt cảnh báo certificate khi mở `https://localhost/swagger-ui.html`, có thể tiếp tục vì đây là self-signed certificate local
- Swagger public có thể mở được nếu bạn chủ động expose qua domain public, nhưng khi trình bày kỹ thuật vẫn nên ưu tiên local để ổn định hơn
