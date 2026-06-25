# Demo 06: Rate Limiting

## 1. Mục tiêu

Demo này nhằm chứng minh:

1. Hệ thống có cơ chế giới hạn số lần gọi API trong một cửa sổ thời gian.
2. Khi vượt ngưỡng cho phép, backend trả `429 Too Many Requests`.
3. Hệ thống có ghi nhận sự kiện rate limit vào audit log.

## 2. Các nhóm API đang được giới hạn

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/forgot-password`
- `POST /api/webhooks/payment-success`
- `GET /api/admin/**`

Các ngưỡng lấy từ biến môi trường:

- `RATE_LIMIT_LOGIN_PER_MINUTE`
- `RATE_LIMIT_REGISTER_PER_MINUTE`
- `RATE_LIMIT_PASSWORD_RESET_PER_MINUTE`
- `RATE_LIMIT_WEBHOOK_PER_MINUTE`
- `RATE_LIMIT_ADMIN_PER_MINUTE`

## 3. Chuẩn bị

### 3.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 3.2. Cấu hình mặc định dễ demo

Trong `.env.example` hiện đang để:

```text
RATE_LIMIT_LOGIN_PER_MINUTE=5
```

Điều này nghĩa là sau khoảng 5 lần gọi login trong cùng một phút, request tiếp theo sẽ bị chặn.

### 3.3. Thực hiện ở đâu

Demo này nên dùng:

- `PowerShell` để bắn nhanh nhiều request liên tiếp
- `Swagger` hoặc `Postman` để xem audit log bằng tài khoản admin

## 4. Các bước thực hiện chi tiết

### Bước 1: Spam request đăng nhập sai bằng PowerShell

Mở PowerShell tại thư mục project:

```powershell
cd E:\PROJECT_MMUD
```

Chạy:

```powershell
1..7 | ForEach-Object {
  Write-Host "----- Lan goi $($_) -----"
  curl.exe -k -i https://localhost/api/auth/login `
    -H "Content-Type: application/json" `
    -d '{"email":"student1@example.com","password":"WrongPassword123!"}'
}
```

Ý nghĩa:

- dùng cùng email `student1@example.com`
- cố tình nhập sai mật khẩu
- gửi dồn trong cùng một phút để mô phỏng brute-force

### Bước 2: Quan sát mốc chuyển từ `401` sang `429`

Kết quả mong đợi:

- những lần đầu:

```text
401 Unauthorized
```

- sau khi vượt ngưỡng:

```text
429 Too Many Requests
```

Điểm cần nói:

- `401` nghĩa là sai thông tin đăng nhập
- `429` nghĩa là backend đã chủ động chặn do vượt giới hạn

### Bước 3: Xem audit log của hành vi vượt ngưỡng

Mở Swagger:

```text
https://localhost/swagger-ui.html
```

Đăng nhập admin:

```json
{
  "email": "admin@example.com",
  "password": "Admin123!"
}
```

`Authorize` bằng token admin rồi gọi:

```http
GET /api/admin/audit-logs
```

Kết quả mong đợi:

- có bản ghi:

```text
RATE_LIMIT_EXCEEDED
```

- message mô tả dạng:

```text
Login rate limit exceeded.
```

## 5. Biến thể có thể demo thêm

### 5.1. Chống spam đăng ký

Gửi nhiều lần:

```http
POST /api/auth/register
```

với cùng email hoặc nhiều email khác nhau trong thời gian ngắn.

### 5.2. Chống spam quên mật khẩu

Gửi nhiều lần:

```http
POST /api/auth/forgot-password
```

để minh họa bảo vệ endpoint reset password.

### 5.3. Chống spam webhook

Gửi liên tiếp nhiều request đến:

```http
POST /api/webhooks/payment-success
```

để minh họa thêm rằng cả luồng webhook cũng có rate limit.

### 5.4. Giới hạn API admin

Đăng nhập admin rồi gọi liên tiếp:

```http
GET /api/admin/courses
GET /api/admin/audit-logs
```

để giải thích rằng khu vực quản trị cũng bị giới hạn lưu lượng.

## 6. Kết quả mong đợi

Sau khi làm xong Demo 06, bạn phải chứng minh được:

1. Login sai nhiều lần liên tiếp sẽ không chỉ dừng ở `401`.
2. Sau một ngưỡng nhất định, backend sẽ trả `429 Too Many Requests`.
3. Audit log có ghi nhận sự kiện `RATE_LIMIT_EXCEEDED`.

## 7. Câu nên nói khi trình bày

- Rate limiting là lớp phòng thủ bổ sung, không thay thế xác thực hay phân quyền.
- Mục tiêu là giảm brute-force, spam API và lạm dụng tài nguyên.
- Với đồ án môn học, cấu hình đơn giản nhưng phải nhìn thấy được kết quả là đủ thuyết phục.

## 8. Ảnh nên chụp cho báo cáo

- Ảnh PowerShell gửi nhiều request login sai.
- Ảnh lần request bị trả `429 Too Many Requests`.
- Ảnh `GET /api/admin/audit-logs` có `RATE_LIMIT_EXCEEDED`.

## 9. Cách reset sau demo

Rate limit dùng cửa sổ thời gian 1 phút. Bạn chỉ cần:

- chờ qua 1 phút
- hoặc restart stack nếu muốn sạch trạng thái nhanh hơn

## 10. Kết luận

Demo này chứng minh hệ thống đã có cơ chế chống lạm dụng tài nguyên ở mức API, phù hợp để minh họa nhóm `Unrestricted Resource Consumption` trong OWASP API Security Top 10.
