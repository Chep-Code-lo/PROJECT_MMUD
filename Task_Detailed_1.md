# Hướng dẫn làm việc nhóm theo luồng phụ thuộc

## 1. Mục tiêu tài liệu

Đây là tài liệu giao việc ngắn cho nhóm 3 người. Tài liệu này ưu tiên đúng trọng tâm của đồ án: mật mã học, bảo mật API và triển khai hệ thống an toàn.

Dự án phải chứng minh được các ý sau bằng code và bằng demo:

- `RESTful API` với `Spring Boot`
- `Next.js` để gọi API thật
- `JWT` cho xác thực
- `OAuth2 cơ bản` nếu môn yêu cầu chạm vào luồng đăng nhập bên thứ ba
- `AES Encryption` cho dữ liệu nhạy cảm
- `bcrypt` cho mật khẩu
- `HTTPS/TLS` khi triển khai
- `OWASP API Security Top 10` ở mức kiểm tra thực tế
- `Swagger/OpenAPI` cho tài liệu và test API
- `Docker Compose` để chạy và demo hệ thống

UI không phải phần chính. Frontend chỉ cần đủ để chứng minh login, authorization, customer flow, ticket flow và lỗi `401` hoặc `403`.

## 2. Bám vào lý thuyết và thực hành của môn

| Nội dung | Phần phải xuất hiện trong project | Người làm chính |
|---|---|---|
| RESTful API | `/api/auth`, `/api/customers`, `/api/tickets`, `/api/audit-logs`, đúng HTTP method và status code | Bạn 1, Bạn 2 |
| Next.js | login, register, dashboard, customers, tickets, protected route | Bạn 3 |
| JWT | `register`, `login`, `me`, filter, validate token, trả `401` khi token sai | Bạn 1 |
| OAuth2 cơ bản | 1 luồng đăng nhập cơ bản bằng Google hoặc GitHub nếu giảng viên yêu cầu | Bạn 1 |
| AES Encryption | mã hóa `phone`, `address`, `taxCode` trước khi lưu DB | Bạn 2 |
| Password Hashing bcrypt | không lưu plaintext password, hash bằng `BCryptPasswordEncoder` | Bạn 1 |
| HTTPS/TLS | deploy sau cùng phải đi qua HTTPS, không demo public bằng HTTP thuần | Bạn 3 |
| OWASP API Security Top 10 | test `401`, `403`, excessive data exposure, authz sai, security config sai, log sai | Cả nhóm |
| Java, Spring Boot, Spring Security | backend base, security config, filter, service, controller | Bạn 1, Bạn 2 |
| Swagger/OpenAPI | `/swagger-ui.html`, `/v3/api-docs`, Postman sync với API hiện tại | Bạn 1, Bạn 3 |

Các đầu ra thực hành tối thiểu phải có:

- Backend Spring Boot chạy được local.
- Frontend Next.js gọi được API thật.
- Mật khẩu trong DB là hash `bcrypt`.
- Dữ liệu customer nhạy cảm trong DB là ciphertext `AES`.
- Swagger mở được.
- Postman collection chạy được.
- Có kết quả test bằng `OWASP ZAP`.
- Hệ thống chạy được bằng `docker compose`.
- Bản demo cuối đi qua `HTTPS/TLS` nếu đã deploy internet.

## 3. Quy tắc làm việc chung

- Trước mỗi phần việc luôn chạy:

```bash
git checkout main
git pull origin main
git checkout -b <ten-branch>
```

- Không code trực tiếp trên `main`.
- Mỗi phần việc dùng một branch riêng.
- Chỉ bắt đầu khi dependency đã merge vào `main`.
- Không dùng branch dở của người khác làm đầu vào chính thức.
- `Task_Detailed.md` là tài liệu bàn giao gốc. Khi code và tài liệu lệch nhau, phải sửa ngay trong cùng lượt làm việc, không để lệch kéo dài.
- Giữ package backend đang dùng trong repo. Nếu repo hiện là `com.company.securityapp` thì giữ nguyên, không đổi package giữa chừng chỉ vì muốn đẹp hơn.
- Xóa file code mẫu hoặc file trùng không còn dùng. Không để song song file cũ và file mới.
- Không xóa code đang chạy thật chỉ để repo trông gọn.
- Push khi build và test của phần mình đã pass.
- Mở Pull Request khi đầu ra giai đoạn đã đủ cho người tiếp theo dùng tiếp.

