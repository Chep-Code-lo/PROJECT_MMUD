# Hướng dẫn kiểm thử bằng Postman

## 1. Mục đích

Tài liệu này hướng dẫn kiểm thử thủ công các cơ chế bảo mật chính của dự án bằng Postman, gồm:

- đăng ký và đăng nhập
- JWT và Bearer token
- refresh token và logout
- forgot password / reset password
- BOLA / IDOR
- webhook HMAC-SHA256
- rate limiting
- phân quyền admin
- audit log

## 2. Chuẩn bị trước khi kiểm thử

### 2.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 2.2. Tắt kiểm tra SSL self-signed trong Postman

Trong `Settings`:

1. tắt `SSL certificate verification`
2. đóng và mở lại request nếu Postman còn giữ kết nối cũ

### 2.3. Import collection

Import file:

```text
postman/online-course-security.postman_collection.json
```

### 2.4. Kiểm tra các biến collection

Các biến quan trọng:

- `baseUrl = https://localhost`
- `webhookSecret = giá trị HMAC_WEBHOOK_SECRET trong .env`

Nếu đang demo qua domain public, có thể đổi:

```text
baseUrl = https://hackerlo.online
```

### 2.5. Các request đã có sẵn trong collection

Collection hiện có sẵn các request sau:

- `Register`
- `Login Student 1`
- `Login Student 2`
- `Login Admin`
- `Get Courses`
- `Checkout Course`
- `Get Lesson With Token`
- `Get My Certificate`
- `BOLA Attack Attempt`
- `Admin Get Courses`
- `Admin Get Course Roster`
- `Admin Approve Pending Enrollment`
- `Admin Add Student To Course`
- `Admin Get Student Profile`
- `Admin Remove Enrollment`
- `Admin Audit Logs`
- `Webhook Valid HMAC`
- `Webhook Invalid HMAC`
- `Rate Limit Test - Wrong Login`

## 3. Thứ tự chạy khuyến nghị

Nếu muốn demo ít lỗi nhất, nên đi theo thứ tự:

1. `Get Courses`
2. `Register`
3. `Login Student 1`
4. `Get Lesson With Token`
5. `Get My Certificate`
6. `Checkout Course`
7. `Webhook Valid HMAC`
8. `Login Student 2`
9. `BOLA Attack Attempt`
10. `Login Admin`
11. `Admin Get Courses`
12. `Admin Get Course Roster`
13. `Admin Approve Pending Enrollment`
14. `Admin Add Student To Course`
15. `Admin Get Student Profile`
16. `Admin Remove Enrollment`
17. `Admin Audit Logs`
18. `Rate Limit Test - Wrong Login`

## 4. Kiểm thử đăng ký và đăng nhập

### 4.1. Request `Register`

Body mặc định trong collection:

```json
{
  "fullName": "Postman Student",
  "email": "postman.student@example.com",
  "password": "Password123!",
  "phoneNumber": "0909555666",
  "billingAddress": "123 Postman Street"
}
```

Kết quả mong đợi:

- HTTP `201 Created`
- response có `accessToken`
- response có `refreshToken`
- `user.role = STUDENT`

### 4.2. Request `Login Student 1`

Kết quả mong đợi:

- HTTP `200 OK`
- collection tự lưu:
  - `accessToken`
  - `refreshToken`
  - `studentUserId`

### 4.3. Request thủ công `GET /api/auth/me`

Collection chưa có sẵn request này, nên tạo thủ công:

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/me`
- Header: `Authorization: Bearer {{accessToken}}`

Kết quả mong đợi:

- HTTP `200 OK`
- response trả đúng email, họ tên, role của người dùng đang đăng nhập

## 5. Kiểm thử refresh token và logout

### 5.1. Refresh token

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

### 5.2. Logout

Tạo request thủ công:

- Method: `POST`
- URL: `{{baseUrl}}/api/auth/logout`
- Header:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- Body:

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- message:

```text
Refresh token revoked successfully.
```

Sau đó gọi lại `/api/auth/refresh` với refresh token vừa logout, kết quả mong đợi:

- HTTP `401 Unauthorized`

## 6. Kiểm thử forgot password và reset password

### 6.1. Forgot password

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

### 6.2. Reset password

Tạo request thủ công:

- Method: `POST`
- URL: `{{baseUrl}}/api/auth/reset-password`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "token": "<demoResetToken>",
  "newPassword": "NewPassword123!"
}
```

Kết quả mong đợi:

- HTTP `200 OK`

### 6.3. Các kiểm tra nên làm ngay sau reset

1. login bằng mật khẩu cũ
   Kỳ vọng: `401 Unauthorized`
2. login bằng mật khẩu mới
   Kỳ vọng: `200 OK`
3. refresh bằng refresh token cũ
   Kỳ vọng: `401 Unauthorized`

