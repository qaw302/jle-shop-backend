# Member API 변경사항 (요청/응답)

## 관리자 - 회원 탈퇴 API
URL : POST /v1/members/{id}/withdraw

[REQUEST]
HEADER
Content-Type : application/json

BODY
```json
{
  "memo": "관리자 처리"
}
```

[RESPONSE]
HEADER
204 No Content

## 관리자 - 회원 검색 API (전체 조건)
URL : POST /v1/members/search

[REQUEST]
HEADER
Content-Type : application/json

BODY
```json
{
  "name": "홍길동",
  "email": "test@test.com",
  "level": "NEW",
  "status": "WITHDRAWN",
  "dateSearch": "JOIN",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "startAge": 20,
  "endAge": 40,
  "gender": "F",
  "orderSerach": "TOTAL_ORDER_AMOUNT",
  "orderSearchStartValue": 10000,
  "orderSearchEndValue": 50000,
  "orderDateSearch": "ORDER_DATE",
  "startOrderDate": "2025-01-01",
  "endOrderDate": "2025-12-31",
  "productId": 1
}
```

파라미터 사용 가능 값
- level: `VIP`, `GENERAL`, `NEW`
- status: `ACTIVE`, `INACTIVE`, `DORMANT`, `WITHDRAWN`, `SUSPENDED`, `PENDING_EMAIL_VERIFICATION`, `PENDING_ADMIN_APPROVAL`
- dateSearch: `JOIN`, `BIRTHDAY`
- gender: `M`, `F`
- orderSerach: `TOTAL_ORDER_AMOUNT`, `TOTAL_PAYMENT_AMOUNT`, `TOTAL_ORDER_COUNT`, `TOTAL_ACTUAL_ORDER_COUNT`
- orderDateSearch: `ORDER_DATE`, `PAYMENT_DATE`, `DELIVERY_DATE`

[RESPONSE]
HEADER
200 OK

BODY
```json
[
  {
    "memberId": "test@test.com",
    "name": "test-user",
    "birthday": "2025-05-06",
    "phoneNumber": "010-2244-5555",
    "gender": "F",
    "status": "WITHDRAWN",
    "level": "NEW",
    "authority": "USER",
    "withdrawn": true,
    "createAt": "2025-05-06T10:30:00",
    "updateAt": "2025-05-07T09:12:00"
  }
]
```

## 사용자 - 회원 조회 API (탈퇴 여부 포함)
URL : GET /v1/members/me

[REQUEST]
HEADER
X-MEMBER-ID : test@test.com

[RESPONSE]
HEADER
200 OK

BODY
```json
{
  "memberId": "test@test.com",
  "name": "test-user",
  "birthday": "2025-05-06",
  "phoneNumber": "010-2244-5555",
  "gender": "F",
  "status": "ACTIVE",
  "level": "NEW",
  "authority": "USER",
  "withdrawn": false,
  "createAt": "2025-05-06T10:30:00",
  "updateAt": "2025-05-07T09:12:00"
}
```
