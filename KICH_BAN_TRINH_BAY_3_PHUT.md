# Kịch bản trình bày dự án trong khoảng 3 phút

## 1. Mục tiêu của dự án

Hôm nay em trình bày dự án bảo mật ứng dụng mạng theo hướng `REST API + mật mã ứng dụng + kiểm thử bảo mật API`.

Dự án của em gồm:

- `Frontend`: Next.js
- `Backend`: Spring Boot, Spring Security
- `Authentication`: JWT
- `Password Hashing`: bcrypt
- `Bảo vệ dữ liệu nhạy cảm`: AES-GCM
- `Tài liệu và kiểm thử API`: Swagger/OpenAPI, Postman/Newman, OWASP ZAP
- `Triển khai local`: Docker Compose

Điểm em muốn nhấn mạnh là dự án này không chỉ có API CRUD thông thường, mà em tập trung chứng minh các cơ chế mật mã đang gắn vào luồng nghiệp vụ thật.

## 2. Hệ thống đang bảo vệ cái gì

Trong dự án này có 3 nhóm dữ liệu cần bảo vệ:

1. `Password` của user
   Em không lưu plaintext trong database mà hash bằng `BCryptPasswordEncoder`.

2. `Token đăng nhập`
   Sau khi login thành công, backend phát hành `JWT` để frontend dùng cho các request tiếp theo.

3. `Dữ liệu nhạy cảm của customer`
   Các trường `phone`, `address`, `taxCode` được mã hóa bằng `AES-GCM` trước khi lưu xuống database.

Nghĩa là mỗi loại dữ liệu được bảo vệ bằng một cơ chế khác nhau:

- bcrypt cho password at rest
- JWT cho xác thực và phân quyền
- AES-GCM cho dữ liệu nhạy cảm trong database

## 3. Luồng hoạt động chính

Luồng chính của hệ thống là:

1. User đăng nhập bằng email và password.
2. Backend dùng Spring Security và bcrypt để xác thực.
3. Nếu đúng, hệ thống sinh ra JWT.
4. Frontend lưu token và gửi lại ở header `Authorization: Bearer`.
5. Backend kiểm tra token qua `JwtAuthenticationFilter`.
6. Nếu role hợp lệ thì mới cho phép gọi các API như `customers` hoặc `audit-logs`.

Về phân quyền:

- `ADMIN` xem được `admin summary`, `audit logs`, `customers`
- `STAFF` thao tác được `customers`
- `USER` không được vào `customers` và `audit logs`

## 4. Phần mật mã ứng dụng em đã chứng minh

Phần quan trọng nhất của bài là em không chỉ mô tả lý thuyết, mà em kiểm tra trực tiếp từ source code và runtime:

### JWT

- Login thật để lấy JWT
- Decode token để xem `alg`, `sub`, `iat`, `exp`
- Kiểm tra token hợp lệ thì `200`
- Không có token hoặc token giả, token sửa, token hết hạn thì `401`
- Token `USER` gọi API `ADMIN` thì `403`

### bcrypt

- Query bảng `users`
- Xác nhận password hash có dạng bcrypt như `$2a$10$...`
- Các user dùng cùng mật khẩu nhưng hash khác nhau
- Điều đó chứng minh có salt và không lưu plaintext

### AES-GCM

- Tạo customer bằng API
- Query bảng `customers`
- Xác nhận `phone_encrypted`, `address_encrypted`, `tax_code_encrypted` là ciphertext chứ không phải dữ liệu gốc
- Sau đó thử sửa ciphertext trong database
- Khi gọi lại API đọc customer thì hệ thống không giải mã được và trả lỗi

Ý nghĩa của bước này là em chứng minh được `AES-GCM` không chỉ che giấu dữ liệu mà còn kiểm tra được tính toàn vẹn của dữ liệu.

## 5. Công cụ kiểm thử em sử dụng

Để hỗ trợ kiểm thử và tài liệu hóa API, em dùng:

- `Swagger/OpenAPI` để xem và test endpoint
- `Postman/Newman` để chạy các luồng auth, customer CRUD, `401`, `403`
- `OWASP ZAP` để quét bề mặt public như Swagger UI

