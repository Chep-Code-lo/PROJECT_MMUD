# Tài liệu API và Swagger/OpenAPI

Swagger/OpenAPI của dự án được sinh động từ backend Spring Boot khi hệ thống khởi chạy. Tài liệu này dùng để mô tả nhanh cách mở Swagger, cách kiểm thử Bearer JWT và những giới hạn truy cập đang áp dụng trong môi trường demo hiện tại.

## 1. Mở Swagger ở đâu

Swagger hiện mở được trên **máy host local**, container trên cùng máy host và domain public demo tại các địa chỉ:

- `https://localhost/swagger-ui.html`
- `https://localhost/v3/api-docs`
- `https://host.docker.internal/swagger-ui.html`
- `https://hackerlo.online/swagger-ui.html`
- `https://hackerlo.online/v3/api-docs`

Lưu ý:

- Domain demo public hiện tại là `hackerlo.online`
- Nếu sau này muốn khóa Swagger lại chỉ cho local, cần chỉnh lại reverse proxy Nginx

## 2. Public domain hiện tại

Các địa chỉ public demo đang dùng:

- `https://hackerlo.online/swagger-ui.html`
- `https://hackerlo.online/v3/api-docs`

## 3. Các nhóm API chính

- Auth API
- Course API
- Lesson API
- Enrollment API
- Certificate API
- Admin API
- Webhook API

## 4. Cách kiểm thử Bearer JWT trên Swagger

### Bước 1: Đăng nhập

Mở nhóm `Auth API`, gọi:

```http
POST /api/auth/login
```

Ví dụ body:

```json
{
  "email": "student1@example.com",
  "password": "Password123!"
}
```

### Bước 2: Sao chép access token

Sau khi request thành công, sao chép giá trị:

- `accessToken`

### Bước 3: Bấm `Authorize`

Trong Swagger UI:

1. Bấm nút `Authorize`
2. Nhập:

```text
Bearer <accessToken>
```

3. Bấm `Authorize`
4. Đóng hộp thoại

### Bước 4: Gọi API bảo vệ

Thử một số API sau:

- `GET /api/auth/me`
- `GET /api/certificates/me`
- `GET /api/admin/courses` với tài khoản admin
- `GET /api/admin/audit-logs` với tài khoản admin

## 5. Khi nào nên dùng Swagger, khi nào nên dùng Postman

### Nên dùng Swagger cho

- Đăng nhập và lấy JWT
- Xem profile hiện tại
- Xem chứng chỉ của chính mình
- Duyệt ghi danh và xem danh sách học viên bằng tài khoản admin
- Xem audit log bằng tài khoản admin
- Kiểm tra nhanh các response và status code

### Nên dùng Postman cho

- Demo BOLA/IDOR với nhiều token cùng lúc
- Demo webhook HMAC-SHA256
- Demo replay attack với `eventId`
- Các tình huống phải giữ nhiều biến request trong một phiên làm việc

## 6. Lưu ý sử dụng trong đồ án

- Không nên commit file `openapi.json` export từ bản cũ nếu nó không được sinh lại từ source hiện tại
- Ưu tiên dùng tài liệu OpenAPI sinh runtime để tránh lệch với code
- Nếu trình duyệt cảnh báo certificate khi mở Swagger qua `https://localhost/swagger-ui.html`, có thể tiếp tục truy cập vì đây là môi trường demo local với self-signed certificate
