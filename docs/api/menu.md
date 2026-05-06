# ☕ 메뉴 API

※ 모든 API는 Base URL(`/api`) 하위에서 동작한다.

---

## 1. 메뉴 목록 조회

| 항목     | 내용       |
|----------|------------|
| Method   | GET        |
| URL      | `/menus`   |
| 설명      | 전체 메뉴 목록 조회 |
| 인증      | 불필요 |

※ 상태가 `AVAILABLE` 또는 `SOLD_OUT` 인 메뉴만 조회 가능하다.

※ `INACTIVE` 상태의 메뉴는 조회되지 않는다.

※ 조회 결과가 없을 경우 content는 빈 배열(`[]`)을 반환한다.

※ 메뉴 목록은 기본 정렬은 menuId 기준 오름차순이며, 추후 정렬 기준은 확장 가능하다.

---

### Query Parameter

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| type | String | 선택 | 메뉴 카테고리 필터 (`COFFEE`, `LATTE`, `ADE`, `TEA`, `DESSERT`, `SET`) |
| page | int | 선택 | 페이지 번호 |
| size | int | 선택 | 페이지 크기 |

---

### type 정의

| 값      | 설명    |
|--------|-------|
| `COFFEE` | 커피    |
| `LATTE`  | 라떼    |
| `ADE`    | 에이드   |
| `TEA`    | 차     |
| `DESSERT` | 디저트   |
| `SET`    | 세트 메뉴 |

※ type은 메뉴의 카테고리를 의미한다.

※ type 파라미터를 통해 특정 카테고리 메뉴만 조회할 수 있다.

※ 허용되지 않은 type 값이 전달될 경우 `400 Bad Request`를 반환한다.

※ 삭제된 메뉴(`deleted_at != null`)는 조회되지 않는다.

---

### Response `200 OK`
```json
{
    "message": "메뉴 목록 조회 성공",
    "data": {
        "content": [
            {
                "menuId": 1,
                "name": "아메리카노",
                "price": 4000,
                "status": "AVAILABLE",
                "type": "COFFEE"
            }
        ],
        "page": 0,
        "size": 10,
        "totalElements": 100,
        "totalPages": 10
    },
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```

---

## 2. 메뉴 상세 조회

| 항목     | 내용                |
| ------ | ----------------- |
| Method | GET               |
| URL    | `/menus/{menuId}` |
| 설명     | 메뉴 상세 조회          |
| 인증 | 불필요                    |

※ 메뉴 상세 조회는 상태와 관계없이 조회 가능하다.

※ 단, 삭제된 메뉴(`deleted_at != null`)는 조회되지 않는다.

※ 응답에 포함된 status 값을 기반으로 클라이언트가 주문 가능 여부를 판단한다.

---

### Path Variable

| 필드     | 타입   | 설명    |
| ------ | ---- | ----- |
| menuId | Long | 메뉴 ID |

---

### Response `200 OK`
```json
{
    "message": "메뉴 상세 조회 성공",
    "data": {
        "menuId": 1,
        "name": "아메리카노",
        "price": 4000,
        "status": "AVAILABLE",
        "type": "COFFEE"
    },
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```

---

### Response `404 Not Found`
```json
{
    "message": "존재하지 않는 메뉴입니다.",
    "data": null,
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```

---

# 📊 인기 메뉴 API

## 1. 인기 메뉴 조회

| 항목     | 내용                     |
| ------ |------------------------|
| Method | GET                    |
| URL    | `/menus/popular`       |
| 설명     | Redis ZSet 기반 인기 메뉴 조회 |
| 인증 | 불필요                    |

※ 인기 메뉴에서도 메뉴 카테고리 구분을 위해 type 정보를 함께 반환한다.

※ `orderCount` 기준으로 내림차순 정렬하여 상위 메뉴를 반환한다.

※ 인기 메뉴는 상위 10개를 반환한다.

※ 인기 메뉴는 누적 주문 횟수를 기준으로 집계한다.

※ 인기 메뉴는 주문 이벤트 발생 시 Redis ZSet에 즉시 반영되며, 분산 환경 특성상 데이터는 최종 일관성(eventual consistency)을 따른다.

※ 주문 취소(`CANCELED_BY_USER`, `CANCELED_BY_SYSTEM`)된 데이터는 인기 메뉴 집계에서 제외된다.

---

### Response `200 OK`
```json
{
    "message": "인기 메뉴 조회 성공",
    "data": [
        {
            "menuId": 1,
            "name": "아메리카노",
            "type": "COFFEE",
            "orderCount": 120
        }
    ],
    "timestamp": "2026-05-11T00:00:00+09:00"
}
```
