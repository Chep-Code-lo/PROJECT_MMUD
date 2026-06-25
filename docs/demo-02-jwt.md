# Demo 02: JWT và Bearer Token

## 1. Mục tiêu

Demo này chứng minh:

- Sau khi đăng nhập, backend phát hành access token dạng JWT
- API protected chỉ chấp nhận request có Bearer token hợp lệ
- Nếu token bị sửa payload hoặc sai chữ ký thì backend từ chối
- Nếu token hết hạn thì backend cũng từ chối

## 2. Chuẩn bị

### 2.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 2.2. Tài khoản sử dụng

- `student1@example.com / Password123!`

Bạn có thể dùng Swagger local-only hoặc Postman để thao tác. Nếu dùng Swagger, truy cập:

```text
https://localhost:8444/swagger-ui.html
```

## 3. Các bước thực hiện

### Bước 1: Đăng nhập để lấy JWT

Gọi:

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

- Trả `200 OK`
- Response chứa `accessToken`
- Response cũng chứa `refreshToken`, `role`, `scope`, thời gian hết hạn

### Bước 2: Dùng Bearer token để gọi API bảo vệ

Lấy `accessToken` vừa nhận được và gọi:

```http
GET /api/auth/me
Authorization: Bearer <accessToken>
```

Kết quả mong đợi:

- Trả `200 OK`
- Nhận được thông tin người dùng hiện tại

Điểm cần nhấn mạnh:

- Client chỉ gửi token qua header `Authorization`
- Backend kiểm tra chữ ký, thời gian sống và quyền hạn trước khi cho truy cập

### Bước 3: Thử gọi API mà không có token

Gọi lại `GET /api/auth/me` nhưng bỏ header `Authorization`.

Kết quả mong đợi:

- Trả `401 Unauthorized`

### Bước 4: Thử token bị sửa payload

Có hai cách demo:

#### Cách thủ công

1. Sao chép `accessToken`
2. Sửa phần payload của token, ví dụ đổi `email` hoặc `role`
3. Giữ nguyên phần chữ ký
4. Gửi lại request:

```http
GET /api/auth/me
Authorization: Bearer <tampered-token>
```

Kết quả mong đợi:

- Trả `401 Unauthorized`
- Backend phát hiện chữ ký không còn hợp lệ

Lưu ý:

- Không nên dán token thật của môi trường nhạy cảm lên dịch vụ công cộng
- Nếu cần minh họa nhanh, hãy thao tác trên token của môi trường demo local

#### Cách tự động bằng test đã có sẵn

```powershell
cd backend
mvn -Dtest=AuthSecurityIntegrationTest#tamperedJwtPayloadIsRejected test
```

Kết quả mong đợi:

- Test chạy thành công
- Chứng minh token bị sửa payload sẽ bị backend từ chối

### Bước 5: Thử token hết hạn

Cách dễ nhất để demo:

1. Tạm thời giảm `JWT_ACCESS_TOKEN_EXPIRE_MINUTES` trong `.env` xuống `1`
2. Chạy lại hệ thống:

```powershell
docker compose down
docker compose up --build -d
```

3. Đăng nhập lấy token mới
4. Chờ quá thời gian hết hạn
5. Gọi lại `GET /api/auth/me`

Kết quả mong đợi:

- Trả `401 Unauthorized`

### Bước 6: Thử refresh token

Gọi:

```http
POST /api/auth/refresh
```

Body:

```json
{
  "refreshToken": "<refreshToken>"
}
```

Kết quả mong đợi:

- Nhận được access token mới
- Có thể dùng token mới để gọi lại `GET /api/auth/me`

## 4. Giải thích ngắn gọn để trình bày

- JWT được dùng để xác thực request trong mô hình stateless
- Payload chỉ có giá trị khi chữ ký còn hợp lệ
- Nếu kẻ tấn công sửa nội dung token nhưng không có secret của server, token sẽ bị từ chối
- Access token sống ngắn để giảm rủi ro lộ lọt
- Refresh token dùng để xin access token mới mà không cần đăng nhập lại liên tục

## 5. Minh chứng nên chụp cho báo cáo

- Ảnh response đăng nhập trả về `accessToken`
- Ảnh `GET /api/auth/me` trả `200`
- Ảnh request không có token trả `401`
- Ảnh token bị sửa payload trả `401`
- Ảnh refresh token hoạt động thành công

## 6. Kết luận

Demo này chứng minh hệ thống đã triển khai xác thực Bearer token đúng bản chất:

- Có phát hành JWT
- Có kiểm tra chữ ký
- Có từ chối token sai hoặc hết hạn
- Có cơ chế refresh token phục vụ trải nghiệm người dùng và minh họa khái niệm OAuth2 cơ bản
