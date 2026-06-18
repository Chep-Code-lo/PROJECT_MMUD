# Giai doan 5: Giai thich chi tiet code va y tuong

File nay dat o thu muc goc de de nop, de mo nhanh va de dung luc thuyet trinh.
Ban day du hon van co trong `docs/stage5-customer-aes-guide.md`, nhung file nay duoc viet de nhin phat la thay ngay.

## 1. Giai doan 5 can dat duoc gi

Theo yeu cau de bai, Giai doan 5 cua Ban 2 gom 3 nhom viec chinh:

1. Tao domain model va schema cho `Customer`, `Ticket`, `AuditLog`.
2. Hoan thanh `Customer API` theo kieu RESTful CRUD.
3. Ma hoa du lieu nhay cam bang AES truoc khi luu vao database.

Noi ngan gon:

- Client gui du lieu binh thuong.
- Backend ma hoa du lieu nhay cam.
- Database chi luu ciphertext.
- API tra du lieu da giai ma ve cho frontend.

## 2. Cac file chinh da lam

### Database

- `database/schema.sql`
- `database/seed.sql`

### Entity / Enum / Repository

- `backend/src/main/java/com/company/securityapp/entity/Customer.java`
- `backend/src/main/java/com/company/securityapp/entity/Ticket.java`
- `backend/src/main/java/com/company/securityapp/entity/AuditLog.java`
- `backend/src/main/java/com/company/securityapp/entity/TicketStatus.java`
- `backend/src/main/java/com/company/securityapp/entity/TicketPriority.java`
- `backend/src/main/java/com/company/securityapp/repository/CustomerRepository.java`
- `backend/src/main/java/com/company/securityapp/repository/TicketRepository.java`
- `backend/src/main/java/com/company/securityapp/repository/AuditLogRepository.java`

### Service / Controller / DTO

- `backend/src/main/java/com/company/securityapp/service/EncryptionService.java`
- `backend/src/main/java/com/company/securityapp/service/CustomerService.java`
- `backend/src/main/java/com/company/securityapp/controller/CustomerController.java`
- `backend/src/main/java/com/company/securityapp/dto/CustomerRequest.java`
- `backend/src/main/java/com/company/securityapp/dto/CustomerResponse.java`

### Xu ly loi va test

- `backend/src/main/java/com/company/securityapp/exception/ApiException.java`
- `backend/src/main/java/com/company/securityapp/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/company/securityapp/CustomerControllerIntegrationTest.java`

## 3. Y tuong kien truc

Backend duoc tach thanh 4 lop co ban:

### Controller

Nhiem vu:

- nhan request HTTP
- map dung endpoint
- tra response

Controller khong nen xu ly nghiep vu phuc tap. No chi chuyen tiep request sang service.

Vi du:

