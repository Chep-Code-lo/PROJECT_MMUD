# Demo 01: Băm mật khẩu bằng bcrypt

## 1. Mục tiêu

Demo này nhằm chứng minh ba ý chính:

1. Mật khẩu không được lưu trực tiếp dưới dạng plaintext trong cơ sở dữ liệu.
2. Người dùng vẫn đăng nhập bình thường bằng mật khẩu gốc nhờ cơ chế so khớp `bcrypt`.
3. `bcrypt` là hàm băm một chiều, không phải cơ chế mã hóa để giải ngược lại mật khẩu.

## 2. Chuẩn bị

### 2.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 2.2. Tài khoản nên dùng

- `student1@example.com / Password123!`
- `student2@example.com / Password123!`
- `admin@example.com / Admin123!`

### 2.3. Thực hiện ở đâu

Demo này nên dùng hai nơi:

- `Swagger` để gọi API đăng nhập hoặc đăng ký:

```text
https://localhost/swagger-ui.html
```

- `PowerShell` để xem dữ liệu trong database:

```text
E:\PROJECT_MMUD
```

Nếu máy chủ của bạn đang bật tunnel public thì vẫn có thể mở Swagger qua domain public, ví dụ `https://hackerlo.online/swagger-ui.html`. Tuy nhiên khi demo kỹ thuật nên ưu tiên `https://localhost` để thao tác ổn định hơn.

## 3. Các bước thực hiện chi tiết

### Bước 1: Mở Swagger và đăng nhập

Truy cập:

```text
https://localhost/swagger-ui.html
```

Trong nhóm `Auth API`, mở endpoint:

```http
POST /api/auth/login
```

Body mẫu:

```json
{
  "email": "student1@example.com",
  "password": "Password123!"
}
```

Bấm `Try it out`, dán body rồi bấm `Execute`.

### Bước 2: Quan sát phản hồi đăng nhập

Kết quả mong đợi:

- HTTP `200 OK`
- response có `accessToken`
- response có `refreshToken`
- response có `user.email = student1@example.com`

Điểm cần nhấn mạnh khi trình bày:

- Người dùng đăng nhập bằng mật khẩu gốc là `Password123!`.
- Nhưng backend không hề lưu trực tiếp chuỗi này trong bảng `users`.

### Bước 3: Mở PowerShell và truy vấn bảng `users`

Mở PowerShell tại thư mục project:

```powershell
cd E:\PROJECT_MMUD
```

Kiểm tra giá trị `MYSQL_ROOT_PASSWORD` trong file `.env`. Nếu đang để:

```text
MYSQL_ROOT_PASSWORD=change-me-root-password
```

thì chạy:

```powershell
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Nếu mật khẩu root trong `.env` khác, hãy thay lại cho đúng.

### Bước 4: Chỉ ra cột `password_hash`

Quan sát kết quả vừa truy vấn:

- cột `password_hash` không chứa `Password123!`
- giá trị thường bắt đầu bằng `$2a$`, `$2b$` hoặc `$2y$`
- chuỗi hash khá dài và không đọc ra được mật khẩu gốc

Ý cần nói với giảng viên:

- Database chỉ lưu hash bcrypt.
- Nếu ai đó lấy được bảng `users` thì cũng không nhìn thấy mật khẩu thật.

### Bước 5: Đăng nhập lại bằng đúng mật khẩu gốc

Quay lại Swagger, gọi lại:

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

Kết quả mong đợi:

- vẫn trả `200 OK`

Ý nghĩa:

- hệ thống không giải mã hash
- backend chỉ dùng `passwordEncoder.matches(rawPassword, passwordHash)` để so khớp

### Bước 6: Thử đăng nhập bằng mật khẩu sai

Vẫn ở endpoint đăng nhập, đổi password thành:

```json
{
  "email": "student1@example.com",
  "password": "WrongPassword123!"
}
```

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
Invalid email or password.
```

### Bước 7: Kiểm tra audit log của lần đăng nhập sai

Đăng nhập admin trên Swagger:

```json
{
  "email": "admin@example.com",
  "password": "Admin123!"
}
```

Copy `accessToken` của admin, bấm `Authorize`, nhập:

```text
Bearer <admin-access-token>
```

Sau đó gọi:

```http
GET /api/admin/audit-logs
```

Kết quả mong đợi:

- có bản ghi `LOGIN_SUCCESS`
- có bản ghi `LOGIN_FAILED`

### Bước 8: Tùy chọn đăng ký thêm một tài khoản mới để minh họa

Nếu muốn chứng minh hash được tạo ra ngay lúc đăng ký, có thể gọi:

```http
POST /api/auth/register
```

Body ví dụ:

```json
{
  "fullName": "Demo BCrypt",
  "email": "demo.bcrypt@example.com",
  "password": "Password123!",
  "phoneNumber": "0909555666",
  "billingAddress": "123 Demo Street"
}
```

Sau đó chạy lại truy vấn SQL:

```powershell
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,email,password_hash FROM users WHERE email='demo.bcrypt@example.com';"
```

Kết quả mong đợi:

- tài khoản vừa tạo đã có `password_hash`
- không có bất kỳ trường nào lưu mật khẩu gốc

## 4. Kết quả mong đợi

Sau khi làm xong Demo 01, bạn phải chứng minh được:

1. Người dùng đăng nhập bằng mật khẩu gốc nhưng database chỉ chứa `password_hash`.
2. Mật khẩu sai bị từ chối bằng `401 Unauthorized`.
3. Audit log có ghi nhận sự kiện đăng nhập thành công và thất bại.

## 5. Câu nên nói khi trình bày

- `bcrypt` là hàm băm một chiều nên không có chuyện giải mã ngược để lấy lại mật khẩu.
- `bcrypt` tự tạo salt nên hai tài khoản dùng cùng mật khẩu vẫn có thể có hash khác nhau.
- Nếu database bị lộ, kẻ tấn công không nhìn thấy plaintext password.
- Đây là cơ chế phù hợp để bảo vệ mật khẩu trong các hệ thống đăng nhập.

## 6. Ảnh nên chụp cho báo cáo

- Ảnh `POST /api/auth/login` trả `200 OK`.
- Ảnh PowerShell hiển thị bảng `users` với cột `password_hash`.
- Ảnh đăng nhập sai trả `401 Unauthorized`.
- Ảnh `GET /api/admin/audit-logs` có `LOGIN_FAILED`.

## 7. Cách reset sau demo

Nếu bạn đã tạo thêm tài khoản phụ để minh họa, có thể:

- giữ lại nếu không ảnh hưởng gì đến buổi demo sau
- hoặc reset toàn bộ seed data bằng:

```powershell
docker compose down -v
docker compose up --build -d
```

## 8. Kết luận

Demo này chứng minh hệ thống đang xử lý mật khẩu đúng theo hướng an toàn:

- không lưu plaintext
- xác thực được đúng người dùng
- giảm rủi ro khi dữ liệu database bị rò rỉ
