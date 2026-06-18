# Giai doan 8: Giai thich chi tiet code va y tuong

Stage 8 cua Ban 2 khong chi la them bang `audit_logs`, ma la buoc chuyen backend tu CRUD app thanh mot he thong co the demo duoc tinh bao mat va truy vet hanh dong.

## 1. Muc tieu cua Stage 8

- Ghi audit log cho auth va business action
- Them API `GET /api/audit-logs`
- Lam sach response loi
- Ra soat lai backend theo huong OWASP API Security Top 10 co ban

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
- `TicketService`

## 3. Cac action dang duoc log

- `LOGIN_SUCCESS`
- `LOGIN_FAILED`
- `CREATE_CUSTOMER`
- `UPDATE_CUSTOMER`
- `DELETE_CUSTOMER`
- `CREATE_TICKET`
- `UPDATE_TICKET`
- `UPDATE_TICKET_STATUS`
- `DELETE_TICKET`

Moi log co the luu:

- `action`
- `entityType`
- `entityId`
- `actorUserId`
- `actorEmail`
- `success`
- `details`
- `createdAt`

## 4. Vi sao `AuditLogService` la trung tam cua Stage 8

Tat ca audit logic duoc don vao 1 service rieng thay vi viet tan man o tung controller.

Loi ich:

- de doi format log
- de tim bug
- service nghiep vu chi can goi 1 ham ro nghia
- frontend va admin chi doc 1 API duy nhat `/api/audit-logs`

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

Dung `REQUIRES_NEW` se mo transaction rieng cho audit log, nen du request chinh fail thi audit van duoc commit.

## 6. Lay actor tu dau

Voi business action, `AuditLogService` doc `SecurityContextHolder`.

Neu principal la `User` thi log duoc:

- `actorUserId`
- `actorEmail`

Neu chua dang nhap hoac la anonymous thi bo trong.

Voi login success/failed, actor duoc ghi truc tiep tu `AuthService` vi luc do request dang o giai doan xac thuc.

## 7. Hardening backend da lam gi

### Khong lo password hash

- `User.passwordHash` co `@JsonIgnore`
- `AuthResponse` va `UserResponse` chi tra thong tin can thiet

### Khong lo AES secret

- key AES chi doc tu config
- khong co endpoint nao tra secret
- khong log secret ra audit

### Khong log full token

- audit login chi ghi email va ket qua thanh cong hay that bai
- khong nhat token vao `details`

### Khong tra stacktrace ky thuat

`GlobalExceptionHandler` tra body JSON gon:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `details` neu la validation

Client se khong nhin thay stacktrace Java thuan.

### Validation va malformed request

Da cover:

- `MethodArgumentNotValidException`
- `ConstraintViolationException`
- `HttpMessageNotReadableException`
- `DataIntegrityViolationException`
- `ApiException`
- `Exception` tong

No giup API dung on dinh hon va de quet ZAP/Swagger hon.

## 8. Authorization cua audit log

`/api/audit-logs/**` duoc khoa chi cho `ADMIN`.

Ly do:

- audit log chua thong tin nhay cam ve hanh vi he thong
- khong nen de `USER` doc lich su login that bai cua nguoi khac
- hop ly khi demo role authorization va phan quyen quan tri

## 9. Test va bang chung

`AuditLogIntegrationTest` da cover:

- login dung sinh `LOGIN_SUCCESS`
- login sai sinh `LOGIN_FAILED`
- tao customer sinh `CREATE_CUSTOMER`
- tao ticket sinh `CREATE_TICKET`
- patch status sinh `UPDATE_TICKET_STATUS`
- admin doc `/api/audit-logs` duoc

Trong smoke test Docker ngay `2026-06-18`, sau khi:

- login admin
- tao customer
- tao ticket
- cap nhat status

API `/api/audit-logs` da tra ve du lieu audit that.

No chung minh Stage 8 khong chi dung trong unit/integration test, ma da chay duoc o stack deploy compose.
