# Demo runbook

## 1. Chay stack

```powershell
docker compose up --build -d
```

Mac dinh local demo:

- Frontend: `https://localhost`
- Swagger: `https://localhost/swagger-ui.html`
- Health: `https://localhost/api/health`

## 2. Chay script demo tu dong

```powershell
& .\scripts\demo-security.ps1
```

Script se tu dong demo:

- login `student1`, `student2`, `admin`
- register 1 demo student moi
- kiem tra password bcrypt trong database
- kiem tra phone/billing/payment/certificate dang duoc ma hoa
- demo lesson bi khoa truoc khi thanh toan
- demo webhook HMAC sai / dung / replay
- demo BOLA bi chan `403`
- demo JWT bi sua va JWT het han bi chan `401`
- demo student khong vao duoc admin API
- demo rate limit login tra `429`
- demo admin doc audit logs

## 3. Neu can reset seed data

```powershell
docker compose down -v
docker compose up --build -d
```

## 4. Artifact bo sung

- ZAP baseline: `docs/security/`
- Postman collection: `postman/online-course-security.postman_collection.json`
- Demo markdown: `docs/demo-01-password-bcrypt.md` den `docs/demo-07-https-tls.md`