Những file thường phải dọn nếu không dùng:

- Backend: controller mẫu, service mẫu, test mẫu do Spring Initializr tạo ra.
- Frontend: `app/page.tsx` mặc định, file demo của Next.js, icon hoặc asset mẫu không dùng.
- Docs: ảnh cũ, collection cũ, file hướng dẫn cũ không còn khớp với code hiện tại.

## 4. Luồng phụ thuộc của cả nhóm

Luồng làm việc ngắn gọn của dự án này:

1. Bạn 1 chuẩn hóa repo và cấu trúc chung.
2. Sau khi phần đó merge, Bạn 1 làm backend base và Bạn 3 làm frontend scaffold song song.
3. Sau khi backend base merge, Bạn 1 làm authentication với `JWT` và `bcrypt`.
4. Sau khi auth merge, Bạn 2 làm domain model, schema và Customer API có `AES`.
5. Sau khi Customer API ổn định, Bạn 2 làm Ticket API.
6. Sau khi auth, customer, ticket đã có, Bạn 1 làm role authorization.
7. Sau khi authorization đã có, Bạn 2 thêm audit log và hardening backend.
8. Sau khi backend đã ổn định, Bạn 3 nối frontend với API thật.
9. Sau khi backend và frontend đã chạy thật, Bạn 3 làm Swagger, Postman, `OWASP ZAP`, Docker và HTTPS/TLS.

Lưu ý về `OAuth2 cơ bản`:

- Không để `OAuth2` làm chậm phần `JWT` lõi.
- Chỉ làm `OAuth2` sau khi `JWT` local auth đã chạy ổn.
- Nếu môn bắt buộc demo `OAuth2`, Bạn 1 mở thêm nhánh riêng `feature/oauth2-basic` sau Giai đoạn 4.

## Giai đoạn 1: Bạn 1 chuẩn hóa project chung

- Người làm: Bạn 1
- Điều kiện bắt đầu: không có
- Branch: `chore/project-bootstrap`

Bạn 1 làm các việc sau:

- Dùng repo hiện có, không tạo repo mới.
- Chuẩn hóa các thư mục: `backend/`, `frontend/`, `database/`, `docs/`, `deploy/`.
- Cập nhật `README.md` với mô tả ngắn về công nghệ, port, cách chạy local.
- Cập nhật `.gitignore`.
- Xóa file mẫu và file trùng không dùng nữa trong repo.

File cần kiểm tra hoặc sửa:

- `README.md`
- `.gitignore`
- `backend/`
- `frontend/`
- `database/`
- `docs/`
- `deploy/`

Test trước khi push:

```bash
rg --files
git status
```

Push và mở PR khi:

- Cấu trúc repo đã sạch.
- Không còn file mẫu thừa gây nhầm lẫn.
- README đủ để người khác clone về làm tiếp.

Sau khi merge:

- Bạn 1 làm Giai đoạn 2.
- Bạn 3 làm Giai đoạn 3.

## Giai đoạn 2: Bạn 1 tạo backend Spring Boot base

- Người làm: Bạn 1
- Điều kiện bắt đầu: Giai đoạn 1 đã merge
- Branch: `feature/backend-foundation`

Bạn 1 làm các việc sau:

- Tạo hoặc chuẩn hóa project `Spring Boot` trong `backend/`.
- Dùng `Java 17`, `Maven`.
- Thêm dependencies: `Spring Web`, `Spring Security`, `Spring Data JPA`, `Validation`, `Lombok`, `MySQL Driver`, `Swagger/OpenAPI`.
- Nếu chắc chắn sẽ demo `OAuth2`, thêm sẵn dependency `oauth2-client`. Chỉ thêm dependency, chưa cần code flow ở giai đoạn này.
- Tạo package nền theo package đang có trong repo.
- Chuẩn hóa các package: `config`, `controller`, `dto`, `entity`, `repository`, `security`, `service`, `exception`.
- Tạo `application.properties` hoặc `application.yml` cho datasource, JWT secret, AES secret.
- Mở được `Swagger` ở mức cấu hình nền.
- Xóa controller mẫu hoặc test mẫu không dùng.

