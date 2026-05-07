package kr.spartaclub.coffeeproject.domain.point.controller;

import kr.spartaclub.coffeeproject.common.response.ApiResponse;
import kr.spartaclub.coffeeproject.common.security.AuthUser;
import kr.spartaclub.coffeeproject.domain.point.dto.request.PointAmountRequest;
import kr.spartaclub.coffeeproject.domain.point.dto.response.PointHistoryListResponse;
import kr.spartaclub.coffeeproject.domain.point.dto.response.PointResponse;
import kr.spartaclub.coffeeproject.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    // 포인트 충전
    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<PointResponse>> chargePoint(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody PointAmountRequest request
    ) {
        PointResponse response = pointService.chargePoint(authUser, request);
        return ResponseEntity.ok(ApiResponse.success("포인트 충전이 완료되었습니다.", response));
    }

    // 포인트 환전
    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<PointResponse>> exchangePoint(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody PointAmountRequest request
    ) {
        PointResponse response = pointService.exchangePoint(authUser, request);
        return ResponseEntity.ok(ApiResponse.success("포인트 환전이 완료되었습니다.", response));
    }

    // 현재 포인트 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PointResponse>> getPoint(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        PointResponse response = pointService.getPoint(authUser);
        return ResponseEntity.ok(ApiResponse.success("포인트 조회 성공", response));
    }

    // 포인트 내역 조회
    @GetMapping("/histories")
    public ResponseEntity<ApiResponse<PointHistoryListResponse>> getPointHistories(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PointHistoryListResponse response = pointService.getPointHistories(authUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("포인트 내역 조회 성공", response));
    }

}
