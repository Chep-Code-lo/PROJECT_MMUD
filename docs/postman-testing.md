# Hướng dẫn kiểm thử bằng Postman

## 1. Mục đích

Tài liệu này hướng dẫn kiểm thử thủ công các cơ chế bảo mật chính của dự án bằng Postman, gồm:

- đăng ký và đăng nhập;
- xác thực bằng Bearer JWT;
- refresh token;
- quên mật khẩu và đặt lại mật khẩu;
- phân quyền và chống BOLA/IDOR;
- webhook HMAC-SHA256;
- rate limit;
- audit log.

## 2. Chuẩn bị trước khi kiểm thử

### 2.1. Khởi động hệ thống

Chạy toàn bộ hệ thống:

```powershell
docker compose up --build -d
```

Sau khi chạy xong, kiểm tra các địa chỉ:

- Frontend: `https://localhost`
- API chính: `https://localhost/api`
- Swagger local-only: `https://localhost:8444/swagger-ui.html`
- Health check: `https://localhost/api/health`

### 2.2. Cấu hình Postman cho HTTPS self-signed

Do hệ thống chạy local với chứng chỉ tự ký, trong Postman cần:

1. Vào `Settings`.
2. Tắt `SSL certificate verification`.
3. Mở lại request nếu Postman còn giữ kết nối cũ.

### 2.3. Import collection

Import file:

```text
postman/online-course-security.postman_collection.json
```

Sau khi import, kiểm tra các biến collection:

- `baseUrl = https://localhost`
- `webhookSecret = giá trị HMAC_WEBHOOK_SECRET trong .env`
- các biến còn lại có thể giữ mặc định để collection tự cập nhật trong quá trình chạy

## 3. Thứ tự chạy khuyến nghị

Nếu muốn demo nhanh và ít lỗi nhất, nên chạy theo thứ tự:

1. `Get Courses`
2. `Register`
3. `Login Student 1`
4. `Get My Certificate`
5. `Get Lesson With Token`
6. `Checkout Course`
7. `Webhook Valid HMAC`
8. `Login Student 2`
9. `BOLA Attack Attempt`
10. `Login Admin`
11. `Admin Audit Logs`
12. `Rate Limit Test - Wrong Login`

## 4. Kiểm thử đăng ký tài khoản mới

### 4.1. Request sử dụng

- `Register`

### 4.2. Dữ liệu gửi đi

Collection mặc định dùng dữ liệu:

```json
{
  "fullName": "Postman Student",
  "email": "postman.student@example.com",
  "password": "Password123!",
  "phoneNumber": "0909555666",
  "billingAddress": "123 Postman Street"
}
```

### 4.3. Kết quả mong đợi

- HTTP `201 Created`
- response có:
  - `accessToken`
  - `refreshToken`
  - `tokenType = Bearer`
  - `user.role = STUDENT`

### 4.4. Lưu ý

- Nếu email đã tồn tại, backend sẽ trả `409 Conflict`.
- Nếu cần test lại nhiều lần, hãy đổi email trong request hoặc reset dữ liệu seed.

## 5. Kiểm thử đăng nhập và Bearer JWT

### 5.1. Đăng nhập Student 1

Request:

- `Login Student 1`

Kết quả mong đợi:

- HTTP `200 OK`
- collection tự lưu:
  - `accessToken`
  - `refreshToken`
  - `studentUserId`

### 5.2. Gọi API bảo vệ bằng access token

Chạy các request:

- `Get Lesson With Token`
- `Get My Certificate`

Kết quả mong đợi:

- các request hợp lệ trả `200 OK`
- nội dung bài học hoặc dữ liệu chứng chỉ chỉ hiển thị khi tài khoản có quyền

### 5.3. Kiểm tra `GET /api/auth/me`

