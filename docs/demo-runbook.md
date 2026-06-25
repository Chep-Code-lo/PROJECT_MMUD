# Kịch bản demo tổng hợp

## 1. Mục đích

Tài liệu này dùng khi bạn muốn trình bày đồ án trước giảng viên theo một mạch rõ ràng, ngắn gọn và đúng trọng tâm môn Mật mã ứng dụng. Mục tiêu của runbook là giúp bạn biết:

- mở ở đâu;
- thao tác trên công cụ nào;
- nên nói gì khi trình bày;
- cần chụp màn hình những gì để đưa vào báo cáo.

## 2. Chuẩn bị trước khi demo

### 2.1. Khởi động hệ thống

Mở PowerShell tại thư mục project:

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 2.2. Các địa chỉ cần nhớ

- Frontend: `https://localhost`
- API health: `https://localhost/api/health`
- Swagger: `https://localhost/swagger-ui.html`

### 2.3. Tài khoản mẫu

- `student1@example.com / Password123!`
- `student2@example.com / Password123!`
- `admin@example.com / Admin123!`

### 2.4. Cần mở sẵn những gì trước khi vào demo

Để buổi demo mượt hơn, nên mở sẵn:

#### Cửa sổ 1: PowerShell

Dùng để:

- kiểm tra HTTP/HTTPS;
- xem database;
- spam request để demo rate limit;
- chạy test minh họa khi cần.

#### Cửa sổ 2: Trình duyệt với Swagger

Mở:

```text
https://localhost/swagger-ui.html
```

Dùng cho các phần:

- JWT;
- bcrypt;
- AES-GCM;
- audit log;
- kiểm tra kết quả sau webhook.

#### Cửa sổ 3: Postman

Dùng cho các phần:

- BOLA/IDOR;
- webhook HMAC-SHA256;
- các tình huống cần giữ nhiều token cùng lúc.

Nếu Postman báo lỗi certificate, vào `Settings` và tắt `SSL certificate verification`.

## 3. Thứ tự demo khuyến nghị

Nếu cần trình bày trong khoảng 10 đến 15 phút, nên đi theo thứ tự này:

1. HTTPS/TLS
2. JWT và Bearer Token
3. bcrypt cho mật khẩu
4. AES-GCM cho dữ liệu nhạy cảm
5. BOLA/IDOR
6. Webhook HMAC-SHA256
7. Rate limit
8. Audit log
9. Swagger

Lý do của thứ tự này:

- đi từ lớp bảo vệ đường truyền;
- đến lớp xác thực người dùng;
- đến lớp bảo vệ dữ liệu;
- rồi mới sang các tình huống tấn công và phòng thủ cụ thể.

## 4. Kịch bản trình bày từng phần

## 4.1. Demo HTTPS/TLS

### Thực hiện ở đâu

- PowerShell
- Trình duyệt

### Các bước thao tác

#### Bước 1: Kiểm tra HTTP bị chuyển hướng

Chạy:

```powershell
curl.exe -I http://localhost/api/health
```

Kỳ vọng:

- trả `301 Moved Permanently`

#### Bước 2: Kiểm tra HTTPS hoạt động

Chạy:

```powershell
curl.exe -k https://localhost/api/health
```

Kỳ vọng:

- trả `200 OK`

#### Bước 3: Mở trình duyệt

Mở:

```text
https://localhost
```

Kỳ vọng:

- giao diện frontend tải bình thường

### Câu nên nói khi trình bày

- Hệ thống không phục vụ API qua HTTP thuần
- HTTP bị ép sang HTTPS để bảo vệ dữ liệu trên đường truyền
- Đây là lớp bảo vệ quan trọng cho JWT, mật khẩu và dữ liệu API

### Ảnh nên chụp

- `curl.exe -I http://localhost/api/health` trả `301`
- `curl.exe -k https://localhost/api/health` trả `200`
- trình duyệt mở `https://localhost`

## 4.2. Demo JWT và Bearer Token

### Thực hiện ở đâu

- Swagger
- PowerShell nếu muốn demo token hết hạn hoặc chạy test tamper token

### Các bước thao tác

#### Bước 1: Mở Swagger

Mở:

```text
https://localhost/swagger-ui.html
```

#### Bước 2: Đăng nhập để lấy token

Vào `Auth API`, mở `POST /api/auth/login`, nhập:

