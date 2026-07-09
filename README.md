# Hướng Dẫn Chạy Dự Án



## 1. Yêu Cầu Môi Trường

- Docker Desktop + Docker Compose
- Nếu chạy tay: Java `17`, Maven `3.9+`, Node.js `18+`, npm
- Các cổng mặc định cần trống: `80`, `443`, `3307`

## 2. Chạy Nhanh Bằng Docker

### Bước 1: Tạo file `.env`

```powershell
Copy-Item .env.example .env
```

Có thể giữ nguyên giá trị mặc định để chạy local.

### Bước 2: Khởi động hệ thống

```powershell
docker compose up --build -d
```

### Bước 3: Kiểm tra trạng thái

```powershell
docker compose ps
```

Nếu cần xem log:

```powershell
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f nginx
```

### Bước 4: Mở hệ thống

- Frontend: `https://localhost`
- Health check: `https://localhost/api/health`
- Swagger UI: `https://localhost/swagger-ui.html`
- OpenAPI JSON: `https://localhost/v3/api-docs`
- MySQL từ máy host: `localhost:3307`

Lưu ý: project dùng certificate tự ký, trình duyệt có thể hiện cảnh báo SSL ở lần mở đầu tiên. Chỉ cần chấp nhận để tiếp tục.

## 3. Tài Khoản Mẫu

- `student1@example.com / Password123!`
- `student2@example.com / Password123!`
- `admin@example.com / Admin123!`

## 4. Dừng Và Reset Dữ Liệu

Dừng hệ thống:

```powershell
docker compose down
```

Reset toàn bộ dữ liệu demo:

```powershell
docker compose down -v
docker compose up --build -d
```

## 5. Truy Cập Database

Nếu giữ mật khẩu root mặc định trong `.env.example`:

```powershell
docker exec -it securityapp-db mysql -uroot -pchange-me-root-password securityapp
```

Nếu bạn đã đổi mật khẩu trong `.env`, thay lại giá trị sau `-p`.

## 6. Chạy Tay Để Phát Triển

### 6.1. Backend

Backend chạy tay nên dùng profile `local` với H2 in-memory.

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="local"
$env:JWT_SECRET="dev-jwt-secret-with-at-least-32-characters"
$env:ENCRYPTION_KEY="dev-encryption-key-with-at-least-32-characters"
mvn spring-boot:run
```

Sau khi chạy:

- Backend API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### 6.2. Frontend

```powershell
cd frontend
npm install
$env:NEXT_PUBLIC_API_URL="http://localhost:8080"
npm run dev
```

Sau khi chạy:

- Frontend dev: `http://localhost:3000`

## 7. Lệnh Kiểm Tra Nhanh

Chạy test backend:

```powershell
cd backend
mvn test
```

Build frontend:

```powershell
cd frontend
npm install
npm run build
```

## 8. Sự Cố Thường Gặp

- Lỗi cổng `80`, `443`, `3307`: dừng dịch vụ đang chiếm cổng hoặc đổi giá trị trong `.env`.
- Không vào được `https://localhost`: kiểm tra `docker compose ps` và log của `nginx`, `frontend`, `backend`.
- Không vào được MySQL: kiểm tra lại `MYSQL_ROOT_PASSWORD` trong `.env`.
- Frontend chạy tay không gọi được API: kiểm tra `NEXT_PUBLIC_API_URL=http://localhost:8080`.
