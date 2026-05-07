package kr.spartaclub.coffeeproject.domain.order.repository;

import kr.spartaclub.coffeeproject.common.enums.OrderStatus;
import kr.spartaclub.coffeeproject.domain.order.entity.Order;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 현재 사용자의 주문 목록을 최신순으로 조회
    Page<Order> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 동일 사용자 기준 특정 상태 주문 존재 여부 확인
    boolean existsByUserAndStatus(User user, OrderStatus status);

}
