# Demo 02: JWT và Bearer Token

## 1. Mục tiêu

Demo này dùng để chứng minh:

- Sau khi đăng nhập, backend phát hành access token dạng JWT
- API protected chỉ chấp nhận request có Bearer token hợp lệ
- Nếu token bị sửa payload hoặc sai chữ ký thì backend từ chối
- Nếu token hết hạn thì backend cũng từ chối
- Refresh token có thể dùng để xin access token mới

## 2. Chuẩn bị

### 2.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 2.2. Tài khoản sử dụng

- `student1@example.com / Password123!`

### 2.3. Thực hiện demo này ở đâu

Demo 02 nên dùng **2 nơi**:

#### Nơi 1: Swagger để gọi API

Mở trên máy host:

```text
https://localhost/swagger-ui.html
```

Tại đây bạn sẽ:

- gọi `POST /api/auth/login`;
- gọi `GET /api/auth/me`;
- gọi `POST /api/auth/refresh`;
- dán Bearer token vào nút `Authorize`.

#### Nơi 2: PowerShell nếu muốn demo token hết hạn nhanh

Bạn chỉ cần PowerShell khi muốn:

- sửa `.env`;
- khởi động lại Docker;
- hoặc chạy test tự động để minh họa token bị sửa payload.

#### Có cần dùng frontend không

Không. Demo JWT nên làm trực tiếp bằng Swagger hoặc Postman để giảng viên nhìn rõ:

- token được trả về thế nào;
- token được gửi qua header ra sao;
- backend trả mã trạng thái gì khi token sai.

## 3. Các bước thực hiện

### Bước 1: Mở Swagger

Truy cập:

```text
https://localhost/swagger-ui.html
```

Sau đó:

1. Tìm nhóm `Auth API`
2. Mở endpoint `POST /api/auth/login`
3. Bấm `Try it out`

Nếu trình duyệt cảnh báo certificate tự ký, hãy chọn tiếp tục vì đây là môi trường demo local.

### Bước 2: Đăng nhập để lấy JWT

Tại `POST /api/auth/login`, nhập body:

```json
{
  "email": "student1@example.com",
  "password": "Password123!"
}
```

Thao tác:

1. Dán JSON vào request body
2. Bấm `Execute`
3. Quan sát response

Kết quả mong đợi:

- Trả `200 OK`
- Response có:
  - `accessToken`
  - `refreshToken`
  - `tokenType`
  - `accessTokenExpiresInSeconds`
  - `refreshTokenExpiresInSeconds`
  - `role`
  - `scope`

Điểm cần nói khi demo:

- `accessToken` chính là JWT dùng cho các request protected
- `refreshToken` dùng để xin token mới khi access token hết hạn

### Bước 3: Dùng Bearer token để gọi API bảo vệ

Sao chép giá trị `accessToken`, sau đó:

1. Bấm nút `Authorize` ở góc trên của Swagger
2. Nhập:

```text
Bearer <accessToken>
```

3. Bấm `Authorize`
4. Đóng hộp thoại

Tiếp theo:

1. Mở endpoint `GET /api/auth/me`
2. Bấm `Try it out`
3. Bấm `Execute`

Kết quả mong đợi:

- Trả `200 OK`
- Response hiển thị đúng thông tin người dùng hiện tại

Ý nghĩa:

- Client chỉ cần gửi `Authorization: Bearer <token>`
- Backend sẽ kiểm tra chữ ký, thời gian sống và quyền trước khi cho truy cập

### Bước 4: Thử gọi API mà không có token

Để mô tả tình huống không có Bearer token:

1. Bấm lại `Authorize`
2. Chọn `Logout` hoặc xóa giá trị Bearer token
3. Đóng hộp thoại
4. Gọi lại `GET /api/auth/me`

Kết quả mong đợi:

- Trả `401 Unauthorized`

Điểm cần nói:

- Đây là bằng chứng API protected không cho truy cập ẩn danh

### Bước 5: Thử token bị sửa payload

Phần này nên làm bằng một token demo local, không dùng token của môi trường thật.

#### Cách làm thủ công

1. Đăng nhập lại để lấy một `accessToken` mới
2. Sao chép token đó
3. Mở công cụ xem JWT hoặc trình chỉnh sửa JWT
4. Giải mã phần payload
5. Sửa một trường dễ thấy, ví dụ:
   - đổi `email`
   - hoặc đổi `role`
6. Giữ nguyên phần chữ ký cũ
7. Ghép lại thành token mới đã bị sửa payload

