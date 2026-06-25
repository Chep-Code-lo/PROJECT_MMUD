# OWASP ZAP testing guide

## 1. Muc tieu scan

- Frontend local: `https://localhost`
- Swagger UI local-only: `https://localhost:8444/swagger-ui.html`
- API docs local-only: `https://localhost:8444/v3/api-docs`

## 2. Cach chay phu hop voi cau hinh hien tai

Do Swagger chi bind vao `127.0.0.1` cua may host, may khac va container khac se khong truy cap duoc cong nay.

Khuyen nghi:

- Chay OWASP ZAP Desktop tren chinh may host va scan `https://localhost:8444/swagger-ui.html`.
- Neu muon quet bang container ZAP, can tam thoi doi cau hinh bind cong Swagger de phuc vu kiem thu noi bo.

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
