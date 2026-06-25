# Demo 04: Chống BOLA / IDOR

## 1. Mục tiêu

Demo này dùng để chứng minh:

- Sinh viên chỉ xem được dữ liệu thuộc về chính mình
- Nếu đổi `id` trên URL để truy cập tài nguyên của người khác thì backend sẽ chặn
- Việc chặn này diễn ra ở phía server, không phụ thuộc vào việc ẩn nút trên giao diện
- Hệ thống có ghi audit log khi phát hiện truy cập trái phép

## 2. Endpoint phù hợp để minh họa

- `GET /api/certificates/{certificateId}`
- `GET /api/enrollments/{enrollmentId}`
- `GET /api/users/{userId}/profile`
- `GET /api/courses/{courseId}/lessons/{lessonId}`

Trong buổi demo, nên ưu tiên endpoint chứng chỉ vì dễ giải thích và dễ chụp minh chứng.

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

## 4. Kịch bản demo khuyến nghị

### Bước 1: Đăng nhập Student 1

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

Lấy `accessToken` của `student1`.

### Bước 2: Lấy danh sách chứng chỉ của Student 1

Gọi:

```http
GET /api/certificates/me
Authorization: Bearer <student1-accessToken>
```

Kết quả mong đợi:

- Trả về danh sách chứng chỉ của `student1`
- Ghi lại `certificateId`, ví dụ `1`

### Bước 3: Đăng nhập Student 2

Gọi:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "student2@example.com",
  "password": "Password123!"
}
```

Lấy `accessToken` của `student2`.

### Bước 4: Student 2 cố xem chứng chỉ của Student 1

Gọi:

```http
GET /api/certificates/{certificateId-cua-student1}
Authorization: Bearer <student2-accessToken>
```

Ví dụ:

```http
GET /api/certificates/1
```

Kết quả mong đợi:

- Backend trả `403 Forbidden`
- Student 2 không nhận được dữ liệu chứng chỉ của Student 1

### Bước 5: Kiểm tra audit log bằng tài khoản admin

Đăng nhập admin, sau đó gọi:

```http
GET /api/admin/audit-logs
Authorization: Bearer <admin-accessToken>
```

Kết quả mong đợi:

- Tìm thấy bản ghi `ACCESS_DENIED`
- Message thể hiện có hành vi truy cập trái phép vào tài nguyên không thuộc quyền sở hữu

## 5. Biến thể có thể demo thêm

### 5.1. Với enrollment

Gọi:

```http
GET /api/enrollments/{enrollmentId}
```

Sau đó đổi `enrollmentId` sang của người khác.

Kết quả mong đợi:

- `403 Forbidden`

### 5.2. Với profile

Gọi:

```http
GET /api/users/{userId}/profile
```

Nếu `student2` cố truy cập `userId` của `student1`, backend cũng phải từ chối.

### 5.3. Với bài học

Nếu người dùng chưa được ghi danh khóa học, khi gọi:

```http
GET /api/courses/{courseId}/lessons/{lessonId}
```

backend sẽ kiểm tra quyền sở hữu hoặc trạng thái ghi danh trước khi cho xem nội dung.

## 6. Giải thích ngắn gọn để trình bày

- BOLA/IDOR xảy ra khi backend chỉ dựa vào `id` trên URL mà không kiểm tra quyền sở hữu
- Việc ẩn nút trên frontend không đủ an toàn
- Cách phòng thủ đúng là kiểm tra ownership ở phía server cho từng đối tượng truy cập
- Khi phát hiện truy cập trái phép, hệ thống nên trả `403` và ghi log để phục vụ giám sát

## 7. Minh chứng nên chụp cho báo cáo

- Ảnh `GET /api/certificates/me` của `student1`
- Ảnh `student2` gọi `GET /api/certificates/{id}` và nhận `403`
- Ảnh admin mở audit log và thấy sự kiện `ACCESS_DENIED`

## 8. Kết luận

Demo này là phần rất quan trọng của đồ án vì nó chứng minh hệ thống không chỉ xác thực người dùng mà còn phân quyền đúng trên từng tài nguyên cụ thể, qua đó ngăn chặn BOLA/IDOR.
