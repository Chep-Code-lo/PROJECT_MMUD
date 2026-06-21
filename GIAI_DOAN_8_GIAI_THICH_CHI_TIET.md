# Giai đoạn 8: Giải thích chi tiết code và ý tưởng

Giai đoạn 8 là bước đưa hệ thống sang hướng `security operations`: không chỉ xác thực và mã hóa, mà còn có khả năng truy vết hành động để phục vụ kiểm tra, giám sát và điều tra sự cố.

## 1. Mục tiêu của giai đoạn 8

- ghi audit log cho authentication và business action
- thêm API `GET /api/audit-logs`
- làm sạch response lỗi
- tăng khả năng chứng minh tính truy vết của hệ thống

## 2. Các file chính của giai đoạn

- `backend/src/main/java/com/company/securityapp/entity/AuditLog.java`
- `backend/src/main/java/com/company/securityapp/repository/AuditLogRepository.java`
- `backend/src/main/java/com/company/securityapp/dto/AuditLogResponse.java`
- `backend/src/main/java/com/company/securityapp/service/AuditLogService.java`
- `backend/src/main/java/com/company/securityapp/controller/AuditLogController.java`
- `backend/src/main/java/com/company/securityapp/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/company/securityapp/AuditLogIntegrationTest.java`

Ngoài ra, các service nghiệp vụ được nối vào audit:

- `AuthService`
- `CustomerService`

## 3. Các action đang được log

Hiện tại hệ thống ghi các action:

- `LOGIN_SUCCESS`
- `LOGIN_FAILED`
- `CREATE_CUSTOMER`
- `UPDATE_CUSTOMER`
- `DELETE_CUSTOMER`

Mỗi log có thể chứa:

- `action`
- `entityType`
- `entityId`
- `actorUserId`
- `actorEmail`
- `success`
- `details`
- `createdAt`

Điểm quan trọng là log phục vụ truy vết nhưng không làm lộ secret như full JWT hoặc AES secret.

## 4. Vì sao `AuditLogService` là trung tâm của stage này

Toàn bộ logic audit được gom về một service riêng thay vì viết rải rác trong controller.

Lợi ích:

- dễ kiểm soát format log
- dễ sửa đổi và kiểm tra
- service nghiệp vụ chỉ cần gọi một hàm rõ nghĩa
- API đọc log chỉ cần đọc từ một nguồn thống nhất

Điều này làm cho phần audit có cấu trúc và dễ chứng minh hơn khi demo.

## 5. Điểm kỹ thuật quan trọng nhất: `REQUIRES_NEW`

`AuditLogService` dùng:

```text
@Transactional(propagation = Propagation.REQUIRES_NEW)
```

cho các hàm ghi log.

Đây là quyết định rất quan trọng.

Ví dụ với login sai:

1. `AuthService.login(...)` ném exception vì sai mật khẩu
2. transaction chính bị rollback
3. nếu audit dùng cùng transaction, `LOGIN_FAILED` cũng bị rollback
4. kết quả là hệ thống mất dấu vết của lần đăng nhập thất bại

Khi dùng `REQUIRES_NEW`, giao dịch ghi log được tách riêng nên log vẫn được commit dù giao dịch chính thất bại.

Đây là điểm rất mạnh về mặt `Accountability`.

## 6. `AuditLogController` được mở cho ai

Route:

- `GET /api/audit-logs`

Chỉ `ADMIN` được xem.

Lý do:

- audit log có thể chứa thông tin vận hành nhạy cảm
- không nên cho role thường truy cập tự do
- đây cũng là cách chứng minh thêm về authorization khi demo

## 7. Giá trị bảo mật của giai đoạn 8

Giai đoạn này giúp hệ thống có bằng chứng cho các câu hỏi:

- ai đã login thành công
- ai đã login thất bại
- ai đã tạo, sửa, xóa customer
- hành động xảy ra vào lúc nào

Vì vậy đồ án không dừng ở `auth + encryption`, mà có thêm lớp `truy vết và giám sát`.

## 8. Liên hệ với frontend và runtime

Audit log hiện được dùng ở cả API và UI:

- API: `/api/audit-logs`
- UI local: `https://localhost/audit-logs`
- UI public: `https://demo.hackerlo.online/audit-logs`

Trong đó:

- local là nơi test nội bộ
- public domain dùng khi cần trình bày từ máy ngoài và `Cloudflare Tunnel` đang bật

## 9. Test của giai đoạn 8 chứng minh điều gì

`AuditLogIntegrationTest` đang cover:

1. login thành công
2. login thất bại
3. create customer
4. update customer
5. delete customer
6. admin đọc được audit log và thấy đủ các action trên

Đây là bằng chứng rất phù hợp cho phần `Accountability/Logging` trong báo cáo Security.

## 10. Cách tóm tắt khi thuyết trình

> Giai đoạn 8 của em là bổ sung khả năng truy vết. Hệ thống không chỉ xác thực và mã hóa, mà còn ghi lại `LOGIN_SUCCESS`, `LOGIN_FAILED` và các hành động trên customer. Em dùng `REQUIRES_NEW` để log vẫn được lưu ngay cả khi giao dịch chính thất bại, nên hệ thống có giá trị hơn về mặt giám sát và điều tra sự cố.
