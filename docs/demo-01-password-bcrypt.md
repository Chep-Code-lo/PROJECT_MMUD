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
- `admin@example.com / Admin123!`

Bạn cũng có thể đăng ký thêm một tài khoản mới để minh họa rõ luồng tạo hash ngay sau khi đăng ký.

### 2.3. Thực hiện demo này ở đâu

Demo 01 cần dùng **2 nơi khác nhau**:

#### Nơi 1: Trình duyệt để gọi API

Bạn nên dùng **Swagger** trên chính máy đang chạy Docker:

```text
https://localhost/swagger-ui.html
```

Tại đây bạn sẽ:

- gọi API đăng nhập;
- có thể gọi thêm API đăng ký nếu muốn tạo tài khoản mới để demo;
- quan sát mã trạng thái và dữ liệu response.

Lưu ý:

- Swagger dùng cùng cổng `443`, nên bấm `Execute` sẽ không bị lệch cổng.
- Swagger chỉ mở được với hostname local, không phải cho máy khác trong mạng.

#### Nơi 2: Terminal hoặc PowerShell để xem dữ liệu database

Bạn mở PowerShell tại thư mục gốc project:

```text
E:\PROJECT_MMUD
```

Tại đây bạn sẽ chạy lệnh:

- `docker exec ... mysql ...`

để xem trực tiếp cột `password_hash` trong bảng `users`.

#### Có thể dùng Postman thay cho Swagger không

Có. Nếu bạn quen Postman hơn thì phần gọi API có thể làm bằng Postman. Tuy nhiên với buổi demo trước giảng viên, **Swagger + PowerShell** thường là cách dễ trình bày nhất vì:

- nhìn rõ endpoint;
- nhìn rõ request/response;
- không cần cấu hình collection trước.

## 3. Các bước thực hiện

### Bước 1: Mở Swagger

Trên trình duyệt của máy đang chạy project, truy cập:

```text
https://localhost/swagger-ui.html
```

Sau đó:

1. Tìm nhóm `Auth API`
2. Mở endpoint `POST /api/auth/login`
3. Bấm `Try it out`

Nếu trình duyệt cảnh báo certificate self-signed, hãy chọn tiếp tục truy cập vì đây là môi trường demo local.

### Bước 2: Đăng nhập bằng tài khoản mẫu trên Swagger

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

Trong Swagger, thao tác cụ thể là:

1. Dán JSON vào ô request body
2. Bấm `Execute`
3. Quan sát phần response bên dưới

Kết quả mong đợi:

- Đăng nhập thành công
- Nhận được `accessToken`, `refreshToken` và thông tin user

Điểm cần nói khi demo:

- Người dùng đăng nhập bằng mật khẩu gốc là `Password123!`
- Nhưng backend không lưu mật khẩu này trực tiếp trong database

### Bước 3: Mở PowerShell để xem dữ liệu mật khẩu trong database

Mở PowerShell tại thư mục project rồi chạy:

```powershell
Get-Location
```

Nếu chưa đứng ở thư mục project thì chuyển về:

```powershell
cd E:\PROJECT_MMUD
```

Sau đó xem mật khẩu băm trong bảng `users`.

Mở MySQL trong container:

```powershell
docker exec securityapp-db mysql -uroot -p<MYSQL_ROOT_PASSWORD> securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Ví dụ nếu trong file `.env` bạn đang để:

```text
MYSQL_ROOT_PASSWORD=change-me-root-password
```

thì lệnh sẽ là:

```powershell
docker exec securityapp-db mysql -uroot -pchange-me-root-password securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Quan sát cột `password_hash`:

- Giá trị không phải `Password123!`
- Chuỗi thường bắt đầu bằng tiền tố bcrypt như `$2a$`, `$2b$` hoặc `$2y$`
- Mỗi tài khoản có hash khác nhau dù có thể dùng cùng mật khẩu

Ví dụ điều cần chứng minh:

