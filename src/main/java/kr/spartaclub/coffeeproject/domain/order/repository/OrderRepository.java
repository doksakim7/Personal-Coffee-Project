package kr.spartaclub.coffeeproject.domain.order.repository;

import kr.spartaclub.coffeeproject.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