- `GET /api/customers`
- `GET /api/customers/{id}`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`

Tat ca nam trong `CustomerController`.

### Service

Nhiem vu:

- xu ly nghiep vu chinh
- validate logic bo sung
- encrypt/decrypt
- goi repository

`CustomerService` la noi quan trong nhat cua Stage 5 vi no quyet dinh:

- khi nao check email trung
- khi nao encrypt
- khi nao decrypt
- khi nao nem loi `404`, `409`

### Repository

Nhiem vu:

- giao tiep voi database thong qua Spring Data JPA

Trong `CustomerRepository`, minh them:

- lay danh sach customer theo thu tu moi nhat
- check email trung khi tao moi
- check email trung khi update

### DTO

DTO giup tach:

- du lieu client gui len
- du lieu API tra ve
- du lieu entity luu that trong DB

Neu tra entity truc tiep thi rat de lo field noi bo, nhat la field da ma hoa.

## 4. Domain model duoc thiet ke ra sao

### Customer

Entity `Customer` la trong tam cua Stage 5.

No co cac field:

- `id`
- `name`
- `email`
- `phoneEncrypted`
- `addressEncrypted`
- `taxCodeEncrypted`
- `createdAt`
- `updatedAt`

Y do thiet ke:

- `name`, `email` co the luu ro rang
- `phone`, `address`, `taxCode` la nhay cam nen khong luu plaintext
- trong Java dung ten field co hau to `Encrypted` de nhin vao la biet field nay dang luu ciphertext

Trong DB, cot duoc dat ten:

- `phone_encrypted`
- `address_encrypted`
- `tax_code_encrypted`

Dat ten ro rang nhu vay co 2 loi:

1. Khong bi nham voi du lieu plaintext.
2. Giup nguoi cham bai hoac thanh vien khac nhin schema la hieu ngay y do bao mat.

### Ticket

Stage 5 chua CRUD ticket, nhung minh da tao san entity `Ticket`.

No gom:

- `customer`
- `title`
- `description`
- `status`
- `priority`
- `createdById`
- `assignedToId`
- `createdAt`
- `updatedAt`

Y tuong:

- Stage 6 chi can them DTO, service, controller cho ticket
- khong can quay nguoc lai sua schema/domain nhieu nua

### AuditLog

Stage 5 cung tao san `AuditLog`.

No gom:

- `action`
- `entityType`
- `entityId`
- `actorUserId`
- `actorEmail`
- `success`
- `details`
- `createdAt`

Y nghia:

- de san cho Stage 8 lam audit log
- co the log login success, login failed, create/update/delete customer, create/update/delete ticket

## 5. Vi sao chi ma hoa 3 truong

De bai bat buoc ma hoa:

- `phone`
- `address`
- `taxCode`

Minh khong ma hoa `email` vi `email` dang phuc vu:

- unique constraint
- check trung lap
- tim kiem nhanh
- lien he khach hang

Neu ma hoa ca `email`, he thong se gap bai toan:

- khong de query chinh xac bang plaintext nua
- can hash hoac search index rieng
- phuc tap hon scope Stage 5

Nen huong hien tai la hop ly:

- ma hoa cac field nhay cam
- giu `email` o dang plaintext co kiem soat

## 6. EncryptionService dang lam gi

`EncryptionService` la trai tim cua phan AES.

No lam 4 viec:

1. Doc secret tu `app.aes.secret`
2. Bien secret thanh AES key
3. Encrypt plaintext thanh ciphertext
4. Decrypt ciphertext thanh plaintext

### Buoc 1: Lay secret

Secret duoc doc tu file config qua:

- `app.aes.secret`

Nhu vay:

- khong hardcode key trong logic API
- de doi key theo moi truong local/dev/prod

### Buoc 2: Tao key

Secret chuoi duoc bam bang `SHA-256` de tao khoa 32-byte.

Y do:

- khong bat user phai nhap dung chinh xac 16/24/32 byte
- van tao duoc key co do dai on dinh

### Buoc 3: Encrypt

Moi lan encrypt:

- sinh mot `IV` ngau nhien 12 byte
- dung `AES/GCM/NoPadding`
- ghep `IV + ciphertext`
- encode Base64 de de luu vao DB

Tai sao dung `AES/GCM`:

- hien dai hon `AES/CBC`
- co kha nang kiem tra toan ven
- neu ciphertext bi sua, decrypt se fail

### Buoc 4: Decrypt

Khi decrypt:

- doc lai Base64
- tach IV va phan encrypted
- dung cung key de giai ma

Khi do API moi co the tra lai:

- `phone`
- `address`
- `taxCode`

duoi dang binh thuong cho frontend.

## 7. Luong xu ly request tu dau den cuoi

Lay vi du `POST /api/customers`.

### Luong chay

1. Frontend gui JSON:

```json
{
  "name": "ACME Ltd",
  "email": "info@acme.test",
  "phone": "0909000999",
  "address": "123 Demo Street",
  "taxCode": "TAX-001"
}
```

2. `CustomerController` nhan request.
3. `@Valid` kiem tra field co hop le khong.
4. `CustomerService` trim du lieu, lower-case email.
5. `CustomerService` check email da ton tai chua.
6. Neu hop le, `EncryptionService` ma hoa 3 field nhay cam.
7. `CustomerRepository` luu entity vao bang `customers`.
8. Sau khi luu xong, `CustomerService` decrypt lai 3 field do de tao `CustomerResponse`.
9. API tra response plaintext cho frontend.

### Dieu quan trong nhat

Frontend khong can tu encrypt hay decrypt.

Tat ca logic bao mat nam trong backend. Day la y tuong dung:

- client don gian
- backend kiem soat duoc luong du lieu nhay cam
- database khong chua plaintext

## 8. Validation duoc lam ra sao

`CustomerRequest` la DTO dau vao.

Minh dat cac annotation:

- `@NotBlank`
- `@Email`
- `@Size`

Tac dung:

- tranh request rong
- tranh email sai dinh dang
- tranh field qua dai gay loi DB hoac gay xau du lieu

Vi du:

- `name` bat buoc
- `email` bat buoc va dung format
- `phone` bat buoc
- `address` bat buoc
- `taxCode` bat buoc

Dieu nay giup API chac hon va cung la mot phan hardening co ban.

## 9. Xu ly loi va ly do phai tach rieng

Minh dung:

- `ApiException`
- `GlobalExceptionHandler`

### `ApiException`

No la custom exception de service co the nem loi co chu dich.

Vi du:

- `404 Customer not found`
- `409 Customer email already exists`

### `GlobalExceptionHandler`

No gom loi tat ca ve mot noi.

Loi ich:

- response loi dong bo
- khong lo stacktrace ky thuat
- frontend de xu ly hon
- phu hop huong secure API

Mau loi co dang:

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

## 10. Vi sao `seed.sql` khong nhieu du lieu

Day la mot quyet dinh co chu dich.

Vi ciphertext phu thuoc:

- AES secret
- IV ngau nhien moi lan encrypt

Neu seed san customer bang SQL tay, co the:

- du lieu khong decrypt duoc o moi truong khac
- secret doi la vo seed
- nguoi dung de bi nham rang plaintext va ciphertext la mot

Nen huong an toan hon la:

- de schema tao bang
- tao customer mau thong qua API sau khi app boot

Cach nay cung demo dung y tuong bao mat hon.

## 11. RESTful API dang co nhung gi

`CustomerController` hien co day du:

- `GET /api/customers`
- `GET /api/customers/{id}`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`

