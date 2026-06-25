# Demo 06 - Rate limiting

## Muc tieu

- Goi login sai nhieu lan
- He thong tra `429 Too Many Requests`
- Ghi nhan hanh vi bat thuong

## Endpoint

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/webhooks/payment-success`
- `GET /api/admin/audit-logs`

## Cach demo

1. Goi `POST /api/auth/login` voi sai password nhieu lan lien tiep
2. Quan sat response
3. Login admin va xem audit logs

## Ky vong

- Sau nguong cau hinh, response la `429`
- Audit log co `RATE_LIMIT_EXCEEDED`
