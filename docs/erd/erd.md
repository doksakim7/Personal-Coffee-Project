# 6. ERD 및 데이터 설계

본 ERD는 주문 흐름(`CART` → `ORDER` → `ORDER_ITEM`)과 포인트 이력 관리를 중심으로 설계하였다.

![커피 프로젝트 ERD.png](../image/%E1%84%8F%E1%85%A5%E1%84%91%E1%85%B5%20%E1%84%91%E1%85%B3%E1%84%85%E1%85%A9%E1%84%8C%E1%85%A6%E1%86%A8%E1%84%90%E1%85%B3%20ERD.png)

## 데이터 설계 정책
- `total_price`는 `ORDER_ITEM`의 (`price × quantity`)의 합으로 계산한다.
- 주문 결제 성공(`ORDERED`) 시 `CART_ITEM`은 초기화된다.
- `amount`는 포인트 증감 방향에 따라 (+, -) 값으로 관리한다.
    - `CHARGE`  : +
    - `USE`     : -
    - `REFUND`  : + (주문 취소 복구)
    - `EXCHANGE`: - (환전)
- 이미지는 외부 스토리지(S3 등)에 저장하고 URL만 관리한다.
- 모든 조회는 Soft Delete 정책에 따라 `deleted_at IS NULL` 조건을 기본으로 한다.
- `USER.point`는 현재 포인트 잔액의 기준 값이며, `POINT_HISTORY`는 포인트 변경 이력을 저장하는 로그 테이블로 사용된다.
- 모든 포인트 변경은 두 테이블에 동시에 반영된다.
- `POINT_HISTORY.balance`는 각 포인트 이력이 반영된 직후의 잔액을 저장한다.
- `ORDER.ordered_at`은 결제 완료 시각을 저장하며, 인기 메뉴 집계 시 기준 시각으로 사용한다.

## 주문 상태 흐름
`PENDING` → `ORDERED` (결제 성공)
`PENDING` → `CANCELED_BY_SYSTEM` (결제 실패)
`PENDING` → `CANCELED_BY_USER` (주문 취소 시)

`ORDERED` → `CANCELED_BY_USER` (주문 취소 및 포인트 복구)

## 장바구니 → 주문 변환 흐름
`CART` → `ORDER` 생성 시
`CART_ITEM` → `ORDER_ITEM`으로 복사

## 성능 최적화
- `order_id`, `menu_id` 컬럼에 인덱스를 적용하여 주문 조회 및 통계 조회 성능을 향상시킨다.
