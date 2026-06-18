# Giai doan 6: Giai thich chi tiet code va y tuong

File nay dat o thu muc goc de dung luc nop bai va luc thuyet trinh. Muc tieu la giai thich ro Stage 6 da code gi, vi sao code nhu vay va no lien ket ra sao voi cac stage sau.

## 1. Muc tieu cua Stage 6

Stage 6 cua Ban 2 can hoan thanh `Ticket API` theo dung kieu RESTful:

- `GET /api/tickets`
- `POST /api/tickets`
- `GET /api/tickets/{id}`
- `PUT /api/tickets/{id}`
- `PATCH /api/tickets/{id}/status`
- `DELETE /api/tickets/{id}`

Noi ngan gon:

- Customer la doi tuong du lieu nhay cam cua Stage 5.
- Ticket la doi tuong nghiep vu de gan voi customer.
- API ticket phai du don gian de frontend demo, nhung van tach ro create/update/status update.

## 2. Cac file chinh da lam

### Backend

- `backend/src/main/java/com/company/securityapp/dto/TicketRequest.java`
- `backend/src/main/java/com/company/securityapp/dto/TicketResponse.java`
- `backend/src/main/java/com/company/securityapp/dto/TicketStatusUpdateRequest.java`
- `backend/src/main/java/com/company/securityapp/service/TicketService.java`
- `backend/src/main/java/com/company/securityapp/controller/TicketController.java`
- `backend/src/main/java/com/company/securityapp/entity/Ticket.java`
- `backend/src/main/java/com/company/securityapp/entity/TicketStatus.java`
- `backend/src/main/java/com/company/securityapp/entity/TicketPriority.java`

### Phan test va phan frontend lien quan

- `backend/src/test/java/com/company/securityapp/TicketControllerIntegrationTest.java`
- `frontend/types/ticket.ts`
- `frontend/services/ticketService.ts`
- `frontend/components/TicketTable.tsx`
- `frontend/app/tickets/new/page.tsx`

## 3. Y tuong domain model cua Ticket

Entity `Ticket` giu cac thong tin sau:

- `customer`
- `title`
- `description`
- `status`
- `priority`
- `createdById`
- `assignedToId`
- `createdAt`
- `updatedAt`

Y do chinh:

- Ticket luon gan voi `Customer`, nen `customerId` la bat buoc.
- `status` duoc tach thanh enum de tranh string linh tinh.
- `priority` cung la enum de UI va backend thong nhat.
- `createdById` va `assignedToId` duoc giu o muc don gian, khong can xay full workflow phan cong qua phuc tap.

## 4. Vi sao can 3 DTO khac nhau

### `TicketRequest`

Dung cho `POST` va `PUT`.

No chua:

- `customerId`
- `title`
- `description`
- `priority`
- `status`
- `createdById`
- `assignedToId`

Nghia la client co the gui full payload khi tao hoac sua ticket.

### `TicketResponse`

Dung de tra ve cho client.

No tra:

- `customerId`
- `customerName`
- thong tin title, description, status, priority
- thong tin thoi gian

Quan trong nhat la response khong tra nhung field nhay cam cua customer. Ticket chi can biet dang gan voi khach hang nao, khong can lo `phone`, `address`, `taxCode`.

### `TicketStatusUpdateRequest`

Dung rieng cho `PATCH /api/tickets/{id}/status`.

Payload chi co:

```json
{
  "status": "PROCESSING"
}
```

Tach nho nhu vay giup endpoint ro y nghia hon va dung tinh than REST:

- `PUT` de cap nhat full resource
- `PATCH` de cap nhat mot phan nho

## 5. Luong xu ly trong `TicketService`

`TicketService` la noi giai quyet nghiep vu chinh.

Luot tao ticket hoat dong nhu sau:

1. Nhan `TicketRequest`.
2. Tim `Customer` theo `customerId`.
3. Neu customer khong ton tai thi nem `404`.
4. Tao `Ticket`.
5. Map du lieu tu request vao entity qua `applyRequest(...)`.
6. Neu request khong gui `status` thi mac dinh la `OPEN`.
7. Save vao database.

Luot update ticket cung dung lai `applyRequest(...)` de tranh lap logic.

Luot update status:

1. Tim ticket theo id.
2. Lay `oldStatus`.
3. Gan status moi.
4. Save.

Sau Stage 8, service nay duoc noi them vao `AuditLogService` nen moi create/update/delete/status update deu de lai dau vet audit.

## 6. Vi sao `PATCH /status` la quyet dinh dung

Trong he thong ticket, hanh dong xay ra nhieu nhat thuong khong phai sua title hay description, ma la doi:

- `OPEN`
- `PROCESSING`
- `RESOLVED`

Neu bat client luon gui full `PUT` chi de doi status thi:

- payload dai hon can thiet
- de ghi de len field khac
- frontend phai mang theo nhieu state khong lien quan

Tach 1 endpoint `PATCH /status` lam intent ro rang hon:

- nguoi cham bai nhin vao la hieu ngay luong ticket
- frontend thao tac nhanh hon
- test cung gon hon

## 7. Validation va xu ly loi

`TicketRequest` bat buoc:

- `customerId`
- `title`
- `description`
- `priority`
- `createdById`

`TicketStatusUpdateRequest` bat buoc:

- `status`

Mot so case quan trong:

- `customerId` sai => `404 Customer not found.`
- body thieu field => `400 Validation failed.`
- ticket khong ton tai => `404 Ticket not found.`

Nghia la Stage 6 khong chi lam CRUD cho co, ma con de luong loi ro rang de frontend va Postman demo duoc.

## 8. Frontend da phai doi gi de khop backend

Vi Stage 6 la API that, frontend cung phai dong bo contract:

- `ticketService` gui `customerId`
- update status dung `PATCH`
- `TicketTable` hien `customerName`
- form tao ticket yeu cau nhap `customerId`

Noi cach khac, Stage 6 khong chi la backend CRUD, ma con chot ca giao keo giua backend va frontend cho Ticket flow.

## 9. Test va bang chung

`TicketControllerIntegrationTest` da cover:

- tao ticket
- list ticket
- lay ticket theo id
- update full ticket
- patch status
- xoa ticket
- gui `customerId` khong ton tai

Ngoai integration test, stack Docker da duoc smoke test thuc te ngay `2026-06-18`:

- login `ADMIN`
- tao customer
- tao ticket
- patch ticket sang `PROCESSING`

Nghia la Stage 6 da dung duoc ca trong test va trong moi truong compose that.
