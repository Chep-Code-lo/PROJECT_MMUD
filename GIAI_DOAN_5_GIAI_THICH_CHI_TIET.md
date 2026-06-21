# Giai đoạn 5: Giải thích chi tiết code và ý tưởng

File này giải thích phần nền tảng dữ liệu và mã hóa của dự án. Ở giai đoạn 5, trọng tâm không phải là làm một API CRUD chung chung, mà là xây dựng luồng xử lý dữ liệu nhạy cảm cho ứng dụng mạng dạng `cloud/API-based` dành cho dịch vụ công ty nhỏ.

## 1. Mục tiêu của giai đoạn 5

Giai đoạn 5 có 3 mục tiêu kỹ thuật chính:

1. Chốt schema và domain model cho `Customer` và `AuditLog`.
2. Hoàn thành `Customer API` làm nền cho các giai đoạn bảo mật tiếp theo.
3. Mã hóa dữ liệu nhạy cảm của customer trước khi lưu database.

Ý tưởng cốt lõi:

- client gửi dữ liệu ở dạng plaintext
- backend là nơi thực hiện mã hóa
- database chỉ lưu ciphertext
- backend giải mã lại khi cần trả dữ liệu hợp lệ cho frontend

## 2. Các file chính của giai đoạn

### Database

- `database/schema.sql`
- `database/seed.sql`

### Entity và repository

- `backend/src/main/java/com/company/securityapp/entity/Customer.java`
- `backend/src/main/java/com/company/securityapp/entity/AuditLog.java`
- `backend/src/main/java/com/company/securityapp/repository/CustomerRepository.java`
- `backend/src/main/java/com/company/securityapp/repository/AuditLogRepository.java`

### Service, controller và DTO

- `backend/src/main/java/com/company/securityapp/service/EncryptionService.java`
- `backend/src/main/java/com/company/securityapp/service/CustomerService.java`
- `backend/src/main/java/com/company/securityapp/controller/CustomerController.java`
- `backend/src/main/java/com/company/securityapp/dto/CustomerRequest.java`
- `backend/src/main/java/com/company/securityapp/dto/CustomerResponse.java`

### Xử lý lỗi và test

- `backend/src/main/java/com/company/securityapp/exception/ApiException.java`
- `backend/src/main/java/com/company/securityapp/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/company/securityapp/CustomerControllerIntegrationTest.java`

## 3. Ngữ cảnh bảo mật của `Customer`

Trong đề tài này, `Customer` không chỉ là một bản ghi thông thường. Đây là đối tượng chứa dữ liệu doanh nghiệp và dữ liệu nhạy cảm của khách hàng.

Các trường nghiệp vụ:

- `name`
- `email`
- `phone`
- `address`
- `taxCode`

Trong đó:

- `email` được giữ ở dạng plaintext để phục vụ tìm kiếm và kiểm tra trùng
- `phone`, `address`, `taxCode` được xem là dữ liệu nhạy cảm và phải mã hóa trước khi lưu

Trong database, backend lưu các cột:

- `phone_encrypted`
- `address_encrypted`
- `tax_code_encrypted`
- `key_version`

Điều này giúp giảm thiểu rủi ro khi database bị lộ hoặc khi dữ liệu bị truy cập trái phép ở tầng lưu trữ.

## 4. Kiến trúc code được tách như thế nào

### Controller

Controller chỉ xử lý lớp HTTP:

- nhận request
- map endpoint
- gọi service
- trả response

Controller không chứa logic mã hóa.

### Service

Service là nơi xử lý nghiệp vụ thật:

- chuẩn hóa dữ liệu
- kiểm tra logic business
- gọi mã hóa và giải mã
- gọi repository
- ghi audit action khi cần

### Repository

Repository chỉ phụ trách truy cập database.

### DTO

DTO tách request và response ra khỏi entity để:

- không lộ các cột `_encrypted`
- giữ contract API ổn định
- cho phép thay đổi cách lưu trữ mà không làm vỡ frontend

## 5. Vì sao mã hóa được đặt ở `CustomerService`

`EncryptionService` không được gọi từ controller mà được gọi từ `CustomerService`.

