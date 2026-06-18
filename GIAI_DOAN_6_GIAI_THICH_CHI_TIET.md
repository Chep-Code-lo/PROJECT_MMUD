# Giai doan 6: Giai thich chi tiet code va y tuong

File nay giai thich phan viec da duoc chot sau khi scope du an duoc lam gon lai.
Trong phien ban hien tai, giai doan 6 tap trung vao viec lam chac REST contract cua `Customer API` va chuan bi du lieu de cac stage bao mat di tiep.

## 1. Muc tieu cua giai doan 6

Giai doan 6 co 4 muc tieu ky thuat:

1. Chot CRUD customer theo dung HTTP method va status code.
2. Lam sach validation va thong diep loi.
3. Giup frontend co contract on dinh de goi that.
4. Dat nen cho authorization, audit log va Swagger/Postman.

## 2. Cac file chinh can doc

- `backend/src/main/java/com/company/securityapp/dto/CustomerRequest.java`
- `backend/src/main/java/com/company/securityapp/dto/CustomerResponse.java`
- `backend/src/main/java/com/company/securityapp/service/CustomerService.java`
- `backend/src/main/java/com/company/securityapp/controller/CustomerController.java`
- `backend/src/main/java/com/company/securityapp/exception/ApiException.java`
- `backend/src/main/java/com/company/securityapp/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/company/securityapp/CustomerControllerIntegrationTest.java`

## 3. Contract REST da duoc chot nhu the nao

API hien tai gom:

- `GET /api/customers`
- `GET /api/customers/{id}`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`

Status code duoc quy uoc ro:

- `200` cho get/update thanh cong
- `201` cho create
- `204` cho delete
- `400` cho validation sai
- `404` cho customer khong ton tai
- `409` cho email bi trung

Dieu nay rat quan trong cho Swagger, Postman va frontend.

## 4. Vi sao tach `CustomerRequest` va `CustomerResponse`

Neu dung entity lam request/response truc tiep se co 3 van de:

1. De lo field persistence noi bo.
2. Kho doi contract neu DB doi.
3. Frontend phai biet qua nhieu ve cau truc luu tru.

DTO giup backend giu quyen kiem soat contract API.

## 5. Validation duoc dat o dau

Validation dat ngay tren `CustomerRequest`.

Ly do:

- request vao sai thi chan som
- service khong phai check lai nhung loi co hoc
- Swagger/OpenAPI doc duoc schema ro hon

Nhung logic nghiep vu van dat o service, vi du:

- chuan hoa email
- check trung email
- ma hoa field nhay cam

## 6. `CustomerService` giai quyet nghiep vu gi

Service nay lam nhung viec chinh:

- tim customer theo id
- check duplicate email
- chuan hoa text
- ma hoa/giai ma field nhay cam
- ghi business audit action

No la noi lien ket giua:

- HTTP layer
- persistence layer
- encryption layer
- audit layer

## 7. Vi sao thong diep loi phai ro rang

Mon hoc khong chi cham "co chay hay khong", ma con cham cach API duoc thiet ke.

Vi vay:

- validation sai phai tra body de doc
- khong ton tai phai tra `404`
- trung email phai tra `409`

`GlobalExceptionHandler` giup thong nhat format loi cho toan bo backend.

## 8. Giai doan 6 dong vai tro gi trong toan he thong

Sau giai doan nay:

- frontend co endpoint that de goi
- authorization co tai nguyen de khoa role
- audit log co nghiep vu de theo doi
- Swagger/Postman co endpoint thuc de tai lieu hoa

No la diem chuyen tu "co ma hoa" sang "co API that de demo".

## 9. Test can chung minh

Khi review giai doan nay, can chung minh duoc:

1. Create customer thanh cong.
2. Validation sai tra `400`.
3. Du lieu nhay cam trong DB la ciphertext.
4. Contract response khong lo cot `_encrypted`.

## 10. Cach tom tat khi thuyet trinh

> Giai doan 6 cua em la buoc chot Customer API thanh 1 REST API dung nghia: co DTO rieng, validation ro, status code ro, response sach, va nghiep vu duoc don vao service de cac giai doan authorization, audit va frontend co the dung chung.
