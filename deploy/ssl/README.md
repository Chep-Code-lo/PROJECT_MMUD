# TLS deployment notes

Dat file sau vao thu muc nay khi deploy internet that:

- `fullchain.pem`
- `privkey.pem`

Reverse proxy duoc mo ta trong `deploy/nginx/securityapp.conf`.

Muc tieu:

1. Frontend va backend di qua HTTPS.
2. Backend khong bi public truc tiep qua HTTP ngoai internet.
3. Nginx terminate TLS, sau do proxy:
   - `/` -> frontend
   - `/api/*` -> backend
   - `/swagger-ui/*` va `/v3/api-docs/*` -> backend

Neu can test local voi self-signed certificate:

```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 ^
  -keyout deploy/ssl/privkey.pem ^
  -out deploy/ssl/fullchain.pem ^
  -subj "/CN=localhost"
```
