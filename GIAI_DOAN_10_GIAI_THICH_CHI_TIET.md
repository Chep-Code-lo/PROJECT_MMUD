# Giai đoạn 10: Giải thích chi tiết code và ý tưởng

Giai đoạn 10 là bước đóng gói toàn bộ hệ thống để có thể demo và nộp bài một cách hoàn chỉnh. Trọng tâm của giai đoạn này là tạo ra bộ bằng chứng kiểm thử, tài liệu API, cấu hình runtime và ghi chú triển khai an toàn.

## 1. Mục tiêu của giai đoạn 10

- `Swagger/OpenAPI` phải khớp với code thật
- có `Postman` collection và environment để kiểm thử
- có `OWASP ZAP` artifact lưu lại
- `Docker Compose` phải boot được đầy đủ stack
- có cấu hình `Nginx/TLS` để trình bày hướng deploy an toàn
- có tùy chọn `public domain` bên cạnh `localhost`

## 2. Các file chính của giai đoạn

- `docs/api/openapi.json`
- `docs/api/README.md`
- `docs/postman/securityapp.postman_collection.json`
- `docs/postman/securityapp.local.postman_environment.json`
- `docs/postman/securityapp.demo.hackerlo_environment.json`
- `docs/postman/README.md`
- `docs/security/zap.yaml`
- `docs/security/zap-baseline-report.html`
- `docs/security/zap-baseline-report.json`
- `docs/security/zap-baseline-report.xml`
- `docs/security/README.md`
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `docker-compose.yml`
- `docker-compose.public-domain.yml`
- `deploy/nginx/securityapp.conf`
- `deploy/ssl/README.md`
- `deploy/cloudflared/start-hackerlo-tunnel.ps1`

## 3. Swagger và OpenAPI được chốt như thế nào

Ở runtime hiện tại, người dùng có thể mở tài liệu API qua:

- local: `https://localhost/swagger-ui.html`
- public domain: `https://demo.hackerlo.online/swagger-ui.html`

OpenAPI JSON:

- local: `https://localhost/v3/api-docs`
- public domain: `https://demo.hackerlo.online/v3/api-docs`

Ngoài ra, khi phát triển nhanh backend độc lập vẫn có thể dùng:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

Swagger được giữ public vì:

- dễ demo nhanh
- dễ test request/response
- dễ đối chiếu schema với Postman và checklist test

## 4. Postman collection được thiết kế theo hướng nào

Collection được chia theo các luồng chính:

- auth
- admin summary
- customers
- audit
- security checks

Hiện tại có 2 environment:

- `Security App Local` cho `https://localhost`
- `Security App Public Domain` cho `https://demo.hackerlo.online`

Request login tự lưu token, request tạo customer tự lưu `customerId`, nên người demo có thể chạy liền mạch mà không phải copy tay quá nhiều.

## 5. OWASP ZAP được cấu hình ra sao

`docs/security/zap.yaml` hiện đặt target:

```text
https://nginx/swagger-ui.html
```

Lý do:

- đây là target đúng với reverse proxy của stack đang chạy trong Docker network
- `Swagger UI` là surface public phù hợp cho baseline spider
- các API có JWT được kiểm tra bổ sung bằng Postman/Newman và integration test

Kết quả artifact hiện có:

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

Hai nhóm warning còn lại:

- `CSP-related [10055]`
- `Modern Web Application [10109]`

## 6. Docker Compose và runtime đang đóng vai trò gì

`docker-compose.yml` dựng đầy đủ:

- `database`
- `backend`
- `frontend`
- `nginx`

Stack này phục vụ mode local:

- `https://localhost`

`docker-compose.public-domain.yml` là file override để dùng cùng stack đó cho mode public:

- `https://demo.hackerlo.online`

Mode public chỉ hoạt động khi `Cloudflare Tunnel` đang bật. Nếu tunnel tắt, hệ thống vẫn chạy đầy đủ ở `https://localhost`.

## 7. Vai trò của `Nginx`, TLS và public domain

`deploy/nginx/securityapp.conf` và `deploy/ssl/README.md` dùng để giải thích hướng triển khai an toàn:

- backend không nên public HTTP trực tiếp ra internet
- ứng dụng nên đi qua `Nginx HTTPS edge`
- TLS nên terminate ở reverse proxy
- local dùng cert trong `deploy/ssl/`
- public domain được publish qua `Cloudflare Tunnel`, không thay thế mode local

Điều này giúp phần “cloud” của đề tài rõ ràng hơn mà vẫn giữ được khả năng cho các thành viên nhóm test nội bộ.

## 8. Vì sao giai đoạn 10 quan trọng với môn học

Môn học không chỉ chấm code chạy.

Nó còn chấm:

- API có tài liệu hay không
- có bằng chứng test hay không
- có security scan hay không
- có mô hình deploy an toàn hay không

Giai đoạn 10 là nơi đóng gói tất cả các bằng chứng đó thành bộ artifact có thể nộp, có thể trình bày và có thể chạy lại.

## 9. Cách tóm tắt khi thuyết trình

> Giai đoạn 10 của em là bước đóng gói hệ thống để demo và nộp bài. Em có `Swagger/OpenAPI` để tài liệu hóa API, `Postman/Newman` để kiểm thử auth và role, `OWASP ZAP` để quét reverse proxy của stack đang chạy, `Docker Compose` để dựng toàn bộ hệ thống, và `Nginx/TLS` cùng `Cloudflare Tunnel` để trình bày hướng truy cập local lẫn public domain một cách an toàn.
