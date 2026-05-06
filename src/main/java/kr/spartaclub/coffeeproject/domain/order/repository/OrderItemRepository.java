package kr.spartaclub.coffeeproject.domain.order.repository;

import kr.spartaclub.coffeeproject.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
