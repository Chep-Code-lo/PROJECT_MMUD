# Giai doan 5: Customer API + AES Guide

## 1. Muc tieu cua phan nay

Giai doan 5 co 4 muc tieu chinh:

1. Tao domain model backend cho `Customer`, `Ticket`, `AuditLog`.
2. Hoan thien CRUD API cho `Customer`.
3. Ma hoa truong nhay cam bang AES truoc khi luu DB.
4. Dat san nen cho Giai doan 6-8 de lam ticket, authorization va audit log.

Phan nay chi giai quyet Customer API hoan chinh. `Ticket` va `AuditLog` duoc tao entity/repository/schema truoc de nhung giai doan sau co the di tiep ngay.

## 2. Cac file da dung/chinh

- `database/schema.sql`
- `database/seed.sql`
- `backend/src/main/java/com/company/securityapp/entity/Customer.java`
- `backend/src/main/java/com/company/securityapp/entity/Ticket.java`
- `backend/src/main/java/com/company/securityapp/entity/AuditLog.java`
- `backend/src/main/java/com/company/securityapp/entity/TicketStatus.java`
- `backend/src/main/java/com/company/securityapp/entity/TicketPriority.java`
- `backend/src/main/java/com/company/securityapp/repository/CustomerRepository.java`
- `backend/src/main/java/com/company/securityapp/repository/TicketRepository.java`
- `backend/src/main/java/com/company/securityapp/repository/AuditLogRepository.java`
- `backend/src/main/java/com/company/securityapp/service/EncryptionService.java`
- `backend/src/main/java/com/company/securityapp/service/CustomerService.java`
- `backend/src/main/java/com/company/securityapp/controller/CustomerController.java`
- `backend/src/main/java/com/company/securityapp/dto/CustomerRequest.java`
- `backend/src/main/java/com/company/securityapp/dto/CustomerResponse.java`
- `backend/src/main/java/com/company/securityapp/exception/ApiException.java`
- `backend/src/main/java/com/company/securityapp/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/company/securityapp/CustomerControllerIntegrationTest.java`

## 3. Luong xu ly request

Luong xu ly cua `POST /api/customers`:

1. Client gui JSON plaintext.
2. `CustomerController` nhan request va bat validation bang `@Valid`.
3. `CustomerService` chuan hoa du lieu, kiem tra email trung.
4. `EncryptionService` ma hoa `phone`, `address`, `taxCode`.
5. `CustomerRepository` luu entity vao DB.
6. `CustomerService` doc entity vua luu, giai ma lai cac field nhay cam.
7. API tra `CustomerResponse` plaintext cho frontend.

Y nghia cua cach lam nay:

- DB chi giu ciphertext.
- API khong tra raw ciphertext ra ngoai.
- Frontend van nhin thay du lieu goc nhu binh thuong.

## 4. Vi sao dung DTO ma khong tra entity truc tiep

Khong nen tra entity JPA truc tiep vi:

- Entity co field encrypted noi bo.
- Ve sau de bi lo them field khong muon expose.
- DTO giup tach ro "du lieu luu trong DB" va "du lieu tra cho client".

Trong code:

- `CustomerRequest`: du lieu dau vao.
- `CustomerResponse`: du lieu dau ra da giai ma.

## 5. Vi sao chi ma hoa 3 field nay

Spec yeu cau ma hoa:

- `phone`
- `address`
- `taxCode`

`email` duoc giu plaintext vi day la field hay dung de:

- unique constraint
- tim kiem/kiem tra trung lap
- auth flow hoac lien he khach hang

Neu muon ma hoa ca `email`, can them chien luoc khac nhu hash de lookup. Phan nay chua can, nen giai phap hien tai la don gian va phu hop demo.

## 6. EncryptionService dang lam gi

`EncryptionService` khong luu secret cung trong code. No doc tu:

```properties
app.aes.secret
```

Ben trong service:

1. Secret text duoc bam `SHA-256` de tao khoa AES 32-byte on dinh.
2. Moi lan encrypt se tao IV ngau nhien 12 byte.
3. Dung `AES/GCM/NoPadding` de vua ma hoa vua kiem tra toan ven.
4. Ghep `IV + ciphertext`, sau do Base64 de de luu vao SQL.

