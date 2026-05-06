package kr.spartaclub.coffeeproject.domain.point.entity;

import jakarta.persistence.*;
import kr.spartaclub.coffeeproject.common.entity.BaseEntity;
import kr.spartaclub.coffeeproject.common.enums.PointType;
import kr.spartaclub.coffeeproject.domain.order.entity.Order;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_histories")
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 포인트 소유 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 관련 주문 (충전/환전은 null 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // 포인트 증감 값
    @Column(nullable = false)
    private Long amount;

    // 처리 후 잔액
    @Column(nullable = false)
    private Long balance;

    // 포인트 이력 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointType type;

    // ==========================
    // 생성자
    // ==========================

    public PointHistory(User user, Order order, Long amount, Long balance, PointType type) {
        this.user = user;
        this.order = order;
        this.amount = amount;
        this.balance = balance;
        this.type = type;
    }

    // ==========================
    // 비즈니스 메서드
    // ==========================

    // 주문 연관 여부 확인
    public boolean hasOrder() {
        return this.order != null;
    }

}
