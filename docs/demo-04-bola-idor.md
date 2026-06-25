# Demo 04: Chống BOLA / IDOR

## 1. Mục tiêu

Demo này nhằm chứng minh:

1. Sinh viên chỉ được xem tài nguyên thuộc về chính mình.
2. Nếu đổi `id` trên URL để truy cập dữ liệu của người khác thì backend sẽ chặn ở phía server.
3. Hệ thống trả `403 Forbidden` và ghi audit log khi phát hiện truy cập trái phép.
4. Admin có luồng quản trị riêng, không dùng các endpoint student-only để đọc dữ liệu cá nhân của học viên.

## 2. Các endpoint phù hợp để minh họa

- `GET /api/certificates/{certificateId}`
- `GET /api/enrollments/{enrollmentId}`
- `GET /api/users/{userId}/profile`
- `GET /api/courses/{courseId}/lessons/{lessonId}`

Trong buổi demo, nên ưu tiên endpoint chứng chỉ vì:

- dễ lấy `certificateId`
- dễ giải thích kiểu tấn công đổi ID trên URL
- response rất rõ ràng giữa `200` và `403`

## 3. Chuẩn bị

### 3.1. Khởi động hệ thống

```powershell
cd E:\PROJECT_MMUD
docker compose up --build -d
```

### 3.2. Tài khoản nên dùng

- `student1@example.com / Password123!`
- `student2@example.com / Password123!`
- `admin@example.com / Admin123!`

### 3.3. Dữ liệu seed phục vụ demo

- `student1` đã có chứng chỉ của khóa `Java Security Basics`
- `student2` không sở hữu chứng chỉ đó
- admin có quyền xem audit log và hồ sơ học viên qua khu vực quản trị

### 3.4. Thực hiện ở đâu

Demo này nên làm bằng `Postman` vì cần giữ nhiều token cùng lúc.

Collection dùng sẵn:

```text
postman/online-course-security.postman_collection.json
```

## 4. Chuẩn bị Postman

### Bước 1: Tắt kiểm tra SSL self-signed

Trong Postman:

1. vào `Settings`
2. tắt `SSL certificate verification`

### Bước 2: Import collection

Import file:

```text
postman/online-course-security.postman_collection.json
```

### Bước 3: Kiểm tra biến collection

Biến quan trọng nhất:

```text
baseUrl = https://localhost
```

Nếu muốn test qua domain public khi tunnel đang bật, có thể tạm đổi thành:

```text
baseUrl = https://hackerlo.online
```

## 5. Kịch bản demo chính: Student 2 cố lấy certificate của Student 1

### Bước 1: Đăng nhập Student 1

Chạy request:

- `Login Student 1`

Kết quả mong đợi:

- HTTP `200 OK`
- collection lưu `accessToken` của student 1

### Bước 2: Lấy certificate của Student 1

Chạy request:

- `Get My Certificate`

Kết quả mong đợi:

- HTTP `200 OK`
- response trả danh sách chứng chỉ của student 1
- collection lưu `victimCertificateId`

Ý nghĩa:

- đây là đối tượng mà student 1 thực sự sở hữu

### Bước 3: Đăng nhập Student 2

Chạy request:

- `Login Student 2`

Kết quả mong đợi:

- HTTP `200 OK`
- collection lưu `student2AccessToken`

### Bước 4: Thực hiện hành vi BOLA / IDOR

Chạy request:

- `BOLA Attack Attempt`

Request này dùng token của `student2` nhưng cố truy cập:

```http
GET /api/certificates/{victimCertificateId}
```

trong đó `victimCertificateId` là chứng chỉ của `student1`.

Kết quả mong đợi:

- HTTP `403 Forbidden`
- message:

```text
Only the owning student can access this certificate.
```

Điểm cần nói:

- đây là mô phỏng chuẩn của lỗi BOLA / IDOR
- nếu backend không kiểm tra ownership thì student 2 sẽ xem được dữ liệu của student 1

### Bước 5: Đăng nhập admin và xem audit log

Chạy:

- `Login Admin`
- `Admin Audit Logs`

Kết quả mong đợi:

- HTTP `200 OK`
- tìm thấy bản ghi có:
  - `action = ACCESS_DENIED`
  - `targetType = Certificate`

## 6. Kịch bản phụ nên biết để trả lời khi giảng viên hỏi thêm

### 6.1. Với hồ sơ cá nhân của student

Student chỉ được dùng:

```http
GET /api/users/{userId}/profile
```

với đúng `userId` của chính mình.

Nếu đổi sang `userId` của người khác, backend phải trả:

- HTTP `403 Forbidden`

### 6.2. Với enrollment

Student chỉ được dùng:

```http
GET /api/enrollments/{enrollmentId}
```

với đúng enrollment của chính mình.

### 6.3. Với lesson

Student chưa được ghi danh hoặc chưa được mở khóa bài học sẽ bị chặn tại:

```http
GET /api/courses/{courseId}/lessons/{lessonId}
```

Kết quả mong đợi:

- HTTP `403 Forbidden`
- message:

```text
Course lessons are only available to the enrolled student.
```

### 6.4. Với admin

Admin có endpoint riêng để xem hồ sơ học viên:

```http
GET /api/admin/users/{userId}
```

Nhưng admin không dùng được các endpoint student-only như:

- `GET /api/certificates/me`
- `GET /api/users/{userId}/profile`
- `GET /api/certificates/{certificateId}`

Đây là điểm rất hay để giải thích việc tách biệt vai trò và bề mặt API.

## 7. Cách làm thủ công nếu không dùng Postman collection

Nếu không dùng collection, có thể làm lần lượt:

1. đăng nhập `student1`
2. gọi `GET /api/certificates/me`
3. ghi lại `certificateId`
4. đăng nhập `student2`
5. gọi `GET /api/certificates/{certificateId}` bằng token của student 2
6. quan sát `403 Forbidden`
7. đăng nhập `admin`
8. gọi `GET /api/admin/audit-logs`

## 8. Kết quả mong đợi

Sau khi làm xong Demo 04, bạn phải chứng minh được:

1. `student1` xem được certificate của chính mình.
2. `student2` bị chặn khi đổi `certificateId` của `student1`.
3. admin xem được audit log của hành vi bị chặn.
4. việc chặn diễn ra ở backend chứ không phải chỉ trên giao diện.

## 9. Câu nên nói khi trình bày

- BOLA xảy ra khi backend xác thực người dùng rồi nhưng không kiểm tra quyền trên từng đối tượng.
- Ẩn nút ở frontend không đủ để chống BOLA.
- Cách phòng thủ đúng là kiểm tra ownership ở phía server cho từng tài nguyên.
- Khi phát hiện truy cập trái phép, hệ thống nên trả `403` và ghi audit log.

## 10. Ảnh nên chụp cho báo cáo

- Ảnh `Get My Certificate` của `student1`.
- Ảnh `BOLA Attack Attempt` của `student2` trả `403 Forbidden`.
- Ảnh `Admin Audit Logs` có `ACCESS_DENIED`.
- Nếu có thời gian, thêm ảnh admin xem hồ sơ học viên bằng `GET /api/admin/users/{userId}`.

## 11. Cách reset sau demo

Demo này gần như không làm thay đổi dữ liệu seed, nên thường không cần reset.

## 12. Kết luận

Đây là một trong những demo quan trọng nhất của đồ án vì nó thể hiện rõ tư tưởng bảo mật API:

- xác thực thôi là chưa đủ
- phải kiểm tra quyền trên từng đối tượng
- audit log giúp chứng minh hệ thống không chỉ chặn mà còn giám sát được hành vi tấn công
