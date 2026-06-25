# Demo 02: JWT, Bearer Token, Refresh Token và Logout

## 1. Mục tiêu

Demo này nhằm chứng minh:

1. Sau khi đăng nhập, backend phát hành `accessToken` dạng JWT.
2. API protected chỉ chấp nhận `Authorization: Bearer <token>`.
3. Nếu token bị sửa payload, sai chữ ký hoặc hết hạn thì backend sẽ từ chối.
4. Refresh token dùng để xin access token mới.
5. Logout thực chất là thu hồi refresh token ở phía server.

## 2. Chuẩn bị

### 2.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 2.2. Tài khoản nên dùng

- `student1@example.com / Password123!`

### 2.3. Thực hiện ở đâu

Demo này nên dùng:

- `Swagger` để đăng nhập, gọi `me`, `refresh`, `logout`:

```text
https://localhost/swagger-ui.html
```

- `PowerShell` để:
  - giải mã payload JWT ngay trên máy local
  - chỉnh thời gian hết hạn trong `.env`
  - hoặc thử token đã bị sửa payload

## 3. Các bước thực hiện chi tiết

### Bước 1: Đăng nhập để lấy JWT

Trên Swagger, vào `Auth API`, mở:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "student1@example.com",
  "password": "Password123!"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- response có:
  - `accessToken`
  - `refreshToken`
  - `tokenType`
  - `accessTokenExpiresInSeconds`
  - `refreshTokenExpiresInSeconds`
  - `role`
  - `scope`

### Bước 2: Giải mã payload JWT ngay trên máy local

Sau khi có `accessToken`, mở PowerShell và thay `<ACCESS_TOKEN>` bằng token thật:

```powershell
$token = "<ACCESS_TOKEN>"
$payloadPart = $token.Split(".")[1]
$padding = "=" * ((4 - $payloadPart.Length % 4) % 4)
$payloadJson = [System.Text.Encoding]::UTF8.GetString(
  [Convert]::FromBase64String(($payloadPart + $padding).Replace("-", "+").Replace("_", "/"))
)
$payloadJson
```

Kết quả mong đợi:

- payload hiển thị được các claim như:
  - `sub`
  - `email`
  - `role`
  - `scope`
  - `token_type`
  - `iat`
  - `exp`

Điểm cần nói:

- `sub` là user id
- `iat` là thời điểm phát hành token
- `exp` là thời điểm hết hạn token

### Bước 3: Dùng Bearer token để gọi API bảo vệ

Trong Swagger:

1. Bấm `Authorize`
2. Nhập:

```text
Bearer <access-token>
```

3. Bấm `Authorize`
4. Đóng hộp thoại

Sau đó gọi:

```http
GET /api/auth/me
```

Kết quả mong đợi:

- HTTP `200 OK`
- response trả đúng thông tin của `student1@example.com`

### Bước 4: Gọi API khi không có token

Trong Swagger:

1. Bấm lại `Authorize`
2. Chọn `Logout`
3. Gọi lại:

```http
GET /api/auth/me
```

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
Authentication is required.
```

### Bước 5: Thử token sai định dạng Bearer

Phần này nên làm bằng PowerShell vì Swagger luôn tự thêm đúng tiền tố `Bearer`.

```powershell
curl.exe -k -i https://localhost/api/auth/me -H "Authorization: <ACCESS_TOKEN>"
```

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
Authorization header must use Bearer token format.
```

### Bước 6: Thử token bị sửa payload

Đây là phần rất quan trọng của demo JWT.

Mở PowerShell và chạy:

