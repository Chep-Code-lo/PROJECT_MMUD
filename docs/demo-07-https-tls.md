# Demo 07: HTTPS / TLS

## 1. Mục tiêu

Demo này dùng để chứng minh:

- Ứng dụng chạy qua HTTPS thay vì chỉ dùng HTTP
- Request HTTP bị ép chuyển hướng sang HTTPS
- Dữ liệu truyền trên mạng như JWT, mật khẩu và dữ liệu API được bảo vệ tốt hơn khi đi qua TLS
- Swagger chạy cùng lớp HTTPS của hệ thống và có thể kiểm tra được cả ở local lẫn domain public khi tunnel đang bật

## 2. Chuẩn bị

### 2.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 2.2. Các địa chỉ cần nhớ

- Ứng dụng: `https://localhost`
- Health check: `https://localhost/api/health`
- Swagger: `https://localhost/swagger-ui.html`

### 2.3. Thực hiện demo này ở đâu

Demo 07 nên dùng **2 nơi**:

#### Nơi 1: PowerShell để kiểm tra mã trạng thái HTTP và HTTPS

Bạn sẽ dùng `curl.exe` để kiểm tra:

- HTTP có bị chuyển hướng không;
- HTTPS có hoạt động không.

#### Nơi 2: Trình duyệt để kiểm tra giao diện và Swagger

Bạn sẽ mở trực tiếp:

- `https://localhost`
- `https://localhost/swagger-ui.html`

để minh họa việc Swagger dùng cùng cổng với ứng dụng nhưng vẫn bị chặn nếu đi bằng hostname public.
để minh họa việc Swagger dùng cùng cổng với ứng dụng và đi qua đúng lớp HTTPS của hệ thống.

## 3. Các bước thực hiện

### Bước 1: Mở PowerShell tại thư mục project

```powershell
cd E:\PROJECT_MMUD
```

### Bước 2: Kiểm tra HTTP bị chuyển hướng sang HTTPS

Chạy:

```powershell
curl.exe -I http://localhost/api/health
```

Kết quả mong đợi:

- Trả `301 Moved Permanently`
- Header `Location` trỏ sang `https://localhost/...`

Điểm cần nói:

- Nginx đang ép mọi truy cập HTTP sang HTTPS

### Bước 3: Kiểm tra HTTPS hoạt động bình thường

Chạy:

```powershell
curl.exe -k https://localhost/api/health
```

Kết quả mong đợi:

- Trả `200 OK`

Lưu ý:

- Tùy chọn `-k` dùng vì certificate local là self-signed

### Bước 4: Mở ứng dụng trên trình duyệt

Truy cập:

```text
https://localhost
```

Kết quả mong đợi:

- Frontend tải bình thường
- Trình duyệt hiển thị kết nối HTTPS

Nếu trình duyệt cảnh báo chứng chỉ, hãy tiếp tục truy cập vì đây là môi trường demo local.

### Bước 5: Kiểm tra Swagger mở được trên hostname local

Trên trình duyệt, mở:

```text
https://localhost/swagger-ui.html
https://localhost/v3/api-docs
```

Kết quả mong đợi:

- Cả hai URL đều mở được trên chính máy host

Ý nghĩa:

- Tài liệu API dùng cùng lớp HTTPS với ứng dụng để tránh lỗi lệch cổng khi demo

### Bước 6: Kiểm tra Swagger qua hostname phù hợp với môi trường demo

Trên PowerShell, chạy:

```powershell
curl.exe -k -I https://localhost/swagger-ui.html -H "Host: demo-public.example"
curl.exe -k -I https://localhost/v3/api-docs -H "Host: demo-public.example"
```

Kết quả mong đợi:

- Nếu chưa bật tunnel public, hai request giả lập trên thường không cho kết quả hợp lệ cho Swagger.
- Nếu đã bật tunnel public, nên kiểm tra trực tiếp domain thật như `https://hackerlo.online/swagger-ui.html`.

Điểm cần nhấn mạnh:

- Swagger nên được kiểm thử trên đúng hostname đang dùng để demo
- `localhost` phù hợp cho buổi báo cáo trực tiếp trên máy chạy hệ thống
- domain public phù hợp cho buổi demo từ xa khi tunnel đã bật

## 4. Cách trình bày ngắn gọn trước giảng viên

Bạn nên trình bày theo đúng thứ tự:

1. Mở PowerShell
2. Chạy `curl.exe -I http://localhost/api/health`
3. Chỉ ra mã `301`
4. Chạy `curl.exe -k https://localhost/api/health`
5. Chỉ ra mã `200`
6. Mở `https://localhost` trên trình duyệt
7. Mở `https://localhost/swagger-ui.html` và chỉ ra Swagger hoạt động cùng cổng `443`
8. Nếu cần demo public, mở thêm `https://hackerlo.online/swagger-ui.html`

Nếu giảng viên hỏi “demo này làm ở đâu”, câu trả lời chuẩn là:

- **PowerShell để kiểm tra trạng thái HTTP/HTTPS**
- **Trình duyệt để kiểm tra giao diện và Swagger**

## 5. Giải thích ngắn gọn để trình bày

- TLS giúp mã hóa kênh truyền giữa client và server
- Khi chỉ dùng HTTP, dữ liệu nhạy cảm có thể bị nhìn thấy trên đường truyền nếu môi trường mạng không an toàn
- Với HTTPS, nội dung request và response được bảo vệ tốt hơn khi đi qua mạng
- Trong đồ án này, TLS được terminate tại Nginx, sau đó Nginx chuyển tiếp request vào backend và frontend trong mạng Docker nội bộ

## 6. Minh chứng nên chụp cho báo cáo

- Ảnh `curl.exe -I http://localhost/api/health` trả `301`
- Ảnh `curl.exe -k https://localhost/api/health` trả `200`
- Ảnh trình duyệt mở `https://localhost`
- Ảnh truy cập `https://localhost/swagger-ui.html` thành công
- Ảnh PowerShell giả lập host public và nhận `404`

## 7. Kết luận

Demo này cho thấy hệ thống không chỉ bảo vệ dữ liệu khi lưu trữ mà còn bảo vệ dữ liệu trong quá trình truyền qua mạng. Đây là phần quan trọng để liên kết giữa xác thực, token, mật khẩu và kênh truyền an toàn trong một hệ thống RESTful API.
