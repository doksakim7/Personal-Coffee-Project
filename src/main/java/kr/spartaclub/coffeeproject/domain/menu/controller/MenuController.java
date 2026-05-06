package kr.spartaclub.coffeeproject.domain.menu.controller;

import kr.spartaclub.coffeeproject.common.enums.MenuType;
import kr.spartaclub.coffeeproject.common.response.ApiResponse;
import kr.spartaclub.coffeeproject.domain.menu.dto.response.MenuDetailResponse;
import kr.spartaclub.coffeeproject.domain.menu.dto.response.MenuListResponse;
import kr.spartaclub.coffeeproject.domain.menu.dto.response.PopularMenuResponse;
import kr.spartaclub.coffeeproject.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    // 메뉴 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<MenuListResponse>> getMenus(
            @RequestParam(required = false) MenuType type,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        MenuListResponse response = menuService.getMenus(type, pageable);
        return ResponseEntity.ok(ApiResponse.success("메뉴 목록 조회 성공", response));
    }

    // 메뉴 상세 조회
    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> getMenu(
            @PathVariable Long menuId
    ) {
        MenuDetailResponse response = menuService.getMenu(menuId);
        return ResponseEntity.ok(ApiResponse.success("메뉴 상세 조회 성공", response));
    }

    // 인기 메뉴 조회
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PopularMenuResponse>>> getPopularMenus() {
        List<PopularMenuResponse> response = menuService.getPopularMenus();
        return ResponseEntity.ok(ApiResponse.success("인기 메뉴 조회 성공", response));
    }

}
