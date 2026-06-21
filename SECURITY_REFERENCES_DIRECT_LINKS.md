# Tài liệu tham khảo bảo mật và liên kết trực tiếp

File này gom các tài liệu tham khảo chính thống để tiện mở nhanh, chèn vào báo cáo hoặc dùng khi trả lời giảng viên. Danh sách được rà lại ngày `2026-06-21`.

## 1. Chuẩn và tài liệu mật mã cốt lõi

### `R1` Argon2id

- Tài liệu: `RFC 9106 - Argon2 Memory-Hard Function for Password Hashing and Proof-of-Work Applications`
- Vai trò: tham chiếu cho password hashing hiện đại
- Link:

```text
https://datatracker.ietf.org/doc/html/rfc9106
```

### `R2` OWASP Password Storage

- Tài liệu: `OWASP Password Storage Cheat Sheet`
- Vai trò: thực hành lưu password an toàn
- Link:

```text
https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
```

### `R3` AES-GCM

- Tài liệu: `NIST SP 800-38D - Recommendation for Block Cipher Modes of Operation: GCM and GMAC`
- Vai trò: tham chiếu cho `AES-GCM`
- Link:

```text
https://csrc.nist.gov/pubs/sp/800/38/d/final
```

### `R4` HKDF

- Tài liệu: `RFC 5869 - HMAC-based Extract-and-Expand Key Derivation Function (HKDF)`
- Vai trò: tham chiếu cho `HKDF-SHA256`
- Link:

```text
https://datatracker.ietf.org/doc/html/rfc5869
```

### `R5` JWT

- Tài liệu: `RFC 7519 - JSON Web Token (JWT)`
- Vai trò: tham chiếu cho JWT access token
- Link:

```text
https://datatracker.ietf.org/doc/html/rfc7519
```

### `R6` TLS 1.3

- Tài liệu: `RFC 8446 - The Transport Layer Security (TLS) Protocol Version 1.3`
- Vai trò: tham chiếu cho HTTPS/TLS ở lớp edge
- Link:

```text
https://datatracker.ietf.org/doc/html/rfc8446
```

## 2. Hướng dẫn OWASP

### `R7` Cryptographic Storage

- Tài liệu: `OWASP Cryptographic Storage Cheat Sheet`
- Vai trò: nguyên tắc bảo vệ dữ liệu nhạy cảm khi lưu trữ
- Link:

```text
https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html
```

### `R8` REST Security

- Tài liệu: `OWASP REST Security Cheat Sheet`
- Vai trò: hardening cho REST API, token, status code, error handling
- Link:

```text
https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html
```

### `R9` API Security Top 10

- Tài liệu: `OWASP API Security Top 10 (2023)`
- Vai trò: tham chiếu các rủi ro như `broken authentication`, `broken authorization`, `BOLA/IDOR`
- Link:

```text
https://owasp.org/API-Security/editions/2023/en/0x11-t10/
```

### `R10` Logging

- Tài liệu: `OWASP Logging Cheat Sheet`
- Vai trò: audit logging không làm lộ secret và vẫn hỗ trợ điều tra
- Link:

```text
https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
```

### `R11` ASVS

- Tài liệu: `OWASP Application Security Verification Standard (ASVS)`
- Vai trò: khung tham chiếu verification cho ứng dụng web và API
- Link:

```text
https://owasp.org/www-project-application-security-verification-standard/
```

## 3. Nền tảng và triển khai

### `R12` Spring Security

- Tài liệu: `Spring Security Reference`
- Vai trò: authentication, authorization, filter chain, exception handling
- Link:

```text
https://docs.spring.io/spring-security/reference/index.html
```

### `R13` Spring Boot Externalized Configuration

- Tài liệu: `Spring Boot Externalized Configuration`
- Vai trò: quản lý `env vars` và secret ngoài source code
- Link:

```text
https://docs.spring.io/spring-boot/reference/features/external-config.html
```

### `R14` NGINX Reverse Proxy

- Tài liệu: `NGINX Reverse Proxy`
- Vai trò: reverse proxy, TLS edge, route forwarding
- Link:

```text
https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/
```

### `R15` Docker Compose

- Tài liệu: `Docker Compose Documentation`
- Vai trò: dựng stack demo nhiều service
- Link:

```text
https://docs.docker.com/compose/
```

### `R16` Cloudflare Tunnel

- Tài liệu: `Cloudflare Tunnel Documentation`
- Vai trò: publish cùng stack local ra public domain mà không phải public trực tiếp backend
- Link:

```text
https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/
```

## 4. Tài liệu API và công cụ kiểm thử

### `R17` OpenAPI

- Tài liệu: `OpenAPI Specification`
- Vai trò: contract API và mô tả schema
- Link:

```text
https://spec.openapis.org/oas/latest.html
```

### `R18` Swagger

- Tài liệu: `Swagger Documentation`
- Vai trò: Swagger UI và tài liệu hóa API
- Link:

```text
https://swagger.io/docs/
```

### `R19` Postman / Newman

- Tài liệu: `Postman Learning Center`
- Vai trò: test API thủ công và tự động
- Link:

```text
https://learning.postman.com/docs/
```

### `R20` OWASP ZAP

- Tài liệu: `OWASP ZAP Documentation`
- Vai trò: quét DAST cho web/API edge
- Link:

```text
https://www.zaproxy.org/docs/
```

## 5. Mẫu chèn vào báo cáo

Nếu cần chèn ngắn gọn ở cuối báo cáo Word, có thể dùng mẫu:

```text
[R1] IETF RFC 9106, Argon2 Memory-Hard Function for Password Hashing and Proof-of-Work Applications.
[R3] NIST SP 800-38D, Recommendation for Block Cipher Modes of Operation: GCM and GMAC.
[R5] IETF RFC 7519, JSON Web Token (JWT).
[R6] IETF RFC 8446, The Transport Layer Security (TLS) Protocol Version 1.3.
[R9] OWASP API Security Top 10 (2023).
[R12] Spring Security Reference.
[R14] NGINX Reverse Proxy Documentation.
[R20] OWASP ZAP Documentation.
```
