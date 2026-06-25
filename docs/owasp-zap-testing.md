# Hướng dẫn kiểm thử bằng OWASP ZAP

## 1. Mục đích

Tài liệu này hướng dẫn kiểm thử bảo mật cơ bản bằng OWASP ZAP cho hệ thống hiện tại, tập trung vào:

- kiểm tra bề mặt web qua HTTPS;
- quan sát security headers;
- xác nhận cơ chế chuyển hướng HTTP sang HTTPS;
- kiểm tra phản hồi `401`, `403`, `404`, `429`;
- quét giao diện Swagger/OpenAPI ở chế độ chỉ mở trên máy chủ local.

## 2. Chuẩn bị trước khi quét

### 2.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 2.2. Các địa chỉ cần dùng

- Frontend: `https://localhost`
- API health: `https://localhost/api/health`
- Swagger local-only: `https://localhost:8444/swagger-ui.html`
- OpenAPI JSON local-only: `https://localhost:8444/v3/api-docs`

### 2.3. Lưu ý về Swagger local-only

Swagger hiện được cấu hình chỉ bind vào `127.0.0.1` của máy host. Điều đó có nghĩa:

- máy khác trong mạng không truy cập được;
- container khác cũng không truy cập được theo cách thông thường;
- cách kiểm thử phù hợp nhất là chạy `OWASP ZAP Desktop` trực tiếp trên chính máy host.

## 3. Cài đặt và mở OWASP ZAP

Nếu chưa có ZAP:

1. Tải OWASP ZAP Desktop từ trang chính thức.
2. Cài đặt theo mặc định.
3. Mở ứng dụng.

Khi ZAP khởi động:

1. Chọn tạo session mới.
2. Có thể dùng chế độ mặc định, không cần cấu hình nâng cao cho bài demo này.

## 4. Kiểm thử nhanh giao diện frontend

### 4.1. Quét tự động

Trong ZAP:

1. Mở tab `Quick Start`.
2. Chọn `Automated Scan`.
3. Nhập URL:

```text
https://localhost
```

4. Bấm `Attack`.

### 4.2. Kết quả cần quan sát

Sau khi quét, xem tab `Alerts` và kiểm tra:

- trang có truy cập được qua HTTPS;
- không xuất hiện lỗi nghiêm trọng kiểu lộ stack trace;
- hệ thống có các header bảo mật cơ bản;
- không có dấu hiệu public Swagger trên cổng người dùng.

## 5. Kiểm thử Swagger/OpenAPI trên máy host

### 5.1. Quét Swagger

Trong `Quick Start` hoặc `Manual Explore`, dùng URL:

```text
https://localhost:8444/swagger-ui.html
```

Hoặc vào trực tiếp:

```text
https://localhost:8444/swagger-ui/index.html
```

### 5.2. Mục tiêu của bước này

Bước này dùng để:

- xác nhận Swagger vẫn hoạt động cho người vận hành trên máy chủ;
- quan sát response header của bề mặt tài liệu API;
- phục vụ kiểm thử mô tả API bằng công cụ quét.

## 6. Kiểm tra thủ công các hành vi bảo mật quan trọng

Ngoài quét tự động, nên dùng ZAP như một proxy quan sát request/response. Các điểm nên kiểm tra:

### 6.1. HTTP có bị ép sang HTTPS hay không

Mở trình duyệt hoặc gửi request tới:

```text
http://localhost/api/health
```

Kỳ vọng:

- hệ thống trả `301` và chuyển sang HTTPS.

### 6.2. Security headers

Kiểm tra response của:

- `https://localhost`
- `https://localhost/api/health`

Nên thấy các header chính như:

- `Strict-Transport-Security`
- `Content-Security-Policy`
- `X-Content-Type-Options`
- `X-Frame-Options`
- `Referrer-Policy`
- `Permissions-Policy`

### 6.3. Endpoint bảo vệ không cho truy cập trái phép

Dùng ZAP hoặc trình duyệt để kiểm tra:

- `GET /api/admin/audit-logs` khi chưa đăng nhập
- `GET /api/certificates/{id}` với token sai quyền

Kỳ vọng:

- thiếu xác thực: `401 Unauthorized`
- sai quyền: `403 Forbidden`

### 6.4. Swagger không lộ trên cổng public

Thử mở:

```text
https://localhost/swagger-ui.html
https://localhost/v3/api-docs
```

Kỳ vọng:

- đều trả `404 Not Found`

Đây là một minh chứng quan trọng cho việc tài liệu API không bị lộ trên cổng public.

## 7. Cách đọc kết quả trong ZAP

### 7.1. Ý nghĩa mức cảnh báo

- `FAIL` hoặc cảnh báo nghiêm trọng:
  cần xử lý ngay vì có khả năng là lỗ hổng hoặc cấu hình nguy hiểm
- `WARN`:
  cần đọc kỹ và giải thích trong báo cáo
- `PASS`:
  cho thấy cơ chế tương ứng đang hoạt động đúng kỳ vọng

### 7.2. Một số cảnh báo thường gặp trong đồ án

Với hệ thống kiểu demo, có thể gặp:

- cảnh báo liên quan `Content-Security-Policy`
- cảnh báo nhận diện ứng dụng web hiện đại
- cảnh báo do chứng chỉ self-signed local

Các cảnh báo này không đồng nghĩa với việc hệ thống bị lỗi xác thực hay lỗi mã hóa, nhưng cần được giải thích rõ trong báo cáo.

## 8. Cách chứng minh đồ án đã xử lý bảo mật

Khi đọc kết quả quét, cần đối chiếu với các cơ chế đã làm trong dự án:

- `BOLA/IDOR`:
  backend chặn bằng ownership check, trả `403`
- `Broken Authentication`:
  JWT sai chữ ký hoặc hết hạn bị từ chối
- `Excessive Data Exposure`:
  API không trả `passwordHash`
- `Rate Limiting`:
  endpoint nhạy cảm có thể trả `429`
- `Security Misconfiguration`:
  có HTTPS, có header bảo mật, không hard-code secret

## 9. Khuyến nghị cách chụp minh chứng cho báo cáo

Nên chụp ít nhất các hình sau:

1. Giao diện ZAP đang quét `https://localhost`
2. Kết quả alert sau khi quét
3. Kết quả truy cập `https://localhost/swagger-ui.html` bị `404`
4. Kết quả truy cập `https://localhost:8444/swagger-ui.html` thành công trên máy host

Các hình này giúp giải thích rõ rằng:

- hệ thống vẫn có tài liệu API để kiểm thử nội bộ;
- nhưng tài liệu đó không bị public ra cổng người dùng thông thường.

## 10. Giới hạn hiện tại của cách quét

- Swagger chỉ mở local-only nên không phù hợp với kiểu quét bằng container ZAP từ bên ngoài.
- ZAP baseline không tự mô phỏng đầy đủ các ca đăng nhập, refresh token hay webhook HMAC.
- Các tình huống nghiệp vụ sâu như BOLA, replay webhook, rate limit vẫn nên kiểm chứng bằng Postman và integration test.

## 11. Kết luận

OWASP ZAP trong dự án này được dùng để:

- kiểm tra nhanh bề mặt công khai của hệ thống;
- xác nhận cấu hình HTTPS và header bảo mật;
- hỗ trợ giải thích rằng Swagger/OpenAPI không còn lộ trên cổng public;
- bổ sung minh chứng cho phần kiểm thử bảo mật trong báo cáo đồ án.
