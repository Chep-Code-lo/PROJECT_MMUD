# Demo 03: Mã hóa dữ liệu nhạy cảm bằng AES-GCM

## 1. Mục tiêu

Demo này nhằm chứng minh:

1. Dữ liệu nhạy cảm trong database không được lưu ở dạng rõ.
2. Backend vẫn có thể giải mã để trả dữ liệu cho đúng người dùng hợp lệ.
3. Nếu ciphertext bị sửa thì quá trình giải mã sẽ thất bại do AES-GCM có cơ chế kiểm tra toàn vẹn.

## 2. Các trường đang được mã hóa trong project

- `users.phone_number_encrypted`
- `users.billing_address_encrypted`
- `enrollments.payment_reference_encrypted`
- `certificates.certificate_code_encrypted`

## 3. Chuẩn bị

### 3.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 3.2. Tài khoản nên dùng

- `student1@example.com / Password123!`

Tài khoản này đã có sẵn:

- thông tin hồ sơ để xem `phoneNumber` và `billingAddress`
- enrollment đã kích hoạt
- certificate đã được cấp để xem `certificateCode`

### 3.3. Thực hiện ở đâu

Demo này nên dùng ba nơi:

- `Swagger` để xem dữ liệu sau khi backend giải mã:

```text
https://localhost/swagger-ui.html
```

- `PowerShell` để truy vấn database và nhìn thấy ciphertext.
- `PowerShell` để chạy unit test chứng minh ciphertext bị sửa sẽ không giải mã được.

## 4. Các bước thực hiện chi tiết

### Bước 1: Đăng nhập và authorize trên Swagger

Mở:

```text
https://localhost/swagger-ui.html
```

Gọi:

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

Copy `accessToken`, bấm `Authorize`, nhập:

```text
Bearer <access-token>
```

### Bước 2: Xem dữ liệu hồ sơ đã được giải mã

Gọi:

```http
GET /api/auth/me
```

Kết quả mong đợi:

- HTTP `200 OK`
- response có các trường:
  - `phoneNumber`
  - `billingAddress`

Điểm cần nói:

- dữ liệu trả về cho người dùng đã được backend giải mã
- nhưng trong database các trường này không lưu ở dạng rõ

### Bước 3: Xem dữ liệu chứng chỉ đã được giải mã

Gọi:

```http
GET /api/certificates/me
```

Kết quả mong đợi:

- HTTP `200 OK`
- response có ít nhất một chứng chỉ
- trong từng phần tử có trường:
  - `certificateCode`
  - `score`
  - `courseTitle`

### Bước 4: Truy vấn database để xem ciphertext thật sự

Mở PowerShell:

```powershell
cd E:\PROJECT_MMUD
```

Nếu `.env` đang để:

```text
MYSQL_ROOT_PASSWORD=change-me-root-password
```

thì chạy lần lượt:

```powershell
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,email,phone_number_encrypted,billing_address_encrypted FROM users;"
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,student_id,course_id,payment_reference_encrypted,status FROM enrollments;"
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,student_id,course_id,certificate_code_encrypted,score FROM certificates;"
```

Quan sát:

- các cột `_encrypted` là chuỗi dài, khó đọc
- không nhìn thấy số điện thoại, địa chỉ hay mã chứng chỉ ở dạng plaintext

### Bước 5: So sánh dữ liệu giữa API và database

Đối chiếu hai nơi:

- trên Swagger:
  - `phoneNumber`
  - `billingAddress`
  - `certificateCode`
- trong database:
  - `phone_number_encrypted`
  - `billing_address_encrypted`
  - `certificate_code_encrypted`

Kết luận phải rút ra:

- cùng một dữ liệu nhưng ở database là ciphertext
- còn ở response API là giá trị đã được giải mã có kiểm soát

### Bước 6: Giải thích riêng về `payment_reference_encrypted`

Trường `payment_reference_encrypted` hiện không được trả về cho phía client. Đây là chủ đích bảo mật.

Vì vậy với trường này, cách chứng minh tốt nhất là:

1. truy vấn bảng `enrollments`
2. chỉ ra cột `payment_reference_encrypted`
3. giải thích rằng hệ thống chỉ giữ bản mã hóa ở tầng lưu trữ

Nếu bạn muốn demo đầy đủ hơn, hãy kết hợp với `docs/demo-05-webhook-hmac.md` để tạo một enrollment mới rồi xem ciphertext tương ứng sau webhook.

### Bước 7: Chứng minh AES-GCM phát hiện dữ liệu bị sửa

Không nên sửa thẳng database khi đang chuẩn bị bảo vệ dữ liệu demo. Cách sạch nhất là chạy unit test.

```powershell
cd E:\PROJECT_MMUD\backend
mvn -Dtest=EncryptionServiceTest test
```

Kết quả mong đợi:

- test `aesGcmRoundTripWorksAndTamperingFails` chạy thành công
- trong test có phần sửa ciphertext rồi giải mã thất bại

### Bước 8: Nếu muốn nói sâu hơn về kỹ thuật

Bạn có thể giải thích ngắn gọn:

- AES là mã hóa đối xứng
- GCM vừa bảo mật dữ liệu vừa kiểm tra tính toàn vẹn
- mỗi lần mã hóa dùng IV ngẫu nhiên
- key lấy từ biến môi trường `ENCRYPTION_KEY`, không hard-code trong source code

## 5. Kết quả mong đợi

Sau khi làm xong Demo 03, bạn phải chứng minh được:

1. Hồ sơ người dùng và mã chứng chỉ không lưu ở dạng rõ trong database.
2. API vẫn trả được dữ liệu đã giải mã cho đúng người dùng hợp lệ.
3. Ciphertext bị sửa sẽ làm quá trình giải mã lỗi.

## 6. Câu nên nói khi trình bày

- Mật khẩu dùng `bcrypt` còn dữ liệu nhạy cảm có thể cần lấy lại thì dùng `AES-GCM`.
- `AES-GCM` phù hợp vì vừa giữ bí mật vừa phát hiện bị sửa dữ liệu.
- Kẻ tấn công đọc trực tiếp database sẽ chỉ thấy ciphertext.
- Secret key không đặt cứng trong source mà lấy từ biến môi trường.

## 7. Ảnh nên chụp cho báo cáo

- Ảnh `GET /api/auth/me` có `phoneNumber`, `billingAddress`.
- Ảnh `GET /api/certificates/me` có `certificateCode`.
- Ảnh PowerShell truy vấn bảng `users`.
- Ảnh PowerShell truy vấn bảng `enrollments`.
- Ảnh PowerShell truy vấn bảng `certificates`.
- Ảnh chạy `EncryptionServiceTest`.

## 8. Cách reset sau demo

Demo này thường không làm thay đổi seed data, nên thường không cần reset. Chỉ cần reset nếu bạn đã kết hợp thêm demo webhook hoặc checkout mới.

## 9. Kết luận

Demo này chứng minh dự án đã áp dụng AES-GCM đúng hướng:

- bảo vệ dữ liệu nhạy cảm khi lưu trong database
- chỉ giải mã ở phía backend khi có quyền phù hợp
- phát hiện được việc sửa đổi ciphertext