File chính:

- `backend/pom.xml`
- `backend/src/main/resources/application.properties`
- package nền trong `backend/src/main/java/...`

Test trước khi push:

```bash
cd backend
mvn -q -DskipTests package
mvn spring-boot:run
```

Phải kiểm tra:

- Backend build được.
- Backend boot được.
- Không lỗi datasource nền.
- Mở được `/swagger-ui.html` hoặc ít nhất `/v3/api-docs`.

Push và mở PR khi:

- Backend đủ ổn để người khác thêm entity, service, controller.

Sau khi merge:

- Bạn 1 làm Giai đoạn 4.
- Bạn 3 làm Giai đoạn 3 nếu chưa xong.

## Giai đoạn 3: Bạn 3 tạo frontend Next.js base

- Người làm: Bạn 3
- Điều kiện bắt đầu: Giai đoạn 1 đã merge
- Branch: `feature/frontend-scaffold`

Bạn 3 làm các việc sau:

- Tạo hoặc chuẩn hóa project `Next.js` trong `frontend/`.
- Tạo các route nền:
  - `frontend/app/login/page.tsx`
  - `frontend/app/register/page.tsx`
  - `frontend/app/dashboard/page.tsx`
  - `frontend/app/customers/page.tsx`
  - `frontend/app/tickets/page.tsx`
- Tạo các thư mục: `components/`, `services/`, `lib/`, `types/`.
- Chưa nối API thật ở giai đoạn này.
- Xóa các file demo mặc định của Next.js nếu không dùng nữa.

File chính:

- `frontend/package.json`
- `frontend/app/**`
- `frontend/components/**`
- `frontend/services/**`
- `frontend/lib/**`
- `frontend/types/**`

Test trước khi push:

```bash
cd frontend
npm install
npm run dev
npm run build
```

Push và mở PR khi:

- Frontend chạy được local.
- Route nền mở được.
- Không còn file demo mặc định gây rối.

Sau khi merge:

- Bạn 3 chờ backend ổn định để làm Giai đoạn 9.

## Giai đoạn 4: Bạn 1 làm Authentication với JWT và bcrypt

- Người làm: Bạn 1
- Điều kiện bắt đầu: Giai đoạn 2 đã merge
- Branch: `feature/auth-jwt`

Bạn 1 làm các việc sau:

- Tạo `User`, `Role`, `UserRepository`.
- Tạo `AuthService`, `AuthController`.
- Tạo `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`.
- Dùng `BCryptPasswordEncoder` để hash password.
- Tạo endpoint:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `GET /api/auth/me`
- Response login tối thiểu phải có:
  - `accessToken`
  - `tokenType`
  - `role`
  - thông tin user tối thiểu
- Không trả password hash ra response.
- Nếu môn bắt buộc demo `OAuth2 cơ bản`, sau khi nhánh này merge, Bạn 1 tạo thêm nhánh `feature/oauth2-basic` để làm 1 luồng login Google hoặc GitHub cơ bản.

File chính:

- `backend/src/main/java/.../entity/User.java`
- `backend/src/main/java/.../entity/Role.java`
- `backend/src/main/java/.../repository/UserRepository.java`
- `backend/src/main/java/.../service/AuthService.java`
- `backend/src/main/java/.../controller/AuthController.java`
- `backend/src/main/java/.../security/JwtService.java`
- `backend/src/main/java/.../security/JwtAuthenticationFilter.java`
- `backend/src/main/java/.../config/SecurityConfig.java`

Test trước khi push:

```bash
cd backend
mvn -q -DskipTests package
mvn spring-boot:run
```

Test bằng Postman:

1. Register thành công.
2. Login thành công.
3. Gọi `/api/auth/me` với token hợp lệ.
4. Login sai mật khẩu phải trả `401` hoặc `400` theo chuẩn nhóm đã chốt.
5. Gọi `/api/auth/me` không có token phải trả `401`.
6. Gọi `/api/auth/me` với token sai phải trả `401`.

Kiểm tra DB:

