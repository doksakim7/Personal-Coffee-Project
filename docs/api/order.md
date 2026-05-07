# 📦 주문 API

※ 모든 API는 Base URL(`/api`) 하위에서 동작한다.

※ 주문은 장바구니 기반으로 생성된다.

※ 주문 결제 성공 시 장바구니는 초기화된다.

※ 주문 상태에 따른 처리
- `PENDING` → 결제 진행 가능
- `ORDERED` → 동일 결과 반환 (멱등성)
- `CANCELED_BY_USER`, `CANCELED_BY_SYSTEM` → 결제 불가 (`409 Conflict`)

---

## 1. 주문 생성

| 항목     | 내용           |
| ------ | ------------ |
| Method | POST         |
| URL    | `/orders`    |
| 설명     | 장바구니 기반 주문 생성 (결제 전 상태) |
| 인증 | 필요 |

※ 주문 생성 시 장바구니 데이터를 기반으로 주문이 생성된다.

※ 주문 생성 단계에서는 포인트 검증을 수행하지 않으며, 모든 주문은 `PENDING` 상태로 생성된다.

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

※ 중복 요청으로 인한 주문 중복 생성을 방지한다.

---

### Response `201 Created`
```json
{
    "message": "주문이 생성되었습니다.",
    "data": {
        "orderId": 1,
        "totalPrice": 8000,
        "status": "PENDING"
    },
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

### Response `400 Bad Request`
```json
{
    "message": "Idempotency-Key가 필요합니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

## 2. 주문 결제

| 항목     | 내용                      |
| ------ | ----------------------- |
| Method | POST                    |
| URL    | `/orders/{orderId}/pay` |
| 설명     | 주문 결제 (포인트 차감 + 상태 변경)  |
| 인증     | 필요                      |

※ `PENDING` 상태에서만 결제 가능

※ 결제 시 포인트가 차감

※ 성공 시 `ORDERED`, 포인트 부족 등 비즈니스 실패 시 `CANCELED_BY_SYSTEM` 상태 변경

※ 락 실패, 시스템 오류 시 상태 변경 없음 (`PENDING` 유지)

※ Redis 분산락 적용

※ `ORDERED` 상태에서 재요청 시 동일 결과를 반환한다 (멱등성 보장)

※ 이미 결제 완료된 주문에 대해서는 실제 결제 로직을 수행하지 않고 기존 결과를 반환한다.

※ `CANCELED_BY_USER`, `CANCELED_BY_SYSTEM` 상태에서는 결제 요청이 불가능하다

---

### Response `200 OK`
```json
{
    "message": "결제가 완료되었습니다.",
    "data": {
        "orderId": 1,
        "status": "ORDERED"
    },
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

### Response `409 Conflict`
```json
{
    "message": "포인트가 부족합니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00:00.000000"
}
```
※ 포인트가 부족한 경우

---

### Response `409 Conflict`
```json
{
    "message": "이미 취소된 주문입니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

※ `CANCELED_BY_USER`, `CANCELED_BY_SYSTEM` 상태인 경우 → 이미 취소된 주문

---

### Response `429 Too Many Requests`
```json
{
    "message": "요청이 많습니다. 잠시 후 다시 시도해주세요.",
    "data": null,
    "timestamp": "2026-05-11T09:00:00.000000"
}
```
※ Redis 분산락 획득 실패 시 반환

---

## 3. 주문 목록 조회

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

※ page는 Spring Pageable 기본 규칙에 따라 0부터 시작한다.

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
                "createdAt": "2026-05-11T09:00:00.000000"
            }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 50,
        "totalPages": 5
    },
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

## 4. 주문 상세 조회

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
        "createdAt": "2026-05-11T09:00:00.000000"
    },
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

### Response `404 Not Found`

```json
{
    "message": "존재하지 않는 주문입니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

## 5. 주문 취소

| 항목     | 내용                        |
| ------ | ------------------------- |
| Method | POST                      |
| URL    | `/orders/{orderId}/cancel` |
| 설명     | 주문 취소 및 포인트 복구            |
| 인증 | 필요 |

※ 주문 상태가 `PENDING`, `ORDERED` 모두 취소 가능

※ `ORDERED` 상태에서 취소 시 포인트를 복구하고 `POINT_HISTORY`에 `REFUND` 기록

※ 취소된 주문은 `CANCELED_BY_USER` 상태로 변경

---

### Response `200 OK`
```json
{
    "message": "주문이 취소되었습니다.",
    "data": {
        "orderId": 1,
        "status": "CANCELED_BY_USER"
    },
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

### Response `409 Conflict`
```json
{
    "message": "이미 취소된 주문입니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00:00.000000"
}
```
※ `CANCELED_BY_USER`, `CANCELED_BY_SYSTEM` 상태인 경우 → 이미 취소된 주문
