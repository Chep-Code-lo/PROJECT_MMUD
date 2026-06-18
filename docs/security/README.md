# Security docs

Thu muc nay chua artifact bao mat cho Stage 10, tap trung vao OWASP ZAP baseline va ghi chu ve TLS/deploy.

## File chinh

- `zap.yaml`: automation plan dung de quet
- `zap-baseline-report.html`
- `zap-baseline-report.json`
- `zap-baseline-report.xml`

## Muc tieu scan

Lan scan moi nhat duoc chay lai ngay `2026-06-18` vao:

```text
http://host.docker.internal:8080/swagger-ui.html
```

Ly do chon target nay:

- Swagger UI la surface public, de truy cap va phu hop cho baseline spider.
- Authenticated API da duoc cover bo sung bang integration test va Postman, vi baseline spider khong tu login JWT.

## Lenh chay

```bash
docker run --rm -v "<repo>/docs/security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

## Ket qua moi nhat

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

Hai warning hien tai:

- `Content Security Policy (CSP) Header Not Set [10038]`
- `Modern Web Application [10109]`

## Cach hieu ket qua

- Warning `CSP` den tu trang Swagger UI public, khong phai do lo JWT hay lo AES secret.
- `Modern Web Application` la nhan dien kieu ung dung web, khong phai lo hong nghiem trong.
- Khong co `FAIL-NEW` trong lan scan moi nhat.

## Lien he voi deploy/TLS

- Reverse proxy mau nam o `deploy/nginx/securityapp.conf`
- Ghi chu terminate TLS nam o `deploy/ssl/README.md`
- Khi deploy that, khong nen public backend thuan HTTP ra internet. Nen dat backend sau reverse proxy HTTPS.
