# Demo 06: Rate limiting

## 1. Mục tiêu

Demo này dùng để chứng minh:

- Hệ thống có giới hạn tốc độ truy cập để giảm brute-force và spam API
- Khi vượt quá ngưỡng cho phép, backend trả `429 Too Many Requests`
- Hệ thống có ghi lại sự kiện bất thường để phục vụ giám sát

## 2. Các nhóm API đang được giới hạn

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/forgot-password`
- `POST /api/webhooks/payment-success`
- `GET /api/admin/**`

Ngưỡng cụ thể được cấu hình qua biến môi trường trong `.env`, ví dụ:

- `RATE_LIMIT_LOGIN_PER_MINUTE`
- `RATE_LIMIT_REGISTER_PER_MINUTE`
- `RATE_LIMIT_PASSWORD_RESET_PER_MINUTE`
- `RATE_LIMIT_WEBHOOK_PER_MINUTE`
- `RATE_LIMIT_ADMIN_PER_MINUTE`

## 3. Chuẩn bị

### 3.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 3.2. Giá trị mặc định dễ demo

Trong `.env.example`, login đang được đặt khá thấp:

```text
RATE_LIMIT_LOGIN_PER_MINUTE=5
```

Điều này phù hợp để demo nhanh.

## 4. Kịch bản demo rate limit cho login

### Bước 1: Chuẩn bị request đăng nhập sai

Endpoint:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "student1@example.com",
  "password": "WrongPassword123!"
}
```

### Bước 2: Gửi lặp lại nhiều lần

Bạn có thể:

- bấm `Send` nhiều lần trong Postman
- dùng collection có sẵn `Rate Limit Test - Wrong Login`
- hoặc chạy vòng lặp bằng PowerShell

Ví dụ PowerShell:

```powershell
1..7 | ForEach-Object {
  curl.exe -k https://localhost/api/auth/login `
    -H "Content-Type: application/json" `
    -d '{"email":"student1@example.com","password":"WrongPassword123!"}'
}
```

### Bước 3: Quan sát phản hồi

Kết quả mong đợi:

- Những lần đầu nhận `401 Unauthorized`
- Sau khi vượt ngưỡng, backend trả `429 Too Many Requests`

## 5. Kiểm tra log quản trị

Đăng nhập bằng tài khoản admin:

- `admin@example.com / Admin123!`

Sau đó gọi:

```http
GET /api/admin/audit-logs
Authorization: Bearer <admin-accessToken>
```

Kết quả mong đợi:

- Có bản ghi `RATE_LIMIT_EXCEEDED`
- Message mô tả IP hoặc danh tính đang gọi vượt ngưỡng

## 6. Biến thể có thể demo thêm

### 6.1. Với đăng ký tài khoản

Gửi liên tiếp:

```http
POST /api/auth/register
```

với nhiều request trong cùng khoảng thời gian ngắn.

Kết quả mong đợi:

- Hệ thống trả `429` sau khi vượt ngưỡng

### 6.2. Với quên mật khẩu

Gửi lặp lại:

```http
POST /api/auth/forgot-password
```

Kết quả mong đợi:

- Hệ thống hạn chế spam tạo token reset

### 6.3. Với webhook

Gửi nhiều webhook liên tục trong thời gian ngắn tới:

```http
POST /api/webhooks/payment-success
```

Kết quả mong đợi:

- Backend chặn khi vượt ngưỡng
- Giảm nguy cơ spam hoặc tấn công làm cạn tài nguyên

## 7. Giải thích ngắn gọn để trình bày

- Rate limiting không thay thế xác thực hay phân quyền
- Nó là lớp bảo vệ bổ sung để giảm brute-force, spam và lạm dụng tài nguyên
- Với đồ án học phần, chỉ cần cấu hình mức đơn giản nhưng phải dễ test và dễ quan sát kết quả

## 8. Minh chứng nên chụp cho báo cáo

- Ảnh nhiều request đăng nhập sai
- Ảnh response `429 Too Many Requests`
- Ảnh audit log hiển thị `RATE_LIMIT_EXCEEDED`

## 9. Kết luận

Demo này chứng minh hệ thống đã có cơ chế kiểm soát lưu lượng cơ bản, phù hợp để minh họa nhóm lỗi `Unrestricted Resource Consumption` trong OWASP API Security Top 10.
