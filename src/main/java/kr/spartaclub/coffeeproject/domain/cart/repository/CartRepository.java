package kr.spartaclub.coffeeproject.domain.cart.repository;

import kr.spartaclub.coffeeproject.domain.cart.entity.Cart;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // 사용자에 해당하는 장바구니 1개 조회
    Optional<Cart> findByUser(User user);

}