```json
{
  "email": "student1@example.com",
  "password": "Password123!"
}
```

Kỳ vọng:

- trả `200 OK`
- nhận được `accessToken` và `refreshToken`

#### Bước 3: Dùng Bearer token gọi API bảo vệ

1. Copy `accessToken`
2. Bấm `Authorize`
3. Nhập `Bearer <accessToken>`
4. Gọi `GET /api/auth/me`

Kỳ vọng:

- trả `200 OK`

#### Bước 4: Xóa token rồi gọi lại

1. Mở lại `Authorize`
2. `Logout` hoặc xóa token
3. Gọi lại `GET /api/auth/me`

Kỳ vọng:

- trả `401 Unauthorized`

#### Bước 5: Nếu cần, demo token bị sửa payload

Có thể:

- sửa payload thủ công trên token demo local;
- hoặc chạy test:

```powershell
cd E:\PROJECT_MMUD\backend
mvn -Dtest=AuthSecurityIntegrationTest#tamperedJwtPayloadIsRejected test
```

Kỳ vọng:

- token bị sửa sẽ bị từ chối

#### Bước 6: Demo refresh token

Gọi `POST /api/auth/refresh` với:

```json
{
  "refreshToken": "<refreshToken>"
}
```

Kỳ vọng:

- trả token mới

### Câu nên nói khi trình bày

- JWT được backend ký bằng secret phía server
- Client gửi token qua header `Authorization: Bearer <token>`
- Nếu token bị sửa payload thì chữ ký không còn hợp lệ
- Access token sống ngắn, refresh token dùng để xin token mới

### Ảnh nên chụp

- đăng nhập trả `accessToken`
- `GET /api/auth/me` trả `200`
- gọi không có token trả `401`
- nếu có, ảnh refresh token thành công

## 4.3. Demo bcrypt cho mật khẩu

### Thực hiện ở đâu

- Swagger
- PowerShell để xem database

### Các bước thao tác

#### Bước 1: Đăng nhập bằng tài khoản seed

Trên Swagger, gọi:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "student1@example.com",
  "password": "Password123!"
}
```

#### Bước 2: Xem `password_hash` trong database

Mở PowerShell, chạy:

```powershell
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Nếu `MYSQL_ROOT_PASSWORD` trong `.env` khác giá trị trên thì thay cho đúng.

Kỳ vọng:

- cột `password_hash` là chuỗi bcrypt
- không thấy mật khẩu gốc

#### Bước 3: Đăng nhập lại bằng đúng mật khẩu

Trên Swagger, gọi lại `POST /api/auth/login`.

Kỳ vọng:

- vẫn đăng nhập được

#### Bước 4: Thử mật khẩu sai

Đổi password thành sai rồi gọi lại.

Kỳ vọng:

- trả `401 Unauthorized`

### Câu nên nói khi trình bày

- bcrypt là băm một chiều, không phải mã hóa để giải ngược
- Database chỉ lưu hash, không lưu plaintext
- Khi đăng nhập, hệ thống chỉ so khớp bằng `matches`

### Ảnh nên chụp

- đăng nhập thành công
- PowerShell hiển thị cột `password_hash`
- đăng nhập sai trả `401`

## 4.4. Demo AES-GCM cho dữ liệu nhạy cảm

### Thực hiện ở đâu

- Swagger
- PowerShell

### Các bước thao tác

#### Bước 1: Đăng nhập `student1`

Trên Swagger, lấy `accessToken` rồi `Authorize`.

#### Bước 2: Gọi API hồ sơ và chứng chỉ

Gọi:

- `GET /api/auth/me`
- `GET /api/certificates/me`

Kỳ vọng:

- response có dữ liệu như:
  - `phoneNumber`
  - `billingAddress`
  - `certificateCode`

#### Bước 3: Xem ciphertext trong database

Mở PowerShell, chạy:

```powershell
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,email,phone_number_encrypted,billing_address_encrypted FROM users;"
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,student_id,course_id,payment_reference_encrypted,status FROM enrollments;"
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,student_id,course_id,certificate_code_encrypted,score FROM certificates;"
```

Kỳ vọng:

- dữ liệu trong DB là ciphertext khó đọc

#### Bước 4: Nếu cần, chạy test toàn vẹn AES-GCM

