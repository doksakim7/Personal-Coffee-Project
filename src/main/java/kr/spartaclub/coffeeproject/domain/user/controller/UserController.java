package kr.spartaclub.coffeeproject.domain.user.controller;

import jakarta.validation.Valid;
import kr.spartaclub.coffeeproject.common.response.ApiResponse;
import kr.spartaclub.coffeeproject.common.security.AuthUser;
import kr.spartaclub.coffeeproject.domain.user.dto.request.UserUpdateRequest;
import kr.spartaclub.coffeeproject.domain.user.dto.response.UserResponse;
import kr.spartaclub.coffeeproject.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserResponse response = userService.getMyInfo(authUser);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("회원 정보 조회 성공", response));
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyInfo(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        userService.updateMyInfo(authUser, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("회원 정보가 수정되었습니다.", null));
    }

}
