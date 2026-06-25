# Hướng dẫn kiểm thử bằng OWASP ZAP

## 1. Mục đích

Tài liệu này hướng dẫn kiểm thử bảo mật cơ bản bằng OWASP ZAP cho hệ thống hiện tại, tập trung vào:

- quét bề mặt web qua HTTPS
- quan sát response header và security header
- xác nhận cơ chế chuyển hướng HTTP sang HTTPS
- quét Swagger / OpenAPI qua HTTPS
- đọc alert và đối chiếu với các biện pháp bảo mật đã triển khai

## 2. Chuẩn bị trước khi quét

### 2.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 2.2. Các địa chỉ nên dùng

- Frontend local: `https://localhost`
- API health local: `https://localhost/api/health`
- Swagger local: `https://localhost/swagger-ui.html`
- OpenAPI JSON local: `https://localhost/v3/api-docs`

Nếu chạy ZAP trong container trên cùng máy host, nên dùng:

- `https://host.docker.internal/swagger-ui.html`

Nếu bạn đang bật tunnel/domain public và muốn quét đúng bề mặt public:

- `https://hackerlo.online`
- `https://hackerlo.online/swagger-ui.html`

## 3. Hai cách chạy OWASP ZAP

## 3.1. Cách 1: ZAP Desktop

Phù hợp khi bạn muốn thao tác bằng giao diện.

Các bước:

1. cài OWASP ZAP Desktop
2. mở ứng dụng
3. chọn tạo session mới
4. dùng `Quick Start` hoặc `Automated Scan`

## 3.2. Cách 2: ZAP bằng Docker

Project đã có sẵn file cấu hình:

```text
docs/security/zap.yaml
```

Lệnh chạy:

```powershell
docker run --rm -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

Kết quả report sẽ được sinh trong:

- `docs/security/zap-baseline-report.html`
- `docs/security/zap-baseline-report.json`
- `docs/security/zap-baseline-report.xml`

## 4. Kiểm thử bằng ZAP Desktop

### Bước 1: Quét frontend local

Trong `Quick Start`, nhập:

```text
https://localhost
```

Sau đó bấm `Attack`.

### Bước 2: Quét Swagger local

Tiếp tục quét:

```text
https://localhost/swagger-ui.html
```

Nếu ZAP chạy trong container thì đổi sang:

```text
https://host.docker.internal/swagger-ui.html
```

### Bước 3: Tùy chọn quét domain public

Chỉ thực hiện khi tunnel/domain public đang bật thật:

```text
https://hackerlo.online
https://hackerlo.online/swagger-ui.html
```

Mục tiêu của bước này là kiểm tra đúng bề mặt public đang được expose ra Internet.

## 5. Những gì cần quan sát trong ZAP

Sau khi quét, vào tab `Alerts` và chú ý các nhóm sau:

- phản hồi HTTPS có hoạt động ổn định không
- có lộ stack trace hay lỗi cấu hình nặng không
- có security header cơ bản hay không
- các đường dẫn như Swagger/OpenAPI có phản hồi đúng như mong đợi không

## 6. Các kiểm tra thủ công nên kết hợp thêm

ZAP baseline không thay thế được các bài test xác thực nâng cao. Vì vậy nên kết hợp thêm một số kiểm tra thủ công:

### 6.1. HTTP sang HTTPS

```powershell
curl.exe -I http://localhost/api/health
curl.exe -k https://localhost/api/health
```

Kỳ vọng:

- HTTP bị chuyển hướng
- HTTPS phản hồi `200 OK`

### 6.2. Security headers

Quan sát response của:

- `https://localhost`
- `https://localhost/api/health`
- `https://localhost/swagger-ui.html`

Những header nên chú ý:

- `X-Content-Type-Options`
- `X-Frame-Options`
- `Referrer-Policy`
- `Permissions-Policy`

### 6.3. Endpoint cần xác thực

Dùng ZAP hoặc trình duyệt để kiểm tra:

- `GET /api/admin/audit-logs` khi chưa đăng nhập
- `GET /api/certificates/{id}` với token sai quyền

Kỳ vọng:

- thiếu xác thực: `401 Unauthorized`
- sai quyền: `403 Forbidden`

## 7. Cách đọc kết quả ZAP cho báo cáo

### 7.1. Ý nghĩa mức cảnh báo

- `FAIL`: có vấn đề cần xử lý ngay
- `WARN`: cần giải thích trong báo cáo
- `PASS`: đúng với kỳ vọng

### 7.2. Một số cảnh báo có thể gặp trong đồ án

- cảnh báo liên quan `Content-Security-Policy`
- cảnh báo `Modern Web Application`
- cảnh báo do self-signed certificate ở local

Những cảnh báo này không đồng nghĩa với việc hệ thống hỏng xác thực hay hỏng mật mã, nhưng cần được giải thích rõ.

## 8. Cách đối chiếu ZAP với các cơ chế bảo mật trong project

Khi viết báo cáo, nên đối chiếu alert của ZAP với các phần đã làm:

- `Broken Authentication`
  - JWT sai chữ ký hoặc hết hạn bị từ chối bằng `401`
- `Broken Object Level Authorization`
  - BOLA bị chặn bằng ownership check và `403`
- `Excessive Data Exposure`
  - API không trả `passwordHash`
- `Unrestricted Resource Consumption`
  - login, webhook, admin API có rate limit
- `Security Misconfiguration`
  - có HTTPS, không hard-code secret, có security header cơ bản

## 9. Artifact và minh chứng có sẵn

Thư mục:

```text
docs/security
```

đang chứa sẵn:

- `zap.yaml`
- `zap-baseline-report.html`
- `zap-baseline-report.json`
- `zap-baseline-report.xml`

Bạn có thể dùng trực tiếp các file này để:

- tham khảo cách chạy baseline scan
- chèn hình minh họa vào báo cáo
- đối chiếu với lần quét mới nếu cần

## 10. Ảnh nên chụp cho báo cáo

- Ảnh ZAP đang quét `https://localhost`
- Ảnh ZAP quét `https://localhost/swagger-ui.html`
- Ảnh tab `Alerts`
- Ảnh report HTML hoặc JSON đã sinh trong `docs/security`
- nếu có demo public, thêm ảnh quét `https://hackerlo.online`

## 11. Giới hạn của OWASP ZAP trong project này

- ZAP baseline không tự mô phỏng đầy đủ login, refresh token, logout, forgot password, webhook HMAC.
- Các bài test sâu như BOLA, replay webhook, rate limit vẫn nên kiểm chứng bằng Postman và integration test.
- Nếu quét local bằng container, cần dùng `host.docker.internal` thay vì `localhost`.

## 12. Kết luận

OWASP ZAP trong đồ án này được dùng để:

- quét nhanh bề mặt web qua HTTPS
- quan sát header và phản hồi cơ bản
- tạo minh chứng kiểm thử bảo mật cho báo cáo
- bổ sung cho Postman, Swagger và integration test chứ không thay thế hoàn toàn các công cụ đó
