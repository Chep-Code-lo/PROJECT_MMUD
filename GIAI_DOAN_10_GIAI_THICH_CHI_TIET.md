# Giai doan 10: Giai thich chi tiet code va y tuong

Stage 10 la buoc chot he thong de co the demo tron ven: co Swagger, co Postman, co ZAP, co Docker Compose va co huong dan TLS/deploy that.

## 1. Muc tieu cua stage 10

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

Swagger duoc giu public de:

- de demo nhanh
- de test request/response
- de doi chieu schema voi Postman

`openapi.json` la ban export tu backend dang chay, dung de nop kem bai va doi chieu contract.

## 4. Postman collection duoc thiet ke theo huong nao

Collection duoc chia de demo nhanh cac luong:

- auth
- admin summary
- customers
- audit
- security checks

Request login se tu dong luu `token`.
Request tao customer se luu `customerId`.

Nghia la nguoi demo co the chay lien mach ma khong phai copy tay ID qua lai nhieu lan.

## 5. OWASP ZAP da duoc xu ly ra sao

`docs/security/zap.yaml` duoc dat target:

```text
http://host.docker.internal:8080/swagger-ui.html
```

Ly do:

- Swagger UI la surface public
- baseline spider truy cap duoc
- authenticated API da duoc cover bo sung bang Postman/Newman va integration test

## 6. Docker Compose va deploy note dong vai tro gi

`docker-compose.yml` giup bat:

- frontend
- backend
- MySQL

Day la cach demo nhanh nhat va on dinh nhat.

`deploy/nginx/securityapp.conf` va `deploy/ssl/README.md` duoc giu de giai thich huong deploy an toan:

- backend khong nen public HTTP truc tiep
- nen dat sau reverse proxy
- TLS nen terminate o Nginx

## 7. Tai sao stage 10 quan trong voi mon hoc

Mon hoc khong chi cham code chay.
No con cham:

- API co tai lieu hay khong
- co bang chung test hay khong
- co security scan hay khong
- co cach deploy an toan hay khong

Stage 10 la noi dong goi cac bang chung do.

## 8. Cach tom tat khi thuyet trinh

> Stage 10 cua em la buoc dong goi he thong de nop va demo: em co Swagger/OpenAPI de tai lieu hoa API, Postman/Newman de kiem thu role va auth, ZAP de quet surface public, Docker Compose de chay tron bo, va Nginx/TLS note de trinh bay huong deploy an toan.
