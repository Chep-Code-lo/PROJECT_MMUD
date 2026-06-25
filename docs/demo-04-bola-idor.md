# Demo 04: Chống BOLA / IDOR

## 1. Mục tiêu

Demo này dùng để chứng minh:

- Sinh viên chỉ xem được dữ liệu thuộc về chính mình
- Nếu đổi `id` trên URL để truy cập tài nguyên của người khác thì backend sẽ chặn
- Việc chặn diễn ra ở phía server, không phụ thuộc vào việc ẩn nút trên giao diện
- Hệ thống có ghi audit log khi phát hiện truy cập trái phép

## 2. Endpoint phù hợp để minh họa

- `GET /api/certificates/{certificateId}`
- `GET /api/enrollments/{enrollmentId}`
- `GET /api/users/{userId}/profile`
- `GET /api/courses/{courseId}/lessons/{lessonId}`

Trong buổi demo, nên ưu tiên endpoint chứng chỉ vì:

- dễ lấy `certificateId`;
- dễ giải thích “đổi ID trên URL”;
- dễ chụp minh chứng.

## 3. Chuẩn bị

### 3.1. Khởi động hệ thống

```powershell
docker compose up --build -d
```

### 3.2. Tài khoản dùng để minh họa

- `student1@example.com / Password123!`
- `student2@example.com / Password123!`
- `admin@example.com / Admin123!`

### 3.3. Dữ liệu seed thuận tiện cho demo

- `student1` đã có certificate của khóa `Java Security Basics`
- `student2` không sở hữu certificate đó

### 3.4. Thực hiện demo này ở đâu

Demo 04 nên dùng **Postman** là chính. Lý do:

- cần giữ đồng thời token của `student1`, `student2`, `admin`;
- cần đổi qua lại nhiều token;
- collection của project đã có sẵn các request gần như đủ cho demo này.

File collection:

```text
postman/online-course-security.postman_collection.json
```

Bạn vẫn có thể làm bằng Swagger, nhưng sẽ phải đổi token liên tục trong nút `Authorize`, khá rối khi demo trước giảng viên.

## 4. Chuẩn bị Postman

### Bước 1: Mở Postman

Nếu Postman đang bật kiểm tra SSL nghiêm ngặt, hãy vào:

1. `Settings`
2. Tắt `SSL certificate verification`

vì hệ thống local đang dùng certificate tự ký.

### Bước 2: Import collection

Import file:

```text
postman/online-course-security.postman_collection.json
```

### Bước 3: Kiểm tra biến collection

Biến quan trọng:

- `baseUrl = https://localhost`

Các biến token và `victimCertificateId` sẽ được collection tự cập nhật trong lúc chạy.

## 5. Kịch bản demo khuyến nghị

### Bước 1: Đăng nhập Student 1

Trong Postman, chạy request:

- `Login Student 1`

Kết quả mong đợi:

- Trả `200 OK`
- Collection tự lưu `accessToken` của Student 1

### Bước 2: Lấy chứng chỉ của Student 1

Chạy request:

- `Get My Certificate`

Kết quả mong đợi:

- Trả `200 OK`
- Response trả danh sách chứng chỉ của Student 1
- Collection tự lưu `victimCertificateId`

Ý cần nói:

- Đây là tài nguyên hợp lệ mà Student 1 sở hữu

### Bước 3: Đăng nhập Student 2

Chạy request:

- `Login Student 2`

Kết quả mong đợi:

- Trả `200 OK`
- Collection tự lưu `student2AccessToken`

### Bước 4: Student 2 cố đọc chứng chỉ của Student 1

Chạy request:

- `BOLA Attack Attempt`

Request này sẽ:

- dùng token của `student2`;
- nhưng cố truy cập `certificateId` thuộc về `student1`.

Kết quả mong đợi:

- Trả `403 Forbidden`
- Student 2 không nhận được dữ liệu chứng chỉ của Student 1

Điểm cần nói khi demo:

- Đây là minh họa điển hình của BOLA/IDOR
- Nếu backend chỉ dựa vào `certificateId` trên URL mà không kiểm tra ownership, lỗi sẽ xảy ra

### Bước 5: Đăng nhập admin để xem audit log

Chạy:

- `Login Admin`

Sau đó chạy:

- `Admin Audit Logs`

Kết quả mong đợi:

- Trả `200 OK`
- Tìm thấy bản ghi liên quan tới:
  - `ACCESS_DENIED`
  - `Certificate`
  - người dùng không đủ quyền

## 6. Cách làm thủ công nếu không dùng collection

Nếu bạn muốn trình bày mà không import collection, có thể làm thủ công như sau:

1. Tạo request `POST https://localhost/api/auth/login` cho `student1`
2. Tạo request `GET https://localhost/api/certificates/me` với token của `student1`
3. Ghi lại `certificateId`
4. Tạo request `POST https://localhost/api/auth/login` cho `student2`
5. Tạo request `GET https://localhost/api/certificates/{certificateId}` với token của `student2`
6. Quan sát `403`
7. Đăng nhập `admin`
8. Gọi `GET https://localhost/api/admin/audit-logs`

## 7. Biến thể có thể demo thêm

### 7.1. Với enrollment

Bạn có thể tạo request:

```http
GET /api/enrollments/{enrollmentId}
```

Rồi dùng token của người khác để thử truy cập.

### 7.2. Với profile

Bạn có thể tạo request:

```http
GET /api/users/{userId}/profile
```

Rồi đổi `userId` sang của tài khoản khác.

### 7.3. Với bài học

Bạn có thể thử:

```http
GET /api/courses/{courseId}/lessons/{lessonId}
```

với tài khoản chưa được ghi danh hoặc không có quyền phù hợp.

## 8. Cách trình bày ngắn gọn trước giảng viên

Bạn nên trình bày đúng thứ tự:

1. Mở Postman
2. Chạy `Login Student 1`
3. Chạy `Get My Certificate` để lấy `certificateId`
4. Chạy `Login Student 2`
5. Chạy `BOLA Attack Attempt`
6. Chỉ ra response `403 Forbidden`
7. Chạy `Login Admin`
8. Chạy `Admin Audit Logs` để chỉ ra bản ghi `ACCESS_DENIED`

Nếu giảng viên hỏi “demo này làm ở đâu”, câu trả lời chuẩn là:

- **Khuyến nghị làm bằng Postman**
- **Có thể dùng Swagger nhưng sẽ bất tiện vì phải đổi token liên tục**

## 9. Giải thích ngắn gọn để trình bày

- BOLA/IDOR xảy ra khi backend không kiểm tra quyền sở hữu đối tượng
- Ẩn nút trên frontend không phải biện pháp bảo mật đầy đủ
- Cách phòng thủ đúng là kiểm tra ownership ở phía server
- Khi phát hiện truy cập trái phép, hệ thống nên trả `403` và ghi log

## 10. Minh chứng nên chụp cho báo cáo

- Ảnh request `Get My Certificate` của `student1`
- Ảnh request `BOLA Attack Attempt` của `student2` nhận `403`
- Ảnh `Admin Audit Logs` hiển thị `ACCESS_DENIED`

## 11. Kết luận

Demo này là một phần rất quan trọng của đồ án vì nó chứng minh hệ thống không chỉ xác thực người dùng mà còn kiểm tra quyền trên từng tài nguyên cụ thể, qua đó chặn được BOLA/IDOR.
