# API docs

Thu muc nay chua tai lieu OpenAPI/Swagger cua backend that.

## File chinh

- `openapi.json`: ban export tu `GET /v3/api-docs`

## URL local

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Nhom endpoint hien co

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET/POST/PUT/DELETE /api/customers`
- `GET/POST/PUT/DELETE /api/tickets`
- `PATCH /api/tickets/{id}/status`
- `GET /api/audit-logs`
- `GET /api/admin/summary`
- `GET /api/health`

## Matrix role tom tat

- Public: `POST /api/auth/register`, `POST /api/auth/login`, Swagger, health
- Authenticated: `GET /api/auth/me`
- `ADMIN`: `/api/admin/**`, `/api/audit-logs/**`
- `ADMIN`, `STAFF`: `/api/customers/**`
- `ADMIN`, `STAFF`, `USER`: `/api/tickets/**`

## Demo accounts

- `admin@securityapp.local` / `Password@123`
- `staff@securityapp.local` / `Password@123`
- `user@securityapp.local` / `Password@123`

## Cach regenerate `openapi.json`

Chay backend, sau do export lai file:

```bash
Invoke-WebRequest -Uri "http://localhost:8080/v3/api-docs" -OutFile "docs/api/openapi.json"
```

File `openapi.json` da duoc regenerate lai theo stack hien tai ngay `2026-06-18` de server URL dung la `http://localhost:8080`.

OpenAPI hien tai da phan anh dung cac status code chinh cua runtime nhu:

- `201` cho `register`, `create customer`, `create ticket`
- `204` cho `delete customer`, `delete ticket`

Luu y:

- OpenAPI duoc dung de tai lieu hoa endpoint, schema va flow co ban.
- Cac bai test security ve `401`, `403`, role matrix nen duoc doi chieu them bang Postman/Newman va ZAP theo tai lieu trong `docs/postman/` va `docs/security/`.
