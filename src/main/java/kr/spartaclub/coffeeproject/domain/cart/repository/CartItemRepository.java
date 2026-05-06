package kr.spartaclub.coffeeproject.domain.cart.repository;

import kr.spartaclub.coffeeproject.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
