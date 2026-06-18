# Cloud API-Based Network Application Security for Small Company Services

Monorepo nay duoc to chuc de demo do an MMUD theo huong `REST API + mat ma ung dung + bao mat API`. Trong nhanh hien tai, trong tam thuc te la:

- `JWT` cho authentication
- `bcrypt` cho password hashing
- `AES-GCM` cho du lieu nhay cam cua customer
- `Role Authorization` cho `ADMIN`, `STAFF`, `USER`
- `Audit Log` de truy vet hanh dong
- `Swagger/OpenAPI`, `Postman`, `OWASP ZAP` de ho tro tai lieu va kiem thu
- `Docker Compose` va ghi chu `HTTPS/TLS` de phuc vu demo/deploy

`spring-boot-starter-oauth2-client` dang co trong `backend/pom.xml` de mo rong neu mon hoc bat buoc cham `OAuth2`, nhung luong demo hien tai su dung `JWT local auth` la chinh.

## 1. Cau truc repo

- `backend/`: Spring Boot API, Spring Security, JWT, AES, bcrypt, audit log
- `frontend/`: Next.js UI goi API that
- `database/`: `schema.sql` va ghi chu seed/reset
- `docs/api/`: OpenAPI export va huong dan Swagger
- `docs/postman/`: collection, environment, huong dan Postman/Newman
- `docs/security/`: ZAP plan, report HTML/JSON/XML, security notes
- `deploy/`: reverse proxy Nginx va ghi chu TLS

Main package:

```text
com.company.securityapp
```

## 2. Cac luong bao mat dang co

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

Password khong luu plaintext. He thong hash bang `BCryptPasswordEncoder`.

### AES Encryption

Customer co 3 truong nhay cam duoc ma hoa bang `AES-GCM` truoc khi luu DB:

- `phone`
- `address`
- `taxCode`

Trong database, cac cot luu that la:

- `phone_encrypted`
- `address_encrypted`
- `tax_code_encrypted`

### Authorization matrix

| Route | Quyen |
|---|---|
| `/api/auth/register`, `/api/auth/login`, Swagger, health | Public |
| `/api/auth/me` | Da dang nhap |
| `/api/admin/**` | `ADMIN` |
| `/api/audit-logs/**` | `ADMIN` |
| `/api/customers/**` | `ADMIN`, `STAFF` |
| `/api/tickets/**` | `ADMIN`, `STAFF`, `USER` |

### Audit log

He thong dang ghi log cho:

- login success
- login failed
- create/update/delete customer
- create/update/delete ticket
- update ticket status

## 3. Port mac dinh

- `frontend`: `3000`
- `backend`: `8080`
- `database`: `3307` tren host, `3306` trong container

## 4. Demo accounts

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

## 5. Cach chay khuyen nghi: Docker Compose

Day la cach phu hop nhat de test du an vi no len day du `frontend + backend + MySQL`.

```powershell
cd E:\PROJECT_MMUD
docker compose up -d --build
docker compose ps
```

Neu can doc log:

```powershell
docker compose logs backend
docker compose logs frontend
docker compose logs database
```

URL sau khi len:

- Frontend: `http://localhost:3000`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/api/health`

Dung he thong:

```powershell
docker compose down
```

Neu muon xoa ca volume database:

```powershell
docker compose down -v
```

## 6. Chay local khong dung Docker

### Backend

Test nhanh bang profile H2:

```powershell
cd backend
mvn test
```

Chay backend voi MySQL:

```powershell
cd backend
mvn clean package -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Can config cac bien sau neu khong dung compose:

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

Frontend mac dinh goi ve `http://localhost:8080` neu `NEXT_PUBLIC_API_URL` khong duoc set.

## 7. Kiem thu bang Swagger/OpenAPI

Muc tieu cua Swagger trong bai nay:

- tai lieu hoa API
- test nhanh request/response
- doi chieu status code va schema that

### Buoc test

1. Mo `http://localhost:8080/swagger-ui.html`
2. Goi `POST /api/auth/login` bang tai khoan `admin`
3. Copy `accessToken`
4. Bam `Authorize`
5. Nhap:

```text
Bearer <accessToken>
```

6. Test cac nhom API:

- `GET /api/auth/me`
- `GET /api/customers`
- `POST /api/customers`
- `GET /api/tickets`
- `POST /api/tickets`
- `PATCH /api/tickets/{id}/status`
- `GET /api/admin/summary`
- `GET /api/audit-logs`

### Dieu can kiem tra

- `register` va `create customer/ticket` tra `201`
- `delete customer/ticket` tra `204`
- `me` khong co token thi `401`
- `user` goi customer API thi `403`

OpenAPI export moi nhat nam o:

- [docs/api/openapi.json](/E:/PROJECT_MMUD/docs/api/openapi.json)
- [docs/api/README.md](/E:/PROJECT_MMUD/docs/api/README.md)

