# Demo 05: Webhook thanh toán giả lập với HMAC-SHA256

## 1. Mục tiêu

Demo này dùng để chứng minh:

- Hệ thống chỉ chấp nhận webhook đến từ nguồn biết secret dùng để ký HMAC
- Chữ ký sai sẽ bị từ chối
- Webhook quá cũ hoặc quá xa thời gian hiện tại sẽ bị từ chối
- Gửi lặp lại cùng `eventId` sẽ bị từ chối để chống replay attack
- Khi webhook hợp lệ, enrollment được kích hoạt và certificate được cấp

## 2. Endpoint và header sử dụng

### 2.1. Endpoint

```http
POST /api/webhooks/payment-success
```

### 2.2. Header bắt buộc

- `X-Signature`
- `X-Timestamp`
- `X-Event-Id`

### 2.3. Công thức chữ ký

```text
signature = hex(HMAC_SHA256(eventId + "." + timestamp + "." + rawBody, HMAC_WEBHOOK_SECRET))
```

## 3. Chuẩn bị

### 3.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 3.2. Tài khoản dùng để tạo enrollment chờ thanh toán

- `student1@example.com / Password123!`

### 3.3. Dữ liệu seed hữu ích

Trong dữ liệu mẫu đã có sẵn:

- `student1` có một enrollment `PENDING` cho khóa `Secure RESTful API with Spring Boot`

Bạn có thể dùng trực tiếp enrollment chờ này hoặc tự tạo lại bằng API checkout.

## 4. Cách chuẩn bị enrollment chờ thanh toán

### Cách 1: Dùng dữ liệu seed có sẵn

Gọi:

```http
GET /api/enrollments/me
Authorization: Bearer <student1-accessToken>
```

Tìm enrollment có `status = PENDING`.

### Cách 2: Tự tạo mới bằng checkout

Đăng nhập `student1`, sau đó gọi:

```http
POST /api/courses/3/checkout
Authorization: Bearer <student1-accessToken>
```

Kết quả mong đợi:

- Nhận được `enrollmentId`
- Nhận được `paymentReference`
- Nhận được trạng thái `PENDING`

## 5. Demo webhook hợp lệ

### 5.1. Tạo body mẫu

Ví dụ:

```json
{
  "enrollmentId": 3,
  "userId": 1,
  "courseId": 3,
  "paymentReference": "PAY-PENDING-DEMO",
  "amount": 299000.00
}
```

Lưu ý:

- `enrollmentId`, `userId`, `courseId`, `paymentReference`, `amount` phải khớp với enrollment thực tế
- `amount` phải đúng bằng giá khóa học

### 5.2. Tính chữ ký bằng PowerShell

```powershell
$eventId = "evt_demo_valid_001"
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
$rawBody = '{"enrollmentId":3,"userId":1,"courseId":3,"paymentReference":"PAY-PENDING-DEMO","amount":299000.00}'
$secret = "<HMAC_WEBHOOK_SECRET>"
$payloadToSign = "$eventId.$timestamp.$rawBody"
$hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($secret))
$signature = ([Convert]::ToHexString($hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($payloadToSign)))).ToLower()
$signature
```

### 5.3. Gửi webhook

```powershell
curl.exe -k https://localhost/api/webhooks/payment-success `
  -H "Content-Type: application/json" `
  -H "X-Event-Id: $eventId" `
  -H "X-Timestamp: $timestamp" `
  -H "X-Signature: $signature" `
  -d "$rawBody"
```

Kết quả mong đợi:

- Trả `200 OK`
- Message cho biết webhook được chấp nhận
- Enrollment chuyển từ `PENDING` sang `ACTIVE`
- Hệ thống cấp certificate cho enrollment đó

### 5.4. Kiểm tra sau khi webhook hợp lệ

Gọi:

```http
GET /api/enrollments/me
Authorization: Bearer <student1-accessToken>
```

và:

```http
GET /api/certificates/me
Authorization: Bearer <student1-accessToken>
```

Kết quả mong đợi:

- Enrollment đã `ACTIVE`
- Xuất hiện certificate mới hoặc đã được cấp

## 6. Demo chữ ký sai

Gửi lại request nhưng thay `X-Signature` thành giá trị giả, ví dụ:

```text
bad-signature
```

Kết quả mong đợi:

- Trả `401 Unauthorized`
- Audit log ghi `WEBHOOK_REJECTED`

## 7. Demo replay attack

Sau khi đã gửi thành công webhook hợp lệ, giữ nguyên `X-Event-Id` và gửi lại đúng request đó thêm một lần nữa.

Kết quả mong đợi:

- Trả `409 Conflict`
- Hệ thống thông báo event đã được xử lý
- Audit log thể hiện hành vi replay bị từ chối

## 8. Demo timestamp không hợp lệ

Thử đặt `X-Timestamp` quá cũ hoặc quá xa thời điểm hiện tại.

Ví dụ:

```text
1735689600
```

Kết quả mong đợi:

- Trả `401 Unauthorized`
- Hệ thống báo timestamp không hợp lệ hoặc quá cũ

## 9. Cách demo nhanh bằng Postman

Project đã có sẵn request:

- `Webhook Valid HMAC`
- `Webhook Invalid HMAC`

trong file:

```text
postman/online-course-security.postman_collection.json
```

Request `Webhook Valid HMAC` đã có pre-request script để tự:

- sinh `eventId`
- lấy `timestamp`
- tạo chữ ký HMAC đúng

## 10. Giải thích ngắn gọn để trình bày

- HMAC-SHA256 giúp backend xác minh rằng webhook đến từ nguồn biết secret dùng chung
- Kẻ tấn công không biết secret thì không thể tạo chữ ký hợp lệ cho body bất kỳ
- `X-Timestamp` giúp giảm nguy cơ phát lại gói tin cũ
- `X-Event-Id` giúp phát hiện và chặn request replay

## 11. Minh chứng nên chụp cho báo cáo

- Ảnh enrollment ở trạng thái `PENDING`
- Ảnh webhook hợp lệ trả `200`
- Ảnh enrollment chuyển `ACTIVE`
- Ảnh webhook sai chữ ký trả `401`
- Ảnh replay cùng `eventId` trả `409`
- Ảnh audit log của các sự kiện webhook

## 12. Kết luận

Demo này thể hiện rõ phần “mật mã ứng dụng” trong đồ án, vì HMAC-SHA256 được áp dụng trực tiếp vào một tình huống thực tế là xác minh webhook thanh toán và chống replay attack.
