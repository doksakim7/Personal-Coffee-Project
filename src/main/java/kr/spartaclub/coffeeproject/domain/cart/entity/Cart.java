package kr.spartaclub.coffeeproject.domain.cart.entity;

import jakarta.persistence.*;
import kr.spartaclub.coffeeproject.common.entity.BaseEntity;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "carts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_user", columnNames = "user_id")
        }
)
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자별 장바구니 1개
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ==========================
    // 생성자
    // ==========================

    public Cart(User user) {
        this.user = user;
    }

}
