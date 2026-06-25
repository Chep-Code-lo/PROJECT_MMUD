# Demo 03: Mã hóa dữ liệu nhạy cảm bằng AES-GCM

## 1. Mục tiêu

Demo này nhằm chứng minh:

- Dữ liệu nhạy cảm trong database không lưu dạng rõ
- Backend vẫn giải mã được đúng dữ liệu cho người có quyền
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

Tài khoản này đã có sẵn dữ liệu để minh họa:

- thông tin cá nhân
- enrollment đã kích hoạt
- certificate đã được cấp

## 4. Các bước thực hiện

### Bước 1: Xem profile qua API

Đăng nhập `student1@example.com`, lấy `accessToken`, sau đó gọi:

```http
GET /api/auth/me
Authorization: Bearer <accessToken>
```

Kết quả mong đợi:

- API trả về thông tin hồ sơ người dùng
- Người dùng hợp lệ thấy được dữ liệu đã được giải mã ở tầng ứng dụng

### Bước 2: Xem dữ liệu trong database

Chạy các câu lệnh sau:

```powershell
docker exec securityapp-db mysql -uroot -p<MYSQL_ROOT_PASSWORD> securityapp -e "SELECT id,email,phone_number_encrypted,billing_address_encrypted FROM users;"
docker exec securityapp-db mysql -uroot -p<MYSQL_ROOT_PASSWORD> securityapp -e "SELECT id,student_id,course_id,payment_reference_encrypted,status FROM enrollments;"
docker exec securityapp-db mysql -uroot -p<MYSQL_ROOT_PASSWORD> securityapp -e "SELECT id,student_id,course_id,certificate_code_encrypted,score FROM certificates;"
```

Quan sát:

- Các cột mã hóa là chuỗi ciphertext dài
- Không đọc được số điện thoại, địa chỉ hay mã chứng chỉ gốc từ database

### Bước 3: So sánh dữ liệu API và dữ liệu database

Đối chiếu:

- API trả về dữ liệu có ý nghĩa với đúng người dùng
- Database chỉ lưu ciphertext

Ý cần trình bày:

- Dữ liệu nhạy cảm được bảo vệ ở trạng thái lưu trữ
- Ứng dụng chỉ giải mã khi cần và trong đúng ngữ cảnh được phép

### Bước 4: Chứng minh AES-GCM phát hiện dữ liệu bị sửa

Khuyến nghị dùng test tự động thay vì sửa trực tiếp database demo.

Chạy:

```powershell
cd backend
mvn -Dtest=EncryptionServiceTest test
```

Kết quả mong đợi:

- Test mã hóa rồi giải mã thành công với dữ liệu nguyên vẹn
- Khi sửa một phần ciphertext, test ném lỗi và báo không thể giải mã

Nếu muốn trình bày bằng lời:

- AES-GCM không chỉ bảo mật bí mật dữ liệu
- Nó còn kiểm tra tính toàn vẹn
- Vì vậy ciphertext bị sửa sẽ không được chấp nhận

## 5. Giải thích ngắn gọn để trình bày

- AES là thuật toán mã hóa đối xứng
- Chế độ GCM vừa cung cấp tính bí mật vừa cung cấp xác thực dữ liệu
- Mỗi lần mã hóa đều dùng IV ngẫu nhiên để tránh sinh ra cùng ciphertext cho cùng một dữ liệu đầu vào
- Secret key lấy từ biến môi trường `ENCRYPTION_KEY`, không hard-code trong source code

## 6. Minh chứng nên chụp cho báo cáo

- Ảnh response `GET /api/auth/me`
- Ảnh kết quả truy vấn bảng `users`, `enrollments`, `certificates`
- Ảnh test `EncryptionServiceTest` chạy thành công

## 7. Kết luận

Demo này chứng minh hệ thống đã áp dụng AES-GCM đúng hướng cho dữ liệu nhạy cảm:

- Dữ liệu lưu trữ không ở dạng rõ
- Người dùng hợp lệ vẫn sử dụng được hệ thống
- Việc sửa ciphertext sẽ bị phát hiện
