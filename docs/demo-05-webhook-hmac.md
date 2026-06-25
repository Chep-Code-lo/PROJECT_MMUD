# Demo 05 - Webhook HMAC-SHA256

## Muc tieu

- Webhook hop le mo khoa course
- Webhook sai signature bi tu choi
- Replay cung `eventId` bi tu choi

## Cong thuc chu ky

`signature = hex(HMAC_SHA256(eventId + "." + timestamp + "." + rawBody, HMAC_WEBHOOK_SECRET))`

## Headers

- `X-Signature`
- `X-Timestamp`
- `X-Event-Id`

## Cach demo

1. Tao hoac dung enrollment `PENDING`
2. Gui webhook hop le
3. Gui webhook sai signature
4. Gui lai webhook hop le voi cung `eventId`

## Ky vong

- Hop le: `200`
- Sai signature: `401`
- Replay: `409`

## Ket luan

- HMAC chung minh request den tu nguon biet secret
- Timestamp + eventId giup giam replay attack
