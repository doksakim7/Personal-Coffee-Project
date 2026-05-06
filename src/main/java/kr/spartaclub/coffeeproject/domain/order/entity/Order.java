package kr.spartaclub.coffeeproject.domain.order.entity;

import jakarta.persistence.*;
import kr.spartaclub.coffeeproject.common.entity.SoftDeleteEntity;
import kr.spartaclub.coffeeproject.common.enums.OrderStatus;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 주문 총 금액
    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    // 주문 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    // ==========================
    // 생성자
    // ==========================

    public Order(User user, Long totalPrice) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING;
    }

    // ==========================
    // 비즈니스 메서드
    // ==========================

    // 결제 완료
    public void complete() {
        this.status = OrderStatus.ORDERED;
    }

    // 결제 실패로 인한 취소
    public void cancelBySystem() {
        this.status = OrderStatus.CANCELED_BY_SYSTEM;
    }

    // 사용자 요청 취소
    public void cancelByUser() {
        this.status = OrderStatus.CANCELED_BY_USER;
    }

    // 총 금액 변경
    public void updateTotalPrice(Long totalPrice) {
        this.totalPrice = totalPrice;
    }

    // 결제 가능 여부
    public boolean isPending() {
        return this.status == OrderStatus.PENDING;
    }

    // 이미 결제 완료된 주문인지
    public boolean isOrdered() {
        return this.status == OrderStatus.ORDERED;
    }

    // 이미 취소된 주문인지
    public boolean isCanceled() {
        return this.status == OrderStatus.CANCELED_BY_USER
                || this.status == OrderStatus.CANCELED_BY_SYSTEM;
    }

}
