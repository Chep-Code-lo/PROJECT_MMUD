# Kịch bản demo tổng hợp

## 1. Mục đích

Tài liệu này là runbook tổng hợp để bạn trình bày đồ án trước giảng viên theo một mạch rõ ràng, đúng trọng tâm môn Mật mã ứng dụng và bảo mật API. Mục tiêu là:

- biết mở gì trước
- biết demo phần nào bằng công cụ nào
- biết nên nói gì trong từng phần
- biết nên chụp ảnh nào để đưa vào báo cáo

## 2. Chuẩn bị trước khi demo

### 2.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 2.2. Các địa chỉ cần nhớ

- Frontend local: `https://localhost`
- API health: `https://localhost/api/health`
- Swagger local: `https://localhost/swagger-ui.html`
- OpenAPI JSON local: `https://localhost/v3/api-docs`

Nếu bạn đang bật tunnel/domain public:

- Frontend public: `https://hackerlo.online`
- Swagger public: `https://hackerlo.online/swagger-ui.html`

### 2.3. Tài khoản mẫu

- `student1@example.com / Password123!`
- `student2@example.com / Password123!`
- `admin@example.com / Admin123!`

### 2.4. Nên mở sẵn gì trước khi bắt đầu

Để buổi demo mượt hơn, nên mở sẵn ba cửa sổ:

1. `PowerShell`
2. `Trình duyệt` tại `https://localhost/swagger-ui.html`
3. `Postman`

### 2.5. Mẹo nhỏ trước khi vào demo thật

- nếu dùng Postman, tắt `SSL certificate verification`
- nếu muốn kiểm tra nhanh toàn hệ thống trước khi trình bày, có thể chạy:

```powershell
& .\scripts\demo-security.ps1
```

## 3. Thứ tự demo khuyến nghị

Nếu bạn có khoảng `10` đến `15` phút, nên đi theo thứ tự sau:

1. Demo 07: HTTPS / TLS
2. Demo 02: JWT, Bearer Token, Refresh Token
3. Demo 01: bcrypt cho mật khẩu
4. Demo 03: AES-GCM cho dữ liệu nhạy cảm
5. Demo 08: Forgot Password và Reset Password
6. Demo 04: Chống BOLA / IDOR
7. Demo 05: Webhook HMAC-SHA256
8. Demo 06: Rate Limiting
9. Audit log và Swagger

Lý do:

- đi từ lớp bảo vệ đường truyền
- sang lớp xác thực
- sang lớp bảo vệ dữ liệu
- rồi mới đến các tình huống tấn công và phòng thủ cụ thể

## 4. Kịch bản trình bày gợi ý

## 4.1. Mở đầu trong 30 giây

Bạn có thể nói ngắn gọn:

- Đề tài tập trung vào bảo mật RESTful API cho dịch vụ khóa học online nhỏ.
- Trọng tâm là `JWT`, `bcrypt`, `AES-GCM`, `HMAC-SHA256`, `HTTPS/TLS`, `audit log` và chống `BOLA/IDOR`.
- Demo sẽ đi từ đường truyền an toàn, đến xác thực, đến bảo vệ dữ liệu và cuối cùng là các tình huống tấn công/phòng thủ.

## 4.2. Demo HTTPS / TLS

### Làm ở đâu

- `PowerShell`
- `Trình duyệt`

### Thao tác nhanh

```powershell
curl.exe -I http://localhost/api/health
curl.exe -k https://localhost/api/health
```

Sau đó mở:

```text
https://localhost
https://localhost/swagger-ui.html
```

### Điều cần nói

- HTTP bị ép sang HTTPS.
- TLS bảo vệ mật khẩu, JWT và dữ liệu API trên đường truyền.
- Swagger cũng chạy trên cùng lớp HTTPS nên test thuận tiện.

### Ảnh nên chụp

- `301` từ HTTP sang HTTPS
- `200` ở `https://localhost/api/health`
- trình duyệt mở `https://localhost`

## 4.3. Demo JWT và Bearer Token

### Làm ở đâu

- `Swagger`
- `PowerShell`

### Thao tác nhanh

