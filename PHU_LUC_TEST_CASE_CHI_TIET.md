# Phụ lục test case chi tiết

## 1. Quy ước dùng trong phụ lục

Cột `Actual Result` được điền theo 3 mức:

- `Đã xác minh runtime`: đã gọi API hoặc query DB trực tiếp trong phiên phân tích ngày `2026-06-18`
- `Đã xác minh bằng test có sẵn`: đã có integration test trong repo
- `Chưa chạy trực tiếp trong phiên phân tích hiện tại.`: không tự suy đoán thêm ngoài source code

Nguồn bằng chứng chính:

- `AuthSecurityIntegrationTest`
- `CustomerControllerIntegrationTest`
- `AuditLogIntegrationTest`
- runtime `docker compose`
- query DB MySQL container

## 2. Test case Swagger chi tiết

### 2.1. `POST /api/auth/register`

Bước chung:

1. Mở `http://localhost:8080/swagger-ui.html`
2. Chọn `POST /api/auth/register`
3. Bấm `Try it out`
4. Dán JSON tương ứng
5. Bấm `Execute`

| ID | Mục tiêu | Input | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|
| `TC-REG-01` | Đăng ký hợp lệ | `{"fullName":"Nguyen Van A","email":"new.user.<timestamp>@example.test","password":"Password@123"}` | `201`, body trả `email`, `role=USER` | Đã xác minh bằng test có sẵn: pass trong `AuthSecurityIntegrationTest.registerLoginAndMeFlowWorks` | Thấp |
| `TC-REG-02` | Thiếu field bắt buộc | `{"fullName":"","email":"new.user@example.test","password":"Password@123"}` | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-REG-03` | Sai kiểu dữ liệu | `{"fullName":123,"email":"new.user@example.test","password":"Password@123"}` | `Chưa xác định từ source code hiện tại.` vì source không custom rule ép kiểu Jackson | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-REG-04` | Chuỗi quá dài | `fullName` dài hơn `150` ký tự hoặc `password` dài hơn `100` ký tự | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Thấp |
| `TC-REG-05` | Ký tự đặc biệt trong tên | `{"fullName":"<script>alert(1)</script>","email":"special.user@example.test","password":"Password@123"}` | `201` nếu email/password hợp lệ vì source không cấm pattern này | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-REG-06` | Unicode tiếng Việt | `{"fullName":"Nguyễn Văn Ánh","email":"unicode.user@example.test","password":"Password@123"}` | `201` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Thấp |
| `TC-REG-07` | JSON lỗi | `{"fullName":"A","email":"a@example.test","password":"Password@123"` | `400`, `message=Request body is malformed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-REG-08` | Request rỗng | `{}` | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Thấp |
| `TC-REG-09` | Password quá ngắn theo source thật | `{"fullName":"Short Password","email":"shortpass@example.test","password":"123456"}` | `400`, lỗi password từ `8` đến `100` ký tự | Đã xác minh runtime: `400` với `details.password=Password must be between 8 and 100 characters.` | Cao |

### 2.2. `POST /api/auth/login`

Bước chung:

1. Mở `http://localhost:8080/swagger-ui.html`
2. Chọn `POST /api/auth/login`
3. Bấm `Try it out`
4. Dán JSON tương ứng
5. Bấm `Execute`

| ID | Mục tiêu | Input | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|
| `TC-LOGIN-01` | Login hợp lệ | `{"email":"admin@securityapp.local","password":"Password@123"}` | `200`, có `accessToken`, `tokenType=Bearer` | Đã xác minh runtime: `200` | Rất cao |
| `TC-LOGIN-02` | Thiếu field bắt buộc | `{"email":"","password":"Password@123"}` | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-LOGIN-03` | Sai kiểu dữ liệu | `{"email":"admin@securityapp.local","password":12345678}` | `Chưa xác định từ source code hiện tại.` vì source không custom Jackson coercion | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-LOGIN-04` | Chuỗi quá dài | `password` dài hơn `100` ký tự | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Thấp |
| `TC-LOGIN-05` | Ký tự đặc biệt | `{"email":"admin@securityapp.local","password":"@@@###$$$"}` | `401`, `message=Invalid email or password.` nếu đủ độ dài; nếu ngắn hơn `8` thì `400` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |
| `TC-LOGIN-06` | Unicode tiếng Việt | `{"email":"admin@securityapp.local","password":"MậtKhẩuSai123"}` | `401` nếu đủ độ dài và không đúng password | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-LOGIN-07` | JSON lỗi | `{"email":"admin@securityapp.local","password":"Password@123"` | `400`, `message=Request body is malformed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-LOGIN-08` | Request rỗng | `{}` | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Thấp |

### 2.3. `POST /api/customers`

Điều kiện trước:

1. Login admin hoặc staff.
2. Bấm `Authorize` trong Swagger.
3. Nhập `Bearer <accessToken>`.

Bước chung:

1. Chọn `POST /api/customers`
2. Bấm `Try it out`
3. Dán JSON tương ứng
4. Bấm `Execute`

| ID | Mục tiêu | Input | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|
| `TC-CUS-POST-01` | Tạo customer hợp lệ | `{"name":"ACME Ltd","email":"acme.<timestamp>@example.test","phone":"0909000999","address":"123 Demo Street","taxCode":"TAX-001"}` | `201`, response trả plaintext; DB lưu ciphertext | Đã xác minh bằng test có sẵn: pass trong `CustomerControllerIntegrationTest.createCustomerEncryptsSensitiveFieldsInDatabase` | Rất cao |
| `TC-CUS-POST-02` | Thiếu field bắt buộc | `{"name":" ","email":"invalid","phone":" ","address":" ","taxCode":" "}` | `400`, `message=Validation failed.` | Đã xác minh bằng test có sẵn: pass trong `CustomerControllerIntegrationTest.createCustomerRejectsMissingRequiredFields` | Cao |
| `TC-CUS-POST-03` | Sai kiểu dữ liệu | `{"name":"ACME","email":"acme@example.test","phone":909000999,"address":"Street","taxCode":"TAX-001"}` | `Chưa xác định từ source code hiện tại.` vì source không custom Jackson coercion | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-CUS-POST-04` | Chuỗi quá dài | `address` dài hơn `500` ký tự hoặc `phone` dài hơn `50` ký tự | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-CUS-POST-05` | Ký tự đặc biệt | `{"name":"<ACME> & Co","email":"special.customer.<timestamp>@example.test","phone":"+84-909-000-111","address":"<script>alert(1)</script>","taxCode":"TAX-!@#"}` | `201` nếu mọi field trong giới hạn độ dài và email hợp lệ | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |
| `TC-CUS-POST-06` | Unicode tiếng Việt | `{"name":"Công ty Ánh Dương","email":"unicode.customer.<timestamp>@example.test","phone":"0909000111","address":"Số 1 Đường Trần Hưng Đạo","taxCode":"MST-ÁNH-DƯƠNG"}` | `201` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-CUS-POST-07` | JSON lỗi | `{"name":"ACME","email":"acme@example.test","phone":"0909"` | `400`, `message=Request body is malformed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-CUS-POST-08` | Request rỗng | `{}` | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |

### 2.4. `PUT /api/customers/{id}`

Điều kiện trước:

1. Đã có `customerId` hợp lệ.
2. Đã login admin hoặc staff.
3. Đã `Authorize` trong Swagger.

Bước chung:

1. Chọn `PUT /api/customers/{id}`
2. Nhập `id`
3. Bấm `Try it out`
4. Dán JSON tương ứng
5. Bấm `Execute`

| ID | Mục tiêu | Input | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|
| `TC-CUS-PUT-01` | Cập nhật hợp lệ | `{"name":"ACME Updated","email":"<email-cu>","phone":"0909000222","address":"Updated Address","taxCode":"TAX-UPDATED"}` | `200`, `updatedAt` thay đổi, email giữ đúng nếu không đổi | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |
| `TC-CUS-PUT-02` | Thiếu field bắt buộc | Một hoặc nhiều field để trống | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-CUS-PUT-03` | Sai kiểu dữ liệu | Ví dụ `phone` là số | `Chưa xác định từ source code hiện tại.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-CUS-PUT-04` | Chuỗi quá dài | `name/address/phone/taxCode` vượt giới hạn | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-CUS-PUT-05` | Ký tự đặc biệt | `name/address/taxCode` chứa ký tự đặc biệt nhưng đúng độ dài | `200` nếu email hợp lệ | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |
| `TC-CUS-PUT-06` | Unicode tiếng Việt | `name/address/taxCode` chứa Unicode tiếng Việt | `200` nếu email hợp lệ | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-CUS-PUT-07` | JSON lỗi | body JSON thiếu dấu ngoặc hoặc thiếu dấu phẩy | `400`, `message=Request body is malformed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-CUS-PUT-08` | Request rỗng | `{}` | `400`, `message=Validation failed.` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |

## 3. Test case cho endpoint không có request body

| ID | Endpoint | Mục tiêu | Bước thực hiện | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|---|
| `TC-NB-01` | `GET /api/auth/me` | JWT hợp lệ | Gọi API với token admin hợp lệ | `200` | Đã xác minh runtime: `200` | Rất cao |
| `TC-NB-02` | `GET /api/auth/me` | Không gửi JWT | Gọi API không kèm `Authorization` | `401` | Đã xác minh runtime: `401` | Rất cao |
| `TC-NB-03` | `GET /api/auth/me` | JWT giả | Gọi API với token có format đúng nhưng signature sai | `401` | Đã xác minh runtime: `401` | Rất cao |
| `TC-NB-04` | `GET /api/auth/me` | JWT hết hạn | Gọi API với token `exp` quá khứ | `401` | Đã xác minh runtime: `401` | Rất cao |
| `TC-NB-05` | `GET /api/admin/summary` | User không được vào API admin | Gọi bằng token `USER` | `403` | Đã xác minh runtime: `403` | Rất cao |
| `TC-NB-06` | `GET /api/admin/summary` | Admin được vào API admin | Gọi bằng token `ADMIN` | `200` | Đã xác minh bằng test có sẵn: pass trong `AuthSecurityIntegrationTest.roleAuthorizationMatrixIsApplied` | Cao |
| `TC-NB-07` | `GET /api/customers` | Staff được xem customer | Gọi bằng token `STAFF` | `200` | Đã xác minh runtime: `200` | Cao |
| `TC-NB-08` | `GET /api/customers` | User bị chặn customer API | Gọi bằng token `USER` | `403` | Đã xác minh runtime: `403` | Rất cao |
| `TC-NB-09` | `GET /api/customers/{id}` | ID không tồn tại | Gọi bằng token admin/staff với `id` không có thật | `404` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Trung bình |
| `TC-NB-10` | `DELETE /api/customers/{id}` | Xóa customer hợp lệ | Gọi bằng token admin/staff với `id` hợp lệ | `204` | Đã xác minh runtime: `204` trong case cleanup customer tạm | Cao |
| `TC-NB-11` | `GET /api/audit-logs` | Admin xem audit log | Gọi bằng token admin | `200` | Đã xác minh bằng test có sẵn: pass trong `AuditLogIntegrationTest.loginAndBusinessActionsProduceAuditLogs` | Cao |
| `TC-NB-12` | `GET /api/audit-logs` | User bị chặn audit log | Gọi bằng token user | `403` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |

## 4. Test case JWT chuyên sâu

### 4.1. Chuẩn bị token

1. Gọi `POST /api/auth/login` với admin.
2. Copy `accessToken`.
3. Decode token tại `jwt.io` hoặc script local nội bộ.

Token runtime thực tế đã decode:

- `alg=HS256`
- `sub=admin@securityapp.local`
- `iat=2026-06-18T14:59:56Z`
- `exp=2026-06-19T14:59:56Z`
- không có claim `role`

### 4.2. Bảng test JWT

| ID | Mục tiêu | Input | Bước thực hiện | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|---|
| `JWT-01` | Xác nhận token hợp lệ | Token vừa login admin | Gọi `GET /api/auth/me` | `200` | Đã xác minh runtime: `200` | Rất cao |
| `JWT-02` | Không gửi token | Không có header `Authorization` | Gọi `GET /api/auth/me` | `401` | Đã xác minh runtime: `401` | Rất cao |
| `JWT-03` | Token giả | Token có signature sai | Gọi `GET /api/auth/me` | `401` | Đã xác minh runtime: `401` | Rất cao |
| `JWT-04` | Token bị sửa | Thay 1 ký tự ở cuối token thật | Gọi `GET /api/auth/me` | `401` | Đã xác minh runtime: `401` | Rất cao |
| `JWT-05` | Token hết hạn | Token ký đúng nhưng `exp` nằm trong quá khứ | Gọi `GET /api/auth/me` | `401` | Đã xác minh runtime: `401` | Rất cao |
| `JWT-06` | Token có subject không tồn tại | Token ký đúng nhưng `sub=ghost@example.test` | Gọi `GET /api/auth/me` | `401` | Đã xác minh runtime: `401` | Cao |
| `JWT-07` | User token gọi API admin | Token `USER` -> `GET /api/admin/summary` | Gọi API admin bằng token user | `403` | Đã xác minh runtime: `403` | Rất cao |
| `JWT-08` | User token gọi API customer | Token `USER` -> `GET /api/customers` | Gọi customer API bằng token user | `403` | Đã xác minh runtime: `403` | Rất cao |
| `JWT-09` | Vai trò nằm trong claim hay DB | Decode token và so với source | So sánh JWT payload với `JwtAuthenticationFilter` | Không có claim `role`; quyền lấy từ DB | Đã xác minh source và runtime | Cao |
| `JWT-10` | userA truy cập dữ liệu userB | Cần endpoint user-owned | Tìm endpoint kiểu `/api/users/{id}` hoặc object owner | `Chưa xác định từ source code hiện tại.` | `Chưa xác định từ source code hiện tại.` | Trung bình |

### 4.3. Vì sao từng case phải có

- `JWT-02` đến `JWT-05` chứng minh Spring Security không chỉ kiểm tra “có token”, mà còn kiểm chữ ký và hạn dùng.
- `JWT-06` chứng minh chỉ ký đúng chưa đủ; subject phải map được tới user thật trong DB.
- `JWT-07` và `JWT-08` chứng minh hệ thống tách rõ `401` với `403`.
- `JWT-09` rất quan trọng vì source hiện tại không hề dùng claim `role` để cấp quyền.

## 5. Test case bcrypt

### 5.1. Bước thực hiện

1. Kiểm tra source:
   - `SecurityConfig.passwordEncoder()` trả `new BCryptPasswordEncoder()`
2. Gọi register thử với password `123456`.
3. Query DB `users`.
4. So sánh hash của 3 user cùng mật khẩu demo.

### 5.2. Bảng test

| ID | Mục tiêu | Input | Bước thực hiện | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|---|
| `BCR-01` | Xác nhận encoder là bcrypt | Source code | Đọc `SecurityConfig` | `BCryptPasswordEncoder` | Đã xác minh bằng source | Rất cao |
| `BCR-02` | Password ngắn `123456` bị chặn trước khi hash | Register với `123456` | Gọi `POST /api/auth/register` | `400` vì password min `8` | Đã xác minh runtime: `400` | Cao |
| `BCR-03` | Cùng password nhưng hash khác nhau | 3 tài khoản demo cùng password `Password@123` | Query `SELECT email,password_hash FROM users` | Hash khác nhau | Đã xác minh runtime: 3 hash khác nhau | Rất cao |
| `BCR-04` | Không lưu plaintext | Query `users.password_hash` | So sánh với password thật | Không có plaintext | Đã xác minh runtime | Rất cao |
| `BCR-05` | Hash vẫn dùng được để login | Login bằng admin/staff/user demo | Gọi `POST /api/auth/login` | `200` | Đã xác minh runtime | Cao |

### 5.3. Vì sao phải test như vậy

- `BCR-02` tránh hiểu sai rằng mọi password đều được hash; thực tế source có validation trước.
- `BCR-03` là bằng chứng rõ nhất cho salt.
- `BCR-04` là tiêu chí tối thiểu của hệ thống có dùng password hashing đúng.

## 6. Test case AES

### 6.1. Bước thực hiện

1. Login admin hoặc staff.
2. Tạo customer bằng `POST /api/customers`.
3. Query DB bảng `customers`.
4. Gọi `GET /api/customers` hoặc `GET /api/customers/{id}` để đối chiếu plaintext response.
5. Nếu muốn test integrity, sửa ciphertext trong DB rồi gọi lại API.

### 6.2. Bảng test

| ID | Mục tiêu | Input | Bước thực hiện | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|---|
| `AES-01` | Dữ liệu nhạy cảm được mã hóa khi lưu | Tạo customer hợp lệ | `POST /api/customers` rồi query DB | DB lưu ciphertext, API response vẫn là plaintext business data | Đã xác minh bằng test có sẵn và runtime DB | Rất cao |
| `AES-02` | `phone` không plaintext trong DB | Query `phone_encrypted` | So sánh với số điện thoại gốc | Khác plaintext | Đã xác minh runtime | Rất cao |
| `AES-03` | `address` không plaintext trong DB | Query `address_encrypted` | So sánh với địa chỉ gốc | Khác plaintext | Đã xác minh runtime | Rất cao |
| `AES-04` | `taxCode` không plaintext trong DB | Query `tax_code_encrypted` | So sánh với MST gốc | Khác plaintext | Đã xác minh runtime | Rất cao |
| `AES-05` | Ciphertext bị sửa bị phát hiện | Cắt bớt hoặc sửa ciphertext trong DB | Sửa `phone_encrypted` rồi gọi `GET /api/customers/{id}` | Giải mã fail | Đã xác minh runtime: API trả `500` | Cao |
| `AES-06` | Sai key | Khởi động app bằng AES secret khác với DB hiện có | Gọi đọc customer cũ | Giải mã fail | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |
| `AES-07` | Sai IV | Làm hỏng phần IV trong payload Base64 | Gọi đọc customer | Giải mã fail | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |
| `AES-08` | Integrity tag hoạt động | Dùng payload bị sửa 1 byte | Gọi đọc customer | Giải mã fail | Suy ra từ source và đã được củng cố bởi `AES-05` | Rất cao |

### 6.3. Vì sao phải test như vậy

- `AES-01` đến `AES-04` chứng minh confidentiality.
- `AES-05` đến `AES-08` chứng minh integrity của GCM.
- Với source hiện tại, ciphertext hỏng sẽ đi tới `500` generic. Đây là behavior đúng theo code hiện có nhưng nên được ghi rõ cho người chấm.

## 7. Test case HTTPS/TLS

### 7.1. Bước thực hiện

1. Chạy `docker compose up -d --build`.
2. Truy cập `http://localhost:8080/api/health`.
3. Kiểm tra `docker-compose.yml`.
4. Kiểm tra `deploy/nginx/securityapp.conf`.
5. Nếu triển khai Nginx thật, dùng trình duyệt hoặc `curl -I` kiểm tra redirect `80 -> 443`.

### 7.2. Bảng test

| ID | Mục tiêu | Input | Bước thực hiện | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|---|
| `TLS-01` | Xác nhận local compose đang chạy HTTP | `GET http://localhost:8080/api/health` | Gọi trực tiếp bằng browser/curl | `200` qua HTTP | Đã xác minh runtime | Cao |
| `TLS-02` | Kiểm tra redirect HTTP -> HTTPS ở Nginx | Truy cập cổng `80` qua Nginx deploy | Dùng `curl -I http://<domain>` | `301` sang `https://...` | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |
| `TLS-03` | Kiểm tra cấu hình TLS version | Đọc `deploy/nginx/securityapp.conf` | Kiểm tra `ssl_protocols` | Có `TLSv1.2 TLSv1.3` | Đã xác minh bằng source | Cao |
| `TLS-04` | Password/JWT không đi qua URL | Đọc source login/axios/JWT flow | Kiểm tra body POST và header Authorization | Không thấy password/JWT trong query string | Đã xác minh bằng source | Rất cao |
| `TLS-05` | Password/JWT không lộ trong log đã kiểm | Xem `docker logs securityapp-backend --tail 200` | Tìm `Authorization`, token, plaintext password | Không phát hiện trong tail log đã kiểm | Đã xác minh runtime ở mức tail log | Trung bình |

## 8. Test case Postman/Newman

### 8.1. Các bước thao tác

1. Mở Postman.
2. Import:
   - `docs/postman/securityapp.postman_collection.json`
   - `docs/postman/securityapp.local.postman_environment.json`
3. Chọn environment `Security App Local`.
4. Chạy theo thứ tự:
   - `Auth / Login as Admin`
   - `Auth / Get Current User`
   - `Admin / Summary`
   - `Customers / Create Customer`
   - `Customers / Get Customer By Id`
   - `Customers / Update Customer`
   - `Customers / Delete Customer`
   - `Security Checks / Customers as Staff (Expect 200)`
   - `Security Checks / Customers as User (Expect 403)`
   - `Security Checks / Audit Logs as User (Expect 403)`
   - `Security Checks / Auth Me without Token (Expect 401)`
   - `Audit / List Audit Logs`

### 8.2. Kiểm tra collection hiện có

| ID | Mục tiêu | Input | Bước thực hiện | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|---|
| `PM-01` | Login admin và lưu token | Request `Auth / Login as Admin` | Run request | `200`, environment có `adminToken` | Đã xác minh bằng source collection script | Cao |
| `PM-02` | Kiểm `401` không token | Request `Security Checks / Auth Me without Token (Expect 401)` | Run request | `401` | Đã xác minh bằng source collection script | Cao |
| `PM-03` | Kiểm `403` user vào customer | Request `Security Checks / Customers as User (Expect 403)` | Run request | `403` | Đã xác minh bằng source collection script | Cao |
| `PM-04` | Kiểm `403` user vào audit log | Request `Security Checks / Audit Logs as User (Expect 403)` | Run request | `403` | Đã xác minh bằng source collection script | Cao |
| `PM-05` | Create customer lưu `customerId` | Request `Customers / Create Customer` | Run request | `201`, environment có `customerId` | Đã xác minh bằng source collection script | Cao |

### 8.3. Script nên bổ sung cho collection

Collection hiện tại chưa có `response time` và `JSON schema` đầy đủ. Nên thêm script như sau vào các request quan trọng:

```javascript
pm.test("Status code is expected", function () {
  pm.expect(pm.response.code).to.be.oneOf([200, 201, 204]);
});

pm.test("Response time < 1000ms", function () {
  pm.expect(pm.response.responseTime).to.be.below(1000);
});

pm.test("JWT exists", function () {
  var data = pm.response.json();
  pm.expect(data.accessToken).to.be.a("string").and.not.empty;
});
```

Ví dụ schema check cho `GET /api/auth/me`:

```javascript
const schema = {
  type: "object",
  required: ["id", "fullName", "email", "role"],
  properties: {
    id: { type: "number" },
    fullName: { type: "string" },
    email: { type: "string" },
    role: { type: "string", enum: ["ADMIN", "STAFF", "USER"] }
  }
};

pm.test("Schema is valid", function () {
  pm.response.to.have.jsonSchema(schema);
});
```

### 8.4. Chạy Newman

```powershell
npx --yes newman run docs\postman\securityapp.postman_collection.json -e docs\postman\securityapp.local.postman_environment.json --reporters cli
```

Kỳ vọng:

- `failed = 0`
- status code đúng
- biến `token/customerId` được set/unset đúng

## 9. Test case OWASP ZAP

### 9.1. Mục tiêu

- Import inventory API từ `OpenAPI`
- Spider surface public
- Passive scan
- Active scan
- Ghi nhận `Alert`, `Risk`, `CWE`, `URL`, `Evidence`

### 9.2. Các bước thao tác bằng ZAP

1. Chạy ứng dụng:

```powershell
cd E:\PROJECT_MMUD
docker compose up -d --build
```

2. Mở OWASP ZAP Desktop.
3. Chọn import OpenAPI bằng URL:

```text
http://localhost:8080/v3/api-docs
```

4. Tạo context cho target.
5. Nếu muốn scan API có auth:
   - login lấy JWT trước
   - thêm header `Authorization: Bearer <token>` qua Replacer rule hoặc Manual Request Editor
6. Chạy:
   - Spider
   - Passive Scan
   - Active Scan
7. Xuất report HTML/JSON/XML.

### 9.3. Bảng ghi nhận alert

| ID | Mục tiêu | Input | Bước thực hiện | Expected Result | Actual Result | Risk |
|---|---|---|---|---|---|---|
| `ZAP-01` | Import OpenAPI | `http://localhost:8080/v3/api-docs` | Import definition vào ZAP | ZAP nhận đầy đủ endpoint | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |
| `ZAP-02` | Spider public surface | Swagger/OpenAPI target | Run Spider | Thu thập URL public | Repo đã có baseline quét `swagger-ui.html`, không phải import OpenAPI | Trung bình |
| `ZAP-03` | Passive Scan | Target public | Run Passive Scan | Có danh sách alert mức Info/Warn/Fail | Đã xác minh bằng artifact repo | Cao |
| `ZAP-04` | Active Scan | Target đã import | Run Active Scan | Có alert chi tiết hơn baseline | Chưa chạy trực tiếp trong phiên phân tích hiện tại. | Cao |

### 9.4. Alert thực tế từ artifact repo

| Alert | Risk | CWE | URL | Evidence |
|---|---|---|---|---|
| `Content Security Policy (CSP) Header Not Set` | `Medium (High)` | `693` | `http://host.docker.internal:8080/swagger-ui.html` | Không có evidence cụ thể trong instance |
| `Modern Web Application` | `Informational (Medium)` | `-1` | `http://host.docker.internal:8080/swagger-ui.html` | `<script src="./swagger-ui-bundle.js" charset="UTF-8"> </script>` |

## 10. Ghi chú cuối cùng cho người test

1. Case `OAuth2` chỉ nên test khi nhóm bổ sung implementation chạy thật.
2. Case `Refresh Token` không áp dụng cho source hiện tại.
3. Case `JWT userA truy cập dữ liệu userB` chưa test được vì source không có endpoint user-owned kiểu đó.
4. Case `123456` ở phần bcrypt phải ghi rõ là source hiện tại chặn từ validation, không đi tới bước hash.
5. Case `AES tamper` hiện có giá trị trình diễn tốt vì đã xác minh runtime rằng ciphertext hỏng làm API trả `500`.