Ngoài ra em còn kiểm tra trực tiếp database để chứng minh phần bcrypt và AES, vì chỉ test API thôi thì chưa đủ để kết luận về mật mã ứng dụng.

## 6. Kết luận ngắn

Kết luận của em là:

1. Dự án đã triển khai đúng các thành phần bảo mật cốt lõi theo hướng mật mã ứng dụng.
2. Password được bảo vệ bằng bcrypt.
3. Token truy cập được bảo vệ bằng JWT và Spring Security.
4. Dữ liệu nhạy cảm của customer được bảo vệ bằng AES-GCM trong database.
5. Hệ thống có tài liệu API và có các bước kiểm thử bằng Swagger, Postman và ZAP.

Nếu triển khai thực tế thêm, phần em sẽ cải thiện tiếp là:

- bỏ secret mặc định khỏi compose
- bật HTTPS/TLS đầy đủ khi deploy
- thêm refresh token hoặc cơ chế revoke
- thêm rate limiting cho login

## 7. Bản nói nhanh 3 phút

Nếu cần nói liền mạch trong khoảng 3 phút, có thể nói theo đúng đoạn sau:

```text
Hôm nay em trình bày dự án bảo mật ứng dụng mạng theo hướng REST API kết hợp mật mã ứng dụng. Dự án của em dùng frontend Next.js, backend Spring Boot, Spring Security, JWT, bcrypt, AES-GCM, Swagger/OpenAPI, Postman/Newman, OWASP ZAP và Docker Compose.

Điểm em tập trung không phải chỉ là CRUD, mà là chứng minh các cơ chế bảo mật đang gắn vào luồng nghiệp vụ thật.

Trong dự án này có 3 nhóm dữ liệu chính cần bảo vệ. Thứ nhất là password của user. Em không lưu password dạng plaintext mà hash bằng bcrypt. Thứ hai là token đăng nhập. Sau khi login thành công, backend phát hành JWT để frontend dùng cho các request tiếp theo. Thứ ba là dữ liệu nhạy cảm của customer, cụ thể là số điện thoại, địa chỉ và mã số thuế. Ba trường này được mã hóa bằng AES-GCM trước khi lưu xuống database.

Luồng hoạt động là user đăng nhập bằng email và password, Spring Security dùng bcrypt để xác thực. Nếu đúng thì hệ thống sinh JWT. Frontend lưu token và gửi lại qua header Authorization. Ở backend, JwtAuthenticationFilter kiểm tra token, sau đó hệ thống áp role authorization. ADMIN được xem admin summary, audit logs và customers. STAFF thao tác được customers. USER sẽ bị chặn ở customers và audit logs.

Phần quan trọng nhất là em đã kiểm tra trực tiếp từ source code và runtime. Với JWT, em login thật để lấy token, decode token để xem alg, sub, iat, exp, rồi test token hợp lệ trả 200, token giả hoặc hết hạn trả 401, còn user gọi API admin thì trả 403. Với bcrypt, em query bảng users và xác nhận hash có dạng $2a$10, cùng một mật khẩu nhưng hash khác nhau nên chứng minh có salt và không lưu plaintext. Với AES-GCM, em tạo customer bằng API, rồi query bảng customers để xác nhận các cột phone_encrypted, address_encrypted và tax_code_encrypted là ciphertext. Sau đó em còn thử sửa ciphertext trong database, khi gọi lại API thì hệ thống không giải mã được, điều đó chứng minh AES-GCM đang bảo vệ cả tính bí mật lẫn tính toàn vẹn dữ liệu.

Ngoài ra em dùng Swagger để tài liệu hóa và test API, dùng Postman và Newman để test các luồng auth và phân quyền, và dùng OWASP ZAP để quét bề mặt public. Kết luận của em là dự án đã triển khai đúng các thành phần cốt lõi của mật mã ứng dụng, gồm bcrypt cho password, JWT cho xác thực và phân quyền, và AES-GCM cho dữ liệu nhạy cảm trong database.
```
