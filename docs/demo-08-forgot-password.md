# Demo 08: Forgot Password và Reset Password

## 1. Mục tiêu

Demo này nhằm chứng minh:

1. Hệ thống có luồng `forgot password` và `reset password`.
2. Ở chế độ demo local, backend trả `demoResetToken` để dễ kiểm thử mà không cần cấu hình mail server.
3. Sau khi đặt lại mật khẩu:
   - mật khẩu cũ không còn dùng được
   - refresh token cũ bị thu hồi
4. Response của `forgot password` là đồng nhất để tránh lộ thông tin email có tồn tại hay không.

## 2. Chuẩn bị

### 2.1. Điều kiện cấu hình

Kiểm tra file `.env`:

```text
PASSWORD_RESET_DEMO_MODE=true
```

Đây là cấu hình mặc định để backend trả `demoResetToken` trực tiếp trong response.

### 2.2. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 2.3. Thực hiện ở đâu

Demo này nên làm bằng `Swagger` hoặc `Postman`. Nếu muốn không ảnh hưởng tài khoản seed thì nên tạo một tài khoản mới ngay trong buổi demo.

## 3. Các bước thực hiện chi tiết

### Bước 1: Tạo một tài khoản mới để demo reset password

Trên Swagger, gọi:

```http
POST /api/auth/register
```

Body ví dụ:

```json
{
  "fullName": "Demo Reset Password",
  "email": "demo.reset@example.com",
  "password": "Password123!",
  "phoneNumber": "0909888777",
  "billingAddress": "456 Reset Street"
}
```

Kết quả mong đợi:

- HTTP `201 Created`
- response có `accessToken` và `refreshToken`

Giữ lại `refreshToken` này để kiểm tra sau khi đổi mật khẩu.

### Bước 2: Đăng nhập lại bằng mật khẩu cũ để chắc chắn tài khoản hoạt động

Gọi:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "demo.reset@example.com",
  "password": "Password123!"
}
```

Kết quả mong đợi:

- HTTP `200 OK`

### Bước 3: Gửi yêu cầu forgot password

Gọi:

```http
POST /api/auth/forgot-password
```

Body:

```json
{
  "email": "demo.reset@example.com"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- response có:
  - `message`
  - `expiresInSeconds`
  - `demoResetToken`

Message mong đợi:

```text
If the email exists, a password reset instruction has been issued.
```

### Bước 4: Dùng `demoResetToken` để đặt lại mật khẩu

Copy `demoResetToken` ở bước 3 rồi gọi:

```http
POST /api/auth/reset-password
```

Body:

```json
{
  "token": "<demo-reset-token>",
  "newPassword": "NewPassword123!"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- message:

```text
Password reset completed successfully.
```

### Bước 5: Thử đăng nhập lại bằng mật khẩu cũ

Gọi lại:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "demo.reset@example.com",
  "password": "Password123!"
}
```

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
Invalid email or password.
```

### Bước 6: Đăng nhập bằng mật khẩu mới

Gọi:

```json
{
  "email": "demo.reset@example.com",
  "password": "NewPassword123!"
}
```

Kết quả mong đợi:

- HTTP `200 OK`

### Bước 7: Dùng lại refresh token cũ trước khi reset

Lấy `refreshToken` cũ ở bước đăng ký hoặc đăng nhập trước khi reset, rồi gọi:

```http
POST /api/auth/refresh
```

Body:

```json
{
  "refreshToken": "<refresh-token-cu>"
}
```

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
Refresh token has been revoked.
```

Ý nghĩa:

- đổi mật khẩu không chỉ thay hash mật khẩu
- mà còn thu hồi các refresh token đang còn hiệu lực

### Bước 8: Chứng minh response forgot password là đồng nhất

Gọi lại:

```http
POST /api/auth/forgot-password
```

với email không tồn tại:

```json
{
  "email": "khong-ton-tai@example.com"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- message vẫn là:

```text
If the email exists, a password reset instruction has been issued.
```

Điểm cần nói:

- đây là cách tránh lộ việc email nào có tồn tại trong hệ thống

## 4. Kết quả mong đợi

Sau khi làm xong Demo 08, bạn phải chứng minh được:

1. hệ thống hỗ trợ forgot password và reset password
2. môi trường demo local trả `demoResetToken` để test dễ dàng
3. mật khẩu cũ bị vô hiệu sau khi reset
4. refresh token cũ bị thu hồi
5. response forgot password không tiết lộ sự tồn tại của email

## 5. Câu nên nói khi trình bày

- Đây là cơ chế hỗ trợ người dùng khi quên mật khẩu nhưng vẫn phải đảm bảo an toàn.
- Ở môi trường demo, token reset được trả về trực tiếp để dễ kiểm thử; khi triển khai thật có thể gửi qua email.
- Sau khi đổi mật khẩu, refresh token cũ bị revoke để giảm rủi ro nếu thiết bị cũ đang giữ phiên đăng nhập.
- Response forgot password luôn giống nhau để tránh lộ thông tin tài khoản hợp lệ.

## 6. Ảnh nên chụp cho báo cáo

- Ảnh `POST /api/auth/forgot-password` có `demoResetToken`.
- Ảnh `POST /api/auth/reset-password` thành công.
- Ảnh đăng nhập bằng mật khẩu cũ bị `401`.
- Ảnh đăng nhập bằng mật khẩu mới thành công.
- Ảnh refresh token cũ bị từ chối.

## 7. Cách reset sau demo

Nếu bạn dùng tài khoản phụ như ví dụ trên thì không nhất thiết phải reset. Nếu bạn đã đổi mật khẩu của tài khoản seed, nên reset toàn bộ dữ liệu:

```powershell
docker compose down -v
docker compose up --build -d
```

## 8. Kết luận

Demo này bổ sung một mắt xích quan trọng của phần xác thực an toàn:

- hỗ trợ quên mật khẩu
- không làm lộ email tồn tại
- revoke phiên cũ sau khi đổi mật khẩu
