# Bảo mật ứng dụng mạng dựa trên Cloud API cho dịch vụ công ty nhỏ

Monorepo này được tổ chức để demo đồ án MMUD theo hướng `REST API + mật mã ứng dụng + bảo mật API`.
Scope hiện tại đã được rút gọn để bám đúng trọng tâm:

- `JWT` cho authentication
- `bcrypt` cho password hashing
- `AES-GCM` cho dữ liệu nhạy cảm của customer
- `Role Authorization` cho `ADMIN`, `STAFF`, `USER`
- `Audit Log` để truy vết hành động
- `Swagger/OpenAPI`, `Postman/Newman`, `OWASP ZAP` để tài liệu hóa và kiểm thử
- `Docker Compose` và ghi chú `HTTPS/TLS` để phục vụ demo/deploy

`spring-boot-starter-oauth2-client` vẫn có trong `backend/pom.xml` để mở rộng nếu môn học bắt buộc chấm `OAuth2`, nhưng luồng demo hiện tại tập trung vào `JWT local auth`.

## 1. Cấu trúc repo

- `backend/`: Spring Boot API, Spring Security, JWT, AES, bcrypt, audit log
- `frontend/`: Next.js UI gọi API thật
- `database/`: `schema.sql` và ghi chú seed/reset
- `docs/api/`: OpenAPI export và hướng dẫn Swagger
- `docs/postman/`: collection, environment, hướng dẫn Postman/Newman
- `docs/security/`: ZAP plan, report HTML/JSON/XML, security notes
- `deploy/`: reverse proxy Nginx và ghi chú TLS

Main package:

```text
com.company.securityapp
```

## 2. Các luồng bảo mật đang có

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

Password không lưu plaintext. Hệ thống hash bằng `BCryptPasswordEncoder`.

### AES Encryption

Customer có 3 trường nhạy cảm được mã hóa bằng `AES-GCM` trước khi lưu DB:

- `phone`
- `address`
- `taxCode`

Trong database, các cột lưu thật là:

- `phone_encrypted`
- `address_encrypted`
- `tax_code_encrypted`

### Authorization matrix

| Route | Quyền |
|---|---|
| `/api/auth/register`, `/api/auth/login`, Swagger, health | Public |
| `/api/auth/me` | Đã đăng nhập |
| `/api/admin/**` | `ADMIN` |
| `/api/audit-logs/**` | `ADMIN` |
| `/api/customers/**` | `ADMIN`, `STAFF` |

### Audit log

Hệ thống đang ghi log cho:

- login success
- login failed
- create customer
- update customer
- delete customer

### Quy ước thời gian

Hệ thống đang dùng quy ước:

- backend lưu và trả thời gian theo `UTC`
- frontend audit log hiển thị theo giờ dự án `UTC+7`
- màn hình audit log tự động refresh mỗi `3` giây để dễ theo dõi gần real-time
- mỗi dòng audit log có thêm thông tin `x giây/phút/giờ trước`

Lý do:

- `UTC` giúp backend, database và môi trường deploy không bị lệch nhau
- `UTC+7` giúp màn demo nhìn thẳng ra giờ sự kiện theo múi giờ dự án
- dòng `x giây/phút/giờ trước` giúp dễ đối chiếu với thời gian thực tế ngay lúc demo
- vì vậy DB có thể hiển thị `UTC`, còn audit log UI hiển thị `UTC+7`, và đó là chủ đích

## 3. Port mặc định

- `frontend`: `3000`
- `backend`: `8080`
- `database`: `3307` trên host, `3306` trong container

## 4. Demo accounts

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

## 5. Cách chạy khuyến nghị: Docker Compose

Đây là cách phù hợp nhất để test dự án vì nó lên đầy đủ `frontend + backend + MySQL`.

```powershell
cd E:\PROJECT_MMUD
docker compose up -d --build
docker compose ps
```

Nếu cần đọc log:

```powershell
docker compose logs backend
docker compose logs frontend
docker compose logs database
```

Nếu trước đây đã chạy phiên bản cũ có module đã bị bỏ, nên reset volume 1 lần để demo sạch:

```powershell
docker compose down -v
docker compose up -d --build
```

URL sau khi lên:

- Frontend: `http://localhost:3000`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/api/health`

Dừng hệ thống:

```powershell
docker compose down
```

## 6. Chạy local không dùng Docker

### Backend

Test nhanh bằng profile H2:

```powershell
cd backend
mvn test
```

Chạy backend với MySQL:

```powershell
cd backend
mvn clean package -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Cần config các biến sau nếu không dùng compose:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`
- `APP_AES_SECRET`
- `APP_DEMO_USERS_ENABLED`
- `APP_DEMO_USERS_PASSWORD`

### Frontend

```powershell
cd frontend
npm install
npm run build
npm run dev
```

Frontend mặc định gọi về `http://localhost:8080` nếu `NEXT_PUBLIC_API_URL` không được set.

## 7. Kiểm thử bằng Swagger/OpenAPI

Mục tiêu của Swagger trong bài này:

- tài liệu hóa API
- test nhanh request/response
- đối chiếu status code và schema thật

### Bước test

1. Mở `http://localhost:8080/swagger-ui.html`
2. Gọi `POST /api/auth/login` bằng tài khoản `admin`
3. Copy `accessToken`
4. Bấm `Authorize`
5. Nhập:

```text
Bearer <accessToken>
```

6. Test các nhóm API:

