package kr.spartaclub.coffeeproject.domain.cart.controller;

import jakarta.validation.Valid;
import kr.spartaclub.coffeeproject.common.response.ApiResponse;
import kr.spartaclub.coffeeproject.common.security.AuthUser;
import kr.spartaclub.coffeeproject.domain.cart.dto.request.CartItemAddRequest;
import kr.spartaclub.coffeeproject.domain.cart.dto.request.CartItemQuantityUpdateRequest;
import kr.spartaclub.coffeeproject.domain.cart.dto.response.CartItemResponse;
import kr.spartaclub.coffeeproject.domain.cart.dto.response.CartResponse;
import kr.spartaclub.coffeeproject.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CartResponse response = cartService.getCart(authUser);
        return ResponseEntity.ok(ApiResponse.success("장바구니 조회 성공", response));
    }

    // 장바구니 상품 추가
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        CartItemResponse response = cartService.addCartItem(authUser, request);
        return ResponseEntity.ok(ApiResponse.success("장바구니에 상품이 추가되었습니다.", response));
    }

    // 장바구니 상품 수량 변경
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItemQuantity(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemQuantityUpdateRequest request
    ) {
        CartItemResponse response = cartService.updateCartItemQuantity(authUser, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("수량이 변경되었습니다.", response));
    }

    // 장바구니 상품 삭제
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long itemId
    ) {
        cartService.deleteCartItem(authUser, itemId);
        return ResponseEntity.ok(ApiResponse.success("상품 삭제 요청이 처리되었습니다.", null));
    }

}
