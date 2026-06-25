# Demo 05: Webhook thanh toán giả lập với HMAC-SHA256

## 1. Mục tiêu

Demo này nhằm chứng minh:

1. Backend chỉ chấp nhận webhook biết `HMAC_WEBHOOK_SECRET`.
2. Chữ ký sai sẽ bị từ chối.
3. Timestamp quá cũ hoặc quá xa thời gian hiện tại sẽ bị từ chối.
4. Gửi lặp lại cùng `eventId` sẽ bị chặn để chống replay attack.
5. Khi webhook hợp lệ, enrollment sẽ được kích hoạt và certificate sẽ được cấp.

## 2. Endpoint và công thức chữ ký

### 2.1. Endpoint

```http
POST /api/webhooks/payment-success
```

### 2.2. Header bắt buộc

- `X-Signature`
- `X-Timestamp`
- `X-Event-Id`

### 2.3. Công thức tạo chữ ký

```text
signature = hex(HMAC_SHA256(eventId + "." + timestamp + "." + rawBody, HMAC_WEBHOOK_SECRET))
```

## 3. Chuẩn bị

### 3.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 3.2. Tài khoản nên dùng

- `student1@example.com / Password123!`

### 3.3. Thực hiện ở đâu

Demo này nên dùng:

- `Postman` để gửi webhook vì cần tính HMAC
- `Swagger` hoặc `Postman` để kiểm tra kết quả sau webhook
- `PowerShell` nếu muốn trình bày cách tự tính HMAC bằng tay

Collection dùng sẵn:

```text
postman/online-course-security.postman_collection.json
```

## 4. Chuẩn bị Postman

### Bước 1: Tắt kiểm tra SSL self-signed

Trong `Settings`, tắt:

```text
SSL certificate verification
```

### Bước 2: Import collection

Import file:

```text
postman/online-course-security.postman_collection.json
```

### Bước 3: Kiểm tra biến collection

Ít nhất phải có:

- `baseUrl = https://localhost`
- `webhookSecret = giá trị HMAC_WEBHOOK_SECRET trong .env`

Nếu bạn demo qua domain public thì đổi:

```text
baseUrl = https://hackerlo.online
```

với điều kiện tunnel/domain đang bật thật.

## 5. Các bước thực hiện chi tiết

### Bước 1: Đăng nhập Student 1

Chạy:

- `Login Student 1`

Kết quả mong đợi:

- HTTP `200 OK`

### Bước 2: Tạo enrollment ở trạng thái chờ thanh toán

Chạy:

- `Checkout Course`

Kết quả mong đợi:

- HTTP `200 OK`
- response có:
  - `enrollmentId`
  - `paymentReference`
  - `status = PENDING`
  - `suggestedEventId`
  - `suggestedTimestampEpochSeconds`

Ý nghĩa:

- student đã tạo một yêu cầu checkout
- nhưng khóa học chưa được mở
- webhook hợp lệ mới là bước kích hoạt sau cùng

### Bước 3: Gửi webhook hợp lệ

Chạy:

- `Webhook Valid HMAC`

Request này đã có pre-request script tự:

- sinh `eventId`
- sinh `timestamp`
- ghép `eventId.timestamp.rawBody`
- ký HMAC-SHA256 bằng `webhookSecret`
- gắn ba header cần thiết

Kết quả mong đợi:

- HTTP `200 OK`
- message:

```text
Webhook accepted. Enrollment activated and certificate issued.
```

### Bước 4: Kiểm tra kết quả sau webhook hợp lệ

Bạn có thể kiểm tra bằng Swagger:

1. đăng nhập `student1@example.com`
2. `Authorize` bằng access token
3. gọi:

```http
GET /api/enrollments/me
GET /api/certificates/me
```

Kết quả mong đợi:

- enrollment vừa thanh toán đã có `status = ACTIVE`
- danh sách certificate có thêm bản ghi tương ứng

### Bước 5: Demo chữ ký sai

Chạy trực tiếp request:

- `Webhook Invalid HMAC`

Request này đã được cấu hình để:

