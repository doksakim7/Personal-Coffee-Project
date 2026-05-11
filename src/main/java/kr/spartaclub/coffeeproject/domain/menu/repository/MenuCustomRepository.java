package kr.spartaclub.coffeeproject.domain.menu.repository;

import kr.spartaclub.coffeeproject.common.enums.MenuType;
import kr.spartaclub.coffeeproject.domain.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MenuCustomRepository {

    // 메뉴 타입 조건 유무에 따라 주문 가능한 메뉴 목록을 동적으로 조회한다.
    Page<Menu> searchMenus(MenuType type, Pageable pageable);

}
