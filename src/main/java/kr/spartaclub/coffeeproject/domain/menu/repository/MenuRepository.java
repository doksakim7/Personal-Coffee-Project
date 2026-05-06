package kr.spartaclub.coffeeproject.domain.menu.repository;

import kr.spartaclub.coffeeproject.common.enums.MenuStatus;
import kr.spartaclub.coffeeproject.common.enums.MenuType;
import kr.spartaclub.coffeeproject.domain.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 상태가 AVAILABLE, SOLD_OUT 인 메뉴 목록 조회
    Page<Menu> findAllByStatusInOrderByIdAsc(List<MenuStatus> statuses, Pageable pageable);

    // 특정 카테고리 + 상태가 AVAILABLE, SOLD_OUT 인 메뉴 목록 조회
    Page<Menu> findAllByTypeAndStatusInOrderByIdAsc(MenuType type, List<MenuStatus> statuses, Pageable pageable);

}
