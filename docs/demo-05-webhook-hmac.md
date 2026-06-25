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

### 3.3. Thực hiện demo này ở đâu

Demo 05 nên dùng **Postman** là chính, vì:

- request hợp lệ cần tự tính HMAC;
- collection đã có sẵn pre-request script tự tạo chữ ký;
- thuận tiện hơn Swagger rất nhiều trong trường hợp webhook.

File collection:

```text
postman/online-course-security.postman_collection.json
```

Swagger vẫn hữu ích để kiểm tra kết quả sau webhook, nhưng **không phải công cụ chính** cho bước gửi webhook.

## 4. Chuẩn bị Postman

### Bước 1: Mở Postman

Nếu Postman đang bật kiểm tra SSL nghiêm ngặt:

1. Vào `Settings`
2. Tắt `SSL certificate verification`

### Bước 2: Import collection

Import file:

```text
postman/online-course-security.postman_collection.json
```

### Bước 3: Kiểm tra biến collection

Kiểm tra ít nhất 2 biến:

- `baseUrl = https://localhost`
- `webhookSecret = giá trị HMAC_WEBHOOK_SECRET trong file .env`

Nếu `webhookSecret` sai, request `Webhook Valid HMAC` cũng sẽ bị backend từ chối.

## 5. Các bước thực hiện

### Bước 1: Đăng nhập Student 1

Chạy request:

- `Login Student 1`

Kết quả mong đợi:

- Trả `200 OK`
- Collection tự lưu `accessToken`

### Bước 2: Tạo enrollment chờ thanh toán

Chạy request:

- `Checkout Course`

Kết quả mong đợi:

- Trả `200 OK`
- Collection tự lưu:
  - `pendingEnrollmentId`
  - `paymentReference`

Ý nghĩa:

- Hệ thống tạo ra enrollment ở trạng thái chờ thanh toán để webhook sau đó kích hoạt

### Bước 3: Gửi webhook hợp lệ

Chạy request:

- `Webhook Valid HMAC`

Collection này đã có pre-request script để tự:

- sinh `eventId`;
- sinh `timestamp`;
- ghép `eventId.timestamp.rawBody`;
- ký HMAC-SHA256 bằng `webhookSecret`;
- chèn đúng các header yêu cầu.

Kết quả mong đợi:

- Trả `200 OK`
- Message cho biết webhook được chấp nhận
- Enrollment được kích hoạt
- Certificate được cấp

### Bước 4: Kiểm tra kết quả sau webhook hợp lệ

Bạn có hai cách:

#### Cách A: Kiểm tra bằng Swagger

1. Mở `https://localhost/swagger-ui.html`
2. Đăng nhập `student1@example.com`
3. Copy `accessToken`
4. Bấm `Authorize`
5. Nhập `Bearer <accessToken>`
6. Gọi:
   - `GET /api/enrollments/me`
   - `GET /api/certificates/me`

Kết quả mong đợi:

- Enrollment vừa thanh toán có `status = ACTIVE`
- Danh sách chứng chỉ có dữ liệu mới hoặc đã được cấp

#### Cách B: Kiểm tra bằng Postman thủ công

Tạo thêm request:

```http
GET https://localhost/api/enrollments/me
Authorization: Bearer <accessToken của student1>
```

và:

```http
GET https://localhost/api/certificates/me
Authorization: Bearer <accessToken của student1>
```

### Bước 5: Gửi webhook sai chữ ký

Chạy request:

- `Webhook Invalid HMAC`

Kết quả mong đợi:

- Trả `401 Unauthorized`
- Backend báo chữ ký không hợp lệ

### Bước 6: Demo replay attack

Request `Webhook Valid HMAC` trong collection sẽ sinh `eventId` mới mỗi lần, nên để test replay bạn làm như sau:

1. Mở `Postman Console`
2. Chạy `Webhook Valid HMAC` một lần thành công
3. Trong console, mở request vừa gửi
4. Ghi lại chính xác:
   - `X-Event-Id`
   - `X-Timestamp`
   - `X-Signature`
   - raw body
