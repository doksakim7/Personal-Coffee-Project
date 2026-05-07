package kr.spartaclub.coffeeproject.domain.order.controller;

import kr.spartaclub.coffeeproject.common.response.ApiResponse;
import kr.spartaclub.coffeeproject.common.security.AuthUser;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderCancelResponse;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderCreateResponse;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderDetailResponse;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderPayResponse;
import kr.spartaclub.coffeeproject.domain.order.dto.response.OrderListResponse;
import kr.spartaclub.coffeeproject.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        OrderCreateResponse response = orderService.createOrder(authUser, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("주문이 생성되었습니다.", response));
    }

    // 주문 결제
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<OrderPayResponse>> payOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long orderId
    ) {
        OrderPayResponse response = orderService.payOrder(authUser, orderId);
        return ResponseEntity.ok(ApiResponse.success("결제가 완료되었습니다.", response));
    }

    // 주문 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<OrderListResponse>> getOrders(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        OrderListResponse response = orderService.getOrders(authUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("주문 목록 조회 성공", response));
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long orderId
    ) {
        OrderDetailResponse response = orderService.getOrder(authUser, orderId);
        return ResponseEntity.ok(ApiResponse.success("주문 상세 조회 성공", response));
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long orderId
    ) {
        OrderCancelResponse response = orderService.cancelOrder(authUser, orderId);
        return ResponseEntity.ok(ApiResponse.success("주문이 취소되었습니다.", response));
    }

}
