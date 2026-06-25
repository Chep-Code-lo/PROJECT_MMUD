# API docs

Swagger/OpenAPI duoc sinh runtime tu backend Spring Boot.

## URL de mo

- `https://localhost:8444/swagger-ui.html`
- `https://localhost:8444/v3/api-docs`

Ghi chu: hai URL nay chi duoc bind vao `127.0.0.1` cua may host. Cac may khac truy cap cong public `443` se khong mo duoc Swagger/OpenAPI.

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
