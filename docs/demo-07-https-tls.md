# Demo 07 - HTTPS / TLS

## Muc tieu

- Chay frontend/backend qua HTTPS
- Giai thich TLS bao ve du lieu tren duong truyen

## Cach demo

1. `docker compose up --build`
2. Goi:

```powershell
curl.exe -I http://localhost/api/health
curl.exe -k https://localhost/api/health
```

## Ky vong

- HTTP tra `301`
- HTTPS tra `200`

## Giai thich

- TLS giup ma hoa kenh truyen
- JWT, password va du lieu API khong bi lo plaintext khi di qua mang
