# Demo 07: HTTPS / TLS

## 1. Mục tiêu

Demo này dùng để chứng minh:

- Ứng dụng chạy qua HTTPS thay vì chỉ dùng HTTP
- Request HTTP bị ép chuyển hướng sang HTTPS
- Dữ liệu truyền trên mạng như JWT, mật khẩu và dữ liệu API được bảo vệ tốt hơn khi đi qua TLS
- Swagger không bị lộ trên cổng public mà chỉ mở cục bộ cho máy chủ

## 2. Chuẩn bị

### 2.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 2.2. Các địa chỉ cần nhớ

- Ứng dụng: `https://localhost`
- Health check: `https://localhost/api/health`
- Swagger local-only: `https://localhost:8444/swagger-ui.html`

## 3. Các bước thực hiện

### Bước 1: Kiểm tra HTTP bị chuyển hướng

```powershell
curl.exe -I http://localhost/api/health
```

Kết quả mong đợi:

- Trả `301 Moved Permanently`
- Header `Location` trỏ sang `https://localhost/...`

### Bước 2: Kiểm tra HTTPS hoạt động

Với self-signed certificate local, có thể dùng:

```powershell
curl.exe -k https://localhost/api/health
```

Kết quả mong đợi:

- Trả `200 OK`

### Bước 3: Kiểm tra ứng dụng truy cập qua HTTPS

Mở trình duyệt:

```text
https://localhost
```

Kết quả mong đợi:

- Frontend tải bình thường qua HTTPS
- Các API từ frontend cũng đi qua HTTPS

### Bước 4: Kiểm tra Swagger không lộ ở cổng public

Thử truy cập:

```text
https://localhost/swagger-ui.html
https://localhost/v3/api-docs
```

Kết quả mong đợi:

- Cả hai URL đều trả `404`

### Bước 5: Kiểm tra Swagger chỉ mở cục bộ

Truy cập trên chính máy host:

```text
https://localhost:8444/swagger-ui.html
```

Kết quả mong đợi:

- Swagger mở được
- Có thể dùng để nhập Bearer token và kiểm thử nội bộ

Điểm cần nhấn mạnh:

- Docker Compose chỉ bind cổng Swagger thành `127.0.0.1:8444`
- Máy khác trong mạng không truy cập được nếu bạn không chủ động mở thêm

## 4. Giải thích ngắn gọn để trình bày

- TLS giúp mã hóa kênh truyền giữa client và server
- Khi chỉ dùng HTTP, dữ liệu nhạy cảm có thể bị nhìn thấy trên đường truyền nếu môi trường mạng không an toàn
- Với HTTPS, nội dung request/response được bảo vệ tốt hơn khi đi qua mạng
- Trong đồ án này, TLS được terminate tại Nginx, sau đó Nginx chuyển tiếp request vào backend và frontend trong mạng Docker nội bộ

## 5. Minh chứng nên chụp cho báo cáo

- Ảnh `curl -I http://localhost/api/health` trả `301`
- Ảnh `curl -k https://localhost/api/health` trả `200`
- Ảnh truy cập `https://localhost/swagger-ui.html` bị `404`
- Ảnh truy cập `https://localhost:8444/swagger-ui.html` thành công trên máy host

## 6. Kết luận

Demo này cho thấy hệ thống không chỉ bảo vệ dữ liệu khi lưu trữ mà còn bảo vệ dữ liệu trong quá trình truyền qua mạng. Đây là phần quan trọng để liên kết giữa xác thực, token, mật khẩu và kênh truyền an toàn trong một hệ thống RESTful API.
