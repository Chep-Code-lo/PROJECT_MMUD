# OWASP ZAP testing guide

## 1. Muc tieu scan

- Frontend local: `https://localhost`
- Swagger UI: `https://localhost/swagger-ui.html`
- API docs: `https://localhost/v3/api-docs`

## 2. Chay ZAP baseline bang Docker

Tren Windows, neu ZAP chay trong container va stack chay tren host:

```powershell
docker run --rm -t ghcr.io/zaproxy/zaproxy:stable `
  zap-baseline.py `
  -t https://host.docker.internal/swagger-ui.html `
  -r zap-report.html `
  -J zap-report.json `
  -z "-config connection.timeoutInSecs=120 -config api.disablekey=true"
```

Neu mo ZAP tren may host, scan truc tiep `https://localhost/swagger-ui.html`.

## 3. Dieu can quan sat

- HTTP co bi redirect sang HTTPS hay khong
- Security headers co ton tai hay khong
- Cac endpoint protected co tra `401` / `403` hop ly hay khong
- CORS co bi mo qua rong hay khong
- Stacktrace co lo ra ngoai hay khong

## 4. Cach doc ket qua

- `FAIL`: can sua ngay
- `WARN`: can giai trinh trong bao cao
- `PASS`: co che bao ve hoat dong

## 5. Cach chung minh project co xu ly bao mat

- BOLA bi chan boi server-side ownership check
- JWT tampered bi tu choi
- Password khong luu plaintext
- Webhook sai signature bi tu choi
- Rate limit tra `429`
- Nginx buoc HTTPS
