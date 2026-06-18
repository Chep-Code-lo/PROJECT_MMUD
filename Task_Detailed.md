# Hướng xử lý hiện tại

Repo hiện đã được khôi phục lại đầy đủ các phần:

- `backend/`
- `frontend/`
- `database/`
- `docker-compose.yml`

Nguyên tắc tiếp theo:

- Không xóa cả file nếu bên trong file đó đã có code làm thật.
- Chỉ dọn phần code mẫu, code rỗng, route giả, service giả hoặc đoạn code không còn khớp với hướng triển khai của dự án.
- Mỗi lần dọn phải giữ cho project và tài liệu không lệch nhau.
- Nếu một file vẫn nằm trong scope của dự án như `AuthController`, `SecurityConfig`, `JwtService`, `AuthService`, thì không xóa file chỉ vì hiện tại nó còn trống.

Trọng tâm khi dọn code:

- Giữ lại phần backend, database và frontend đang thuộc scope của dự án.
- Xóa các đoạn placeholder hoặc luồng giả không còn dùng.
- Ưu tiên làm cho source tree bám sát hướng trong dự án hơn là xóa cho gọn bằng mọi giá.

## Phần đã loại khỏi scope hiện tại

Những nhánh sau được xem là không còn cần thiết cho hướng tập trung vào mật mã ứng dụng và có thể bỏ khỏi source:

- quản lý `admin users` riêng
- các trang `admin/*` chỉ để placeholder
- `forgot-password` giả lập nhưng không có backend thật
- các trang chi tiết `customers/[id]` và `tickets/[id]` chỉ có nội dung mẫu
- service hoặc helper frontend không còn được dùng như `adminService`, `authGuard`, `types/user`
- asset và file scaffold mặc định của Next.js không phục vụ demo chính
- placeholder backend ngoài scope như `AdminController` hoặc `OAuth2SuccessHandler`

Những phần vẫn phải giữ vì còn nằm trong scope chính của dự án:

- auth với `JWT`
- `bcrypt`
- `AES`
- `Customer`, `Ticket`, `AuditLog`
- `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`, `AuthService`, `AuthController`
- schema và seed database
