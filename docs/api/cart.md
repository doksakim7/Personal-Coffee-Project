# 🛒 장바구니 API

※ 모든 API는 Base URL(`/api`) 하위에서 동작한다.

※ 장바구니는 사용자별로 1개만 존재한다.

---

## 1. 장바구니 조회

| 항목     | 내용              |
| ------ | --------------- |
| Method | GET             |
| URL    | `/carts`        |
| 설명     | 현재 사용자의 장바구니 조회 |
| 인증     | 필요              |

※ 장바구니가 존재하지 않을 경우, 빈 장바구니를 생성하여 반환한다.

※ `totalPrice`는 각 item의 `(price × quantity)`의 합으로 계산된다.

---

### Response `200 OK`
```json
{
    "message": "장바구니 조회 성공",
    "data": {
        "cartId": 1,
        "items": [
            {
                "itemId": 1,
                "menuId": 1,
                "name": "아메리카노",
                "price": 4000,
                "quantity": 2,
                "totalPrice": 8000,
                "status": "AVAILABLE"
            }
        ],
        "totalPrice": 8000
    }, 
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

## 2. 장바구니 상품 추가

| 항목     | 내용            |
| ------ | ------------- |
| Method | POST          |
| URL    | `/carts/items` |
| 설명     | 장바구니에 상품 추가   |
| 인증 | 필요 |

※ 동일한 `menuId`가 이미 존재할 경우, 새로운 item을 생성하지 않고 기존 item의 수량을 증가시킨다.

※ 메뉴 상태가 `AVAILABLE`인 경우에만 장바구니에 추가할 수 있다.

---

### Request

| 필드       | 타입   | 설명    |
| -------- | ---- | ----- |
| menuId   | Long | 메뉴 ID |
| quantity | int  | 수량    |

```json
{
    "menuId": 1,
    "quantity": 2
}
```

---

### Response `200 OK`
```json
{
    "message": "장바구니에 상품이 추가되었습니다.",
    "data": {
        "itemId": 1,
        "menuId": 1,
        "quantity": 2
    }, 
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

### Response `400 Bad Request`
```json
{
    "message": "수량은 1 이상이어야 합니다.",
    "data": null, 
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

### Response `404 Not Found`
```json
{
    "message": "존재하지 않는 메뉴입니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

## 3. 장바구니 상품 수량 변경

| 항목     | 내용                     |
| ------ | ---------------------- |
| Method | PATCH                  |
| URL    | `/carts/items/{itemId}` |
| 설명     | 장바구니 상품 수량 변경          |
| 인증 | 필요 |

※ 수량은 1 이상이어야 한다.

---

### Path Variable

| 필드     | 타입   | 설명          |
| ------ | ---- | ----------- |
| itemId | Long | 장바구니 아이템 ID |

---

### Request

| 필드       | 타입  | 설명     |
| -------- | --- | ------ |
| quantity | int | 변경할 수량 |

```json
{
    "quantity": 3
}
```

---

### Response `200 OK`
```json
{
    "message": "수량이 변경되었습니다.",
    "data": {
        "itemId": 1,
        "quantity": 3
    }, 
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

### Response `404 Not Found`
```json
{
    "message": "존재하지 않는 장바구니 상품입니다.",
    "data": null, 
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

### Response ` 400 Bad Request`
```json
{
    "message": "수량은 1 이상이어야 합니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00.00.000000"
}
```

---

## 4. 장바구니 상품 삭제

| 항목     | 내용                     |
| ------ | ---------------------- |
| Method | DELETE                 |
| URL    | `/carts/items/{itemId}` |
| 설명     | 장바구니 상품 삭제             |
| 인증 | 필요 |

※ 해당 API는 멱등성을 보장한다.

※ 이미 삭제되었거나 존재하지 않는 item에 대해서도 예외를 발생시키지 않고 `200 OK`를 반환한다.

---

### Path Variable

| 필드     | 타입   | 설명          |
| ------ | ---- | ----------- |
| itemId | Long | 장바구니 아이템 ID |

---

### Response `200 OK`
```json
{
    "message": "상품 삭제 요청이 처리되었습니다.",
    "data": null,
    "timestamp": "2026-05-11T09:00.00.000000"
}
```