- tự sinh `eventId`
- tự sinh `timestamp` hiện tại
- cố tình gửi `X-Signature = bad-signature`

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
Webhook signature is invalid.
```

### Bước 6: Demo replay attack

Để test replay, cần gửi lại đúng cùng `eventId`, `timestamp`, `signature` và body của một request hợp lệ đã được chấp nhận.

Thực hiện:

1. mở `Postman Console`
2. chạy `Webhook Valid HMAC` một lần thành công
3. copy lại:
  - `X-Event-Id`
  - `X-Timestamp`
  - `X-Signature`
  - raw body
4. tạo request mới hoặc gửi lại y hệt request cũ

Kết quả mong đợi:

- HTTP `409 Conflict`
- message:

```text
Webhook event has already been processed.
```

### Bước 7: Demo timestamp quá cũ

Tạo request thủ công với:

- `X-Timestamp` là thời gian cũ hơn quá 5 phút
- `X-Event-Id` bất kỳ
- `X-Signature` hợp lệ hoặc không, đều không quan trọng vì backend kiểm tra timestamp trước

Ví dụ:

```text
1735689600
```

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
Webhook timestamp is too old or too far in the future.
```

## 6. Cách làm thủ công bằng PowerShell

Nếu giảng viên muốn xem đúng cách tự tính HMAC bằng tay, dùng PowerShell:

```powershell
$eventId = "evt_demo_valid_001"
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
$rawBody = '{"enrollmentId":3,"userId":1,"courseId":3,"paymentReference":"PAY-PENDING-DEMO","amount":299000.00}'
$secret = "<HMAC_WEBHOOK_SECRET>"
$payloadToSign = "$eventId.$timestamp.$rawBody"
$hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($secret))
$signature = ([Convert]::ToHexString($hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($payloadToSign)))).ToLower()

curl.exe -k -i https://localhost/api/webhooks/payment-success `
  -H "Content-Type: application/json" `
  -H "X-Event-Id: $eventId" `
  -H "X-Timestamp: $timestamp" `
  -H "X-Signature: $signature" `
  -d "$rawBody"
```

Lưu ý:

- phải thay body cho khớp với enrollment thật bạn vừa checkout
- nếu `enrollmentId`, `userId`, `courseId`, `amount` không khớp thì backend vẫn từ chối

## 7. Kết quả mong đợi

Sau khi làm xong Demo 05, bạn phải chứng minh được:

1. Webhook hợp lệ kích hoạt enrollment và cấp certificate.
2. Webhook sai chữ ký bị từ chối.
3. Webhook dùng lại cùng `eventId` bị chặn.
4. Webhook timestamp quá cũ bị từ chối.

## 8. Câu nên nói khi trình bày

- `HMAC-SHA256` giúp backend xác minh request đến từ nguồn biết secret dùng chung.
- Nếu không biết secret, kẻ tấn công không thể tạo chữ ký đúng cho body tùy ý.
- `X-Timestamp` giảm nguy cơ phát lại gói tin cũ.
- `X-Event-Id` giúp chống replay attack.

## 9. Ảnh nên chụp cho báo cáo

- Ảnh `Checkout Course` trả `PENDING`.
- Ảnh `Webhook Valid HMAC` trả `200 OK`.
- Ảnh `GET /api/enrollments/me` cho thấy enrollment chuyển `ACTIVE`.
- Ảnh `Webhook signature is invalid.`
- Ảnh replay trả `409 Conflict`.
- Ảnh audit log liên quan `WEBHOOK_ACCEPTED` và `WEBHOOK_REJECTED`.

## 10. Cách reset sau demo

Nếu bạn đã làm nhiều lần checkout và webhook, có thể reset toàn bộ seed data:

```powershell
docker compose down -v
docker compose up --build -d
```

## 11. Kết luận

Demo này thể hiện rất rõ phần Mật mã ứng dụng trong đồ án, vì HMAC-SHA256 được dùng trực tiếp trong một tình huống thực tế là xác minh webhook thanh toán và chống replay attack.