Collection hiện chưa có sẵn request này, có thể tạo thủ công:

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/me`
- Header: `Authorization: Bearer {{accessToken}}`

Kết quả mong đợi:

- HTTP `200 OK`
- response trả đúng email, họ tên, role của người dùng đang đăng nhập

## 6. Kiểm thử refresh token

Tạo request thủ công:

- Method: `POST`
- URL: `{{baseUrl}}/api/auth/refresh`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- response trả `accessToken` mới
- response trả `refreshToken` mới

Lưu ý:

- refresh token cũ sẽ bị thu hồi;
- nếu gửi lại refresh token cũ sau khi đã đổi, backend sẽ từ chối.

## 7. Kiểm thử quên mật khẩu và đặt lại mật khẩu

### 7.1. Gửi yêu cầu quên mật khẩu

Tạo request thủ công:

- Method: `POST`
- URL: `{{baseUrl}}/api/auth/forgot-password`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "email": "student1@example.com"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- response có:
  - `message`
  - `expiresInSeconds`
  - `demoResetToken`

Với môi trường demo hiện tại, `demoResetToken` được trả về để thuận tiện kiểm thử local.

### 7.2. Đặt lại mật khẩu

Tạo request thủ công:

- Method: `POST`
- URL: `{{baseUrl}}/api/auth/reset-password`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "token": "<demoResetToken vừa nhận được>",
  "newPassword": "NewPassword123!"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- response có thông báo đặt lại mật khẩu thành công

### 7.3. Kiểm tra sau khi đặt lại mật khẩu

Thực hiện ba bước:

1. Đăng nhập bằng mật khẩu cũ.
   Kỳ vọng: `401 Unauthorized`
2. Đăng nhập bằng mật khẩu mới.
   Kỳ vọng: `200 OK`
3. Dùng refresh token cũ trước khi đổi mật khẩu để gọi `/api/auth/refresh`.
   Kỳ vọng: bị từ chối vì refresh token cũ đã bị thu hồi

Lưu ý:

- nếu bạn đổi mật khẩu của tài khoản seed như `student1@example.com`, sau buổi demo nên reset dữ liệu bằng `docker compose down -v` rồi chạy lại stack.

## 8. Kiểm thử chống BOLA / IDOR

### 8.1. Chuẩn bị dữ liệu

1. Chạy `Login Student 1`
2. Chạy `Get My Certificate`

Sau bước này, collection sẽ tự lưu `victimCertificateId` từ chứng chỉ của Student 1.

### 8.2. Thực hiện tấn công thử

1. Chạy `Login Student 2`
2. Chạy `BOLA Attack Attempt`

Request này dùng access token của Student 2 nhưng cố đọc `certificateId` của Student 1.

### 8.3. Kết quả mong đợi

- HTTP `403 Forbidden`
- backend từ chối truy cập

### 8.4. Kiểm tra audit log

1. Chạy `Login Admin`
2. Chạy `Admin Audit Logs`

Tìm bản ghi có nội dung gần giống:

- `ACCESS_DENIED`
- `Certificate`
- email người dùng truy cập sai quyền

## 9. Kiểm thử webhook HMAC-SHA256

### 9.1. Tạo enrollment chờ thanh toán

1. Chạy `Login Student 1`
2. Chạy `Checkout Course`

Kết quả mong đợi:

- HTTP `200 OK`
- collection tự lưu:
  - `pendingEnrollmentId`
  - `paymentReference`

### 9.2. Gửi webhook hợp lệ

Chạy request:

- `Webhook Valid HMAC`

Kết quả mong đợi:

- HTTP `200 OK`
- enrollment được chuyển sang `ACTIVE`
- hệ thống phát hành chứng chỉ

### 9.3. Gửi webhook sai chữ ký

Chạy request:

- `Webhook Invalid HMAC`

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- thông báo chữ ký webhook không hợp lệ

### 9.4. Gửi lại webhook cùng `eventId` để test replay

Collection hiện tự sinh `eventId` mới mỗi lần chạy `Webhook Valid HMAC`, vì vậy để test replay cần làm thủ công:

1. Chạy `Webhook Valid HMAC` một lần thành công.
2. Mở `Postman Console` để xem request thực tế đã gửi.
3. Ghi lại nguyên ba header:
   - `X-Event-Id`
   - `X-Timestamp`
   - `X-Signature`
4. Tạo một request mới tên tùy ý, gửi lại đúng cùng body và cùng ba header đó tới:

```text
POST {{baseUrl}}/api/webhooks/payment-success
```

Kết quả mong đợi:

- HTTP `409 Conflict`
- thông báo `Webhook event has already been processed.`

## 10. Kiểm thử rate limit

### 10.1. Request sử dụng

- `Rate Limit Test - Wrong Login`

### 10.2. Cách chạy

Gửi request này liên tục nhiều lần, thông thường từ lần thứ 6 trở đi trong cùng một phút sẽ bắt đầu bị chặn theo cấu hình mặc định.

### 10.3. Kết quả mong đợi

- một vài lần đầu: `401 Unauthorized`
- sau khi vượt ngưỡng: `429 Too Many Requests`

### 10.4. Cách kiểm tra bổ sung

Đăng nhập admin rồi xem:

- `Admin Audit Logs`

Tìm bản ghi có:

- `RATE_LIMIT_EXCEEDED`

## 11. Kiểm thử phân quyền admin

### 11.1. Tài khoản student thử vào API admin

Tạo request thủ công:

- Method: `GET`
- URL: `{{baseUrl}}/api/admin/audit-logs`
- Header: `Authorization: Bearer {{accessToken}}`

Nếu `accessToken` hiện tại là của student, kết quả mong đợi:

- HTTP `403 Forbidden`

### 11.2. Tài khoản admin vào API admin

1. Chạy `Login Admin`
2. Chạy `Admin Audit Logs`

Kết quả mong đợi:

- HTTP `200 OK`
- nhận được danh sách audit log

## 12. Kiểm tra nhanh các mã trạng thái cần nhớ

| Tình huống | Mã trạng thái mong đợi |
| --- | --- |
| Đăng ký thành công | `201` |
| Đăng nhập thành công | `200` |
| Không có token hoặc token sai | `401` |
| Không đủ quyền / BOLA bị chặn | `403` |
| Dữ liệu không tồn tại | `404` |
| Gửi lại webhook cùng `eventId` | `409` |
| Vượt rate limit | `429` |

## 13. Reset dữ liệu sau khi kiểm thử

Nếu trong quá trình test bạn đã:

- đổi mật khẩu tài khoản seed;
- kích hoạt enrollment;
- tạo thêm user mới;

thì nên reset lại dữ liệu trước khi demo chính thức:

```powershell
docker compose down -v
docker compose up --build -d
```

Sau khi reset, các tài khoản mẫu sẽ quay lại trạng thái ban đầu.