Tai sao chon `AES/GCM`:

- Manh hon AES/CBC khi demo he thong API hien dai.
- Co authentication tag, tranh bi sua ciphertext ma khong biet.
- Java 17 ho tro rat tot.

## 7. Domain model duoc chot ra sao

### Customer

Field chinh:

- `id`
- `name`
- `email`
- `phoneEncrypted`
- `addressEncrypted`
- `taxCodeEncrypted`
- `createdAt`
- `updatedAt`

Luu y:

- Ten field trong Java noi ro day la encrypted field.
- DB dung ten cot `*_encrypted` de tranh hieu lam.

### Ticket

Da tao san cac thanh phan cho giai doan sau:

- lien ket `customer`
- `title`
- `description`
- `status`
- `priority`
- `createdById`
- `assignedToId`

Y tuong o day la de Giai doan 6 chi can them service/controller/DTO, khong phai quay lai ve domain model nua.

### AuditLog

Da tao san:

- `action`
- `entityType`
- `entityId`
- `actorUserId`
- `actorEmail`
- `success`
- `details`
- `createdAt`

Giai doan 8 co the dung thang model nay de log:

- login success / failed
- create/update/delete customer
- create/update/delete ticket

## 8. Validation va xu ly loi

`CustomerRequest` dung validation annotation:

- `@NotBlank`
- `@Email`
- `@Size`

`GlobalExceptionHandler` duoc them de:

- tra loi validation gon gang
- tra `404` khi customer khong ton tai
- tra `409` khi email bi trung
- khong dua stacktrace ky thuat ra client

Mau loi se co dang gan nhu:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/api/customers",
  "details": {
    "email": "Email format is invalid."
  }
}
```

## 9. Tai sao `seed.sql` gan nhu de trong

Day la chu y quan trong:

- ciphertext phu thuoc `APP_AES_SECRET`
- moi lan encrypt dung IV ngau nhien

Neu minh hardcode customer seed trong SQL, du lieu do chi giai ma dung khi secret giong het secret luc tao seed. Vi vay, cach an toan hon la:

1. boot backend
2. goi `POST /api/customers`
3. de backend tu sinh ciphertext dung voi khoa dang chay

Noi ngan gon: customer nhay cam nen seed bang API se dung hon seed bang SQL.

## 10. Test can lam

### Test API

Tao customer:

```http
POST /api/customers
Content-Type: application/json

{
  "name": "ACME Ltd",
  "email": "info@acme.test",
  "phone": "0909000999",
  "address": "123 Demo Street",
  "taxCode": "TAX-001"
}
```

Lay danh sach:

```http
GET /api/customers
```

Cap nhat:

```http
PUT /api/customers/1
```

Xoa:

```http
DELETE /api/customers/1
```

### Test DB

Sau khi tao customer, check SQL:

```sql
SELECT id, email, phone_encrypted, address_encrypted, tax_code_encrypted
FROM customers;
```

Ban phai thay:

- `email` doc duoc
- 3 cot con lai la chuoi Base64 da ma hoa
- khong co plaintext phone/address/tax code trong DB

## 11. Test tu dong da them

`CustomerControllerIntegrationTest` cover 2 y chinh:

1. Tao customer thanh cong va DB luu ciphertext.
2. Gui request sai validation se bi `400`.

Test nay rat hop voi Giai doan 5 vi no kiem tra dung thu backend can demo: API + AES.

## 12. Trade-off va ghi chu cho nhom

- Hien tai `SecurityConfig` van cho phep tat ca request vi Stage 4 auth trong repo chua hoan thien. Stage 7 se khoa route lai theo role.
- Email dang de plaintext de phuc vu unique va lookup. Neu mon hoc bat buoc ma hoa them email, can bo sung hash/index chien luoc.
- `Ticket` va `AuditLog` moi chi dung o muc schema/domain. CRUD cua ticket va API audit log se lam o Giai doan 6 va 8.

## 13. Neu ban can thuyet trinh nhanh

Co the noi ngan gon nhu sau:

> Em tach Customer API thanh controller-service-repository, ma hoa 3 field nhay cam bang AES/GCM truoc khi luu, chi tra plaintext da giai ma ra DTO, va chuan bi san domain model Ticket/AuditLog de nhom di tiep sang authorization va audit log.