Lý do:

- controller phải giữ nhẹ
- mọi luồng ghi customer đều đi qua cùng một nơi
- giảm rủi ro có endpoint quên mã hóa
- dễ test và dễ mở rộng sau này

Luồng tạo customer:

1. `CustomerController` nhận `CustomerRequest`
2. `CustomerService` chuẩn hóa `email`, `name` và dữ liệu đầu vào
3. `CustomerService` gọi `EncryptionService.encryptCustomerField(...)`
4. entity được lưu vào DB ở dạng ciphertext
5. khi trả ra ngoài, backend giải mã rồi map thành `CustomerResponse`

## 6. `EncryptionService` đang làm gì

Phiên bản hiện tại không chỉ dùng `AES-GCM` đơn giản mà đã được nâng lên theo hướng an toàn hơn:

- dùng `AES/GCM/NoPadding`
- sinh `IV` ngẫu nhiên cho mỗi lần mã hóa
- dùng `HKDF-SHA256` để dẫn xuất key theo phiên bản
- gắn `AAD` theo ngữ cảnh `customer + field + version`
- lưu `key_version` để backend biết cách giải mã đúng dữ liệu

Ý nghĩa bảo mật:

- `Confidentiality`: DB không thấy plaintext
- `Integrity`: nếu sửa ciphertext hoặc tag, giải mã sẽ thất bại
- `Maintainability`: có thể xoay vòng key theo phiên bản

Ngoài luồng mới, service vẫn giữ khả năng đọc dữ liệu cũ để tương thích với `legacy key version`.

## 7. Request và response được tách như thế nào

### `CustomerRequest`

Request chỉ chứa dữ liệu client được phép gửi:

- `name`
- `email`
- `phone`
- `address`
- `taxCode`

### `CustomerResponse`

Response trả ra:

- `id`
- `name`
- `email`
- `phone`
- `address`
- `taxCode`
- `createdAt`
- `updatedAt`

Điểm quan trọng là response không bao giờ trả về:

- `phone_encrypted`
- `address_encrypted`
- `tax_code_encrypted`
- `key_version`

## 8. Ý nghĩa của giai đoạn 5 đối với hệ thống

Sau giai đoạn này, hệ thống đã có:

- một mô hình `Customer` phù hợp với bài toán dữ liệu nhạy cảm
- một API có thể dùng để demo luồng dữ liệu thật
- một cơ chế mã hóa phục vụ đúng trọng tâm môn Security
- nền tảng để các giai đoạn sau gắn thêm `JWT`, authorization, audit log và bằng chứng kiểm thử

Ở runtime hiện tại, luồng customer này được truy cập qua:

- `https://localhost/api/customers`
- `https://demo.hackerlo.online/api/customers`

Trong đó `demo.hackerlo.online` chỉ hoạt động khi `Cloudflare Tunnel` đang bật.

## 9. Test của giai đoạn 5 chứng minh điều gì

`CustomerControllerIntegrationTest` đang chứng minh các điểm quan trọng:

1. tạo customer thành công thì response vẫn là plaintext hợp lệ
2. dữ liệu trong DB không còn giống plaintext ban đầu
3. dữ liệu legacy chưa có `key_version` vẫn đọc được
4. dữ liệu bị tamper sẽ gây lỗi giải mã thay vì trả về dữ liệu giả

Đây là bằng chứng trực tiếp cho yêu cầu “mã hóa dữ liệu nhạy cảm trước khi lưu DB”.

## 10. Cách tóm tắt khi thuyết trình

> Giai đoạn 5 của em là giai đoạn đặt nền cho bảo mật dữ liệu. Em thiết kế `Customer` theo hướng chỉ giữ `email` ở dạng plaintext để tra cứu, còn `phone`, `address`, `taxCode` được mã hóa bằng `AES-GCM` kết hợp `HKDF`, `AAD` và `key_version` trước khi lưu DB. Backend là nơi thực hiện mã hóa và giải mã, nên người dùng vẫn nhìn thấy dữ liệu đúng, còn database chỉ lưu ciphertext.
