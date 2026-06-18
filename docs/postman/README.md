# Postman docs

Thu muc nay chua collection va environment de test nhanh auth, customer, ticket, audit log va admin summary.

## File chinh

- `securityapp.postman_collection.json`
- `securityapp.local.postman_environment.json`

## Cach import

1. Import collection.
2. Import environment.
3. Chon environment `Security App Local`.

## Bien moi truong quan trong

- `baseUrl`: mac dinh `http://localhost:8080`
- `adminEmail`, `staffEmail`, `userEmail`
- `adminPassword`, `staffPassword`, `userPassword`
- `token`
- `adminToken`
- `staffToken`
- `userToken`
- `customerId`
- `ticketId`

## Thu tu chay de demo nhanh

1. `Auth / Login as Admin`
2. `Auth / Get Current User`
3. `Admin / Summary`
4. `Customers / Create Customer`
5. `Customers / List Customers`
6. `Tickets / Create Ticket`
7. `Tickets / Update Ticket Status`
8. `Security Checks / Customers as User (Expect 403)`
9. `Security Checks / Auth Me without Token (Expect 401)`
10. `Audit / List Audit Logs`

## Luu y quan trong

- Request login se tu dong luu `token` vao environment.
- `Login as Admin` se luu them `adminToken`.
- `Login as Staff` se luu `staffToken`.
- `Login as User` se luu `userToken`.
- Request create customer se tu dong luu `customerId`.
- Request create ticket se tu dong luu `ticketId`.
- Body tao customer va ticket da dung Postman dynamic variable `{{$timestamp}}` de giam trung lap khi chay lai nhieu lan.
- Collection da duoc sap lai de cleanup customer dien ra sau khi ticket da bi xoa.

## Chay bang Newman

```bash
npx --yes newman run docs/postman/securityapp.postman_collection.json -e docs/postman/securityapp.local.postman_environment.json --reporters cli
```

Collection da duoc verify thanh cong bang Newman trong repo nay.
