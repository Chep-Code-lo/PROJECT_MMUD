# CHECKLIST TEST

File nay dung de demo nhanh trong 5-10 phut, tap trung vao dung trong tam mon hoc:

- JWT
- bcrypt
- AES-GCM
- role authorization
- Swagger/OpenAPI
- Postman
- OWASP ZAP

## 1. Khoi dong he thong

Chay:

```powershell
cd E:\PROJECT_MMUD
docker compose up -d --build
docker compose ps
```

Can thay:

- `securityapp-frontend` = `Up`
- `securityapp-backend` = `Up`
- `securityapp-db` = `Up (healthy)`

Neu can log:

```powershell
docker compose logs backend
docker compose logs frontend
docker compose logs database
```

## 2. Tai khoan demo

Dung san 3 tai khoan:

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

## 3. Demo nhanh tren giao dien

Mo:

- Frontend: `http://localhost:3000`
- Swagger: `http://localhost:8080/swagger-ui.html`

### Buoc UI goi y

- [ ] Dang nhap `admin`
- [ ] Vao `Customers`, tao 1 customer moi
- [ ] Vao `Tickets`, tao 1 ticket moi
- [ ] Doi status ticket
- [ ] Vao `Audit Logs`, cho thay action vua phat sinh
- [ ] Dang xuat
- [ ] Dang nhap `user`
- [ ] Thu vao route customer va xac nhan bi chan `403`

Noi khi demo:

- `ADMIN` xem duoc customer, ticket, audit
- `USER` xem duoc ticket nhung khong duoc vao customer hoac audit

## 4. Test API bang Swagger/OpenAPI

Mo `http://localhost:8080/swagger-ui.html`

### Thu tu test

- [ ] `POST /api/auth/login`
- [ ] Copy `accessToken`
- [ ] Bam `Authorize`
- [ ] Nhap `Bearer <accessToken>`
- [ ] `GET /api/auth/me`
- [ ] `GET /api/customers`
- [ ] `POST /api/customers`
- [ ] `GET /api/tickets`
- [ ] `POST /api/tickets`
- [ ] `PATCH /api/tickets/{id}/status`
- [ ] `GET /api/admin/summary`
- [ ] `GET /api/audit-logs`

### Dieu phai nhan manh

- [ ] `register`, `create customer`, `create ticket` tra `201`
- [ ] `delete customer`, `delete ticket` tra `204`
- [ ] Khong co token goi `/api/auth/me` tra `401`
- [ ] `USER` goi `/api/customers` tra `403`

OpenAPI JSON:

- `http://localhost:8080/v3/api-docs`
- file export: `docs/api/openapi.json`

## 5. Test bao mat bang Postman

Import:

- `docs/postman/securityapp.postman_collection.json`
- `docs/postman/securityapp.local.postman_environment.json`

### Thu tu chay nhanh trong Postman

- [ ] `Auth / Login as Admin`
- [ ] `Customers / Create Customer`
- [ ] `Tickets / Create Ticket`
- [ ] `Security Checks / Customers as Staff (Expect 200)`
- [ ] `Security Checks / Tickets as User (Expect 200)`
- [ ] `Security Checks / Customers as User (Expect 403)`
- [ ] `Security Checks / Audit Logs as User (Expect 403)`
- [ ] `Security Checks / Auth Me without Token (Expect 401)`
- [ ] `Audit / List Audit Logs`

## 6. Chay tu terminal bang Newman

Neu muon co bang chung chay tu dong:

```powershell
npx --yes newman run docs\postman\securityapp.postman_collection.json -e docs\postman\securityapp.local.postman_environment.json --reporters cli
```

Ky vong:

- `requests` > 0
- `assertions` pass
- `failed = 0`

## 7. Chung minh phan mat ma hoc trong database

### 7.1. bcrypt

Chay:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,email,password_hash,role FROM users;"
```

Can thay:

- `password_hash` co dang `bcrypt`, thuong bat dau bang `$2a$`
- khong co plaintext password

### 7.2. AES ciphertext

Chay:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,name,email,phone_encrypted,address_encrypted,tax_code_encrypted FROM customers;"
```

Can thay:

- `email` doc duoc
- `phone_encrypted`, `address_encrypted`, `tax_code_encrypted` la chuoi ma hoa
- khong lo plaintext so dien thoai, dia chi, ma so thue

### 7.3. audit log

Chay:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,action,actor_email,success,created_at FROM audit_logs ORDER BY id DESC LIMIT 10;"
```

Can thay:

- `LOGIN_SUCCESS`
- `LOGIN_FAILED`
- `CREATE_CUSTOMER`
- `CREATE_TICKET`
- `UPDATE_TICKET_STATUS`

## 8. Test OWASP ZAP

Artifact co san:

- `docs/security/zap-baseline-report.html`
- `docs/security/zap-baseline-report.json`
- `docs/security/zap-baseline-report.xml`

Neu muon chay lai:

```powershell
docker run --rm -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

Noi khi demo:

- target baseline la `http://host.docker.internal:8080/swagger-ui.html`
- ZAP dung de quet surface public
- API co JWT duoc bo sung test bang Postman/Newman

Ket qua hien tai:

- `PASS: 59`
- `WARN: 2`
- `FAIL: 0`

## 9. Ban ket luan nen noi

Khi chot demo, co the noi gon:

1. He thong xac thuc bang `JWT`.
2. Password duoc hash bang `bcrypt`, khong luu plaintext.
3. Du lieu nhay cam cua customer duoc ma hoa bang `AES-GCM` truoc khi luu DB.
4. He thong co role `ADMIN`, `STAFF`, `USER` va phan biet ro `401` voi `403`.
5. API da duoc tai lieu hoa bang `Swagger/OpenAPI`.
6. API da duoc kiem thu bang `Postman/Newman` va quet bao mat bang `OWASP ZAP`.

## 10. Neu bi hoi "mo file nao"

- Huong dan tong: `README.md`
- Swagger/OpenAPI: `docs/api/README.md`
- Postman: `docs/postman/README.md`
- ZAP: `docs/security/README.md`
