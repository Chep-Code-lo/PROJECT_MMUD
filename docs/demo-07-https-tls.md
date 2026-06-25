# Demo 07: HTTPS / TLS

## 1. Mục tiêu

Demo này nhằm chứng minh:

1. Hệ thống phục vụ dịch vụ qua `HTTPS` thay vì chỉ `HTTP`.
2. Truy cập `HTTP` sẽ bị chuyển hướng sang `HTTPS`.
3. Dữ liệu truyền trên mạng như mật khẩu, JWT và response API được bảo vệ tốt hơn nhờ `TLS`.
4. Swagger/OpenAPI chạy trên cùng lớp HTTPS với hệ thống nên có thể test API mà không bị lệch cổng.

## 2. Chuẩn bị

### 2.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 2.2. Các địa chỉ cần nhớ

- Frontend local: `https://localhost`
- Health check local: `https://localhost/api/health`
- Swagger local: `https://localhost/swagger-ui.html`
- OpenAPI JSON local: `https://localhost/v3/api-docs`

Nếu bạn đang bật tunnel/domain public, có thể kiểm tra thêm:

- Frontend public: `https://hackerlo.online`
- Swagger public: `https://hackerlo.online/swagger-ui.html`

### 2.3. Thực hiện ở đâu

Demo này nên dùng:

- `PowerShell` để kiểm tra trạng thái HTTP và HTTPS
- `Trình duyệt` để mở frontend và Swagger

## 3. Các bước thực hiện chi tiết

### Bước 1: Kiểm tra HTTP bị chuyển hướng sang HTTPS

Mở PowerShell:

```powershell
cd E:\PROJECT_MMUD
curl.exe -I http://localhost/api/health
```

Kết quả mong đợi:

- trả `301 Moved Permanently`
- header `Location` trỏ sang `https://localhost/...`

Điểm cần nói:

- reverse proxy đang ép toàn bộ request HTTP sang HTTPS

### Bước 2: Kiểm tra HTTPS hoạt động bình thường

Chạy:

```powershell
curl.exe -k https://localhost/api/health
```

Kết quả mong đợi:

- HTTP `200 OK`

Lưu ý:

- dùng `-k` vì chứng chỉ local là self-signed

### Bước 3: Mở frontend qua HTTPS

Trên trình duyệt, mở:

```text
https://localhost
```

Kết quả mong đợi:

- trang chủ tải bình thường
- trình duyệt hiển thị kết nối HTTPS

Nếu trình duyệt cảnh báo self-signed certificate, chọn tiếp tục truy cập vì đây là môi trường demo local.

### Bước 4: Mở Swagger qua HTTPS

Mở:

```text
https://localhost/swagger-ui.html
```

hoặc nếu được chuyển hướng:

```text
https://localhost/swagger-ui/index.html
```

Kết quả mong đợi:

- Swagger mở được bình thường
- bấm `Execute` không bị lỗi lệch cổng vì frontend proxy và API đều chạy sau HTTPS edge

### Bước 5: Kiểm tra OpenAPI JSON

Mở:

```text
https://localhost/v3/api-docs
```

Kết quả mong đợi:

- trả ra tài liệu OpenAPI ở dạng JSON

### Bước 6: Tùy chọn kiểm tra public domain

Chỉ thực hiện bước này khi tunnel/domain public đang bật thật.

Trên PowerShell:

```powershell
curl.exe -I https://hackerlo.online
curl.exe -I https://hackerlo.online/swagger-ui.html
```

Hoặc mở trực tiếp trên trình duyệt:

```text
https://hackerlo.online
https://hackerlo.online/swagger-ui.html
```

Kết quả mong đợi:

- public domain phản hồi qua HTTPS
- Swagger public mở được nếu bạn đang cho phép public swagger ở reverse proxy

## 4. Kết quả mong đợi

Sau khi làm xong Demo 07, bạn phải chứng minh được:

1. `HTTP` bị ép sang `HTTPS`.
2. API health phản hồi thành công trên `HTTPS`.
3. Frontend và Swagger đều phục vụ qua cùng lớp HTTPS.
4. Có thể dùng local host hoặc domain public để demo tùy ngữ cảnh triển khai.

## 5. Câu nên nói khi trình bày

- `TLS` giúp mã hóa kênh truyền giữa client và server.
- Nếu chỉ dùng HTTP thì JWT, mật khẩu và response API có thể bị nhìn thấy trên đường truyền.
- Trong project này, TLS được terminate tại Nginx rồi reverse proxy vào backend/frontend trong mạng Docker nội bộ.
- HTTPS là lớp bảo vệ cho dữ liệu khi truyền, còn JWT, bcrypt, AES và HMAC là các lớp bảo vệ ở tầng ứng dụng.

## 6. Ảnh nên chụp cho báo cáo

- Ảnh `curl.exe -I http://localhost/api/health` trả `301`.
- Ảnh `curl.exe -k https://localhost/api/health` trả `200`.
- Ảnh trình duyệt mở `https://localhost`.
- Ảnh truy cập `https://localhost/swagger-ui.html`.
- Nếu có demo từ xa, thêm ảnh `https://hackerlo.online`.

## 7. Cách reset sau demo

Demo này không làm thay đổi dữ liệu hệ thống nên không cần reset.

## 8. Kết luận

Demo này cho thấy project không chỉ bảo vệ dữ liệu khi lưu trữ mà còn bảo vệ dữ liệu trong quá trình truyền qua mạng. Đây là mắt xích quan trọng để hoàn thiện chuỗi bảo mật tổng thể của hệ thống RESTful API.
