package kr.spartaclub.coffeeproject.domain.point.repository;

import kr.spartaclub.coffeeproject.domain.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