```powershell
$login = curl.exe -k -s https://localhost/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"student1@example.com","password":"Password123!"}' | ConvertFrom-Json

$parts = $login.accessToken.Split(".")
$payloadPart = $parts[1]
$padding = "=" * ((4 - $payloadPart.Length % 4) % 4)
$payloadJson = [System.Text.Encoding]::UTF8.GetString(
  [Convert]::FromBase64String(($payloadPart + $padding).Replace("-", "+").Replace("_", "/"))
)
$payload = $payloadJson | ConvertFrom-Json
$payload.role = "ADMIN"
$newPayloadJson = $payload | ConvertTo-Json -Compress
$newPayload = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($newPayloadJson)).TrimEnd("=").Replace("+", "-").Replace("/", "_")
$tamperedToken = "$($parts[0]).$newPayload.$($parts[2])"

curl.exe -k -i https://localhost/api/auth/me -H "Authorization: Bearer $tamperedToken"
```

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
JWT signature is invalid.
```

Nếu muốn minh họa lại bằng integration test:

```powershell
cd E:\PROJECT_MMUD\backend
mvn -Dtest=AuthSecurityIntegrationTest#tamperedJwtPayloadIsRejected test
```

### Bước 7: Thử refresh token

Trên Swagger, mở:

```http
POST /api/auth/refresh
```

Body:

```json
{
  "refreshToken": "<refresh-token vừa lấy được>"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- response trả `accessToken` mới
- response trả `refreshToken` mới

Sau đó dùng `accessToken` mới gọi lại:

```http
GET /api/auth/me
```

Kết quả mong đợi:

- vẫn trả `200 OK`

### Bước 8: Thử logout và dùng lại refresh token cũ

Đầu tiên, `Authorize` bằng `accessToken` mới ở bước 7. Sau đó gọi:

```http
POST /api/auth/logout
```

Body:

```json
{
  "refreshToken": "<refresh-token mới ở bước 7>"
}
```

Kết quả mong đợi:

- HTTP `200 OK`
- message:

```text
Refresh token revoked successfully.
```

Tiếp theo thử dùng lại chính refresh token đó tại:

```http
POST /api/auth/refresh
```

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
Refresh token has been revoked.
```

### Bước 9: Demo token hết hạn

Hiện tại project chưa có test riêng cho token hết hạn, nên phần này nên demo thủ công.

Mở `.env`, chỉnh:

```text
JWT_ACCESS_TOKEN_EXPIRE_MINUTES=1
```

Khởi động lại stack:

```powershell
cd E:\PROJECT_MMUD
docker compose down
docker compose up --build -d
```

Sau đó:

1. đăng nhập lại để lấy access token mới
2. chờ khoảng `70` đến `90` giây
3. gọi lại:

```http
GET /api/auth/me
```

Kết quả mong đợi:

- HTTP `401 Unauthorized`
- message:

```text
JWT access token has expired.
```

Sau khi demo xong, nhớ đổi lại:

```text
JWT_ACCESS_TOKEN_EXPIRE_MINUTES=15
```

rồi khởi động lại Docker.

## 4. Kết quả mong đợi

Sau khi làm xong Demo 02, bạn cần chứng minh được:

1. `accessToken` chứa đầy đủ claim phục vụ xác thực và phân quyền.
2. API protected chỉ chấp nhận đúng Bearer token hợp lệ.
3. Token bị sửa payload sẽ bị từ chối vì chữ ký không còn hợp lệ.
4. Refresh token có thể quay vòng để xin token mới.
5. Refresh token đã logout thì không dùng lại được.

## 5. Câu nên nói khi trình bày

- JWT giúp backend xác thực request theo mô hình stateless.
- Payload của JWT chỉ có ý nghĩa khi chữ ký còn hợp lệ.
- Nếu ai đó tự ý sửa payload nhưng không có secret phía server thì token sẽ bị bác bỏ.
- Access token nên sống ngắn để giảm rủi ro nếu bị lộ.
- Refresh token tồn tại lâu hơn nhưng vẫn có thể bị thu hồi khi logout hoặc reset password.

## 6. Ảnh nên chụp cho báo cáo

- Ảnh `POST /api/auth/login` trả `accessToken`.
- Ảnh PowerShell giải mã payload JWT.
- Ảnh `GET /api/auth/me` trả `200 OK`.
- Ảnh gọi `GET /api/auth/me` khi không có token và nhận `401`.
- Ảnh token bị sửa payload và bị từ chối.
- Ảnh `POST /api/auth/refresh` trả token mới.
- Ảnh `POST /api/auth/logout` thu hồi refresh token.
- Nếu demo phần hết hạn, thêm ảnh `JWT access token has expired.`

## 7. Cách reset sau demo

Nếu bạn đã đổi thời gian hết hạn token trong `.env`, hãy đổi lại cấu hình mặc định rồi khởi động lại stack:

```powershell
docker compose down
docker compose up --build -d
```

## 8. Kết luận

Demo này chứng minh hệ thống đã triển khai JWT đúng trọng tâm môn Mật mã ứng dụng:

- có phát hành access token
- có kiểm tra chữ ký và thời gian sống
- có cơ chế refresh token
- có thu hồi refresh token khi logout
