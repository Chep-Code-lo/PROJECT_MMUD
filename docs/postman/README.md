# Tài liệu Postman và Newman

Thư mục này chứa collection và environment để test nhanh `auth`, `customer`, `audit log` và `admin summary`.

## 1. File chính

- `securityapp.postman_collection.json`
- `securityapp.local.postman_environment.json`
- `securityapp.demo.hackerlo_environment.json`

## 2. Chọn environment

| Environment | Base URL | Dùng khi nào |
|---|---|---|
| `Security App Local` | `https://localhost` | Test nội bộ trên máy đang chạy dự án |
| `Security App Public Domain` | `https://demo.hackerlo.online` | Test từ máy ngoài khi `Cloudflare Tunnel` đang bật |

## 3. Cách import

1. Import collection `securityapp.postman_collection.json`.
2. Import một hoặc cả hai environment.
3. Chọn environment phù hợp trước khi chạy request.

## 4. Biến môi trường quan trọng

- `baseUrl`
- `adminEmail`, `staffEmail`, `userEmail`
- `adminPassword`, `staffPassword`, `userPassword`
- `token`
- `adminToken`
- `staffToken`
- `userToken`
- `customerId`
- `customerEmail`

## 5. Thứ tự chạy để demo nhanh

1. `Auth / Login as Admin`
2. `Auth / Get Current User`
3. `Admin / Summary`
4. `Customers / Create Customer`
5. `Customers / Get Customer By Id`
6. `Customers / Update Customer`
7. `Customers / Delete Customer`
8. `Security Checks / Customers as Staff (Expect 200)`
9. `Security Checks / Customers as User (Expect 403)`
10. `Security Checks / Audit Logs as User (Expect 403)`
11. `Security Checks / Auth Me without Token (Expect 401)`
12. `Audit / List Audit Logs`

## 6. Lưu ý quan trọng

- Request login sẽ tự động lưu `token` vào environment.
- `Login as Admin` sẽ lưu thêm `adminToken`.
- `Login as Staff` sẽ lưu `staffToken`.
- `Login as User` sẽ lưu `userToken`.
- Request create customer sẽ tự động lưu `customerId`.
- Body tạo customer dùng Postman dynamic variable `{{$timestamp}}` để giảm trùng lặp khi chạy lại nhiều lần.
- Collection đã được rút gọn để tập trung vào `auth`, `customer CRUD` và `security checks`.

## 7. Chạy bằng Newman

### Chạy local

```powershell
npx --yes newman run docs/postman/securityapp.postman_collection.json -e docs/postman/securityapp.local.postman_environment.json --insecure --reporters cli
```

Nếu sau này local đổi sang cert đã được máy tin cậy, có thể bỏ `--insecure`:

```powershell
npx --yes newman run docs/postman/securityapp.postman_collection.json -e docs/postman/securityapp.local.postman_environment.json --reporters cli
```

### Chạy public domain

```powershell
npx --yes newman run docs/postman/securityapp.postman_collection.json -e docs/postman/securityapp.demo.hackerlo_environment.json --reporters cli
```

Ghi chú:

- Environment public domain chỉ chạy được khi `https://demo.hackerlo.online` đang được publish qua `Cloudflare Tunnel`.
- Collection đã được verify thành công bằng Newman trong repo này.

## 8. Kết quả rà ngày `2026-06-21`

- Local: `15 requests`, `22 assertions`, `0 failed`
- Public domain: `15 requests`, `22 assertions`, `0 failed`
- Bộ request đã cover lại các nhóm `auth`, `admin summary`, `customer CRUD`, `audit log`, `401` và `403`
