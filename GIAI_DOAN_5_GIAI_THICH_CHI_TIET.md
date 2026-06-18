# Giai doan 5: Giai thich chi tiet code va y tuong

File nay dat o thu muc goc de de nop, de mo nhanh va de dung luc thuyet trinh.
No tap trung vao nen tang du lieu va ma hoa cua backend.

## 1. Muc tieu cua giai doan 5

Giai doan 5 trong phien ban hien tai co 3 muc tieu chinh:

1. Chot schema va domain model cho `Customer` va `AuditLog`.
2. Hoan thanh `Customer API` theo kieu RESTful CRUD.
3. Ma hoa du lieu nhay cam bang `AES-GCM` truoc khi luu vao database.

Noi ngan gon:

- Client gui plaintext.
- Backend ma hoa truoc khi persist.
- Database chi thay ciphertext.
- API tra plaintext da giai ma ra cho frontend.

## 2. Cac file chinh da lam

### Database

- `database/schema.sql`
- `database/seed.sql`

### Entity / Repository

- `backend/src/main/java/com/company/securityapp/entity/Customer.java`
- `backend/src/main/java/com/company/securityapp/entity/AuditLog.java`
- `backend/src/main/java/com/company/securityapp/repository/CustomerRepository.java`
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

Backend duoc tach thanh 4 lop ro rang:

### Controller

Controller chi lam viec HTTP:

- nhan request
- map endpoint
- goi service
- tra response

No khong xu ly AES hay nghiep vu phuc tap.

### Service

Service la noi xu ly nghiep vu that:

- validate logic
- chuan hoa du lieu
- goi ma hoa/giai ma
- goi repository
- sinh audit log business

### Repository

Repository chi phu trach truy cap database.

### DTO

DTO tach request/response ra khoi entity de:

- tranh lo field khong muon expose
- giu contract API on dinh
- de thay doi persistence ma khong vo frontend

## 4. Thiet ke du lieu customer

Customer luu:

- `name`
- `email`
- `phone_encrypted`
- `address_encrypted`
- `tax_code_encrypted`
- `createdAt`
- `updatedAt`

Y do chinh:

- `email` la field nghiep vu co the tim kiem va check unique nen de plaintext.
- `phone`, `address`, `taxCode` la du lieu nhay cam nen phai ma hoa.
- DB khong duoc luu lai ban ro cua 3 field tren.

## 5. Vi sao ma hoa trong service thay vi controller

`EncryptionService` duoc goi trong `CustomerService`, khong nam trong controller.

Ly do:

- controller phai nhe
- moi luong ghi customer deu di qua 1 diem chung
- tranh truong hop mot endpoint quen ma hoa
- de test va refactor sau nay

Luong create customer:

1. `CustomerController` nhan `CustomerRequest`.
2. `CustomerService` chuan hoa text va email.
3. Goi `EncryptionService.encrypt(...)` cho 3 field nhay cam.
4. Luu entity vao DB.
5. Map entity thanh `CustomerResponse`.
6. Response tra ra plaintext da duoc giai ma.

## 6. `EncryptionService` dang lam gi

Service nay tap trung vao 2 ham chinh:

- `encrypt(...)`
- `decrypt(...)`

Y tuong ky thuat:

- dung secret key doi xung
- dung `AES/GCM/NoPadding`
- moi lan ma hoa sinh `IV` rieng
- ghep `IV + ciphertext + auth tag` thanh chuoi luu DB

Tai sao `GCM` quan trong:

- co tinh bao mat
- co xac thuc toan ven
- phu hop cho du lieu ung dung luu trong DB

## 7. Request/response duoc tach the nao

### `CustomerRequest`

Request chi chua du lieu client duoc phep gui:

- `name`
- `email`
- `phone`
- `address`
- `taxCode`

### `CustomerResponse`

Response tra:

- `id`
- `name`
- `email`
- `phone`
- `address`
- `taxCode`
- `createdAt`
- `updatedAt`

Dieu quan trong la response khong tra ve cac cot `_encrypted`.

## 8. Validation va xu ly loi

Validation duoc dat ngay o DTO bang annotation:

- `@NotBlank`
- `@Email`
- `@Size`

Neu request sai, `GlobalExceptionHandler` tra ve body loi ro rang.

Tac dung:

- frontend de xu ly
- Postman/Swagger de test
- nguoi cham bai de nhin status code cho chuan

## 9. Test cua giai doan 5 chung minh dieu gi

`CustomerControllerIntegrationTest` cover 2 diem rat quan trong:

1. Tao customer thanh cong thi response van la plaintext hop le.
2. Ban ghi trong DB khong giong plaintext ban dau.

Day la bang chung thuc te cho yeu cau "ma hoa du lieu nhay cam truoc khi luu".

## 10. Gia tri cua giai doan 5 doi voi toan he thong

Sau giai doan nay, he thong da co:

- 1 API CRUD co the demo that
- 1 mau xu ly service/repository/DTO ro rang
- 1 co che AES-GCM phuc vu dung trong tam mon hoc
- 1 bang audit de giai doan sau gan vao truy vet hanh dong

No la lop nen quan trong nhat cho huong mat ma ung dung cua de tai.

## 11. Cach tom tat khi thuyet trinh

Co the noi gon nhu sau:

> Em tach Customer API thanh controller-service-repository, dung DTO de giu contract API sach, ma hoa `phone`, `address`, `taxCode` bang AES-GCM truoc khi luu DB, va chi tra plaintext da giai ma ra ngoai. Nguoi dung nhin thay du lieu dung, nhung database chi luu ciphertext.
