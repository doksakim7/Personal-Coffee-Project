package kr.spartaclub.coffeeproject.domain.order.repository;

import kr.spartaclub.coffeeproject.domain.order.entity.Order;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 현재 사용자의 주문 목록을 최신순으로 조회
    Page<Order> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

}
