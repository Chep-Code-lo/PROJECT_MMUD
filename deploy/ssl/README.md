# Ghi chú TLS cho môi trường local

Thư mục này chứa certificate mà `Nginx` dùng để phục vụ môi trường demo local qua `https://localhost`.

## 1. Các file cần có

- `fullchain.pem`
- `privkey.pem`

Reverse proxy sử dụng các file này được mô tả tại [deploy/nginx/securityapp.conf](/E:/PROJECT_MMUD/deploy/nginx/securityapp.conf).

## 2. Cách dùng trong project

- `Nginx` dùng certificate trong thư mục này để phục vụ frontend và API qua HTTPS
- Đây là chế độ chạy chuẩn cho demo đồ án và kiểm thử bảo mật nội bộ
- Trong luồng nộp đồ án hiện tại, chỉ cần `https://localhost`; chưa cần public Internet

## 3. Mục tiêu của lớp TLS trong project

1. Frontend và backend được truy cập qua `HTTPS`
2. Backend không bị public trực tiếp ra ngoài bằng HTTP thuần
3. `Nginx` terminate TLS rồi proxy:
   - `/` -> frontend
   - `/api/*` -> backend
   - `/swagger-ui*` và `/v3/api-docs*` -> backend nhưng chỉ mở với hostname local

## 4. Tạo certificate self-signed cho local

Nếu cần sinh lại certificate local:

```powershell
openssl req -x509 -nodes -days 365 -newkey rsa:2048 `
  -keyout deploy/ssl/privkey.pem `
  -out deploy/ssl/fullchain.pem `
  -subj "/CN=localhost"
```

## 5. Ghi chú thêm

- Nếu cần sinh lại cert local, có thể xóa `deploy/ssl/fullchain.pem` và `deploy/ssl/privkey.pem`, sau đó chạy lại:

```powershell
docker compose up -d --build
```

- Khi triển khai Internet thật, nên thay self-signed certificate bằng certificate hợp lệ, ví dụ từ Let's Encrypt hoặc hệ thống PKI nội bộ phù hợp
