package kr.spartaclub.coffeeproject.domain.menu.service;

import kr.spartaclub.coffeeproject.common.enums.MenuStatus;
import kr.spartaclub.coffeeproject.common.enums.MenuType;
import kr.spartaclub.coffeeproject.common.exception.CustomException;
import kr.spartaclub.coffeeproject.common.exception.ErrorCode;
import kr.spartaclub.coffeeproject.domain.menu.dto.response.MenuDetailResponse;
import kr.spartaclub.coffeeproject.domain.menu.dto.response.MenuListResponse;
import kr.spartaclub.coffeeproject.domain.menu.dto.response.PopularMenuResponse;
import kr.spartaclub.coffeeproject.domain.menu.entity.Menu;
import kr.spartaclub.coffeeproject.domain.menu.repository.MenuRepository;
import kr.spartaclub.coffeeproject.domain.menu.repository.PopularMenuRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuService {

    private static final int POPULAR_MENU_LIMIT = 10;

    private final MenuRepository menuRepository;
    private final PopularMenuRedisRepository popularMenuRedisRepository;

    // AVAILABLE, SOLD_OUT 상태의 메뉴를 조회하며, type 조건은 QueryDSL로 동적으로 반영한다.
    @Transactional(readOnly = true)
    public MenuListResponse getMenus(MenuType type, Pageable pageable) {

        // 메뉴 타입 조건을 동적으로 반영하기 위해 QueryDSL 기반 조회를 사용한다.
        Page<Menu> menuPage = menuRepository.searchMenus(type, pageable);

        List<MenuListResponse.MenuSummary> content = menuPage.getContent().stream()
                .map(menu -> new MenuListResponse.MenuSummary(
                        menu.getId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getStatus().name(),
                        menu.getType().name()
                ))
                .toList();

        return new MenuListResponse(
                content,
                menuPage.getNumber(),
                menuPage.getSize(),
                menuPage.getTotalElements(),
                menuPage.getTotalPages()
        );
    }

    // 삭제되지 않은 메뉴를 상세 조회한다.
    @Transactional(readOnly = true)
    public MenuDetailResponse getMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        return new MenuDetailResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getStatus().name(),
                menu.getType().name()
        );
    }

    // Redis ZSet 기준 상위 10개의 인기 메뉴를 조회한다.
    @Transactional(readOnly = true)
    public List<PopularMenuResponse> getPopularMenus() {
        Map<Long, Long> topMenus = popularMenuRedisRepository.getTopMenus(POPULAR_MENU_LIMIT);

        if (topMenus.isEmpty()) {
            return List.of();
        }

        List<Long> menuIds = new ArrayList<>(topMenus.keySet());

        Map<Long, Menu> menuMap = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, menu -> menu));

        List<PopularMenuResponse> result = new ArrayList<>();

        // ZSet score 순서를 유지하기 위해 menuIds 순서대로 응답 생성
        for (Long menuId : menuIds) {
            Menu menu = menuMap.get(menuId);

            // 삭제되었거나 조회되지 않는 메뉴는 제외
            if (menu == null) {
                continue;
            }

            result.add(new PopularMenuResponse(
                    menu.getId(),
                    menu.getName(),
                    menu.getType().name(),
                    topMenus.get(menuId)
            ));
        }

        return result;
    }

}
