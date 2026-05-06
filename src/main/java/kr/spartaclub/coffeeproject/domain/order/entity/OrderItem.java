package kr.spartaclub.coffeeproject.domain.order.entity;

import jakarta.persistence.*;
import kr.spartaclub.coffeeproject.common.entity.BaseEntity;
import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import kr.spartaclub.coffeeproject.domain.menu.entity.Menu;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 주문
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 주문한 메뉴
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    // 주문 시점 가격 스냅샷
    @Column(nullable = false)
    private Long price;

    // 주문 수량
    @Column(nullable = false)
    private Integer quantity;

    // ==========================
    // 생성자
    // ==========================

    public OrderItem(Order order, Menu menu, Long price, Integer quantity) {
        validateQuantity(quantity);
        this.order = order;
        this.menu = menu;
        this.price = price;
        this.quantity = quantity;
    }

    // ==========================
    // 비즈니스 메서드
    // ==========================

    // 총 가격 계산
    public Long getTotalPrice() {
        return this.price * this.quantity;
    }

    // 수량 검증
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new CustomException(ErrorCode.CART_INVALID_QUANTITY);
        }
    }

}
