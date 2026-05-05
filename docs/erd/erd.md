# 6. ERD 및 데이터 설계

본 ERD는 주문 흐름(CART → ORDER → ORDER_ITEM)과 포인트 이력 관리를 중심으로 설계하였다.

<img width="1118" height="768" alt="커피 프로젝트 ERD" src="https://github.com/user-attachments/assets/514a5736-7db0-43ac-88e1-f5a955d23f7b" />

## 데이터 설계 정책
- total_price는 ORDER_ITEM의 (price × quantity)의 합으로 계산한다.
- 주문 생성 후 CART_ITEM은 초기화된다.
- amount는 포인트 증감 방향에 따라 (+, -) 값으로 관리한다.
    - CHARGE  : +
    - USE     : -
    - REFUND  : + (주문 취소 복구)
    - EXCHANGE: - (환전)
- 이미지는 외부 스토리지(S3 등)에 저장하고 URL만 관리한다.
- 모든 조회는 Soft Delete 정책에 따라 deleted_at IS NULL 조건을 기본으로 한다.
- USER.point는 조회 성능을 위한 캐시 역할을 하며, 실제 정합성은 POINT_HISTORY를 기준으로 관리한다.

## 주문 상태 흐름
PENDING → ORDERED (결제 성공)
PENDING → CANCELED (결제 실패/취소)
ORDERED → CANCELED (주문 취소 및 포인트 복구)

## 장바구니 → 주문 변환 흐름
CART → ORDER 생성 시
CART_ITEM → ORDER_ITEM으로 복사

## 성능 최적화
- order_id, menu_id 컬럼에 인덱스를 적용하여 주문 조회 및 통계 조회 성능을 향상시킨다.
