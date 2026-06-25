# API docs

Swagger/OpenAPI duoc sinh runtime tu backend Spring Boot.

## URL de mo

- `https://localhost/swagger-ui.html`
- `https://localhost/v3/api-docs`

## Cac nhom API chinh

- Auth API
- Course API
- Lesson API
- Enrollment API
- Certificate API
- Admin API
- Webhook API

## Cach test Bearer JWT

1. `POST /api/auth/login`
2. Copy `accessToken`
3. Bam `Authorize`
4. Nhap `Bearer <accessToken>`

## Ghi chu

Khong commit file `openapi.json` tinh duoc export tu ban cu. Hay dung tai lieu OpenAPI sinh runtime de tranh lech so voi code hien tai.
