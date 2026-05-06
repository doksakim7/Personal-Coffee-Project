package kr.spartaclub.coffeeproject.domain.point.repository;

import kr.spartaclub.coffeeproject.domain.point.entity.PointHistory;
import kr.spartaclub.coffeeproject.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 현재 사용자의 포인트 내역을 최신순으로 조회
    Page<PointHistory> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

}