- Password không được lưu plaintext.
- Hash `bcrypt` thường bắt đầu bằng `$2a$`, `$2b$` hoặc `$2y$`.

Push và mở PR khi:

- Auth flow đã ổn định để backend khác dùng `User` hoặc `Role`.
- Password hash đúng.
- JWT validate đúng.

Sau khi merge:

- Bạn 2 làm Giai đoạn 5.
- Nếu cần, Bạn 1 làm thêm `feature/oauth2-basic`.

## Giai đoạn 5: Bạn 2 làm domain model, schema và Customer API có AES

- Người làm: Bạn 2
- Điều kiện bắt đầu: Giai đoạn 4 đã merge
- Branch: `feature/customer-aes-api`

Bạn 2 làm các việc sau:

- Tạo entity:
  - `Customer`
  - `Ticket`
  - `AuditLog`
- Tạo enum:
  - `TicketStatus`
  - `TicketPriority`
- Tạo repository:
  - `CustomerRepository`
  - `TicketRepository`
  - `AuditLogRepository`
- Cập nhật `database/schema.sql` và `database/seed.sql` nếu cần.
- Tạo `EncryptionService`.
- Tạo `CustomerRequest`, `CustomerResponse`.
- Tạo `CustomerService`, `CustomerController`.
- Làm các API:
  - `GET /api/customers`
  - `POST /api/customers`
  - `GET /api/customers/{id}`
  - `PUT /api/customers/{id}`
  - `DELETE /api/customers/{id}`
- Mã hóa `phone`, `address`, `taxCode` bằng `AES` trước khi lưu DB.
- Không trả ciphertext thô ra ngoài API.
- Nếu đổi tên DTO, entity hoặc field, phải xóa file cũ không còn dùng để tránh trùng class và trùng luồng xử lý.

File chính:

- `database/schema.sql`
- `database/seed.sql`
- `backend/src/main/java/.../entity/Customer.java`
- `backend/src/main/java/.../entity/Ticket.java`
- `backend/src/main/java/.../entity/AuditLog.java`
- `backend/src/main/java/.../entity/TicketStatus.java`
- `backend/src/main/java/.../entity/TicketPriority.java`
- `backend/src/main/java/.../repository/CustomerRepository.java`
- `backend/src/main/java/.../repository/TicketRepository.java`
- `backend/src/main/java/.../repository/AuditLogRepository.java`
- `backend/src/main/java/.../service/EncryptionService.java`
- `backend/src/main/java/.../service/CustomerService.java`
- `backend/src/main/java/.../controller/CustomerController.java`

Test trước khi push:

```bash
cd backend
mvn -q -DskipTests package
mvn spring-boot:run
```

Nếu import schema bằng CLI:

```bash
mysql -u root -p securityapp < database/schema.sql
```

Test bằng Postman:

1. Tạo customer hợp lệ.
2. Tạo customer thiếu field bắt buộc.
3. Lấy danh sách customer.
4. Lấy chi tiết customer.
5. Cập nhật customer.
6. Xóa customer.

Kiểm tra DB:

- `phone`, `address`, `taxCode` không được hiện plaintext.
- Dữ liệu trong bảng phải là ciphertext.

Push và mở PR khi:

- Customer API chạy ổn.
- Schema ổn.
- AES encrypt đúng và đọc lại được dữ liệu.

Sau khi merge:

- Bạn 2 làm Giai đoạn 6.

## Giai đoạn 6: Bạn 2 làm Ticket API

- Người làm: Bạn 2
- Điều kiện bắt đầu: Giai đoạn 5 đã merge
- Branch: `feature/ticket-api`

Bạn 2 làm các việc sau:

- Tạo `TicketRequest`, `TicketResponse`, `TicketStatusUpdateRequest`.
- Tạo `TicketService`, `TicketController`.
- Làm API:
  - `GET /api/tickets`
  - `POST /api/tickets`
  - `GET /api/tickets/{id}`
  - `PUT /api/tickets/{id}`
  - `PATCH /api/tickets/{id}/status`
  - `DELETE /api/tickets/{id}`