5. Tạo một request mới thủ công:

```http
POST https://localhost/api/webhooks/payment-success
```

6. Dán lại đúng body cũ
7. Dán lại đúng ba header cũ
8. Gửi lại lần nữa

Kết quả mong đợi:

- Trả `409 Conflict`
- Backend thông báo event đã được xử lý

### Bước 7: Demo timestamp không hợp lệ

Tạo một request webhook thủ công với:

- `X-Timestamp` là giá trị quá cũ;
- hoặc quá xa thời điểm hiện tại.

Ví dụ:

```text
1735689600
```

Kết quả mong đợi:

- Trả `401 Unauthorized`
- Backend từ chối vì timestamp không hợp lệ

## 6. Cách làm thủ công bằng PowerShell nếu cần

Nếu giảng viên muốn xem cách tự tính HMAC mà không dựa vào Postman script, bạn có thể dùng PowerShell:

Lưu ý trước khi chạy:

- thay `enrollmentId`, `userId`, `courseId`, `paymentReference`, `amount` bằng đúng giá trị thực tế bạn vừa nhận sau bước `Checkout Course`;
- nếu giữ nguyên ví dụ mẫu nhưng dữ liệu hiện tại không khớp, backend sẽ từ chối webhook.

```powershell
$eventId = "evt_demo_valid_001"
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
$rawBody = '{"enrollmentId":3,"userId":1,"courseId":3,"paymentReference":"PAY-PENDING-DEMO","amount":299000.00}'
$secret = "<HMAC_WEBHOOK_SECRET>"
$payloadToSign = "$eventId.$timestamp.$rawBody"
$hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($secret))
$signature = ([Convert]::ToHexString($hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($payloadToSign)))).ToLower()
curl.exe -k https://localhost/api/webhooks/payment-success `
  -H "Content-Type: application/json" `
  -H "X-Event-Id: $eventId" `
  -H "X-Timestamp: $timestamp" `
  -H "X-Signature: $signature" `
  -d "$rawBody"
```

## 7. Cách trình bày ngắn gọn trước giảng viên

Bạn nên trình bày theo đúng thứ tự:

1. Mở Postman
2. Chạy `Login Student 1`
3. Chạy `Checkout Course`
4. Chạy `Webhook Valid HMAC`
5. Mở Swagger hoặc Postman để kiểm tra enrollment đã `ACTIVE`
6. Chạy `Webhook Invalid HMAC`
7. Dùng Postman Console để gửi lại cùng `eventId` và minh họa replay bị chặn

Nếu giảng viên hỏi “demo này làm ở đâu”, câu trả lời chuẩn là:

- **Khuyến nghị dùng Postman**
- **Swagger chỉ dùng để kiểm tra kết quả sau webhook**
- **PowerShell chỉ là cách minh họa thêm nếu muốn tự tính HMAC bằng tay**

## 8. Giải thích ngắn gọn để trình bày

- HMAC-SHA256 giúp backend xác minh request đến từ nguồn biết secret dùng chung
- Kẻ tấn công không biết secret thì không thể tạo chữ ký đúng cho body bất kỳ
- `X-Timestamp` giúp giảm nguy cơ phát lại gói tin cũ
- `X-Event-Id` giúp phát hiện và chặn replay attack

## 9. Minh chứng nên chụp cho báo cáo

- Ảnh `Checkout Course` trả `pendingEnrollmentId`
- Ảnh `Webhook Valid HMAC` trả `200`
- Ảnh enrollment chuyển `ACTIVE`
- Ảnh `Webhook Invalid HMAC` trả `401`
- Ảnh replay cùng `eventId` trả `409`
- Ảnh audit log của các sự kiện webhook nếu có

## 10. Kết luận

Demo này thể hiện rõ phần “mật mã ứng dụng” trong đồ án vì HMAC-SHA256 được áp dụng trực tiếp vào một tình huống thực tế: xác minh webhook thanh toán và chống replay attack.
