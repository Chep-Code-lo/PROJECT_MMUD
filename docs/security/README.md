# Tài liệu bảo mật và OWASP ZAP

Thư mục này chứa các artifact phục vụ kiểm thử bảo mật, tập trung vào `OWASP ZAP baseline`, kết quả quét và ghi chú liên quan đến `TLS/deploy`.

## 1. File chính

- `zap.yaml`: automation plan dùng để quét
- `zap-baseline-report.html`
- `zap-baseline-report.json`
- `zap-baseline-report.xml`

## 2. Target scan đang dùng

```text
https://host.docker.internal/swagger-ui.html
```

Từ góc nhìn người dùng, cùng bề mặt này tương ứng với:

- `https://localhost/swagger-ui.html`

Lý do chọn target này:

- Swagger UI là bề mặt dễ truy cập và phù hợp cho baseline spider.
- Các API cần JWT đã được kiểm thử bổ sung bằng integration test, Postman và script demo vì baseline spider không tự đăng nhập.
- `host.docker.internal` trỏ về chính reverse proxy HTTPS đang expose trên máy host, phù hợp khi chạy ZAP trong container.

## 3. Lệnh chạy

```powershell
docker run --rm -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

## 4. Kết quả artifact hiện có

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

Kết quả trên đã được chạy lại vào ngày `2026-06-25` bằng chính lệnh trong file này.

Hai nhóm warning hiện tại:

- `CSP-related [10055]`
- `Modern Web Application [10109]`

## 5. Cách hiểu kết quả

- Nhóm `CSP-related [10055]` chủ yếu xuất phát từ Swagger UI, gồm các cảnh báo như wildcard hoặc `unsafe-inline`, `unsafe-eval`.
- `Modern Web Application [10109]` là nhận diện kiểu ứng dụng web, không phải lỗ hổng nghiêm trọng về xác thực hay mã hóa.
- Không có `FAIL` trong artifact hiện tại.

## 6. Liên hệ với deploy và TLS

- Reverse proxy mẫu nằm ở `deploy/nginx/securityapp.conf`
- Ghi chú TLS nằm ở `deploy/ssl/README.md`
- Khi deploy thật, không nên public backend thuần HTTP ra internet; nên đặt backend sau reverse proxy HTTPS
