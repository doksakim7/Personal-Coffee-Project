package kr.spartaclub.coffeeproject.domain.order.repository;

import kr.spartaclub.coffeeproject.domain.order.entity.Order;
import kr.spartaclub.coffeeproject.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 특정 주문에 포함된 상품 목록 조회
    List<OrderItem> findAllByOrder(Order order);

}