Y nghia tung cai:

- `GET list`: lay danh sach
- `GET by id`: lay chi tiet
- `POST`: tao moi
- `PUT`: cap nhat toan bo
- `DELETE`: xoa

No dung dung form RESTful ma de bai yeu cau, khong che them endpoint linh tinh.

## 12. Tai sao API khong tra ciphertext

De bai co mot yeu cau quan trong:

- khong tra ciphertext tho ra ngoai API

Minh xu ly bang cach:

- entity luu encrypted field
- response DTO chi chua gia tri da decrypt

Nghia la:

- DB thay chuoi ma hoa
- client thay gia tri dung de hien thi

Neu tra ciphertext ra ngoai thi frontend se:

- khong dung duoc
- phai tu giai ma, sai kien truc
- de lo logic bao mat

Nen cach minh lam la dung huong hon.

## 13. Security trade-off va gioi han hien tai

Day la phan nen noi that khi bao cao.

### Chua khoa role that

`SecurityConfig` trong repo hien tai van `permitAll`, nghia la phan auth/authorization cua Stage 4-7 chua hoan tat trong codebase hien tai.

Nen:

- Stage 5 da xong theo logic customer + AES
- nhung route chua bi khoa role that

Do khong phai loi cua Customer API, ma la do Stage 4 va Stage 7 la scope khac.

### Email van de plaintext

Nhu da noi:

- de unique va lookup
- chua ma hoa email

Day la trade-off chu dong, khong phai bo sot.

### Audit log va ticket moi la nen

`Ticket` va `AuditLog` da co:

- entity
- repository
- schema

Nhung service/controller cua chung chua lam xong vi do la viec cua Stage 6 va 8.

## 14. Test da lam

Minh da them test tich hop `CustomerControllerIntegrationTest`.

No cover 2 thu quan trong nhat:

1. Tao customer thanh cong va DB luu ciphertext.
2. Gui du lieu sai validation thi bi `400`.

Y nghia:

- khong chi build duoc
- ma logic Stage 5 cung da duoc test

Ngoai ra da chay:

- `mvn clean test`
- `mvn -q -DskipTests package`

de xac nhan backend build on.

## 15. Cach giai thich ngan gon khi thuyet trinh

Neu bi hoi nhanh "em da lam gi o Giai doan 5?", co the tra loi:

> Em hoan thien Customer API theo kieu controller-service-repository, dung DTO de tach request/response voi entity, ma hoa phone, address va taxCode bang AES/GCM truoc khi luu DB, khong tra ciphertext ra ngoai, dong thoi tao san schema va domain cho Ticket va AuditLog de nhom di tiep Stage 6 va Stage 8.

## 16. Mot cau tom tat de nho

Stage 5 khong chi la CRUD customer, ma la phan bien du lieu nhay cam thanh:

- plaintext o client
- ciphertext o database
- plaintext da kiem soat o API response

Do chinh la y tuong bao mat trung tam cua phan nay.
