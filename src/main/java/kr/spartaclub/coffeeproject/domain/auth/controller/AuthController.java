package kr.spartaclub.coffeeproject.domain.auth.controller;

import jakarta.validation.Valid;
import kr.spartaclub.coffeeproject.common.response.ApiResponse;
import kr.spartaclub.coffeeproject.domain.auth.dto.request.LoginRequest;
import kr.spartaclub.coffeeproject.domain.auth.dto.request.SignupRequest;
import kr.spartaclub.coffeeproject.domain.auth.dto.response.LoginResponse;
import kr.spartaclub.coffeeproject.domain.auth.dto.response.SignupResponse;
import kr.spartaclub.coffeeproject.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> signin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", authService.login(request)));
    }

}