```powershell
cd E:\PROJECT_MMUD\backend
mvn -Dtest=EncryptionServiceTest test
```

Kỳ vọng:

- test cho thấy ciphertext bị sửa sẽ không giải mã được

### Câu nên nói khi trình bày

- Dữ liệu nhạy cảm không lưu trực tiếp trong DB
- Backend chỉ giải mã khi đúng người, đúng ngữ cảnh
- AES-GCM vừa bảo vệ bí mật vừa kiểm tra toàn vẹn

### Ảnh nên chụp

- `GET /api/auth/me`
- `GET /api/certificates/me`
- PowerShell truy vấn bảng mã hóa
- nếu có, ảnh test `EncryptionServiceTest`

## 4.5. Demo BOLA / IDOR

### Thực hiện ở đâu

- Postman
- có thể dùng Swagger để đối chiếu, nhưng Postman là công cụ chính

### Các bước thao tác

#### Bước 1: Import collection

Import:

```text
postman/online-course-security.postman_collection.json
```

#### Bước 2: Chạy `Login Student 1`

Kỳ vọng:

- lưu được token của `student1`

#### Bước 3: Chạy `Get My Certificate`

Kỳ vọng:

- lấy được `victimCertificateId`

#### Bước 4: Chạy `Login Student 2`

Kỳ vọng:

- lưu được token của `student2`

#### Bước 5: Chạy `BOLA Attack Attempt`

Kỳ vọng:

- trả `403 Forbidden`

#### Bước 6: Chạy `Login Admin` và `Admin Audit Logs`

Kỳ vọng:

- thấy bản ghi `ACCESS_DENIED`

### Câu nên nói khi trình bày

- Đây là kiểu lỗi đổi ID trên URL để xem dữ liệu người khác
- Frontend có ẩn nút cũng không đủ
- Backend phải kiểm tra ownership ở phía server

### Ảnh nên chụp

- `Get My Certificate`
- `BOLA Attack Attempt` trả `403`
- `Admin Audit Logs` có `ACCESS_DENIED`

## 4.6. Demo webhook HMAC-SHA256

### Thực hiện ở đâu

- Postman là công cụ chính
- Swagger chỉ để kiểm tra kết quả sau webhook

### Các bước thao tác

#### Bước 1: Chạy `Login Student 1`

#### Bước 2: Chạy `Checkout Course`

Kỳ vọng:

- có `pendingEnrollmentId`
- có `paymentReference`

#### Bước 3: Chạy `Webhook Valid HMAC`

Kỳ vọng:

- trả `200 OK`
- enrollment được kích hoạt

#### Bước 4: Kiểm tra kết quả

Mở Swagger hoặc Postman, kiểm tra:

- `GET /api/enrollments/me`
- `GET /api/certificates/me`

Kỳ vọng:

- enrollment thành `ACTIVE`
- có certificate

#### Bước 5: Chạy `Webhook Invalid HMAC`

Kỳ vọng:

- trả `401 Unauthorized`

#### Bước 6: Demo replay attack

1. Mở `Postman Console`
2. Ghi lại `X-Event-Id`, `X-Timestamp`, `X-Signature` của request hợp lệ
3. Gửi lại đúng body và đúng các header đó

Kỳ vọng:

- trả `409 Conflict`

### Câu nên nói khi trình bày

- HMAC giúp xác minh webhook đến từ nguồn biết secret dùng chung
- Timestamp và eventId giúp chống replay

### Ảnh nên chụp

- `Checkout Course`
- `Webhook Valid HMAC`
- enrollment `ACTIVE`
- `Webhook Invalid HMAC`
- replay bị `409`

## 4.7. Demo rate limit

### Thực hiện ở đâu

- PowerShell
- Swagger hoặc Postman để xem audit log

### Các bước thao tác

#### Bước 1: Spam login sai bằng PowerShell

```powershell
1..7 | ForEach-Object {
  Write-Host "----- Lan goi $($_) -----"
  curl.exe -k -i https://localhost/api/auth/login `
    -H "Content-Type: application/json" `
    -d '{"email":"student1@example.com","password":"WrongPassword123!"}'
}
```

Kỳ vọng:

- vài lần đầu `401`
- sau đó `429`

#### Bước 2: Mở audit log

Dùng Swagger hoặc Postman đăng nhập `admin` rồi gọi:

