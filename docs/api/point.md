# 💰 포인트 API

※ 모든 API는 Base URL(`/api`) 하위에서 동작한다.

※ 포인트는 정수 단위로 관리되며 음수가 될 수 없다.

※ 모든 포인트 변경은 `POINT_HISTORY`에 기록된다.

※ 포인트 차감(`USE`)은 동시성 제어 대상이며 Redis 분산락으로 보호된다.

※ 해당 로직은 주문 결제 시 적용된다.

---

## 1. 포인트 충전

| 항목     | 내용               |
| ------ | ---------------- |
| Method | POST             |
| URL    | `/points/charge` |
| 설명     | 포인트 충전 (현금 → 포인트) |
| 인증 | 필요 |

※ 충전 금액은 10,000원 이상이어야 한다

※ 충전 금액은 5,000원 단위여야 한다

※ 최대 보유 포인트는 2,000,000원을 초과할 수 없다

---

### Request

| 필드     | 타입  | 설명    |
| ------ | --- | ----- |
| amount | int | 충전 금액 |

```json
{
    "amount": 10000
}
```

---

### Response `200 OK`
```json
{
    "message": "포인트 충전이 완료되었습니다.",
    "data": {
        "currentPoint": 15000
    },
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

### Response `400 Bad Request`
```json
{
    "message": "충전 금액은 10,000원 이상이며 5,000원 단위여야 합니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

### Response `409 Conflict`

```json
{
    "message": "최대 보유 포인트를 초과할 수 없습니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---
## 2. 포인트 환전

| 항목     | 내용                |
| ------ | ----------------- |
| Method | POST              |
| URL    | `/points/exchange` |
| 설명     | 포인트 환전 (포인트 → 현금) |
| 인증 | 필요 |

※ 보유 포인트 이상 환전할 수 없다

※ 환전은 5,000 포인트 이상부터 가능하다.

※ 환전 금액은 5,000 단위여야 한다

---

### Request
```json
{
    "amount": 5000
}
```

---

### Response `200 OK`
```json
{
    "message": "포인트 환전이 완료되었습니다.",
    "data": {
        "currentPoint": 10000
    },
    "timestamp": "2026-05-11T09:00:00.000000"
}
```

---

### Response `400 Bad Request`
```json
{
    "message": "환전 금액은 5,000 이상이며 5,000 단위여야 합니다.",
    "data": null,
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

---

## 3. 포인트 조회

| 항목     | 내용       |
| ------ | -------- |
| Method | GET      |
| URL    | `/points` |
| 설명     | 현재 포인트 조회 |
| 인증 | 필요 |

---

### Response `200 OK`
```json
{
    "message": "포인트 조회 성공",
    "data": {
        "currentPoint": 15000
    },
    "timestamp": "2026-05-11T09:00:00.000000"
}
```
---

## 4. 포인트 내역 조회

| 항목     | 내용                 |
| ------ | ------------------ |
| Method | GET                |
| URL    | `/points/histories` |
| 설명     | 포인트 사용/충전/환전 이력 조회 |
| 인증 | 필요 |

※ 최신순(createdAt DESC)으로 정렬하여 반환한다.

---

### Query Parameter
| 필드   | 타입  | 필수 | 설명     |
| ---- | --- | -- | ------ |
| page | int | 선택 | 페이지 번호 |
| size | int | 선택 | 페이지 크기 |

※ page는 Spring Pageable 기본 규칙에 따라 0부터 시작한다.

---
### type 정의
| 값        | 설명      |
| -------- | ------- |
| CHARGE   | 충전      |
| USE      | 사용 (주문) |
| REFUND   | 환불      |
| EXCHANGE | 환전      |

---

### Response `200 OK`
```json
{
    "message": "포인트 내역 조회 성공",
    "data": {
        "content": [
            {
                "historyId": 1,
                "type": "CHARGE",
                "amount": 10000,
                "balance": 15000,
                "createdAt": "2026-05-11T09:00:00.000000"
            }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 30,
        "totalPages": 3
    },
    "timestamp": "2026-05-11T09:00:00.000000"
}
```
