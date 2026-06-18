# Giai doan 8: Giai thich chi tiet code va y tuong

Stage 8 la buoc bien backend tu mot CRUD app thanh he thong co the demo duoc tinh truy vet va bao mat van hanh.

## 1. Muc tieu cua giai doan 8

- Ghi audit log cho auth va business action
- Them API `GET /api/audit-logs`
- Lam sach response loi
- Ra soat backend theo huong OWASP API Security co ban

## 2. Cac file chinh da lam

- `backend/src/main/java/com/company/securityapp/entity/AuditLog.java`
- `backend/src/main/java/com/company/securityapp/repository/AuditLogRepository.java`
- `backend/src/main/java/com/company/securityapp/dto/AuditLogResponse.java`
- `backend/src/main/java/com/company/securityapp/service/AuditLogService.java`
- `backend/src/main/java/com/company/securityapp/controller/AuditLogController.java`
- `backend/src/main/java/com/company/securityapp/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/company/securityapp/AuditLogIntegrationTest.java`

Ngoai ra, cac service nghiep vu cung duoc noi vao audit:

- `AuthService`
- `CustomerService`

## 3. Cac action dang duoc log

- `LOGIN_SUCCESS`
- `LOGIN_FAILED`
- `CREATE_CUSTOMER`
- `UPDATE_CUSTOMER`
- `DELETE_CUSTOMER`

Moi log co the luu:

- `action`
- `entityType`
- `entityId`
- `actorUserId`
- `actorEmail`
- `success`
- `details`
- `createdAt`

## 4. Vi sao `AuditLogService` la trung tam cua stage

Tat ca audit logic duoc don vao 1 service rieng thay vi viet tan man o tung controller.

Loi ich:

- de doi format log
- de tim bug
- service nghiep vu chi can goi 1 ham ro nghia
- admin chi doc 1 API duy nhat `/api/audit-logs`

## 5. Diem ky thuat quan trong nhat: `REQUIRES_NEW`

`AuditLogService` dung:

```text
@Transactional(propagation = Propagation.REQUIRES_NEW)
```

Cho cac ham ghi log.

Day la quyet dinh rat quan trong.

Neu khong co `REQUIRES_NEW`, tinh huong login sai se co van de:

1. `AuthService.login(...)` nem exception vi sai password
2. transaction chinh rollback
3. log `LOGIN_FAILED` bi rollback theo
4. ket qua la he thong khong de lai dau vet login fail

`REQUIRES_NEW` tach giao dich ghi log ra rieng, nen log van duoc commit du transaction chinh fail.

## 6. `AuditLogController` duoc mo cho ai

Route:

- `GET /api/audit-logs`

Chi `ADMIN` duoc xem.

Ly do:

- audit log co the chua thong tin nhay cam ve van hanh
- khong nen de role thuong doc tu do
- dung de demo phan quyen ro rang trong Swagger/Postman/UI

## 7. Gia tri bao mat cua giai doan 8

Stage nay giup he thong co bang chung cho:

- ai da login thanh cong
- ai da login that bai
- ai da tao/sua/xoa customer
- hanh dong xay ra luc nao

Day la phan giup de tai co tinh "security operations" thay vi chi dung ma hoa du lieu.

## 8. Test cua giai doan 8 chung minh dieu gi

`AuditLogIntegrationTest` cover:

1. login thanh cong
2. login that bai
3. create customer
4. update customer
5. delete customer
6. admin doc duoc audit log va thay du cac action tren

## 9. Cach tom tat khi thuyet trinh

> Giai doan 8 cua em la them kha nang truy vet. He thong khong chi xac thuc va ma hoa, ma con ghi lai login success, login failed va business action tren customer. Em dung `REQUIRES_NEW` de log van duoc luu ngay ca khi giao dich chinh fail.