```http
GET /api/admin/audit-logs
```

Kỳ vọng:

- có bản ghi `RATE_LIMIT_EXCEEDED`

### Câu nên nói khi trình bày

- Rate limiting là lớp bảo vệ bổ sung chống brute-force và spam
- Đây không thay thế xác thực hay phân quyền, nhưng giúp giảm lạm dụng tài nguyên

### Ảnh nên chụp

- PowerShell spam login
- response `429`
- audit log có `RATE_LIMIT_EXCEEDED`

## 4.8. Demo audit log

### Thực hiện ở đâu

- Swagger hoặc Postman

### Các bước thao tác

#### Bước 1: Đăng nhập admin

Gọi:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "admin@example.com",
  "password": "Admin123!"
}
```

#### Bước 2: Dùng token admin gọi audit log

Gọi:

```http
GET /api/admin/audit-logs
```

Kỳ vọng:

- nhìn thấy các sự kiện như:
  - `LOGIN_SUCCESS`
  - `LOGIN_FAILED`
  - `ACCESS_DENIED`
  - `WEBHOOK_ACCEPTED`
  - `WEBHOOK_REJECTED`
  - `RATE_LIMIT_EXCEEDED`

### Câu nên nói khi trình bày

- Audit log là lớp giám sát giúp phát hiện và truy vết hành vi bất thường
- Đồ án không chỉ chặn tấn công mà còn có khả năng quan sát sự kiện bảo mật

### Ảnh nên chụp

- danh sách audit log
- một vài action tiêu biểu

## 4.9. Demo Swagger

### Thực hiện ở đâu

- Trình duyệt

### Các bước thao tác

#### Bước 1: Mở Swagger trên hostname local

Mở:

```text
https://localhost/swagger-ui.html
https://localhost/v3/api-docs
```

Kỳ vọng:

- đều mở được bình thường trên chính máy host

#### Bước 2: Giả lập truy cập bằng hostname public để kiểm tra chặn

Mở PowerShell và chạy:

```powershell
curl.exe -k -I https://localhost/swagger-ui.html -H "Host: demo-public.example"
curl.exe -k -I https://localhost/v3/api-docs -H "Host: demo-public.example"
```

Kỳ vọng:

- đều trả `404`

### Câu nên nói khi trình bày

- Swagger dùng cùng cổng với ứng dụng để tránh lệch cổng khi bấm `Execute`
- Nhưng chỉ các hostname local mới được mở tài liệu API để kiểm thử nội bộ

### Ảnh nên chụp

- `https://localhost/swagger-ui.html` mở được
- PowerShell giả lập host public và nhận `404`

## 5. Nếu cần demo nhanh trong thời gian rất ngắn

Nếu chỉ có khoảng 5 đến 7 phút, nên chọn 5 phần sau:

1. HTTPS/TLS
2. JWT
3. bcrypt
4. BOLA/IDOR
5. Webhook HMAC-SHA256

Đây là 5 phần dễ gây ấn tượng nhất vì:

- có lớp bảo vệ đường truyền;
- có xác thực hiện đại;
- có băm mật khẩu;
- có chống truy cập trái phép;
- có kỹ thuật mật mã ứng dụng rõ ràng ở webhook.

## 6. Nếu cần chạy demo tự động

Project có script:

```powershell
& .\scripts\demo-security.ps1
```

Script này phù hợp khi muốn:

- rà nhanh toàn hệ thống;
- lấy minh chứng kỹ thuật;
- kiểm tra lại sau khi sửa mã nguồn.

## 7. Reset dữ liệu sau buổi demo

Nếu trong lúc trình bày bạn đã:

- đổi mật khẩu tài khoản seed;
- kích hoạt enrollment;
- tạo thêm user mới;

hãy reset dữ liệu:

```powershell
docker compose down -v
docker compose up --build -d
```

## 8. Tài liệu liên quan

- `docs/postman-testing.md`
- `docs/owasp-zap-testing.md`
- `docs/demo-01-password-bcrypt.md`
- `docs/demo-02-jwt.md`
- `docs/demo-03-aes-encryption.md`
- `docs/demo-04-bola-idor.md`
- `docs/demo-05-webhook-hmac.md`
- `docs/demo-06-rate-limit.md`
- `docs/demo-07-https-tls.md`
