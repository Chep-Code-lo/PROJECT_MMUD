# Tài liệu bảo mật và OWASP ZAP

Thư mục này chứa artifact bảo mật cho giai đoạn kiểm thử, tập trung vào `OWASP ZAP baseline`, kết quả quét và ghi chú liên quan đến `TLS/deploy`.

## 1. File chính

- `zap.yaml`: automation plan dùng để quét
- `zap-baseline-report.html`
- `zap-baseline-report.json`
- `zap-baseline-report.xml`

## 2. Target scan đang dùng

Target nội bộ của ZAP:

```text
https://nginx/swagger-ui.html
```

Từ góc nhìn người dùng, cùng surface này tương ứng với:

- `https://localhost/swagger-ui.html`
- `https://demo.hackerlo.online/swagger-ui.html`

Lý do chọn target nội bộ:

- Swagger UI là surface public, dễ truy cập và phù hợp cho baseline spider.
- Authenticated API đã được cover bổ sung bằng integration test và Postman/Newman vì baseline spider không tự login JWT.
- Quét trực tiếp `nginx` service trong Docker network phản ánh đúng stack dự án đang chạy.

## 3. Lệnh chạy

```powershell
docker run --rm --network project_mmud_default -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

## 4. Kết quả artifact hiện có

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

Kết quả trên đã được chạy lại và rà ngày `2026-06-21` bằng chính lệnh trong file này.

Hai nhóm warning hiện tại:

- `CSP-related [10055]`
- `Modern Web Application [10109]`

## 5. Cách hiểu kết quả

- Nhóm `CSP-related [10055]` chủ yếu xuất phát từ Swagger UI, gồm các cảnh báo như wildcard hoặc `unsafe-inline`, `unsafe-eval`.
- `Modern Web Application [10109]` là nhận diện kiểu ứng dụng web, không phải lỗ hổng nghiêm trọng về auth hay mã hóa.
- Không có `FAIL` trong artifact hiện tại.

## 6. Liên hệ với local và public domain

- `localhost` là nơi thuận tiện để nhóm tự chạy, tự sửa lỗi và tự test.
- `public domain` là cùng hệ thống đó nhưng được publish ra ngoài qua `Cloudflare Tunnel`.
- Dù người dùng truy cập theo mode nào, ZAP vẫn quét đúng reverse proxy của stack qua target nội bộ `https://nginx/swagger-ui.html`.

## 7. Liên hệ với deploy và TLS

- Reverse proxy mẫu nằm ở `deploy/nginx/securityapp.conf`
- Ghi chú TLS nằm ở `deploy/ssl/README.md`
- Khi deploy thật, không nên public backend thuần HTTP ra internet; nên đặt backend sau reverse proxy HTTPS