## 8. Kiem thu bao mat API bang Postman

Thu muc lien quan:

- [docs/postman/securityapp.postman_collection.json](/E:/PROJECT_MMUD/docs/postman/securityapp.postman_collection.json)
- [docs/postman/securityapp.local.postman_environment.json](/E:/PROJECT_MMUD/docs/postman/securityapp.local.postman_environment.json)
- [docs/postman/README.md](/E:/PROJECT_MMUD/docs/postman/README.md)

### Import va chay trong Postman

1. Import collection
2. Import environment `Security App Local`
3. Chon environment
4. Chay collection theo thu tu co san

Collection hien tai da duoc sua de:

- giu `admin token` cho luong CRUD chinh
- luu rieng `staffToken` va `userToken`
- chay tron luong customer/ticket khong bi sai thu tu cleanup
- co folder `Security Checks` de test `401`, `403`, `STAFF=200`, `USER=200`

### Chay tu terminal bang Newman

```powershell
npx --yes newman run docs\postman\securityapp.postman_collection.json -e docs\postman\securityapp.local.postman_environment.json --reporters cli
```

Collection da duoc verify thanh cong bang Newman ngay `2026-06-18`.

### Cac case bao mat nen test

- `Auth Me without Token (Expect 401)`
- `Customers as Staff (Expect 200)`
- `Tickets as User (Expect 200)`
- `Customers as User (Expect 403)`
- `Audit Logs as User (Expect 403)`

## 9. Kiem thu bao mat bang OWASP ZAP

Thu muc lien quan:

- [docs/security/zap.yaml](/E:/PROJECT_MMUD/docs/security/zap.yaml)
- [docs/security/zap-baseline-report.html](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.html)
- [docs/security/zap-baseline-report.json](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.json)
- [docs/security/zap-baseline-report.xml](/E:/PROJECT_MMUD/docs/security/zap-baseline-report.xml)
- [docs/security/README.md](/E:/PROJECT_MMUD/docs/security/README.md)

### Cach chay lai ZAP baseline

Yeu cau: backend dang chay o `http://localhost:8080`.

```powershell
docker run --rm -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

Target baseline hien tai:

```text
http://host.docker.internal:8080/swagger-ui.html
```

### Cach hieu

- ZAP baseline duoc dung cho surface public de spider duoc
- API co JWT duoc bo sung bang Postman/Newman va integration test
- Muc tieu la co bang chung quet bao mat, khong phai thay the het test xac thuc/phan quyen

Lan scan moi nhat:

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

## 10. Kiem chung phan mat ma hoc trong database

### Kiem tra bcrypt hash

Chay:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Ky vong:

- cot `password_hash` bat dau bang dang hash `bcrypt` nhu `$2a$...`
- khong thay password goc

### Kiem tra AES ciphertext

Tao it nhat 1 customer roi chay:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,name,email,phone_encrypted,address_encrypted,tax_code_encrypted FROM customers;"
```

Ky vong:

- `email` doc duoc
- `phone_encrypted`, `address_encrypted`, `tax_code_encrypted` la chuoi ciphertext
- khong thay plaintext nhu so dien thoai hay dia chi goc

### Kiem tra audit log

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,action,actor_email,success,created_at FROM audit_logs ORDER BY id DESC LIMIT 10;"
```

Ky vong:

- thay `LOGIN_SUCCESS`, `LOGIN_FAILED`, `CREATE_CUSTOMER`, `CREATE_TICKET`, `UPDATE_TICKET_STATUS`
- khong thay full JWT hoac AES secret trong log

## 11. Kiem tra nhanh bang giao dien

Mo `http://localhost:3000/login` va test:

1. Dang nhap `admin`
2. Vao `Customers`, tao customer moi
3. Vao `Tickets`, tao ticket moi va doi status
4. Vao `Audit Logs`, xem cac action vua phat sinh
5. Dang xuat
6. Dang nhap `user`
7. Thu vao route customer va xac nhan bi chan `403`

## 12. Tai lieu quan trong

- [docs/api/README.md](/E:/PROJECT_MMUD/docs/api/README.md)
- [docs/postman/README.md](/E:/PROJECT_MMUD/docs/postman/README.md)
- [docs/security/README.md](/E:/PROJECT_MMUD/docs/security/README.md)
- [deploy/nginx/securityapp.conf](/E:/PROJECT_MMUD/deploy/nginx/securityapp.conf)
- [deploy/ssl/README.md](/E:/PROJECT_MMUD/deploy/ssl/README.md)

## 13. Ghi chu deploy va TLS

Repo da co:

- reverse proxy Nginx trong `deploy/nginx/securityapp.conf`
- ghi chu TLS trong `deploy/ssl/README.md`

Khi deploy that:

- khong nen public backend thuan HTTP ra internet
- nen terminate TLS o reverse proxy
- backend va frontend nen dat sau Nginx/HTTPS