1. đăng nhập `student1@example.com`
2. lấy `accessToken`
3. `Authorize` rồi gọi `GET /api/auth/me`
4. xóa token rồi gọi lại `GET /api/auth/me`
5. dùng PowerShell sửa payload token để minh họa chữ ký sai
6. gọi `POST /api/auth/refresh`
7. gọi `POST /api/auth/logout`

### Điều cần nói

- access token là JWT, refresh token là token opaque lưu dạng hash trong DB
- token sai chữ ký hoặc hết hạn sẽ bị từ chối
- logout là thu hồi refresh token

### Ảnh nên chụp

- login trả `accessToken`
- `GET /api/auth/me` trả `200`
- token sai bị `401`
- refresh token mới
- refresh token cũ bị revoke sau logout

## 4.4. Demo bcrypt

### Làm ở đâu

- `Swagger`
- `PowerShell`

### Thao tác nhanh

1. đăng nhập `student1`
2. mở PowerShell và truy vấn:

```powershell
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

3. chỉ ra cột `password_hash`
4. thử đăng nhập sai để thấy `401`

### Điều cần nói

- mật khẩu không lưu plaintext
- bcrypt là băm một chiều
- khi đăng nhập chỉ có bước so khớp `matches`

### Ảnh nên chụp

- login thành công
- query bảng `users`
- login sai trả `401`

## 4.5. Demo AES-GCM

### Làm ở đâu

- `Swagger`
- `PowerShell`

### Thao tác nhanh

1. authorize bằng token của `student1`
2. gọi:
   - `GET /api/auth/me`
   - `GET /api/certificates/me`
3. truy vấn DB:

```powershell
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,email,phone_number_encrypted,billing_address_encrypted FROM users;"
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,student_id,course_id,certificate_code_encrypted,score FROM certificates;"
```

4. nếu cần, chạy:

```powershell
cd E:\PROJECT_MMUD\backend
mvn -Dtest=EncryptionServiceTest test
```

### Điều cần nói

- dữ liệu nhạy cảm lưu dưới dạng mã hóa
- backend giải mã khi đúng người dùng, đúng ngữ cảnh
- AES-GCM còn kiểm tra tính toàn vẹn của ciphertext

### Ảnh nên chụp

- profile có `phoneNumber`
- certificate có `certificateCode`
- bảng DB có các cột `_encrypted`

## 4.6. Demo Forgot Password và Reset Password

### Làm ở đâu

- `Swagger` hoặc `Postman`

### Thao tác nhanh

1. tạo một tài khoản mới để demo
2. gọi `POST /api/auth/forgot-password`
3. lấy `demoResetToken`
4. gọi `POST /api/auth/reset-password`
5. thử login bằng mật khẩu cũ
6. login bằng mật khẩu mới
7. thử refresh bằng refresh token cũ

### Điều cần nói

- chế độ demo local trả `demoResetToken` để dễ test
- response forgot password là đồng nhất để không lộ email tồn tại
- reset password sẽ revoke refresh token cũ

### Ảnh nên chụp

- forgot password có `demoResetToken`
- reset password thành công
- refresh token cũ bị từ chối

## 4.7. Demo BOLA / IDOR

### Làm ở đâu

- `Postman`

### Thao tác nhanh

1. `Login Student 1`
2. `Get My Certificate`
3. `Login Student 2`
4. `BOLA Attack Attempt`
5. `Login Admin`
6. `Admin Audit Logs`

### Điều cần nói

- đây là lỗi đổi ID trên URL để xem dữ liệu người khác
- frontend ẩn nút là chưa đủ
- backend phải kiểm tra ownership ở phía server

### Ảnh nên chụp

- `Get My Certificate`
- `BOLA Attack Attempt` trả `403`
- `Admin Audit Logs` có `ACCESS_DENIED`

## 4.8. Demo Webhook HMAC-SHA256

### Làm ở đâu

- `Postman`
- `Swagger` để kiểm tra kết quả

### Thao tác nhanh

1. `Login Student 1`
2. `Checkout Course`
3. `Webhook Valid HMAC`
4. kiểm tra `GET /api/enrollments/me`
5. kiểm tra `GET /api/certificates/me`
6. `Webhook Invalid HMAC`
7. gửi lại cùng `eventId` để demo replay

### Điều cần nói

- HMAC xác minh nguồn gửi webhook
- timestamp và `eventId` giúp chống replay
- webhook hợp lệ mới kích hoạt enrollment

### Ảnh nên chụp

- `Checkout Course`
- `Webhook Valid HMAC`
- enrollment `ACTIVE`
- webhook sai bị từ chối
- replay trả `409`

## 4.9. Demo Rate Limit

### Làm ở đâu

- `PowerShell`
- `Swagger` hoặc `Postman`

### Thao tác nhanh

```powershell
1..7 | ForEach-Object {
  curl.exe -k -i https://localhost/api/auth/login `
    -H "Content-Type: application/json" `
    -d '{"email":"student1@example.com","password":"WrongPassword123!"}'
}
```

Sau đó đăng nhập admin và gọi:

```http
GET /api/admin/audit-logs
```

### Điều cần nói

- vài lần đầu là `401`
- vượt ngưỡng sẽ thành `429 Too Many Requests`
- audit log ghi `RATE_LIMIT_EXCEEDED`

### Ảnh nên chụp

- PowerShell spam login
- response `429`
- audit log có `RATE_LIMIT_EXCEEDED`

## 4.10. Demo Audit Log

### Làm ở đâu

- `Swagger`
- `Postman`

### Thao tác nhanh

Đăng nhập admin rồi mở:

```http
GET /api/admin/audit-logs
```

### Điều cần nói

- audit log giúp giám sát sự kiện bảo mật
- ở đây có thể thấy các action như:
  - `LOGIN_SUCCESS`
  - `LOGIN_FAILED`
  - `ACCESS_DENIED`
  - `TOKEN_REJECTED`
  - `WEBHOOK_ACCEPTED`
  - `WEBHOOK_REJECTED`
  - `RATE_LIMIT_EXCEEDED`

### Ảnh nên chụp

- danh sách audit log
- một vài action tiêu biểu

## 4.11. Demo Swagger / OpenAPI

### Làm ở đâu

- `Trình duyệt`

### Thao tác nhanh

Mở:

```text
https://localhost/swagger-ui.html
https://localhost/v3/api-docs
```

Nếu đang bật tunnel public, có thể mở thêm:

```text
https://hackerlo.online/swagger-ui.html
https://hackerlo.online/v3/api-docs
```

### Điều cần nói

- Swagger dùng để test nhanh endpoint có JWT
- OpenAPI JSON dùng cho Postman, ZAP và đối chiếu tài liệu

### Ảnh nên chụp

- Swagger local
- OpenAPI JSON local
- nếu có demo public thì thêm ảnh public domain

## 5. Nếu chỉ có 5 đến 7 phút

Nên chọn 5 phần sau:

1. HTTPS / TLS
2. JWT
3. bcrypt
4. BOLA / IDOR
5. Webhook HMAC-SHA256

Đây là bộ 5 phần gọn nhất nhưng vẫn thể hiện đủ:

- đường truyền an toàn
- xác thực hiện đại
- bảo vệ mật khẩu
- chống truy cập sai đối tượng
- áp dụng mật mã thực tế với webhook

## 6. Nếu giảng viên hỏi riêng về admin

Bạn có thể mở thêm các request Postman:

- `Admin Get Courses`
- `Admin Get Course Roster`
- `Admin Approve Pending Enrollment`
- `Admin Add Student To Course`
- `Admin Get Student Profile`
- `Admin Remove Enrollment`

Điểm nên giải thích:

- admin quản lý học viên theo từng khóa học
- admin có thể xem roster, duyệt yêu cầu ghi danh, thêm hoặc xóa học viên
- admin có endpoint riêng, không dùng API student-only

## 7. Reset dữ liệu sau buổi demo

Nếu trong lúc trình bày bạn đã:

- tạo thêm user
- đổi mật khẩu
- kích hoạt enrollment
- cấp thêm certificate

thì reset như sau:

```powershell
docker compose down -v
docker compose up --build -d
```

## 8. Tài liệu liên quan

- `docs/demo-01-password-bcrypt.md`
- `docs/demo-02-jwt.md`
- `docs/demo-03-aes-encryption.md`
- `docs/demo-04-bola-idor.md`
- `docs/demo-05-webhook-hmac.md`
- `docs/demo-06-rate-limit.md`
- `docs/demo-07-https-tls.md`
- `docs/demo-08-forgot-password.md`
- `docs/postman-testing.md`
- `docs/owasp-zap-testing.md`