## 7. Kiểm thử bài học, chứng chỉ và BOLA / IDOR

### 7.1. Request `Get Lesson With Token`

Kết quả mong đợi:

- nếu student đã có active enrollment ở khóa học tương ứng thì trả `200 OK`
- nếu chưa có quyền thì sẽ bị `403 Forbidden`

### 7.2. Request `Get My Certificate`

Kết quả mong đợi:

- HTTP `200 OK`
- collection lưu `victimCertificateId`

### 7.3. Request `BOLA Attack Attempt`

Luồng chuẩn:

1. chạy `Login Student 1`
2. chạy `Get My Certificate`
3. chạy `Login Student 2`
4. chạy `BOLA Attack Attempt`

Kết quả mong đợi:

- HTTP `403 Forbidden`
- Student 2 không thể đọc certificate của Student 1

### 7.4. Kiểm tra audit log sau BOLA

1. chạy `Login Admin`
2. chạy `Admin Audit Logs`

Tìm bản ghi:

- `ACCESS_DENIED`
- `targetType = Certificate`

## 8. Kiểm thử webhook HMAC-SHA256

### 8.1. Request `Checkout Course`

Kết quả mong đợi:

- HTTP `200 OK`
- collection tự lưu:
  - `pendingEnrollmentId`
  - `paymentReference`

### 8.2. Request `Webhook Valid HMAC`

Kết quả mong đợi:

- HTTP `200 OK`
- enrollment được chuyển sang `ACTIVE`
- certificate được cấp

### 8.3. Request `Webhook Invalid HMAC`

Request này hiện được dùng để minh họa webhook chữ ký sai.

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message gần đúng:

```text
Webhook signature is invalid.
```

### 8.4. Test replay attack

1. chạy `Webhook Valid HMAC` một lần thành công
2. mở `Postman Console`
3. copy lại `X-Event-Id`, `X-Timestamp`, `X-Signature` và body
4. gửi lại đúng y hệt request đó

Kết quả mong đợi:

- HTTP `409 Conflict`
- message:

```text
Webhook event has already been processed.
```

## 9. Kiểm thử rate limit

### 9.1. Request `Rate Limit Test - Wrong Login`

Chạy request này nhiều lần liên tiếp trong cùng một phút.

Kết quả mong đợi:

- vài lần đầu: `401 Unauthorized`
- sau khi vượt ngưỡng: `429 Too Many Requests`

### 9.2. Kiểm tra audit log

Đăng nhập admin rồi gọi:

- `Admin Audit Logs`

Tìm action:

```text
RATE_LIMIT_EXCEEDED
```

## 10. Kiểm thử phân quyền admin

### 10.1. Student thử truy cập API admin

Tạo thủ công:

- Method: `GET`
- URL: `{{baseUrl}}/api/admin/audit-logs`
- Header: `Authorization: Bearer {{accessToken}}`

Nếu token hiện tại là của student, kết quả mong đợi:

- HTTP `403 Forbidden`

### 10.2. Admin quản lý học viên theo khóa học

Chạy lần lượt:

1. `Login Admin`
2. `Admin Get Courses`
3. `Admin Get Course Roster`
4. `Admin Approve Pending Enrollment`
5. `Admin Add Student To Course`
6. `Admin Get Student Profile`
7. `Admin Remove Enrollment`

Kết quả mong đợi:

- admin xem được số lượng học viên đang học và số yêu cầu chờ duyệt
- admin duyệt enrollment `PENDING` thành `ACTIVE`
- admin thêm trực tiếp một student vào khóa học
- admin xem được hồ sơ học viên qua endpoint quản trị
- admin xóa enrollment khi cần

### 10.3. Admin không dùng được endpoint student-only

Thử tạo thủ công:

- Method: `GET`
- URL: `{{baseUrl}}/api/certificates/{{victimCertificateId}}`
- Header: `Authorization: Bearer {{adminAccessToken}}`

Kết quả mong đợi:

- HTTP `403 Forbidden`

Điểm này rất quan trọng để chứng minh tách biệt vai trò.

## 11. Các mã trạng thái cần nhớ

| Tình huống | Mã trạng thái mong đợi |
| --- | --- |
| Đăng ký thành công | `201` |
| Đăng nhập thành công | `200` |
| Không có token hoặc token sai | `401` |
| Không đủ quyền / BOLA bị chặn | `403` |
| Dữ liệu không tồn tại | `404` |
| Gửi lại webhook cùng `eventId` | `409` |
| Vượt rate limit | `429` |

## 12. Reset dữ liệu sau khi kiểm thử

Nếu bạn đã:

- đổi mật khẩu tài khoản seed
- tạo thêm user
- kích hoạt nhiều enrollment
- thêm hoặc xóa học viên khỏi khóa học

thì nên reset lại trước buổi demo chính:

```powershell
docker compose down -v
docker compose up --build -d
```
