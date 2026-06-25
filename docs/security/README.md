# Tài liệu bảo mật và artifact OWASP ZAP

Thư mục này chứa các artifact phục vụ kiểm thử bảo mật, tập trung vào:

- kết quả quét `OWASP ZAP baseline`;
- file cấu hình quét;
- ghi chú liên quan đến TLS, reverse proxy và bề mặt quét.

## 1. Các file chính

- `zap.yaml`: automation plan dùng để chạy ZAP
- `zap-baseline-report.html`
- `zap-baseline-report.json`
- `zap-baseline-report.xml`

## 2. Bối cảnh của artifact hiện có

Các artifact trong thư mục này được giữ lại để lưu vết các lần quét đã thực hiện trong quá trình phát triển và hoàn thiện đồ án.

Target đã từng được dùng cho artifact hiện có:

```text
https://host.docker.internal/swagger-ui.html
```

Ở giai đoạn cấu hình hiện tại, bề mặt quét tham chiếu vẫn tương ứng với:

```text
https://localhost/swagger-ui.html
```

Lý do chọn target này ở giai đoạn trước:

- Swagger UI là bề mặt dễ truy cập, phù hợp cho baseline spider
- Các API cần JWT đã được kiểm thử bổ sung bằng integration test, Postman và script demo vì baseline spider không tự đăng nhập
- `host.docker.internal` trỏ về reverse proxy HTTPS đang expose trên máy host, phù hợp khi chạy ZAP trong container

## 3. Lưu ý với cấu hình hiện tại

Hiện tại Swagger/OpenAPI hoạt động qua HTTPS và ưu tiên các target sau:

- `https://localhost/swagger-ui.html`
- `https://localhost/v3/api-docs`
- `https://host.docker.internal/swagger-ui.html`

Khuyến nghị:

- container ZAP trên cùng máy host dùng `host.docker.internal`;
- ZAP Desktop trên chính máy host dùng `localhost`;
- nếu cần quét đúng bề mặt public đang mở, có thể thay target bằng domain public tương ứng.

Artifact trong thư mục này được giữ nguyên để phục vụ minh chứng cho các lần quét trước, không nên sửa thủ công nội dung báo cáo đã sinh ra.

## 4. Cách chạy lại OWASP ZAP

Nếu cần chạy lại baseline scan bằng container:

```powershell
docker run --rm -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

Lưu ý:

- Lệnh trên phù hợp với artifact hiện có trong thư mục này
- Nếu target quét đã thay đổi so với cấu hình cũ, cần cập nhật `zap.yaml` cho phù hợp trước khi chạy lại

## 5. Kết quả artifact hiện có

Theo lần chạy gần nhất được lưu trong thư mục:

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

Kết quả này đã được chạy lại vào ngày `2026-06-25`.

Hai nhóm warning hiện tại:

- `CSP-related [10055]`
- `Modern Web Application [10109]`

## 6. Cách hiểu nhanh kết quả

- `CSP-related [10055]` chủ yếu xuất phát từ Swagger UI, liên quan đến các cấu hình như wildcard, `unsafe-inline` hoặc `unsafe-eval`
- `Modern Web Application [10109]` là dạng nhận diện kiểu ứng dụng web, không phải lỗ hổng nghiêm trọng về xác thực hay mã hóa
- Không có `FAIL` trong artifact hiện tại

## 7. Liên hệ với deploy và TLS

- Reverse proxy mẫu nằm ở `deploy/nginx/securityapp.conf`
- Ghi chú TLS nằm ở `deploy/ssl/README.md`
- Khi triển khai thật, không nên public backend HTTP thuần ra Internet; nên đặt backend sau reverse proxy HTTPS