Quay lại Swagger:

1. Bấm `Authorize`
2. Nhập:

```text
Bearer <tamperedToken>
```

3. Gọi lại `GET /api/auth/me`

Kết quả mong đợi:

- Trả `401 Unauthorized`
- Backend phát hiện chữ ký không còn hợp lệ

#### Cách minh họa nhanh bằng test tự động

Mở PowerShell:

```powershell
cd E:\PROJECT_MMUD\backend
mvn -Dtest=AuthSecurityIntegrationTest#tamperedJwtPayloadIsRejected test
```

Kết quả mong đợi:

- Test chạy thành công
- Chứng minh hệ thống từ chối JWT bị sửa payload

### Bước 6: Thử refresh token

Quay lại Swagger, mở:

```http
POST /api/auth/refresh
```

Body:

```json
{
  "refreshToken": "<refreshToken lấy từ bước đăng nhập>"
}
```

Thao tác:

1. Bấm `Try it out`
2. Dán `refreshToken`
3. Bấm `Execute`

Kết quả mong đợi:

- Trả `200 OK`
- Response trả:
  - `accessToken` mới
  - `refreshToken` mới

Tiếp tục kiểm tra:

1. Copy `accessToken` mới
2. Bấm `Authorize`
3. Nhập `Bearer <accessToken-moi>`
4. Gọi lại `GET /api/auth/me`

Kết quả mong đợi:

- Vẫn trả `200 OK`

### Bước 7: Thử token hết hạn

Phần này cần làm trên PowerShell vì phải chỉnh cấu hình môi trường.

#### 7.1. Giảm thời gian sống của access token

Mở file `.env`, chỉnh:

```text
JWT_ACCESS_TOKEN_EXPIRE_MINUTES=1
```

#### 7.2. Khởi động lại hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose down
docker compose up --build -d
```

#### 7.3. Đăng nhập lại trên Swagger

Lấy `accessToken` mới bằng `POST /api/auth/login`.

#### 7.4. Chờ token hết hạn

Chờ khoảng 70 đến 90 giây, sau đó gọi:

```http
GET /api/auth/me
```

Kết quả mong đợi:

- Trả `401 Unauthorized`

Sau khi demo xong, nên đổi lại `.env` về:

```text
JWT_ACCESS_TOKEN_EXPIRE_MINUTES=15
```

rồi khởi động lại Docker để hệ thống quay về cấu hình bình thường.

## 4. Cách trình bày ngắn gọn trước giảng viên

Bạn có thể trình bày theo đúng thứ tự này:

1. Mở Swagger tại `https://localhost/swagger-ui.html`
2. Đăng nhập để lấy `accessToken`
3. Dùng `Authorize` để gọi `GET /api/auth/me`
4. Xóa Bearer token và gọi lại để chứng minh API trả `401`
5. Sửa payload token rồi gọi lại để chứng minh chữ ký không còn hợp lệ
6. Dùng `refreshToken` để xin `accessToken` mới
7. Nếu cần, giảm thời gian sống token xuống 1 phút để demo hết hạn

Nếu giảng viên hỏi “demo này thực hiện ở đâu”, câu trả lời chuẩn là:

- **Swagger để gọi API**
- **PowerShell khi cần chỉnh thời gian hết hạn hoặc chạy test tự động**

## 5. Giải thích ngắn gọn để trình bày

- JWT được dùng để xác thực request trong mô hình stateless
- Payload chỉ có giá trị khi chữ ký còn hợp lệ
- Nếu kẻ tấn công sửa nội dung token nhưng không có secret của server, token sẽ bị từ chối
- Access token sống ngắn để giảm rủi ro lộ lọt
- Refresh token giúp xin access token mới mà không cần đăng nhập lại ngay

## 6. Minh chứng nên chụp cho báo cáo

- Ảnh `POST /api/auth/login` trả về `accessToken`
- Ảnh `GET /api/auth/me` trả `200`
- Ảnh gọi `GET /api/auth/me` khi không có token và nhận `401`
- Ảnh token bị sửa payload và bị từ chối
- Ảnh `POST /api/auth/refresh` trả token mới
- Nếu có, ảnh token hết hạn bị từ chối

## 7. Kết luận

Demo này chứng minh hệ thống đã triển khai xác thực Bearer token đúng bản chất:

- Có phát hành JWT
- Có kiểm tra chữ ký
- Có từ chối token sai hoặc hết hạn
- Có cơ chế refresh token để minh họa khái niệm OAuth2 cơ bản
