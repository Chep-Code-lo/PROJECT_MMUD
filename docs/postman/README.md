# Postman docs

Thu muc nay chua collection va environment de test nhanh auth, customer, audit log va admin summary.

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

## Thu tu chay de demo nhanh

1. `Auth / Login as Admin`
2. `Auth / Get Current User`
3. `Admin / Summary`
4. `Customers / Create Customer`
5. `Customers / Get Customer By Id`
6. `Customers / Update Customer`
7. `Customers / Delete Customer`
8. `Security Checks / Customers as Staff (Expect 200)`
9. `Security Checks / Customers as User (Expect 403)`
10. `Security Checks / Audit Logs as User (Expect 403)`
11. `Security Checks / Auth Me without Token (Expect 401)`
12. `Audit / List Audit Logs`

## Luu y quan trong

- Request login se tu dong luu `token` vao environment.
- `Login as Admin` se luu them `adminToken`.
- `Login as Staff` se luu `staffToken`.
- `Login as User` se luu `userToken`.
- Request create customer se tu dong luu `customerId`.
- Body tao customer dung Postman dynamic variable `{{$timestamp}}` de giam trung lap khi chay lai nhieu lan.
- Collection da duoc rut gon de tap trung vao auth, customer CRUD va security checks.

## Chay bang Newman

```bash
npx --yes newman run docs/postman/securityapp.postman_collection.json -e docs/postman/securityapp.local.postman_environment.json --reporters cli
```

Collection da duoc verify thanh cong bang Newman trong repo nay.
