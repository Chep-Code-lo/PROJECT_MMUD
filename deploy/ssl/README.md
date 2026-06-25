# Ghi chú TLS cho môi trường local

Thư mục này chứa certificate dùng cho `Nginx` local origin.

## 1. File cần có

- `fullchain.pem`
- `privkey.pem`

Reverse proxy được mô tả tại [deploy/nginx/securityapp.conf](/E:/PROJECT_MMUD/deploy/nginx/securityapp.conf).

## 2. Cách dùng trong project

- `Nginx` dùng certificate trong thư mục này để phục vụ `https://localhost`
- Đây là chế độ chạy chuẩn cho demo đồ án và kiểm thử bảo mật nội bộ
- Nếu sau này cần publish ra internet, nên để TLS kết thúc ở reverse proxy hoặc dịch vụ edge phù hợp, nhưng luồng nộp đồ án hiện tại chỉ cần `https://localhost`

## 3. Mục tiêu của lớp TLS

1. Frontend và backend đi qua `HTTPS`
2. Backend không bị public trực tiếp qua HTTP ngoài internet
3. `Nginx` terminate TLS rồi proxy:
   - `/` -> frontend
   - `/api/*` -> backend
   - `/swagger-ui/*` và `/v3/api-docs/*` -> backend

## 4. Tạo cert self-signed cho local

Nếu cần test local với self-signed certificate:

```powershell
openssl req -x509 -nodes -days 365 -newkey rsa:2048 ^
  -keyout deploy/ssl/privkey.pem ^
  -out deploy/ssl/fullchain.pem ^
  -subj "/CN=localhost"
```

## 5. Ghi chú thêm

- File cert local đã được đưa vào `.gitignore`.
- Nếu cần sinh lại cert local, có thể xóa `deploy/ssl/fullchain.pem` và `deploy/ssl/privkey.pem`, sau đó chạy lại `docker compose up -d --build`.
