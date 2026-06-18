# Giai doan 10: Giai thich chi tiet code va y tuong

Stage 10 la buoc chot he thong de co the demo tron ven: co Swagger, co Postman, co ZAP, co Docker Compose va co huong dan TLS/deploy that.

## 1. Muc tieu cua Stage 10

- Swagger/OpenAPI phai khop code that
- Co Postman collection va environment
- Co OWASP ZAP artifact luu lai
- Docker Compose phai boot duoc day du
- Co cau hinh reverse proxy/TLS de trinh bay huong deploy an toan

## 2. Cac file chinh da lam

- `docs/api/openapi.json`
- `docs/api/README.md`
- `docs/postman/securityapp.postman_collection.json`
- `docs/postman/securityapp.local.postman_environment.json`
- `docs/postman/README.md`
- `docs/security/zap.yaml`
- `docs/security/zap-baseline-report.html`
- `docs/security/zap-baseline-report.json`
- `docs/security/zap-baseline-report.xml`
- `docs/security/README.md`
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `docker-compose.yml`
- `deploy/nginx/securityapp.conf`
- `deploy/ssl/README.md`

## 3. Swagger va OpenAPI duoc chot the nao

Backend mo cong khai:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

`docs/api/openapi.json` da duoc regenerate lai ngay `2026-06-18` tu backend dang chay de sua stale server URL tu `8084` ve dung `8080`.

Dieu nay quan trong vi:

- Postman import khong bi lech port
- Tai lieu nop bai khop code that
- ZAP va demo thu cong khong bi nham endpoint

## 4. Postman collection duoc thiet ke theo huong nao

Collection duoc chia de demo nhanh cac luong:

- auth
- customers
- tickets
- audit
- admin

Request login se tu dong luu `token`.
Request tao customer se luu `customerId`.
Request tao ticket se luu `ticketId`.

Nghia la nguoi demo co the chay lien mach ma khong phai copy tay ID qua lai nhieu lan.

## 5. OWASP ZAP da duoc xu ly ra sao

`docs/security/zap.yaml` da duoc cap nhat lai dung target hien tai:

```text
http://host.docker.internal:8080/swagger-ui.html
```

Lan scan moi nhat ngay `2026-06-18` cho ket qua:

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

Hai warning hien tai:

- `Content Security Policy (CSP) Header Not Set [10038]`
- `Modern Web Application [10109]`

Quan trong:

- khong co `FAIL-NEW`
- artifact HTML/JSON/XML da luu lai day du trong `docs/security/`

## 6. Docker Compose da duoc chot the nao

Stack gom 3 service:

- `database`: `mysql:8.4`
- `backend`: Spring Boot app
- `frontend`: Next.js app

Port expose:

- `3000 -> frontend`
- `8080 -> backend`
- `3307 -> 3306` cua MySQL

### Loi quan trong da sua

Ban compose cu dung:

```text
--default-authentication-plugin=mysql_native_password
```

Nhung `mysql:8.4` khong con chap nhan option nay.

Ket qua la database fail ngay luc start.

Ban compose hien tai da bo option loi thoi do, sau do stack boot lai thanh cong.

## 7. Dockerfile backend va frontend

### Backend

- multi-stage build voi Maven 17
- stage runtime dung `eclipse-temurin:17-jre`

### Frontend

- install dependency
- build Next.js
- copy `.next`, `public`, `node_modules` vao runner image

No du de chay demo local bang compose ma khong can cai dat tay tren may khac.

## 8. Reverse proxy va TLS

Stage 10 khong chi dung o local. Repo con de san:

- `deploy/nginx/securityapp.conf`
- `deploy/ssl/README.md`

Y tuong deploy that:

- frontend/backend dat sau reverse proxy
- TLS terminate tai Nginx
- backend khong public thuan HTTP ra internet

Day la cach noi ket yeu cau mon hoc ve HTTPS/TLS voi stack hien tai.

## 9. Kiem tra thuc te da chay

Ngay `2026-06-18`, stack da duoc verify lai bang:

```bash
docker compose up -d --build
docker compose ps
docker compose logs backend
docker compose logs frontend
docker compose logs database
```

Ket qua:

- database `healthy`
- backend `Up`
- frontend `Up`

Smoke test API tren stack Docker cung da chay:

- login admin thanh cong
- goi `/api/auth/me`
- goi `/api/admin/summary`
- tao customer
- tao ticket
- patch ticket sang `PROCESSING`
- login user va goi `/api/customers` nhan `403`

No chung minh Stage 10 khong chi la viet tai lieu, ma da verify duoc deployment flow that.