- Giữ đúng kiểu `RESTful API`, không tạo endpoint tùy tiện.
- Nếu có file ticket cũ hoặc DTO cũ không dùng nữa, xóa luôn để tránh duplicate flow.

File chính:

- `backend/src/main/java/.../dto/TicketRequest.java`
- `backend/src/main/java/.../dto/TicketResponse.java`
- `backend/src/main/java/.../dto/TicketStatusUpdateRequest.java`
- `backend/src/main/java/.../service/TicketService.java`
- `backend/src/main/java/.../controller/TicketController.java`

Test trước khi push:

```bash
cd backend
mvn -q -DskipTests package
mvn spring-boot:run
```

Test bằng Postman:

1. Tạo ticket mới.
2. Lấy danh sách ticket.
3. Xem chi tiết ticket.
4. Sửa ticket.
5. Update status.
6. Xóa ticket.
7. Gửi `customerId` không tồn tại.

Push và mở PR khi:

- Ticket CRUD chạy ổn.
- Update status chạy đúng.
- Response không lộ field nhạy cảm không cần thiết.

Sau khi merge:

- Bạn 1 làm Giai đoạn 7.

## Giai đoạn 7: Bạn 1 làm Role Authorization

- Người làm: Bạn 1
- Điều kiện bắt đầu: Giai đoạn 4, 5, 6 đã merge
- Branch: `feature/role-authorization`

Bạn 1 làm các việc sau:

- Chốt 3 role: `ADMIN`, `STAFF`, `USER`.
- Cập nhật `SecurityConfig` và các rule truy cập.
- Rule tối thiểu:
  - `/api/auth/**`: public
  - `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`: mở cho test và tài liệu
  - `/api/admin/**`: chỉ `ADMIN`
  - `/api/customers/**`: `ADMIN`, `STAFF`
  - `/api/tickets/**`: `ADMIN`, `STAFF`, `USER` theo phạm vi nhóm đã chốt
- Nếu có `OAuth2`, mở thêm route callback cần thiết.
- Không để `USER` gọi nhầm API quản trị.

File chính:

- `backend/src/main/java/.../config/SecurityConfig.java`
- `backend/src/main/java/.../security/JwtAuthenticationFilter.java`
- `backend/src/main/java/.../entity/Role.java`

Test trước khi push:

Tạo 3 tài khoản mẫu:

- `admin`
- `staff`
- `user`

Test bằng Postman:

1. Không có token phải trả `401`.
2. Token sai phải trả `401`.
3. `USER` gọi `/api/customers` phải trả `403`.
4. `STAFF` gọi customer API phải được phép nếu nhóm chốt như trên.
5. `USER` gọi route admin phải trả `403`.
6. Swagger vẫn mở được.

Push và mở PR khi:

- Matrix phân quyền đã ổn.
- Phân biệt được `401` và `403`.

Sau khi merge:

- Bạn 2 làm Giai đoạn 8.
- Bạn 3 chờ backend ổn định để làm Giai đoạn 9.

## Giai đoạn 8: Bạn 2 làm Audit Log và hardening backend

- Người làm: Bạn 2
- Điều kiện bắt đầu: Giai đoạn 7 đã merge
- Branch: `feature/audit-logging`

Bạn 2 làm các việc sau:

- Ghi log cho các action:
  - login success
  - login failed
  - create customer
  - update customer
  - delete customer
  - create ticket
  - update ticket status
  - delete ticket nếu hệ thống có chức năng này
- Tạo `GET /api/audit-logs`.
- Rà lại backend theo hướng `OWASP API Security Top 10` ở mức cơ bản:
  - Không lộ password hash
  - Không lộ AES secret
  - Không log token đầy đủ
  - Không trả stacktrace kỹ thuật ra client
  - Input có validation
  - Danh sách API trong Swagger phải khớp API thật

File chính:

- `backend/src/main/java/.../service/AuditLogService.java`
- `backend/src/main/java/.../controller/AuditLogController.java`
- `backend/src/main/java/.../entity/AuditLog.java`
- `backend/src/main/java/.../dto/AuditLogResponse.java`
- `backend/src/main/java/.../exception/GlobalExceptionHandler.java`

Test trước khi push:

1. Login đúng phải sinh log.
2. Login sai phải sinh log.
3. Tạo, sửa, xóa customer phải sinh log.
4. Tạo ticket và update status phải sinh log.
5. Gọi `GET /api/audit-logs` lấy được dữ liệu.
6. Kiểm tra log không chứa plaintext password, token đầy đủ hoặc AES key.

Push và mở PR khi:

- Audit log đã đủ để demo luồng bảo mật.
- Response lỗi đã được làm sạch ở mức cần thiết.

Sau khi merge:

- Bạn 3 làm Giai đoạn 9.

## Giai đoạn 9: Bạn 3 nối frontend với API thật

- Người làm: Bạn 3
- Điều kiện bắt đầu: Giai đoạn 3, 4, 5, 6, 7 đã merge
- Branch: `feature/frontend-security-ui`

Bạn 3 làm các việc sau:

- Tạo `axiosClient`.
- Tạo `tokenStorage`.
- Tạo `authService`, `customerService`, `ticketService`.
- Làm các màn hình:
  - login
  - register
  - dashboard
  - customers
  - tickets
- Làm protected route.
- Ẩn hoặc hiện menu theo role nếu cần.
- Xử lý `401` và `403`.
- Không tốn thời gian cho UI đẹp. Chỉ cần nhìn rõ để demo được auth và authorization.
- Xóa mock data, mock service hoặc component cũ không dùng nữa.

File chính:

- `frontend/app/login/page.tsx`
- `frontend/app/register/page.tsx`
- `frontend/app/dashboard/page.tsx`
- `frontend/app/customers/page.tsx`
- `frontend/app/tickets/page.tsx`
- `frontend/services/**`
- `frontend/lib/**`
- `frontend/components/**`

Test trước khi push:

```bash
cd frontend
npm run build
```

Test thực tế:

1. Register thật với backend.
2. Login thật với backend.
3. Vào dashboard khi có token.
4. Bị chặn khi không có token.
5. Gọi customer flow từ UI.
6. Gọi ticket flow từ UI.
7. Tài khoản không đủ role phải thấy lỗi `403`.

Push và mở PR khi:

- Frontend gọi đúng API thật.
- Không còn mock data.
- Có thể demo được login, customer và ticket từ giao diện.

Sau khi merge:

- Bạn 3 làm Giai đoạn 10.

## Giai đoạn 10: Bạn 3 làm Swagger, Postman, ZAP, Docker và HTTPS/TLS

- Người làm: Bạn 3
- Điều kiện bắt đầu: Giai đoạn 8 và 9 đã merge
- Branch: `chore/security-docs-deploy`

Bạn 3 làm các việc sau:

- Kiểm tra Swagger UI đang mở được.
- Tạo Postman collection và environment.
- Chạy `OWASP ZAP` baseline scan cho API local hoặc bản deploy.
- Lưu báo cáo test vào `docs/security/`.
- Tạo hoặc cập nhật:
  - `backend/Dockerfile`
  - `frontend/Dockerfile`
  - `docker-compose.yml`
- Nếu deploy internet:
  - thêm cấu hình reverse proxy trong `deploy/`
  - dùng `HTTPS/TLS`
  - không public backend thuần HTTP
- Nếu chưa deploy internet kịp:
  - vẫn phải chạy được `docker compose`
  - vẫn phải viết rõ cách terminate TLS khi deploy thật

File chính:

- `docs/postman/*.json`
- `docs/api/*`
- `docs/security/*`
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `docker-compose.yml`
- file cấu hình trong `deploy/` nếu có

Test trước khi push:

```bash
docker compose up -d --build
docker compose ps
docker compose logs backend
docker compose logs frontend
docker compose logs database
```

Phải kiểm tra:

1. Swagger mở được.
2. Postman collection chạy được auth, customer, ticket, audit.
3. `OWASP ZAP` có báo cáo lưu lại.
4. Frontend mở được.
5. Backend mở được.
6. Database lên được.
7. Nếu đã deploy internet, kiểm tra `HTTPS/TLS` bằng trình duyệt hoặc:

```bash
curl -vk https://<domain>
```

Push và mở PR khi:

- Tài liệu đang khớp với code hiện tại.
- Docker chạy được.
- Có bằng chứng test bảo mật.

