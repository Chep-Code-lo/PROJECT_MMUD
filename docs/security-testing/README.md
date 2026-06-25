# Tài liệu kiểm thử bảo mật

Đây là thư mục tổng hợp các tài liệu phục vụ kiểm thử, demo và báo cáo phần bảo mật của đồ án.

## 1. Các tài liệu chính

- `docs/demo-runbook.md`: kịch bản demo tổng hợp theo thứ tự trình bày
- `docs/postman-testing.md`: hướng dẫn kiểm thử thủ công bằng Postman
- `docs/owasp-zap-testing.md`: hướng dẫn quét và đọc kết quả OWASP ZAP
- `docs/demo-01-password-bcrypt.md`: demo `bcrypt`
- `docs/demo-02-jwt.md`: demo `JWT`, `Bearer token`, `refresh token`, `logout`
- `docs/demo-03-aes-encryption.md`: demo `AES-GCM`
- `docs/demo-04-bola-idor.md`: demo chống `BOLA / IDOR`
- `docs/demo-05-webhook-hmac.md`: demo `HMAC-SHA256` cho webhook
- `docs/demo-06-rate-limit.md`: demo `rate limiting`
- `docs/demo-07-https-tls.md`: demo `HTTPS / TLS`
- `docs/demo-08-forgot-password.md`: demo `forgot password / reset password`

## 2. Nên bắt đầu từ đâu

Nếu cần chuẩn bị nhanh cho buổi demo:

1. đọc `docs/demo-runbook.md`
2. mở `docs/postman-testing.md`
3. mở `docs/owasp-zap-testing.md` khi cần phần kiểm thử bằng ZAP

Nếu cần viết báo cáo chi tiết:

1. dùng `docs/demo-runbook.md` làm xương sống
2. lấy bước thao tác cụ thể từ từng file `demo-01` đến `demo-08`

## 3. Công cụ nên dùng theo từng nhóm bài test

### PowerShell

Dùng cho:

- kiểm tra HTTP / HTTPS
- xem dữ liệu trong database
- spam request để demo rate limit
- chạy unit test hoặc integration test khi cần minh họa kỹ thuật

### Swagger

Dùng cho:

- đăng nhập và lấy JWT
- gọi `me`, `refresh`, `logout`, `forgot-password`, `reset-password`
- xem audit log bằng admin
- kiểm tra nhanh response của backend

Địa chỉ local:

- `https://localhost/swagger-ui.html`

Nếu tunnel/domain public đang bật:

- `https://hackerlo.online/swagger-ui.html`

### Postman

Dùng cho:

- BOLA / IDOR
- webhook HMAC-SHA256
- quản trị enrollment bằng admin
- các tình huống cần giữ nhiều token cùng lúc

Collection:

- `postman/online-course-security.postman_collection.json`

## 4. Lưu ý quan trọng

- Tài liệu trong thư mục này phục vụ kiểm thử kỹ thuật, không phải nội dung hiển thị cho người dùng cuối.
- Khi đã thay đổi seed data trong lúc demo, nên reset lại:

```powershell
docker compose down -v
docker compose up --build -d
```
