# Demo 03: Mã hóa dữ liệu nhạy cảm bằng AES-GCM

## 1. Mục tiêu

Demo này dùng để chứng minh:

- Dữ liệu nhạy cảm trong database không lưu dạng rõ
- Backend vẫn giải mã được dữ liệu cho đúng người có quyền
- Nếu ciphertext bị sửa, quá trình giải mã sẽ thất bại vì AES-GCM có kiểm tra toàn vẹn

## 2. Các trường đang được mã hóa

- `users.phone_number_encrypted`
- `users.billing_address_encrypted`
- `enrollments.payment_reference_encrypted`
- `certificates.certificate_code_encrypted`

## 3. Chuẩn bị

### 3.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 3.2. Tài khoản gợi ý

- `student1@example.com / Password123!`

Tài khoản này có sẵn:

- thông tin cá nhân để xem `phoneNumber`, `billingAddress`;
- enrollment đã kích hoạt;
- certificate đã được cấp để xem `certificateCode`.

### 3.3. Thực hiện demo này ở đâu

Demo 03 nên dùng **3 nơi**:

#### Nơi 1: Swagger để gọi API

```text
https://localhost/swagger-ui.html
```

Tại đây bạn sẽ:

- đăng nhập;
- gọi `GET /api/auth/me`;
- gọi `GET /api/certificates/me`.

#### Nơi 2: PowerShell để xem trực tiếp dữ liệu trong database

Bạn mở PowerShell tại:

```text
E:\PROJECT_MMUD
```

Rồi chạy các lệnh `docker exec ... mysql ...` để xem ciphertext.

#### Nơi 3: PowerShell để chạy test toàn vẹn AES-GCM

Bạn cũng dùng PowerShell để chạy:

```powershell
mvn -Dtest=EncryptionServiceTest test
```

Nhằm chứng minh ciphertext bị sửa sẽ không giải mã được.

## 4. Các bước thực hiện

### Bước 1: Mở Swagger

Truy cập:

```text
https://localhost/swagger-ui.html
```

Sau đó:

1. Tìm nhóm `Auth API`
2. Mở `POST /api/auth/login`
3. Bấm `Try it out`

### Bước 2: Đăng nhập bằng tài khoản mẫu

Body:

```json
{
  "email": "student1@example.com",
  "password": "Password123!"
}
```

Thao tác:

1. Dán JSON vào request body
2. Bấm `Execute`
3. Copy `accessToken`

### Bước 3: Dán Bearer token vào Swagger

1. Bấm `Authorize`
2. Nhập:

```text
Bearer <accessToken>
```

3. Bấm `Authorize`
4. Đóng hộp thoại

### Bước 4: Xem dữ liệu đã được giải mã qua API profile

Mở:

```http
GET /api/auth/me
```

Rồi:

1. Bấm `Try it out`
2. Bấm `Execute`

Kết quả mong đợi:

- Trả `200 OK`
- Response có các trường như:
  - `phoneNumber`
  - `billingAddress`

Điểm cần nói:

- Đây là dữ liệu đã được ứng dụng giải mã ở tầng backend
- Người dùng hợp lệ mới được nhận dữ liệu này

### Bước 5: Xem mã chứng chỉ đã được giải mã

Mở:

```http
GET /api/certificates/me
```

Rồi:

1. Bấm `Try it out`
2. Bấm `Execute`

Kết quả mong đợi:

- Trả `200 OK`
- Trong response có trường:
  - `certificateCode`

Ý nghĩa:

- Dù database lưu dạng mã hóa, API vẫn trả được giá trị đã giải mã cho đúng chủ sở hữu

### Bước 6: Mở PowerShell để xem dữ liệu thật trong database

Mở PowerShell và đứng tại thư mục project:

```powershell
cd E:\PROJECT_MMUD
```

Nếu trong `.env` bạn đang để:

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

- Các cột mã hóa là chuỗi dài, không đọc hiểu trực tiếp
- Không thấy số điện thoại, địa chỉ, mã thanh toán hay mã chứng chỉ ở dạng rõ

### Bước 7: So sánh dữ liệu giữa API và database

Lúc này bạn đối chiếu:

- Trên Swagger:
  - `phoneNumber`
  - `billingAddress`
  - `certificateCode`
- Trong database:
  - `phone_number_encrypted`
  - `billing_address_encrypted`
  - `certificate_code_encrypted`

Kết luận cần rút ra:

- Dữ liệu lưu trữ được bảo vệ
- Ứng dụng chỉ giải mã khi đúng người dùng, đúng ngữ cảnh

### Bước 8: Chứng minh AES-GCM phát hiện dữ liệu bị sửa

Khuyến nghị không sửa thẳng database demo. Cách sạch nhất là chạy test.

Mở PowerShell:

```powershell
cd E:\PROJECT_MMUD\backend
mvn -Dtest=EncryptionServiceTest test
```

Kết quả mong đợi:

- Có test mã hóa rồi giải mã thành công
- Có test sửa ciphertext và nhận lỗi giải mã

Điểm cần nói:

- AES-GCM không chỉ bảo mật bí mật dữ liệu
- Nó còn kiểm tra tính toàn vẹn
- Vì vậy ciphertext bị sửa sẽ không được chấp nhận

## 5. Cách trình bày ngắn gọn trước giảng viên

Bạn có thể trình bày theo thứ tự:

1. Mở Swagger
2. Đăng nhập `student1`
3. Gọi `GET /api/auth/me` và `GET /api/certificates/me`
4. Chỉ cho giảng viên thấy các trường đã giải mã
5. Mở PowerShell, truy vấn bảng `users`, `enrollments`, `certificates`
6. So sánh ciphertext trong database với dữ liệu có nghĩa ở response API
7. Chạy `EncryptionServiceTest` để chứng minh sửa ciphertext sẽ bị phát hiện

Nếu giảng viên hỏi “demo này làm ở đâu”, câu trả lời chuẩn là:

- **Swagger để xem dữ liệu sau khi backend giải mã**
- **PowerShell để xem ciphertext trong database**
- **PowerShell để chạy test minh họa AES-GCM phát hiện dữ liệu bị sửa**

## 6. Giải thích ngắn gọn để trình bày

- AES là thuật toán mã hóa đối xứng
- GCM là chế độ vừa bảo mật dữ liệu vừa kiểm tra toàn vẹn
- Mỗi lần mã hóa đều dùng IV ngẫu nhiên để tránh sinh cùng ciphertext cho cùng đầu vào
- Secret key lấy từ biến môi trường `ENCRYPTION_KEY`, không hard-code trong source code

## 7. Minh chứng nên chụp cho báo cáo

- Ảnh `GET /api/auth/me` có `phoneNumber`, `billingAddress`
- Ảnh `GET /api/certificates/me` có `certificateCode`
- Ảnh PowerShell truy vấn bảng `users`
- Ảnh PowerShell truy vấn bảng `enrollments`
- Ảnh PowerShell truy vấn bảng `certificates`
- Ảnh test `EncryptionServiceTest` chạy thành công

## 8. Kết luận

Demo này chứng minh hệ thống đã áp dụng AES-GCM đúng hướng cho dữ liệu nhạy cảm:

- Dữ liệu lưu trữ không ở dạng rõ
- Người dùng hợp lệ vẫn sử dụng được dữ liệu cần thiết
- Việc sửa ciphertext sẽ bị phát hiện