Sau khi merge:

- Cả nhóm regression cuối và chốt MVP.

## 5. Test bảo mật bắt buộc trước khi chốt đồ án

### JWT

- Login thành công nhận được token.
- Không có token gọi API protected phải ra `401`.
- Token sai phải ra `401`.
- Token hết hạn phải ra `401`.

### bcrypt

- Password trong DB không phải plaintext.
- Dữ liệu hash nhìn đúng định dạng `bcrypt`.
- Login vẫn hoạt động bằng password gốc.

### AES

- `phone`, `address`, `taxCode` trong DB không phải plaintext.
- API vẫn trả đúng dữ liệu đã giải mã cho người được phép.
- Không log AES secret.

### Authorization

- `ADMIN`, `STAFF`, `USER` bị chặn hoặc được phép đúng route.
- Có sự khác nhau rõ giữa `401` và `403`.

### OWASP API Security

- Không lộ dữ liệu nhạy cảm trong response.
- Không để route quản trị mở sai role.
- Không để tài liệu API mô tả một kiểu nhưng code chạy một kiểu khác.
- Không trả stacktrace đầy đủ cho client.
- Có log audit cho action chính.

### Swagger, Postman, ZAP

- Swagger mở được.
- Postman collection import được và chạy được.
- Có file báo cáo `ZAP`.

### Docker và HTTPS/TLS

- `docker compose up -d --build` chạy được.
- Nếu deploy internet thì truy cập qua `HTTPS`.

## 6. Checklist bàn giao

| Giai đoạn | Người phụ trách | Điều kiện bắt đầu | Đầu ra bắt buộc | Cách test chính | Người làm tiếp |
|---|---|---|---|---|---|
| 1 | Bạn 1 | Không có | Repo sạch, README, cấu trúc thư mục | `rg --files`, `git status` | Bạn 1, Bạn 3 |
| 2 | Bạn 1 | Giai đoạn 1 merge | Backend base chạy được | `mvn package`, chạy app, mở Swagger | Bạn 1 |
| 3 | Bạn 3 | Giai đoạn 1 merge | Frontend scaffold chạy được | `npm run build` | Bạn 3 |
| 4 | Bạn 1 | Giai đoạn 2 merge | JWT auth, bcrypt, `register/login/me` | Postman auth flow, kiểm tra DB | Bạn 2 |
| 5 | Bạn 2 | Giai đoạn 4 merge | Schema, entity, Customer API, AES | Postman customer flow, kiểm tra DB ciphertext | Bạn 2 |
| 6 | Bạn 2 | Giai đoạn 5 merge | Ticket API | Postman ticket flow | Bạn 1 |
| 7 | Bạn 1 | Giai đoạn 4, 5, 6 merge | Rule role và `401/403` đúng | Test bằng 3 tài khoản | Bạn 2, Bạn 3 |
| 8 | Bạn 2 | Giai đoạn 7 merge | Audit log và hardening | Postman audit flow | Bạn 3 |
| 9 | Bạn 3 | Giai đoạn 3, 4, 5, 6, 7 merge | Frontend gọi API thật | Demo login, customer, ticket | Bạn 3 |
| 10 | Bạn 3 | Giai đoạn 8, 9 merge | Swagger, Postman, ZAP, Docker, HTTPS/TLS | Docker, Swagger, Postman, ZAP | Cả nhóm |

## 7. Nếu thiếu thời gian thì ưu tiên gì

Nếu gần deadline, không được cắt các phần sau:

- `JWT`
- `bcrypt`
- `AES`
- role authorization
- audit log
- Swagger
- Postman
- `OWASP ZAP`
- Docker
- `HTTPS/TLS` khi deploy

Những phần có thể làm tối giản:

- giao diện frontend
- animation
- dashboard đẹp
- filter nâng cao
- báo cáo UI phức tạp

Tóm lại, đồ án này phải nhìn ra ngay trọng tâm bảo mật. Người chấm phải thấy được 4 thứ bằng demo thật: xác thực bằng `JWT`, mật khẩu hash bằng `bcrypt`, dữ liệu nhạy cảm mã hóa bằng `AES`, và hệ thống được test cũng như triển khai theo hướng an toàn.
