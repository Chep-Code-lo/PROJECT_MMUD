# Kịch bản demo tổng hợp

## 1. Mục đích

Tài liệu này dùng khi bạn muốn trình bày đồ án theo thứ tự mạch lạc trước giảng viên. Nội dung tập trung vào các điểm bảo mật quan trọng, không sa đà vào giao diện.

## 2. Chuẩn bị trước khi demo

### 2.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 2.2. Địa chỉ cần nhớ

- Frontend: `https://localhost`
- API health: `https://localhost/api/health`
- Swagger local-only: `https://localhost:8444/swagger-ui.html`

### 2.3. Tài khoản mẫu

- `student1@example.com / Password123!`
- `student2@example.com / Password123!`
- `instructor@example.com / Password123!`
- `admin@example.com / Admin123!`

## 3. Thứ tự demo khuyến nghị

Nên trình bày theo thứ tự sau:

1. HTTPS/TLS
2. Đăng nhập và JWT
3. bcrypt cho mật khẩu
4. AES-GCM cho dữ liệu nhạy cảm
5. BOLA/IDOR
6. Webhook HMAC-SHA256
7. Rate limit
8. Audit log
9. Swagger local-only

## 4. Demo từng phần

### 4.1. Demo HTTPS/TLS

Mục tiêu:

- chứng minh hệ thống chạy qua HTTPS;
- chứng minh HTTP bị chuyển hướng sang HTTPS.

Thao tác:

```powershell
curl.exe -I http://localhost/api/health
curl.exe -k https://localhost/api/health
```

Kết quả mong đợi:

- lệnh HTTP trả `301`;
- lệnh HTTPS trả `200`.

### 4.2. Demo đăng nhập và JWT

Mục tiêu:

- chứng minh backend phát hành access token và refresh token;
- chứng minh request có token hợp lệ mới gọi được API bảo vệ.

Thao tác:

1. Dùng Postman hoặc Swagger local-only để gọi `POST /api/auth/login`.
2. Lấy `accessToken`.
3. Gọi `GET /api/auth/me` với `Authorization: Bearer <token>`.

Kết quả mong đợi:

- đăng nhập thành công trả `200`;
- `GET /api/auth/me` trả đúng thông tin người dùng.

### 4.3. Demo bcrypt

Mục tiêu:

- chứng minh mật khẩu không lưu plaintext;
- chứng minh vẫn đăng nhập được bằng mật khẩu gốc.

Thao tác:

1. Đăng ký tài khoản mới hoặc dùng tài khoản seed.
2. Mở database để xem cột `password_hash`.

Kết quả mong đợi:

- giá trị bắt đầu dạng bcrypt, thường có tiền tố `$2`;
- không nhìn thấy mật khẩu gốc trong cơ sở dữ liệu.

### 4.4. Demo AES-GCM

Mục tiêu:

- chứng minh dữ liệu nhạy cảm được mã hóa khi lưu;
- chứng minh ứng dụng vẫn giải mã được khi trả ra cho đúng người dùng.

Thao tác:

1. Xem dữ liệu `phone_number_encrypted`, `billing_address_encrypted`, `payment_reference_encrypted`, `certificate_code_encrypted` trong database.
2. Gọi API profile hoặc certificate bằng tài khoản hợp lệ.

Kết quả mong đợi:

- trong database là ciphertext;
- trên API trả về đúng dữ liệu đã giải mã cho người có quyền.

### 4.5. Demo BOLA / IDOR

Mục tiêu:

- chứng minh sinh viên không xem được tài nguyên của sinh viên khác.

Thao tác:

1. Đăng nhập `student1`.
2. Lấy `certificateId` của `student1`.
3. Đăng nhập `student2`.
4. Gọi `GET /api/certificates/{certificateId-của-student1}` bằng token của `student2`.

Kết quả mong đợi:

- backend trả `403 Forbidden`;
- sau đó admin xem audit log sẽ thấy bản ghi `ACCESS_DENIED`.

### 4.6. Demo webhook HMAC-SHA256

Mục tiêu:

- chứng minh webhook hợp lệ mới được chấp nhận;
- chứng minh hệ thống chống chữ ký sai và chống replay.

Thao tác:

1. Tạo `enrollment PENDING` bằng checkout.
2. Gửi webhook hợp lệ.
3. Gửi webhook sai chữ ký.
4. Gửi lại cùng `eventId` để test replay.

Kết quả mong đợi:

- webhook hợp lệ: `200`, enrollment chuyển `ACTIVE`;
- webhook sai chữ ký: `401`;
- replay cùng `eventId`: `409 Conflict`.

### 4.7. Demo rate limit

Mục tiêu:

- chứng minh hệ thống chống spam hoặc brute-force cơ bản.

Thao tác:

1. Gửi nhiều lần `POST /api/auth/login` với mật khẩu sai.

Kết quả mong đợi:

- vài lần đầu nhận `401`;
- sau khi vượt ngưỡng nhận `429 Too Many Requests`.

### 4.8. Demo audit log

Mục tiêu:

- chứng minh hệ thống có theo dõi hành vi bảo mật.

Thao tác:

1. Đăng nhập admin.
2. Gọi `GET /api/admin/audit-logs`.

Kết quả mong đợi:

- nhìn thấy các sự kiện như:
  - `LOGIN_SUCCESS`
  - `LOGIN_FAILED`
  - `ACCESS_DENIED`
  - `WEBHOOK_ACCEPTED`
  - `WEBHOOK_REJECTED`
  - `RATE_LIMIT_EXCEEDED`

### 4.9. Demo Swagger local-only

Mục tiêu:

- chứng minh tài liệu API không bị lộ ra cổng public;
- nhưng người vận hành trên máy chủ vẫn dùng được để kiểm thử nội bộ.

Thao tác:

1. Mở `https://localhost/swagger-ui.html`
2. Mở `https://localhost:8444/swagger-ui.html`

Kết quả mong đợi:

- URL thứ nhất trả `404`;
- URL thứ hai mở được Swagger.

## 5. Nếu cần chạy demo tự động

Project có script:

```powershell
& .\scripts\demo-security.ps1
```

Script này phù hợp khi muốn lấy minh chứng nhanh hoặc kiểm tra lại toàn bộ hệ thống sau khi sửa mã nguồn.

## 6. Reset dữ liệu sau buổi demo

Nếu trong lúc trình bày bạn đã:

- đổi mật khẩu tài khoản seed;
- kích hoạt enrollment;
- tạo thêm user mới;

hãy reset dữ liệu:

```powershell
docker compose down -v
docker compose up --build -d
```

## 7. Tài liệu liên quan

- `docs/postman-testing.md`
- `docs/owasp-zap-testing.md`
- `docs/demo-01-password-bcrypt.md`
- `docs/demo-02-jwt.md`
- `docs/demo-03-aes-encryption.md`
- `docs/demo-04-bola-idor.md`
- `docs/demo-05-webhook-hmac.md`
- `docs/demo-06-rate-limit.md`
- `docs/demo-07-https-tls.md`
