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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    // AVAILABLE, SOLD_OUT 상태의 메뉴만 목록 조회한다.
    @Transactional(readOnly = true)
    public MenuListResponse getMenus(MenuType type, Pageable pageable) {
        Page<Menu> menuPage;

        if (type == null) {
            menuPage = menuRepository.findAllByStatusInOrderByIdAsc(
                    List.of(MenuStatus.AVAILABLE, MenuStatus.SOLD_OUT),
                    pageable
            );
        } else {
            menuPage = menuRepository.findAllByTypeAndStatusInOrderByIdAsc(
                    type,
                    List.of(MenuStatus.AVAILABLE, MenuStatus.SOLD_OUT),
                    pageable
            );
        }

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

    // 인기 메뉴를 조회한다.
    // 현재 Redis 연동 전이면 빈 리스트를 반환하고, 추후 ZSet 연동으로 확장한다.
    @Transactional(readOnly = true)
    public List<PopularMenuResponse> getPopularMenus() {
        return Collections.emptyList();
    }

}
