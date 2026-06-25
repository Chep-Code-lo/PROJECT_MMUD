# Demo 02 - JWT authentication

## Muc tieu

- Login nhan access token JWT
- Dung Bearer token goi API protected
- Sua payload token thi backend tu choi
- Token het han se bi tu choi

## Cach demo

1. `POST /api/auth/login`
2. Copy `accessToken`
3. Goi `GET /api/auth/me` voi `Authorization: Bearer <token>`
4. Tamper payload JWT bang jwt.io hoac Postman
5. Goi lai `GET /api/auth/me`

## Ky vong

- Token hop le: `200`
- Token tampered: `401`
- Token het han: `401`

## Giai thich

- JWT duoc ky bang secret phia server
- Sua payload lam chu ky khong con hop le
