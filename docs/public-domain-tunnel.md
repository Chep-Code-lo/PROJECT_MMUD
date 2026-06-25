# Hướng dẫn bật tunnel để truy cập domain public

Tài liệu này hướng dẫn mở hệ thống ra Internet theo cách **giữ nguyên local `https://localhost`** nhưng bổ sung một tunnel khi cần demo từ bên ngoài.

Project hỗ trợ 2 chế độ:

- **Quick tunnel**: tạo domain public tạm thời, không cần cấu hình DNS riêng.
- **Named tunnel**: dùng domain riêng ổn định qua Cloudflare Tunnel.

Lưu ý bảo mật:

- Swagger/OpenAPI hiện có thể mở qua domain public nếu reverse proxy được cấu hình cho phép
- Tunnel chỉ nên bật khi cần demo, không nên để chạy liên tục nếu không cần

## 1. Điều kiện trước khi bật tunnel

Hệ thống local phải chạy trước:

```powershell
docker compose up --build -d
```

Kiểm tra nhanh:

```powershell
curl.exe -sk https://localhost/api/health
```

Kỳ vọng:

- trả `200 OK`

## 2. Cách 1: Quick tunnel ra domain public tạm thời

Đây là cách nhanh nhất để demo cho giảng viên hoặc người xem từ bên ngoài.

### Bước 1: Chạy script bật quick tunnel

```powershell
.\scripts\start-public-tunnel.ps1 -Quick
```

### Bước 2: Xem domain public được cấp

```powershell
docker logs -f securityapp-cloudflared-quick
```

Trong log sẽ xuất hiện một URL dạng:

```text
https://something.trycloudflare.com
```

### Bước 3: Truy cập từ máy khác

Mở URL vừa nhận được trên trình duyệt hoặc điện thoại.

Kỳ vọng:

- trang chủ mở được
- đăng nhập, đăng ký, học viên, admin vẫn hoạt động
- `https://<public-domain>/swagger-ui.html` mở được nếu reverse proxy hiện tại cho phép public Swagger

### Bước 4: Tắt quick tunnel khi không dùng nữa

```powershell
.\scripts\stop-public-tunnel.ps1
```

## 3. Cách 2: Named tunnel với domain riêng Cloudflare

Chế độ này phù hợp khi bạn muốn một domain ổn định để demo nhiều lần.

Ví dụ:

```text
https://demo.example.com
```

### Bước 1: Đăng nhập Cloudflare Tunnel trên máy host

Cài `cloudflared` trên máy host rồi chạy:

```powershell
cloudflared tunnel login
```

### Bước 2: Tạo tunnel

```powershell
cloudflared tunnel create securityapp-demo
```

Sau bước này, Cloudflare sẽ tạo:

- một `tunnel UUID`
- một file credential JSON trong thư mục `.cloudflared` của người dùng hiện tại

### Bước 3: Tạo DNS route cho domain public

```powershell
cloudflared tunnel route dns securityapp-demo demo.example.com
```

### Bước 4: Chép file credential vào project

Tạo thư mục nếu chưa có:

```powershell
New-Item -ItemType Directory -Force deploy\cloudflared\credentials
```

Sao chép file credential:

```powershell
Copy-Item "$env:USERPROFILE\.cloudflared\<TUNNEL_UUID>.json" "deploy\cloudflared\credentials\<TUNNEL_UUID>.json"
```

### Bước 5: Tạo file cấu hình local cho tunnel

```powershell
Copy-Item deploy\cloudflared\config.example.yml deploy\cloudflared\config.local.yml
```

Sửa các giá trị sau trong `deploy/cloudflared/config.local.yml`:

- `tunnel: <TUNNEL_UUID>`
- `credentials-file: /etc/cloudflared/credentials/<TUNNEL_UUID>.json`
- `hostname: demo.example.com`

### Bước 6: Khai báo domain public trong `.env`

Thêm hoặc sửa:

```env
PUBLIC_BASE_URL=https://demo.example.com
```

### Bước 7: Chạy named tunnel

```powershell
.\scripts\start-public-tunnel.ps1
```

### Bước 8: Kiểm tra

Mở:

```text
https://demo.example.com
```

Kỳ vọng:

- truy cập được giao diện từ Internet
- API hoạt động qua cùng origin
- Swagger/OpenAPI có thể mở được trên domain public nếu Nginx không chặn hostname public

## 4. Kiểm tra Swagger trên domain public

Kiểm tra:

```powershell
curl.exe -I https://hackerlo.online/swagger-ui.html
curl.exe -I https://hackerlo.online/v3/api-docs
```

Kỳ vọng:

- `swagger-ui.html` trả `302` sang `/swagger-ui/index.html` hoặc `200`
- `v3/api-docs` trả `200`

Nếu domain `hackerlo.online` đang trỏ đúng về tunnel/reverse proxy hiện tại, Swagger/OpenAPI sẽ mở được qua domain này.

## 5. Các file liên quan

- `docker-compose.public-domain.yml`
- `deploy/cloudflared/config.example.yml`
- `deploy/cloudflared/config.local.yml`
- `deploy/cloudflared/credentials/`
- `scripts/start-public-tunnel.ps1`
- `scripts/stop-public-tunnel.ps1`

## 6. Gợi ý dùng trong buổi demo

- Nếu chỉ cần cho giảng viên xem nhanh từ điện thoại hoặc máy khác: dùng **quick tunnel**
- Nếu cần một domain cố định để gửi trước: dùng **named tunnel**
- Sau buổi demo, nên tắt tunnel để giảm bề mặt truy cập từ bên ngoài