- `GET /api/auth/me`
- `GET /api/customers`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`
- `GET /api/admin/summary`
- `GET /api/audit-logs`

### Điều cần kiểm tra

- `register` và `create customer` trả `201`
- `delete customer` trả `204`
- `me` không có token thì `401`
- `user` gọi customer API thì `403`

OpenAPI export mới nhất nằm ở:

- [docs/api/openapi.json](/E:/PROJECT_MMUD/docs/api/openapi.json)
- [docs/api/README.md](/E:/PROJECT_MMUD/docs/api/README.md)

## 8. Kiểm thử bảo mật API bằng Postman

Thư mục liên quan:

- [docs/postman/securityapp.postman_collection.json](/E:/PROJECT_MMUD/docs/postman/securityapp.postman_collection.json)
- [docs/postman/securityapp.local.postman_environment.json](/E:/PROJECT_MMUD/docs/postman/securityapp.local.postman_environment.json)
- [docs/postman/README.md](/E:/PROJECT_MMUD/docs/postman/README.md)

### Import và chạy trong Postman

1. Import collection
2. Import environment `Security App Local`
3. Chọn environment
4. Chạy collection theo thứ tự có sẵn

Collection hiện tại đã được rút gọn theo scope mới:

- auth
- admin summary
- customer CRUD
- audit log
- security checks cho `401` và `403`

### Chạy từ terminal bằng Newman

```powershell
npx --yes newman run docs\postman\securityapp.postman_collection.json -e docs\postman\securityapp.local.postman_environment.json --reporters cli
```

### Các case bảo mật nên test

- `Auth Me without Token (Expect 401)`
- `Customers as Staff (Expect 200)`
- `Customers as User (Expect 403)`
- `Audit Logs as User (Expect 403)`

## 9. Kiểm thử bảo mật bằng OWASP ZAP

Thư mục liên quan:

- [docs/security/zap.yaml](/E:/PROJECT_MMUD/docs/security/zap.yaml)
- [docs/security/zap-baseline-report.html](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.html)
- [docs/security/zap-baseline-report.json](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.json)
- [docs/security/zap-baseline-report.xml](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.xml)
- [docs/security/README.md](/E:/PROJECT_MMUD/docs/security/README.md)

### Cách chạy lại ZAP baseline

Yêu cầu: backend đang chạy ở `http://localhost:8080`.

```powershell
docker run --rm -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

Target baseline hiện tại:

```text
http://host.docker.internal:8080/swagger-ui.html
```

### Cách hiểu

- ZAP baseline được dùng cho surface public để spider được
- API có JWT được bổ sung bằng Postman/Newman và integration test
- mục tiêu là có bằng chứng quét bảo mật, không phải thay thế hết test xác thực/phân quyền

## 10. Kiểm chứng phần mật mã học trong database

### Kiểm tra bcrypt hash

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Kỳ vọng:

- cột `password_hash` bắt đầu bằng dạng hash `bcrypt` như `$2a$...` hoặc `$2b$...`
- không thấy password gốc

### Kiểm tra AES ciphertext

Tạo ít nhất 1 customer rồi chạy:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,name,email,phone_encrypted,address_encrypted,tax_code_encrypted FROM customers;"
```

Kỳ vọng:

- `email` đọc được
- `phone_encrypted`, `address_encrypted`, `tax_code_encrypted` là chuỗi ciphertext
- không thấy plaintext như số điện thoại hay địa chỉ gốc

### Kiểm tra audit log

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,action,actor_email,success,created_at FROM audit_logs ORDER BY id DESC LIMIT 10;"
```

Kỳ vọng:

- thấy `LOGIN_SUCCESS`, `LOGIN_FAILED`, `CREATE_CUSTOMER`, `UPDATE_CUSTOMER`, `DELETE_CUSTOMER`
- không thấy full JWT hoặc AES secret trong log

## 11. Kiểm tra nhanh bằng giao diện

Mở `http://localhost:3000/login` và test:

1. Đăng nhập `admin`
2. Vào `Customers`, tạo customer mới
3. Sửa hoặc xóa customer để sinh thêm audit log
4. Vào `Audit Logs`, xem các action vừa phát sinh
5. Đăng xuất
6. Đăng nhập `user`
7. Thử vào route customer và xác nhận bị chặn `403`

## 12. Tài liệu quan trọng

- [CHECKLIST_TEST.md](/E:/PROJECT_MMUD/CHECKLIST_TEST.md)
- [CHECKLIST_TEST_CHI_TIET_GIAI_THICH.md](/E:/PROJECT_MMUD/CHECKLIST_TEST_CHI_TIET_GIAI_THICH.md)
- [docs/api/README.md](/E:/PROJECT_MMUD/docs/api/README.md)
- [docs/postman/README.md](/E:/PROJECT_MMUD/docs/postman/README.md)
- [docs/security/README.md](/E:/PROJECT_MMUD/docs/security/README.md)
- [deploy/nginx/securityapp.conf](/E:/PROJECT_MMUD/deploy/nginx/securityapp.conf)
- [deploy/ssl/README.md](/E:/PROJECT_MMUD/deploy/ssl/README.md)

## 13. Ghi chú deploy và TLS

Repo đã có:

- reverse proxy Nginx trong `deploy/nginx/securityapp.conf`
- ghi chú TLS trong `deploy/ssl/README.md`

Khi deploy thật:

- không nên public backend thuần HTTP ra internet
- nên terminate TLS ở reverse proxy
- backend và frontend nên đặt sau Nginx/HTTPS
