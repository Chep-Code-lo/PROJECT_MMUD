# Tài liệu kiểm thử bảo mật

Đây là thư mục tổng hợp các tài liệu kiểm thử bảo mật của đồ án. Mục tiêu là giúp bạn có đủ tài liệu để:

- kiểm thử thủ công bằng Postman;
- quét cơ bản bằng OWASP ZAP;
- trình bày demo tấn công và phòng thủ trước giảng viên.

## 1. Các tài liệu chính

- `docs/postman-testing.md`: hướng dẫn kiểm thử thủ công bằng Postman
- `docs/owasp-zap-testing.md`: hướng dẫn quét và đọc kết quả OWASP ZAP
- `docs/demo-runbook.md`: kịch bản demo tổng hợp theo thứ tự trình bày
- `docs/demo-01-password-bcrypt.md`: demo băm mật khẩu bằng bcrypt
- `docs/demo-02-jwt.md`: demo JWT, Bearer token, refresh token
- `docs/demo-03-aes-encryption.md`: demo mã hóa dữ liệu nhạy cảm bằng AES-GCM
- `docs/demo-04-bola-idor.md`: demo chống BOLA / IDOR
- `docs/demo-05-webhook-hmac.md`: demo webhook HMAC-SHA256
- `docs/demo-06-rate-limit.md`: demo rate limiting
- `docs/demo-07-https-tls.md`: demo HTTPS / TLS

## 2. Nên bắt đầu từ đâu

Nếu bạn cần kiểm thử nhanh:

1. Đọc `docs/demo-runbook.md`
2. Mở `docs/postman-testing.md`
3. Mở `docs/owasp-zap-testing.md` khi cần phần quét bảo mật

Nếu bạn cần viết báo cáo hoặc chuẩn bị thuyết trình:

1. Dùng `docs/demo-runbook.md` làm kịch bản tổng quát
2. Mở từng file `demo-01` đến `demo-07` để lấy bước thực hiện chi tiết

## 3. Công cụ nên dùng theo từng nhóm bài test

### PowerShell

Dùng cho:

- kiểm tra HTTP và HTTPS;
- xem dữ liệu trong database;
- spam request để demo rate limit;
- chạy test tự động khi cần minh họa kỹ thuật.

### Swagger

Dùng cho:

- đăng nhập;
- lấy JWT;
- gọi API profile, chứng chỉ, duyệt khóa học admin, audit log;
- kiểm tra nhanh response của backend trên máy host.

Địa chỉ:

- `https://localhost/swagger-ui.html`

### Postman

Dùng cho:

- BOLA/IDOR;
- webhook HMAC-SHA256;
- các tình huống cần giữ nhiều token cùng lúc.

Collection:

- `postman/online-course-security.postman_collection.json`

## 4. Lưu ý quan trọng

- Swagger dùng cùng cổng `443`; khi tunnel public đang bật có thể truy cập cả qua domain public để phục vụ demo từ xa
- Các tài liệu trong thư mục này phục vụ kiểm thử kỹ thuật, không phải nội dung hiển thị cho người dùng cuối
- Khi đã thay đổi dữ liệu seed trong lúc demo, nên reset lại bằng:

```powershell
docker compose down -v
docker compose up --build -d
```
