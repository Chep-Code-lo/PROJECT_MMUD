# Demo 01: Băm mật khẩu bằng bcrypt

## 1. Mục tiêu

Demo này dùng để chứng minh ba điểm quan trọng:

- Mật khẩu không được lưu dạng plaintext trong cơ sở dữ liệu
- Người dùng vẫn đăng nhập bình thường bằng mật khẩu gốc
- bcrypt là hàm băm một chiều, không phải cơ chế mã hóa để giải ngược

## 2. Chuẩn bị

### 2.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 2.2. Tài khoản có thể dùng ngay

- `student1@example.com / Password123!`
- `student2@example.com / Password123!`
- `instructor@example.com / Password123!`
- `admin@example.com / Admin123!`

Bạn cũng có thể đăng ký thêm một tài khoản mới để minh họa rõ luồng tạo hash ngay sau khi đăng ký.

## 3. Các bước thực hiện

### Bước 1: Đăng nhập bằng tài khoản seed

Gọi API:

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

Kết quả mong đợi:

- Đăng nhập thành công
- Nhận được `accessToken`, `refreshToken` và thông tin user

### Bước 2: Xem dữ liệu mật khẩu trong database

Mở MySQL trong container:

```powershell
docker exec securityapp-db mysql -uroot -p<MYSQL_ROOT_PASSWORD> securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Quan sát cột `password_hash`:

- Giá trị không phải `Password123!`
- Chuỗi thường bắt đầu bằng tiền tố bcrypt như `$2a$`, `$2b$` hoặc `$2y$`
- Mỗi tài khoản có hash khác nhau dù có thể dùng cùng mật khẩu

Ví dụ điều cần chứng minh:

- `student1@example.com` dùng mật khẩu `Password123!`
- Trong bảng `users`, cột `password_hash` là một chuỗi băm dài
- Người xem database không thể nhìn ra mật khẩu gốc

### Bước 3: Thử đăng nhập lại bằng mật khẩu gốc

Tiếp tục dùng đúng mật khẩu:

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

- Backend dùng `passwordEncoder.matches(rawPassword, passwordHash)`
- Đăng nhập vẫn thành công

### Bước 4: Thử đăng nhập bằng mật khẩu sai

Body:

```json
{
  "email": "student1@example.com",
  "password": "WrongPassword123!"
}
```

Kết quả mong đợi:

- Backend trả `401 Unauthorized`
- Audit log ghi nhận `LOGIN_FAILED`

## 4. Giải thích ngắn gọn để trình bày với giảng viên

- bcrypt là hàm băm một chiều, không có chức năng giải mã ngược
- bcrypt tự sinh salt nên hai người dùng cùng mật khẩu vẫn có hash khác nhau
- Khi đăng nhập, hệ thống không giải mã hash mà so khớp bằng `matches`
- Đây là phương án phù hợp để bảo vệ mật khẩu trong cơ sở dữ liệu nếu database bị lộ

## 5. Minh chứng nên chụp cho báo cáo

- Ảnh request đăng nhập thành công
- Ảnh bảng `users` hiển thị cột `password_hash`
- Ảnh request đăng nhập sai trả `401`

## 6. Kết luận

Demo này chứng minh hệ thống đã xử lý mật khẩu đúng theo hướng an toàn:

- Không lưu plaintext
- Có thể xác thực đúng người dùng
- Giảm rủi ro lộ mật khẩu thật khi lộ dữ liệu database
