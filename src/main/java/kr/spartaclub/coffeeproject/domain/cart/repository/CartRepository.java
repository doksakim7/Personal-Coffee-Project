package kr.spartaclub.coffeeproject.domain.cart.repository;

import kr.spartaclub.coffeeproject.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
