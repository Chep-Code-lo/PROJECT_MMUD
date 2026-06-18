# Giai doan 9: Giai thich chi tiet code va y tuong

Stage 9 la buoc bien frontend tu scaffold thanh giao dien co the demo auth, authorization, customer va audit bang API that.

## 1. Muc tieu cua giai doan 9

- Frontend khong dung mock data nua
- Login/Register goi backend that
- Co protected route
- Menu an/hien theo role
- Xu ly `401` va `403`
- Demo duoc customer flow va audit flow

## 2. Cac file chinh da lam

- `frontend/lib/axiosClient.ts`
- `frontend/lib/tokenStorage.ts`
- `frontend/services/authService.ts`
- `frontend/services/customerService.ts`
- `frontend/services/auditLogService.ts`
- `frontend/components/ProtectedRoute.tsx`
- `frontend/components/Navbar.tsx`
- `frontend/components/CustomerTable.tsx`
- `frontend/components/AuditLogTable.tsx`
- `frontend/app/login/page.tsx`
- `frontend/app/register/page.tsx`
- `frontend/app/dashboard/page.tsx`
- `frontend/app/customers/page.tsx`
- `frontend/app/customers/new/page.tsx`
- `frontend/app/audit-logs/page.tsx`

## 3. Kien truc frontend duoc tach the nao

### `axiosClient`

Day la lop giao tiep HTTP dung chung.

No lam 3 viec:

- set `baseURL`
- chen `Authorization: Bearer ...` neu co token
- bat `401` de xoa token va day nguoi dung ve trang login

Mac dinh `NEXT_PUBLIC_API_URL` neu co, neu khong thi fallback ve:

```text
http://localhost:8080
```

### `tokenStorage`

Tach rieng logic luu token vao local storage de:

- de doi cach luu sau nay
- service auth va axios dung chung 1 noi

### `services/*`

Moi nghiep vu co 1 service:

- `authService`
- `customerService`
- `auditLogService`

UI page khong goi `axios` truc tiep nua. No chi goi service.

## 4. `ProtectedRoute` dang lam gi

`ProtectedRoute` la khoa bao ve o cap page.

Moi page protected se:

1. Kiem tra token co ton tai khong
2. Goi `me` de lay user hien tai
3. So role hien tai voi `allowedRoles`
4. Neu khong hop le thi day ve trang login hoac chan truy cap

Tac dung:

- tranh lo route tren UI
- de thong diep loi ro hon
- giu frontend va backend thong nhat ve role

## 5. Navbar va dashboard da doi vai tro gi

`Navbar` khong hien cung 1 menu cho moi role nua.

- `ADMIN`, `STAFF`: thay `Customers`
- `ADMIN`: thay them `Audit Logs`
- moi user dang nhap: thay thong tin session va nut logout

`Dashboard` duoc chuyen thanh man hinh tong quan:

- hien user hien tai
- nhan manh scope bao mat cua du an
- dieu huong nhanh den customer va audit theo role

## 6. Customer flow tren UI

Luong chinh:

1. Dang nhap
2. Vao `/customers`
3. Goi `GET /api/customers`
4. Vao form tao moi
5. Goi `POST /api/customers`
6. Sau khi thanh cong quay lai list

Bang customer hien:

- thong tin da duoc backend giai ma
- nut xoa
- trang thai loi neu khong du quyen hoac token het han

## 7. Audit flow tren UI

Chi `ADMIN` thay route `/audit-logs`.

Page nay goi:

- `GET /api/audit-logs`

Muc tieu la de nguoi demo mo UI ra va chi ngay:

- login success
- login failed
- create/update/delete customer

## 8. Vi sao stage 9 quan trong

Neu chi co backend thi de tai van dung, nhung luc demo se kho.

Frontend that giup:

- cho thay role rule o tang giao dien
- chung minh API contract da on dinh
- giup nguoi cham thay ro login, customer, audit di het vong

## 9. Cach tom tat khi thuyet trinh

> Stage 9 cua em la bo mock data va noi frontend vao backend that. Em dung `axiosClient` de tu dong gan JWT, `ProtectedRoute` de khoa role o cap page, va rut giao dien ve dung cac flow can demo: auth, customer va audit.