- `student1@example.com` dùng mật khẩu `Password123!`
- Trong bảng `users`, cột `password_hash` là một chuỗi băm dài
- Người xem database không thể nhìn ra mật khẩu gốc

### Bước 4: Thử đăng nhập lại bằng đúng mật khẩu gốc

Quay lại tab Swagger vừa mở lúc nãy.

Tiếp tục dùng đúng endpoint:

- `POST /api/auth/login`

Body:

```json
{
  "email": "student1@example.com",
  "password": "Password123!"
}
```

Thao tác:

1. Bấm `Try it out` nếu Swagger đã reset form
2. Dán lại body
3. Bấm `Execute`

Kết quả mong đợi:

- Backend dùng `passwordEncoder.matches(rawPassword, passwordHash)`
- Đăng nhập vẫn thành công

Ý cần trình bày:

- Hệ thống không cần biết mật khẩu cũ trong database ở dạng rõ
- Chỉ cần lấy mật khẩu người dùng vừa nhập rồi so khớp với hash bcrypt

### Bước 5: Thử đăng nhập bằng mật khẩu sai

Vẫn ở Swagger, tiếp tục gọi:

- `POST /api/auth/login`

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

### Bước 6: Tùy chọn mở audit log để minh họa thêm

Nếu muốn trình bày đầy đủ hơn, bạn có thể:

1. Đăng nhập bằng tài khoản admin:

```json
{
  "email": "admin@example.com",
  "password": "Admin123!"
}
```

2. Copy `accessToken` của admin
3. Bấm nút `Authorize` trên Swagger
4. Nhập:

```text
Bearer <admin-accessToken>
```

5. Gọi:

```http
GET /api/admin/audit-logs
```

Kết quả mong đợi:

- Có bản ghi đăng nhập thành công
- Có bản ghi đăng nhập thất bại khi bạn thử mật khẩu sai ở bước trước

## 4. Cách trình bày ngắn gọn trước giảng viên

Bạn có thể trình bày đúng thứ tự này:

1. Mở Swagger tại `https://localhost/swagger-ui.html`
2. Đăng nhập bằng `student1@example.com / Password123!`
3. Mở PowerShell và truy vấn bảng `users`
4. Chỉ vào cột `password_hash` để chứng minh database không lưu plaintext
5. Quay lại Swagger, đăng nhập lại bằng đúng mật khẩu để chứng minh bcrypt vẫn xác thực được
6. Thử một lần với mật khẩu sai để chứng minh hệ thống từ chối

Nếu giảng viên hỏi “thực hiện demo này ở đâu”, câu trả lời chuẩn là:

- **Gọi API ở Swagger hoặc Postman**
- **Xem hash trong database bằng PowerShell/terminal**
- **Không thực hiện phần này trên giao diện người dùng vì đây là demo kỹ thuật bảo mật**

## 5. Giải thích ngắn gọn để trình bày với giảng viên

- bcrypt là hàm băm một chiều, không có chức năng giải mã ngược
- bcrypt tự sinh salt nên hai người dùng cùng mật khẩu vẫn có hash khác nhau
- Khi đăng nhập, hệ thống không giải mã hash mà so khớp bằng `matches`
- Đây là phương án phù hợp để bảo vệ mật khẩu trong cơ sở dữ liệu nếu database bị lộ

## 6. Minh chứng nên chụp cho báo cáo

- Ảnh mở `https://localhost/swagger-ui.html`
- Ảnh request đăng nhập thành công trên Swagger
- Ảnh PowerShell hiển thị bảng `users` với cột `password_hash`
- Ảnh request đăng nhập sai trả `401`
- Nếu có, thêm ảnh audit log ghi `LOGIN_FAILED`

## 7. Kết luận

Demo này chứng minh hệ thống đã xử lý mật khẩu đúng theo hướng an toàn:

- Không lưu plaintext
- Có thể xác thực đúng người dùng
- Giảm rủi ro lộ mật khẩu thật khi lộ dữ liệu database
