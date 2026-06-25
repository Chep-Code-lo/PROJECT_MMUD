# Demo 04 - BOLA / IDOR

## Muc tieu

- Student A co certificate cua minh
- Student B co gang doi ID tren URL
- Backend tra `403`
- Audit log ghi nhan hanh vi bi chan

## Endpoint goi y

- `GET /api/certificates/{certificateId}`
- `GET /api/enrollments/{enrollmentId}`
- `GET /api/users/{userId}/profile`
- `GET /api/courses/{courseId}/lessons/{lessonId}`

## Cach demo

1. Dang nhap Student A va lay `certificateId`
2. Dang nhap Student B
3. Goi `GET /api/certificates/{certificateId}` cua Student A bang token Student B
4. Login admin va mo `GET /api/admin/audit-logs`

## Ky vong

- Request cua Student B bi `403 Forbidden`
- Audit log co action `ACCESS_DENIED`
