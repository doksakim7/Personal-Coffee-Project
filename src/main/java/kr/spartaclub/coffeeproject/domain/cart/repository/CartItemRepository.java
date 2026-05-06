package kr.spartaclub.coffeeproject.domain.cart.repository;

import kr.spartaclub.coffeeproject.domain.cart.entity.Cart;
import kr.spartaclub.coffeeproject.domain.cart.entity.CartItem;
import kr.spartaclub.coffeeproject.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 특정 장바구니에 담긴 모든 상품 조회
    List<CartItem> findAllByCart(Cart cart);

    // 특정 장바구니에 동일 메뉴가 이미 담겨있는지 조회
    Optional<CartItem> findByCartAndMenu(Cart cart, Menu menu);

}
