# 📦 주문 API

※ 모든 API는 Base URL(`/api`) 하위에서 동작한다.

※ 주문은 장바구니 기반으로 생성된다.

※ 주문 생성 시 장바구니는 초기화된다.

---

## 1. 주문 생성

| 항목     | 내용           |
| ------ | ------------ |
| Method | POST         |
| URL    | `/orders`    |
| 설명     | 장바구니 기반 주문 생성 |
| 인증 | 필요 |

※ 주문 생성 시 장바구니 데이터를 기반으로 주문이 생성된다.

---

### 🔑 Idempotency Key

※ 본 API는 멱등성을 보장한다.

※ 클라이언트는 `Idempotency-Key` 헤더를 포함해야 한다.

※ `Idempotency-Key`가 누락된 경우 `400 Bad Request`를 반환한다.

※ 동일한 `Idempotency-Key`로 서로 다른 요청을 보낼 경우 `409 Conflict`를 반환한다.

```html
Idempotency-Key: {unique-key}
```
※ 동일한 `Idempotency-Key`로 요청 시, 동일한 결과를 반환한다.

※ 중복 요청으로 인한 결제 중복을 방지한다.

---

### Response `201 Created`
```json
{
    "message": "주문이 완료되었습니다.",
    "data": {
        "orderId": 1,
        "totalPrice": 8000,
        "status": "ORDERED"
    },
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```

---

### Response `409 Conflict`
```json
{
    "message": "포인트가 부족합니다.",
    "data": null,
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```

---

### Response `429 Too Many Requests`
```json
{
    "message": "요청이 많습니다. 잠시 후 다시 시도해주세요.",
    "data": null,
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```
※ Redis 분산락 획득 실패 시 반환

---

## 2. 주문 목록 조회

| 항목     | 내용       |
| ------ | -------- |
| Method | GET      |
| URL    | `/orders` |
| 설명     | 주문 목록 조회 |
| 인증 | 필요 |

---

### Query Parameter

| 필드   | 타입  | 필수 | 설명     |
| ---- | --- | -- | ------ |
| page | int | 선택 | 페이지 번호 |
| size | int | 선택 | 페이지 크기 |

---

### Response `200 OK`

```json
{
    "message": "주문 목록 조회 성공",
    "data": {
        "content": [
            {
                "orderId": 1,
                "totalPrice": 8000,
                "status": "ORDERED",
                "createdAt": "2026-05-11T00:00:00+09:00"
            }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 50,
        "totalPages": 5
    },
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```

---

## 3. 주문 상세 조회

| 항목     | 내용                 |
| ------ | ------------------ |
| Method | GET                |
| URL    | `/orders/{orderId}` |
| 설명     | 주문 상세 조회           |
| 인증 | 필요 |

---

### Path Variable

| 필드      | 타입   | 설명    |
| ------- | ---- | ----- |
| orderId | Long | 주문 ID |

---

### Response `200 OK`
```json
{
    "message": "주문 상세 조회 성공",
    "data": {
        "orderId": 1,
        "items": [
            {
                "menuId": 1,
                "name": "아메리카노",
                "price": 4000,
                "quantity": 2
            }
        ],
        "totalPrice": 8000,
        "status": "ORDERED",
        "createdAt": "2026-05-11T00:00:00+09:00"
    },
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```

---

### Response `404 Not Found`

```json
{
    "message": "존재하지 않는 주문입니다.",
    "data": null,
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```

---

## 4. 주문 취소

| 항목     | 내용                        |
| ------ | ------------------------- |
| Method | POST                      |
| URL    | `/orders/{orderId}/cancel` |
| 설명     | 주문 취소 및 포인트 복구            |
| 인증 | 필요 |

※ 주문 상태가 `ORDERED`인 경우에만 취소 가능

※ 취소 시 포인트를 복구하고 `POINT_HISTORY`에 `REFUND` 기록

※ 취소된 주문은 `CANCELED` 상태로 변경된다

---

### Response `200 OK`
```json
{
    "message": "주문이 취소되었습니다.",
    "data": {
        "orderId": 1,
        "status": "CANCELED"
    },
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```

### Response 409 Conflict
```json
{
    "message": "이미 취소된 주문입니다.",
    "data": null,
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```
