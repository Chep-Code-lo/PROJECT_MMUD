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

### 3.3. Thực hiện demo này ở đâu

Demo 06 nên dùng **2 nơi**:

#### Nơi 1: PowerShell để spam request nhanh

Đây là cách dễ nhất để:

- gửi nhiều request liên tiếp;
- quan sát mốc chuyển từ `401` sang `429`.

#### Nơi 2: Swagger hoặc Postman để xem audit log

Sau khi spam xong, bạn dùng:

```text
https://localhost/swagger-ui.html
```

hoặc Postman để đăng nhập admin và mở audit log.

Không nên dùng Swagger để bấm tay nhiều lần cho phần spam, vì chậm và khó quan sát hơn.

## 4. Kịch bản demo rate limit cho login

### Bước 1: Mở PowerShell tại thư mục project

```powershell
cd E:\PROJECT_MMUD
```

### Bước 2: Gửi nhiều request đăng nhập sai liên tiếp

Chạy lệnh:

```powershell
1..7 | ForEach-Object {
  Write-Host "----- Lan goi $($_) -----"
  curl.exe -k -i https://localhost/api/auth/login `
    -H "Content-Type: application/json" `
    -d '{"email":"student1@example.com","password":"WrongPassword123!"}'
}
```

Ý nghĩa:

- dùng cùng email `student1@example.com`;
- gửi liên tiếp trong cùng một phút;
- cố tình nhập sai mật khẩu để minh họa brute-force.

### Bước 3: Quan sát phản hồi

Kết quả mong đợi:

- Những lần đầu nhận `401 Unauthorized`
- Sau khi vượt ngưỡng, backend trả `429 Too Many Requests`

Điểm cần nói:

- `401` là sai thông tin đăng nhập
- `429` là bị chặn do vượt rate limit

## 5. Kiểm tra audit log sau khi bị rate limit

### Cách A: Xem bằng Swagger

1. Mở `https://localhost/swagger-ui.html`
2. Tìm `Auth API`
3. Gọi `POST /api/auth/login` với:

```json
{
  "email": "admin@example.com",
  "password": "Admin123!"
}
```

4. Copy `accessToken`
5. Bấm `Authorize`
6. Nhập:

```text
Bearer <admin-accessToken>
```

7. Mở nhóm `Admin API`
8. Gọi `GET /api/admin/audit-logs`

Kết quả mong đợi:

- Có bản ghi `RATE_LIMIT_EXCEEDED`
- Message mô tả hành vi vượt ngưỡng

### Cách B: Xem bằng Postman

Nếu thích Postman hơn:

1. Chạy `Login Admin`
2. Chạy `Admin Audit Logs`

Kết quả mong đợi là giống nhau.

## 6. Biến thể có thể demo thêm

### 6.1. Với đăng ký tài khoản

Bạn có thể gửi liên tiếp:

```http
POST /api/auth/register
```

trong thời gian ngắn để minh họa chống spam đăng ký.

### 6.2. Với quên mật khẩu

Bạn có thể gửi liên tiếp:

```http
POST /api/auth/forgot-password
```

để minh họa chống spam yêu cầu reset mật khẩu.

### 6.3. Với webhook

Bạn có thể gửi nhiều request tới:

```http
POST /api/webhooks/payment-success
```

để minh họa giới hạn tốc độ ở lớp webhook.

## 7. Cách trình bày ngắn gọn trước giảng viên

Bạn nên trình bày theo đúng thứ tự:

1. Mở PowerShell
2. Chạy vòng lặp gửi sai mật khẩu nhiều lần
3. Chỉ ra vài lần đầu là `401`
4. Chỉ ra các lần sau thành `429`
5. Mở Swagger
6. Đăng nhập admin
7. Mở `GET /api/admin/audit-logs`
8. Chỉ ra bản ghi `RATE_LIMIT_EXCEEDED`

Nếu giảng viên hỏi “demo này làm ở đâu”, câu trả lời chuẩn là:

- **PowerShell để gửi nhiều request thật nhanh**
- **Swagger hoặc Postman để xem audit log**

## 8. Giải thích ngắn gọn để trình bày

- Rate limiting không thay thế xác thực hay phân quyền
- Đây là lớp bảo vệ bổ sung để giảm brute-force, spam và lạm dụng tài nguyên
- Với đồ án học phần, chỉ cần cấu hình đơn giản nhưng phải dễ test và dễ nhìn thấy kết quả

## 9. Minh chứng nên chụp cho báo cáo

- Ảnh PowerShell gửi nhiều request đăng nhập sai
- Ảnh response `429 Too Many Requests`
- Ảnh `GET /api/admin/audit-logs` hiển thị `RATE_LIMIT_EXCEEDED`

## 10. Kết luận

Demo này chứng minh hệ thống đã có cơ chế kiểm soát lưu lượng cơ bản, phù hợp để minh họa nhóm lỗi `Unrestricted Resource Consumption` trong OWASP API Security Top 10.
