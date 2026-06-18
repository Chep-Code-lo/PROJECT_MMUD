# Huong dan lam viec nhom theo scope hien tai

## 1. Muc tieu tai lieu

Day la tai lieu giao viec va doi chieu scope cho nhom 3 nguoi.
Ban nay da duoc cap nhat theo huong tap trung vao:

- `RESTful API` voi `Spring Boot`
- `Next.js` de goi API that
- `JWT` cho xac thuc
- `AES-GCM` cho du lieu nhay cam
- `bcrypt` cho mat khau
- `Role Authorization`
- `Audit Log`
- `Swagger/OpenAPI`
- `Postman/Newman`
- `OWASP ZAP`
- `Docker Compose`
- `HTTPS/TLS` khi trien khai

UI khong phai phan chinh. Frontend chi can du de chung minh:

- login
- register
- protected route
- customer flow
- audit flow
- loi `401` va `403`

## 2. Dau ra thuc hanh toi thieu

Project phai chung minh duoc:

- Backend Spring Boot chay duoc local.
- Frontend Next.js goi duoc API that.
- Mat khau trong DB la hash `bcrypt`.
- Du lieu customer nhay cam trong DB la ciphertext `AES-GCM`.
- Swagger mo duoc.
- Postman collection chay duoc.
- Co ket qua test bang `OWASP ZAP`.
- He thong chay duoc bang `docker compose`.
- Ban demo cuoi co ghi chu ro ve `HTTPS/TLS`.

## 3. Phan cong vai tro

### Ban 1

Tap trung vao:

- auth
- JWT
- bcrypt
- role authorization
- security config

### Ban 2

Tap trung vao:

- customer domain
- AES encryption
- customer API
- audit log
- backend hardening

### Ban 3

Tap trung vao:

- frontend Next.js
- Swagger/OpenAPI artifact
- Postman/Newman
- OWASP ZAP
- Docker va TLS note

## 4. Luong phu thuoc chinh

Thu tu hop ly cua du an:

1. Chot cau truc repo va README.
2. Chot backend base.
3. Hoan thien auth va JWT.
4. Xay customer domain va AES.
5. Chot customer CRUD contract.
6. Khoa role authorization.
7. Them audit log va hardening backend.
8. Noi frontend vao API that.
9. Tao Swagger/Postman/ZAP artifact.
10. Dong goi Docker va ghi chu TLS/deploy.

## 5. Stage hien tai duoc hieu lai nhu the nao

### Giai doan 5

- `Customer` schema
- `AuditLog` schema
- `EncryptionService`
- CRUD customer co AES-GCM

### Giai doan 6

- chot request/response contract cua customer
- validation
- status code
- integration test cho customer API

### Giai doan 7

- `User`, `Role`
- `register`, `login`, `me`
- `JwtService`, `JwtAuthenticationFilter`
- role matrix trong `SecurityConfig`

### Giai doan 8

- `AuditLogService`
- `AuditLogController`
- log cho `LOGIN_SUCCESS`, `LOGIN_FAILED`, `CREATE_CUSTOMER`, `UPDATE_CUSTOMER`, `DELETE_CUSTOMER`
- response loi ro rang

### Giai doan 9

- frontend bo mock data
- login/register goi backend that
- protected route
- customer page
- audit log page

### Giai doan 10

- `docs/api/openapi.json`
- Postman collection va environment
- OWASP ZAP artifact
- Docker Compose
- Nginx/TLS note

## 6. Matrix route can ghi nho

| Route | Quyen |
|---|---|
| `/api/auth/register`, `/api/auth/login` | Public |
| `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`, `/api/health` | Public |
| `/api/auth/me` | Chi can da dang nhap |
| `/api/admin/**` | `ADMIN` |
| `/api/audit-logs/**` | `ADMIN` |
| `/api/customers/**` | `ADMIN`, `STAFF` |

## 7. File quan trong can giu dong bo

- `README.md`
- `CHECKLIST_TEST.md`
- `database/schema.sql`
- `database/seed.sql`
- `docs/api/openapi.json`
- `docs/postman/securityapp.postman_collection.json`
- `docs/postman/securityapp.local.postman_environment.json`
- `docs/security/zap.yaml`
- `docs/security/zap-baseline-report.*`

Neu code va tai lieu lech nhau, phai sua trong cung mot dot lam viec.

## 8. Rule lam viec chung

- Khong code truc tiep tren `main`.
- Moi phan viec dung branch rieng.
- Chi mo PR khi build/test phan minh da pass.
- Khong giu lai placeholder, route gia, service gia neu no khong con nam trong scope.
- Khong xoa code dang chay that chi de repo trong gon hon.
- Moi thay doi lien quan den API phai cap nhat lai docs va artifact test.

## 9. Cac bai test toi thieu truoc khi giao

### Backend

```bash
cd backend
mvn test
```

### Frontend

```bash
cd frontend
npm run build
```

### Postman/Newman

```bash
npx --yes newman run docs/postman/securityapp.postman_collection.json -e docs/postman/securityapp.local.postman_environment.json --reporters cli
```

## 10. Cach tom tat khi nop bai

> Du an hien tai tap trung vao huong mat ma ung dung: xac thuc bang JWT, hash mat khau bang bcrypt, ma hoa du lieu customer bang AES-GCM, khoa role bang Spring Security, ghi audit log cho auth va customer action, va co bo tai lieu/kiem thu day du bang Swagger, Postman, ZAP, Docker va TLS note.
