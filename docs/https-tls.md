# HTTPS / TLS setup

Project nay uu tien demo TLS theo cach 2: Nginx reverse proxy terminate TLS.

## 1. Tai sao can TLS

- Bao ve JWT, password va du lieu API khi truyen tren mang
- Chan nghe len plaintext neu chi dung HTTP
- Tao boi canh de test an toan API qua `https://localhost`

## 2. File lien quan

- `deploy/nginx/securityapp.conf`
- `deploy/ssl/fullchain.pem`
- `deploy/ssl/privkey.pem`
- `docker-compose.yml`

## 3. Tao cert self-signed local

```powershell
openssl req -x509 -nodes -days 365 -newkey rsa:2048 `
  -keyout deploy/ssl/privkey.pem `
  -out deploy/ssl/fullchain.pem `
  -subj "/CN=localhost"
```

Neu khong co `openssl`, co the dung `mkcert` hoac cert cong ty/giang vien cap.

## 4. Chay demo local qua HTTPS

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Sau do truy cap:

- `https://localhost`
- `https://localhost:8444/swagger-ui.html` tren chinh may host
- `https://localhost/api/health`

Luu y:

- Cong `443` chi phuc vu frontend va API cho nguoi dung.
- Swagger/OpenAPI duoc tach ra cong `8444` va chi bind local host de tranh lo tai lieu API ra ben ngoai.

## 5. Chung minh HTTP -> HTTPS

```powershell
curl.exe -I http://localhost/api/health
curl.exe -k https://localhost/api/health
```

Ky vong:

- lenh HTTP tra `301`
- lenh HTTPS tra `200`

## 6. Khi deploy internet

- Dung reverse proxy co cert hop le, vi du Nginx + Let's Encrypt
- Hoac dat sau cloud load balancer / reverse proxy cua nha cung cap
- Khong public backend HTTP thuan ra internet
