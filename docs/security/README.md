# Tài liệu bảo mật và OWASP ZAP

Thư mục này chứa các artifact phục vụ kiểm thử bảo mật, tập trung vào `OWASP ZAP baseline`, kết quả quét và ghi chú liên quan đến `TLS/deploy`.

## 1. File chính

- `zap.yaml`: automation plan dùng để quét
- `zap-baseline-report.html`
- `zap-baseline-report.json`
- `zap-baseline-report.xml`

## 2. Target scan da tung dung cho artifact hien co

```text
https://host.docker.internal/swagger-ui.html
```

Truoc khi khoa Swagger ve local-only, cung be mat nay tuong ung voi:

- `https://localhost/swagger-ui.html`

Ly do chon target nay:

- Swagger UI là bề mặt dễ truy cập và phù hợp cho baseline spider.
- Các API cần JWT đã được kiểm thử bổ sung bằng integration test, Postman và script demo vì baseline spider không tự đăng nhập.
- `host.docker.internal` tro ve chinh reverse proxy HTTPS dang expose tren may host, phu hop khi chay ZAP trong container.

## 3. Luu y voi cau hinh hien tai

Hien tai Swagger/OpenAPI da duoc khoa ve local host-only:

- `https://localhost:8444/swagger-ui.html`
- `https://localhost:8444/v3/api-docs`

Cong nay chi bind vao `127.0.0.1` cua may host, vi vay:

- may khac trong mang khong truy cap duoc;
- container ZAP khac cung khong truy cap duoc theo cach cu;
- neu can quet lai Swagger, hay chay ZAP tren chinh may host hoac tam thoi mo cong noi bo phuc vu kiem thu.

Artifact trong thu muc nay duoc giu nguyen de luu vet lan quet truoc.

## 4. Lệnh chạy

```powershell
docker run --rm -v "${PWD}\docs\security:/zap/wrk" ghcr.io/zaproxy/zaproxy:stable zap.sh -cmd -autorun /zap/wrk/zap.yaml
```

## 5. Kết quả artifact hiện có

- `PASS`: `59`
- `WARN`: `2`
- `FAIL`: `0`

Kết quả trên đã được chạy lại vào ngày `2026-06-25` bằng chính lệnh trong file này.

Hai nhom warning hien tai:

- `CSP-related [10055]`
- `Modern Web Application [10109]`

## 6. Cách hiểu kết quả

- Nhóm `CSP-related [10055]` chủ yếu xuất phát từ Swagger UI, gồm các cảnh báo như wildcard hoặc `unsafe-inline`, `unsafe-eval`.
- `Modern Web Application [10109]` là nhận diện kiểu ứng dụng web, không phải lỗ hổng nghiêm trọng về xác thực hay mã hóa.
- Không có `FAIL` trong artifact hiện tại.

## 7. Liên hệ với deploy và TLS

- Reverse proxy mẫu nằm ở `deploy/nginx/securityapp.conf`
- Ghi chú TLS nằm ở `deploy/ssl/README.md`
- Khi deploy thật, không nên public backend thuần HTTP ra internet; nên đặt backend sau reverse proxy HTTPS
