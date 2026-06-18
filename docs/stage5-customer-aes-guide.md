# Stage 5 Customer AES Guide

Tai lieu nay viet ky thuat hon file o thu muc goc.
Muc tieu la giai thich ro vi sao customer la tam diem cua huong mat ma ung dung.

## 1. Trong tam cua stage

Stage 5 khong phai chi la CRUD thong thuong.
No phai chung minh 3 y:

1. API van dung duoc nhu he thong binh thuong.
2. Du lieu nhay cam khong bi luu plaintext trong DB.
3. Contract giua frontend va backend van giu de doc, de test.

## 2. Cac file can doc truoc

- `database/schema.sql`
- `backend/src/main/java/com/company/securityapp/entity/Customer.java`
- `backend/src/main/java/com/company/securityapp/service/EncryptionService.java`
- `backend/src/main/java/com/company/securityapp/service/CustomerService.java`
- `backend/src/main/java/com/company/securityapp/controller/CustomerController.java`
- `backend/src/test/java/com/company/securityapp/CustomerControllerIntegrationTest.java`

## 3. Vi sao chi ma hoa mot so field

Khong phai field nao cung nen ma hoa.

Trong du an nay:

- `email` duoc giu plaintext de login, check unique va tim kiem.
- `phone`, `address`, `taxCode` la field nhay cam nen duoc ma hoa.

Neu ma hoa ca `email`, bai toan query va unique se phuc tap hon rat nhieu.
Vi vay cach chon field nay la can bang giua bao mat va kha nang van hanh.

## 4. Luong ghi du lieu

Khi frontend goi `POST /api/customers`:

1. Payload vao `CustomerRequest`.
2. `CustomerService` trim text va chuan hoa email.
3. `EncryptionService` ma hoa 3 field nhay cam.
4. Entity duoc luu vao bang `customers`.
5. Service doc lai entity va giai ma de map ra `CustomerResponse`.

Ket qua:

- frontend thay du lieu dung
- DB chi thay ciphertext

## 5. Ly do chon AES-GCM

`AES-GCM` phu hop cho application data vi:

- nhanh
- pho bien
- co xac thuc toan ven
- tranh duoc tinh huong du lieu bi sua ma khong biet

Trong bai nay, no phu hop hon viec tu ghep AES-CBC voi MAC rieng.

## 6. Diem can nhan manh khi nop bai

- Ma hoa duoc thuc hien o backend, khong day secret ra frontend.
- Key duoc lay tu config/env, khong hardcode trong code nghiep vu.
- Test integration da chung minh DB khong luu ban ro.
- API van co status code va response de frontend demo binh thuong.

## 7. Kiem tra nhanh trong DB

Sau khi tao 1 customer, chay:

```powershell
docker exec securityapp-db mysql -uroot -proot securityapp -e "SELECT id,name,email,phone_encrypted,address_encrypted,tax_code_encrypted FROM customers;"
```

Can thay:

- `email` van doc duoc
- 3 cot `_encrypted` la chuoi ma hoa
- khong thay lai ban ro cua so dien thoai, dia chi, ma so thue

## 8. Loi ich kien truc

Stage 5 dat ra 1 khuon ro rang:

- controller lo HTTP
- service lo nghiep vu va ma hoa
- repository lo DB
- DTO lo contract

Khuon nay giup cac giai doan sau tiep tuc duoc ma khong lam roi code.

## 9. Cach tom tat 30 giay

> Stage 5 cua em khong chi CRUD customer, ma la xay dung 1 luong luu tru an toan: frontend gui plaintext, backend ma hoa bang AES-GCM truoc khi persist, database chi luu ciphertext, va API chi tra du lieu da giai ma cho client.
