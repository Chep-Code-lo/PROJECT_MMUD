# Giai đoạn 6: Giải thích chi tiết code và ý tưởng

Giai đoạn 6 tập trung vào việc làm chắc `REST contract` của `Customer API`. Với một ứng dụng mạng dạng `cloud/API-based`, contract API phải rõ ràng, status code phải đúng và response không được làm lộ chi tiết lưu trữ nội bộ.

## 1. Mục tiêu của giai đoạn 6

Giai đoạn 6 có 4 mục tiêu kỹ thuật:

1. chốt CRUD customer theo đúng HTTP method và status code
2. làm sạch validation và thông điệp lỗi
3. giữ contract API ổn định cho frontend, Swagger và Postman
4. chuẩn bị nền cho authorization, audit log và security testing

## 2. Các file chính cần đọc

- `backend/src/main/java/com/company/securityapp/dto/CustomerRequest.java`
- `backend/src/main/java/com/company/securityapp/dto/CustomerResponse.java`
- `backend/src/main/java/com/company/securityapp/service/CustomerService.java`
- `backend/src/main/java/com/company/securityapp/controller/CustomerController.java`
- `backend/src/main/java/com/company/securityapp/exception/ApiException.java`
- `backend/src/main/java/com/company/securityapp/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/company/securityapp/CustomerControllerIntegrationTest.java`

## 3. Contract REST đã được chốt như thế nào

API hiện tại gồm:

- `GET /api/customers`
- `GET /api/customers/{id}`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`

Status code được quy ước rõ:

- `200` cho `GET` và `PUT` thành công
- `201` cho `POST`
- `204` cho `DELETE`
- `400` cho validation sai
- `404` cho customer không tồn tại
- `409` cho email bị trùng

Điều này quan trọng vì toàn bộ frontend, Swagger, Postman/Newman và checklist test đều dựa vào contract này.

## 4. Vì sao phải tách `CustomerRequest` và `CustomerResponse`

Nếu dùng entity làm request/response trực tiếp sẽ có các vấn đề:

1. dễ lộ field lưu trữ nội bộ như `_encrypted`
2. khó thay đổi schema mà không ảnh hưởng frontend
3. frontend bị phụ thuộc quá nhiều vào cấu trúc persistence

DTO giúp backend giữ quyền kiểm soát contract API và che đi cách dữ liệu đang được mã hóa phía sau.

## 5. Validation được đặt ở đâu

Validation được đặt trên `CustomerRequest`.

Lý do:

- request sai bị chặn sớm
- service không phải gánh các lỗi dữ liệu cơ bản
- `Swagger/OpenAPI` đọc được schema rõ hơn

Các logic nghiệp vụ vẫn thuộc về service, ví dụ:

- chuẩn hóa email
- kiểm tra trùng email
- mã hóa dữ liệu nhạy cảm
- ghi audit action

## 6. `CustomerService` xử lý nghiệp vụ gì

`CustomerService` là nơi liên kết giữa:

- lớp HTTP
- lớp lưu trữ
- lớp mã hóa
- lớp audit

Những việc service đang làm:

- tìm customer theo id
- chuẩn hóa `name` và `email`
- kiểm tra duplicate email
- mã hóa `phone`, `address`, `taxCode`
- giải mã khi trả response
- ghi `CREATE_CUSTOMER`, `UPDATE_CUSTOMER`, `DELETE_CUSTOMER`

Điểm quan trọng là service đang xử lý theo ngữ cảnh bảo mật ứng dụng mạng, không chỉ là CRUD thuần túy.

## 7. Vì sao thông điệp lỗi phải rõ nhưng vẫn an toàn

Ở bài này, API không chỉ cần “chạy được” mà còn cần thể hiện được tính đúng đắn về bảo mật.

Vì vậy:

- validation sai phải trả `400`
- không tìm thấy dữ liệu phải trả `404`
- trùng email phải trả `409`
- lỗi server không được làm lộ stack trace hoặc chi tiết mã hóa

`GlobalExceptionHandler` giúp thống nhất format lỗi trên toàn bộ backend.

Ví dụ về mặt security:

- khi ciphertext bị tamper, hệ thống không trả chi tiết cryptographic failure ra ngoài
- client chỉ nhận lỗi server tổng quát

## 8. Giai đoạn 6 đóng vai trò gì trong toàn hệ thống

Sau giai đoạn này:

- frontend có endpoint thật để gọi
- `Swagger` có contract đúng để hiển thị
- `Postman/Newman` có status code đúng để assert
- authorization ở giai đoạn sau có tài nguyên thật để khóa role
- audit log có hành động thật để ghi

Nói ngắn gọn, đây là giai đoạn biến phần lưu trữ mã hóa thành một `REST API` có thể kiểm thử và chứng minh được.

## 9. Liên hệ với runtime hiện tại

Ở môi trường chạy hiện tại, `Customer API` được đi qua `Nginx HTTPS edge`, không public raw backend ra ngoài.

Các địa chỉ truy cập:

- local: `https://localhost/api/customers`
- public domain: `https://demo.hackerlo.online/api/customers`

Trong đó:

- `localhost` là mode fallback để cả nhóm tự test
- `demo.hackerlo.online` là mode public khi `Cloudflare Tunnel` đang bật

## 10. Test cần chứng minh gì

Khi review giai đoạn 6, cần chứng minh được:

1. tạo customer thành công và trả `201`
2. validation sai trả `400`
3. response không lộ các cột `_encrypted`
4. dữ liệu trong DB là ciphertext
5. dữ liệu bị tamper sẽ không được backend chấp nhận

## 11. Cách tóm tắt khi thuyết trình

> Giai đoạn 6 của em là bước chốt `Customer API` thành một REST API đúng nghĩa: có DTO riêng, validation rõ, status code rõ, response sạch và không lộ chi tiết lưu trữ. Đây là nền để frontend, Swagger, Postman và các giai đoạn authorization, audit hoạt động đúng với cùng một contract.
